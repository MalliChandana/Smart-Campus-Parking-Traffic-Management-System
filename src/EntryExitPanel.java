import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * EntryExitPanel - Pure Java AWT Gate Entry & Exit Operations Module
 *
 * Rates:
 * - Bike  = ₹10.00 / hour
 * - Car   = ₹20.00 / hour
 * - Truck = ₹30.00 / hour
 *
 * Demonstrates:
 * 1. PreparedStatement for active session verification & double-entry prevention
 * 2. PreparedStatement for INSERT into parking_sessions (Vehicle Entry)
 * 3. PreparedStatement for UPDATE parking_slots status to 'OCCUPIED' / 'AVAILABLE'
 * 4. PreparedStatement for duration & tariff calculation on Vehicle Exit
 */
public class EntryExitPanel extends Panel {

    public static final double RATE_BIKE = 10.0;
    public static final double RATE_CAR = 20.0;
    public static final double RATE_TRUCK = 30.0;

    // Entry Form
    private TextField txtEntryVehicleNumber;
    private Choice cmbEntrySlot;
    private Button btnVehicleEntry;
    private Button btnRefreshSlots;

    // Exit Form
    private TextField txtExitVehicleNumber;
    private Label lblExitSlotDisplay;
    private Label lblExitFeeDisplay;
    private Button btnFetchExitDetails;
    private Button btnVehicleExit;

    // Active Sessions List
    private Label lblActiveCount;
    private List listActiveSessions;

    private static class SlotOption {
        int id;
        String number;
        String zoneName;
        SlotOption(int id, String number, String zoneName) { this.id = id; this.number = number; this.zoneName = zoneName; }
    }

    private java.util.List<SlotOption> availableSlots = new ArrayList<>();
    private java.util.List<String> activePlates = new ArrayList<>();

    public EntryExitPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.COLOR_BG);

        initUI();
        loadAvailableSlots();
        loadActiveSessions();
    }

    private void initUI() {
        // Top Header
        Panel headerPanel = UITheme.createHeaderPanel(
                "Gate Entry & Exit Management",
                "Automated entry recording, slot status locking, departure fee computation, and bay release."
        );
        add(headerPanel, BorderLayout.NORTH);

        // Center Split: Operations (Top) + Active Sessions (Bottom)
        Panel centerPanel = new Panel(new BorderLayout(12, 12));
        centerPanel.setBackground(UITheme.COLOR_BG);

        // Operations Split (Entry Left, Exit Right)
        Panel opsGrid = new Panel(new GridLayout(1, 2, 12, 0));
        opsGrid.setPreferredSize(new Dimension(800, 260));

        // 1. VEHICLE ENTRY CARD
        UITheme.CardPanel entryCard = new UITheme.CardPanel(12, 14, 12, 14);
        entryCard.setLayout(new BorderLayout(8, 8));

        Panel entryTop = new Panel(new BorderLayout());
        Label lblEntryTitle = new Label("VEHICLE ENTRY (GATE IN)");
        lblEntryTitle.setFont(UITheme.FONT_SECTION_TITLE);
        lblEntryTitle.setForeground(new Color(21, 128, 61));
        entryTop.add(lblEntryTitle, BorderLayout.WEST);

        btnRefreshSlots = UITheme.createSecondaryButton("Refresh Slots");
        btnRefreshSlots.addActionListener(e -> loadAvailableSlots());
        entryTop.add(btnRefreshSlots, BorderLayout.EAST);
        entryCard.add(entryTop, BorderLayout.NORTH);

        Panel entryFields = new Panel(new GridLayout(3, 2, 8, 8));
        entryFields.setBackground(Color.WHITE);

        entryFields.add(new Label("Vehicle Plate *:"));
        txtEntryVehicleNumber = UITheme.createTextField(12);
        entryFields.add(txtEntryVehicleNumber);

        entryFields.add(new Label("Assign Slot *:"));
        cmbEntrySlot = new Choice();
        cmbEntrySlot.setFont(UITheme.FONT_REGULAR);
        entryFields.add(cmbEntrySlot);

        entryFields.add(new Label("Entry Time:"));
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Label lblTimeNow = new Label("Live: " + sdf.format(new Date()));
        lblTimeNow.setFont(UITheme.FONT_SMALL);
        lblTimeNow.setForeground(UITheme.COLOR_TEXT_MUTED);
        entryFields.add(lblTimeNow);

        entryCard.add(entryFields, BorderLayout.CENTER);

        btnVehicleEntry = UITheme.createSuccessButton("RECORD VEHICLE ENTRY");
        btnVehicleEntry.addActionListener(e -> processVehicleEntry());
        entryCard.add(btnVehicleEntry, BorderLayout.SOUTH);

        opsGrid.add(entryCard);

        // 2. VEHICLE EXIT & TARIFF CARD
        UITheme.CardPanel exitCard = new UITheme.CardPanel(12, 14, 12, 14);
        exitCard.setLayout(new BorderLayout(8, 8));

        Label lblExitTitle = new Label("VEHICLE EXIT & TARIFF (GATE OUT)");
        lblExitTitle.setFont(UITheme.FONT_SECTION_TITLE);
        lblExitTitle.setForeground(new Color(220, 38, 38));
        exitCard.add(lblExitTitle, BorderLayout.NORTH);

        Panel exitFields = new Panel(new GridLayout(4, 2, 8, 6));
        exitFields.setBackground(Color.WHITE);

        exitFields.add(new Label("Vehicle Plate *:"));
        Panel plateRow = new Panel(new BorderLayout(4, 0));
        txtExitVehicleNumber = UITheme.createTextField(10);
        btnFetchExitDetails = UITheme.createSecondaryButton("Check Fee");
        btnFetchExitDetails.addActionListener(e -> previewExitFee());
        plateRow.add(txtExitVehicleNumber, BorderLayout.CENTER);
        plateRow.add(btnFetchExitDetails, BorderLayout.EAST);
        exitFields.add(plateRow);

        exitFields.add(new Label("Parked Slot:"));
        lblExitSlotDisplay = new Label("-");
        lblExitSlotDisplay.setFont(UITheme.FONT_BOLD);
        exitFields.add(lblExitSlotDisplay);

        exitFields.add(new Label("Calculated Fee:"));
        lblExitFeeDisplay = new Label("Select vehicle to preview fee");
        lblExitFeeDisplay.setFont(UITheme.FONT_BOLD);
        lblExitFeeDisplay.setForeground(UITheme.COLOR_PRIMARY);
        exitFields.add(lblExitFeeDisplay);

        exitFields.add(new Label("Tariff Rules:"));
        Label lblTariffNote = new Label("Bike ₹10/hr | Car ₹20/hr | Truck ₹30/hr");
        lblTariffNote.setFont(UITheme.FONT_SMALL);
        lblTariffNote.setForeground(UITheme.COLOR_TEXT_MUTED);
        exitFields.add(lblTariffNote);

        exitCard.add(exitFields, BorderLayout.CENTER);

        btnVehicleExit = UITheme.createDangerButton("PROCESS VEHICLE EXIT");
        btnVehicleExit.addActionListener(e -> processVehicleExit());
        exitCard.add(btnVehicleExit, BorderLayout.SOUTH);

        opsGrid.add(exitCard);
        centerPanel.add(opsGrid, BorderLayout.NORTH);

        // 3. BOTTOM: Currently Parked Vehicles List Card
        UITheme.CardPanel tableCard = new UITheme.CardPanel(10, 14, 10, 14);
        tableCard.setLayout(new BorderLayout(8, 6));

        Panel listTop = new Panel(new BorderLayout());
        Label lblTableTitle = new Label("Currently Parked Vehicles (Click item to select for departure)");
        lblTableTitle.setFont(UITheme.FONT_SECTION_TITLE);
        lblTableTitle.setForeground(UITheme.COLOR_PRIMARY_DARK);
        listTop.add(lblTableTitle, BorderLayout.WEST);

        Panel rightTop = new Panel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        lblActiveCount = new Label("Active: 0");
        lblActiveCount.setFont(UITheme.FONT_BOLD);
        lblActiveCount.setForeground(UITheme.COLOR_OCCUPIED);
        rightTop.add(lblActiveCount);

        Button btnRefreshActive = UITheme.createSecondaryButton("Refresh Active");
        btnRefreshActive.addActionListener(e -> {
            loadAvailableSlots();
            loadActiveSessions();
        });
        rightTop.add(btnRefreshActive);
        listTop.add(rightTop, BorderLayout.EAST);
        tableCard.add(listTop, BorderLayout.NORTH);

        Label headerRow = new Label("  SESSION ID   |  VEHICLE PLATE  |  TYPE     |  OWNER NAME             |  SLOT   |  ZONE     |  ENTRY TIME");
        headerRow.setFont(UITheme.FONT_MONO);
        headerRow.setBackground(new Color(241, 245, 249));

        listActiveSessions = new List(10, false);
        listActiveSessions.setFont(UITheme.FONT_MONO);
        listActiveSessions.setBackground(Color.WHITE);

        Panel listWrapper = new Panel(new BorderLayout(2, 2));
        listWrapper.add(headerRow, BorderLayout.NORTH);
        listWrapper.add(listActiveSessions, BorderLayout.CENTER);
        tableCard.add(listWrapper, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Click row to auto-select
        listActiveSessions.addItemListener(e -> {
            int idx = listActiveSessions.getSelectedIndex();
            if (idx >= 0 && idx < activePlates.size()) {
                txtExitVehicleNumber.setText(activePlates.get(idx));
                previewExitFee();
            }
        });
    }

    public void loadAvailableSlots() {
        cmbEntrySlot.removeAll();
        availableSlots.clear();

        String sql = "SELECT s.slot_id, s.slot_number, z.zone_name "
                   + "FROM parking_slots s JOIN parking_zones z ON s.zone_id = z.zone_id "
                   + "WHERE s.status IN ('AVAILABLE', 'RESERVED') "
                   + "ORDER BY s.status ASC, z.zone_name ASC, s.slot_number ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("slot_id");
                String num = rs.getString("slot_number");
                String zone = rs.getString("zone_name");
                availableSlots.add(new SlotOption(id, num, zone));
                cmbEntrySlot.add(num + " (" + zone + ")");
            }
        } catch (SQLException e) {
            System.err.println("Error loading available slots: " + e.getMessage());
        }
    }

    public void loadActiveSessions() {
        listActiveSessions.removeAll();
        activePlates.clear();

        String sql = "SELECT ps.session_id, v.vehicle_number, v.vehicle_type, u.name, s.slot_number, z.zone_name, ps.entry_time "
                   + "FROM parking_sessions ps "
                   + "JOIN vehicles v ON ps.vehicle_id = v.vehicle_id "
                   + "JOIN users u ON v.user_id = u.user_id "
                   + "JOIN parking_slots s ON ps.slot_id = s.slot_id "
                   + "JOIN parking_zones z ON s.zone_id = z.zone_id "
                   + "WHERE ps.exit_time IS NULL ORDER BY ps.entry_time DESC";

        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                count++;
                String plate = rs.getString("vehicle_number");
                activePlates.add(plate);

                String line = String.format("  #%-11d | %-14s | %-8s | %-23s | %-7s | %-9s | %s",
                        rs.getInt("session_id"),
                        plate,
                        rs.getString("vehicle_type"),
                        truncate(rs.getString("name"), 23),
                        rs.getString("slot_number"),
                        rs.getString("zone_name"),
                        rs.getTimestamp("entry_time").toString().substring(11, 19));
                listActiveSessions.add(line);
            }
            lblActiveCount.setText("Active: " + count);

        } catch (SQLException ex) {
            System.err.println("Error loading active sessions: " + ex.getMessage());
        }
    }

    private void processVehicleEntry() {
        String vehNum = txtEntryVehicleNumber.getText().trim().toUpperCase();
        int slotIdx = cmbEntrySlot.getSelectedIndex();

        if (vehNum.isEmpty() || slotIdx < 0 || slotIdx >= availableSlots.size()) {
            UITheme.showWarning(this, "Missing Input", "Please enter plate number and select an available slot.");
            return;
        }

        SlotOption selectedSlot = availableSlots.get(slotIdx);

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Verify vehicle exists
            int vehicleId = -1;
            String vehicleType = "";
            String ownerName = "";
            String findVehSql = "SELECT v.vehicle_id, v.vehicle_type, u.name FROM vehicles v JOIN users u ON v.user_id = u.user_id WHERE v.vehicle_number = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(findVehSql)) {
                pstmt.setString(1, vehNum);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        vehicleId = rs.getInt("vehicle_id");
                        vehicleType = rs.getString("vehicle_type");
                        ownerName = rs.getString("name");
                    } else {
                        UITheme.showError(this, "Unregistered", "Vehicle '" + vehNum + "' is not registered.\nRegister it in Vehicle Registry first.");
                        return;
                    }
                }
            }

            // 2. Check if already parked
            String activeCheckSql = "SELECT session_id FROM parking_sessions WHERE vehicle_id = ? AND exit_time IS NULL";
            try (PreparedStatement pstmt = conn.prepareStatement(activeCheckSql)) {
                pstmt.setInt(1, vehicleId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        UITheme.showError(this, "Double Entry", "Vehicle '" + vehNum + "' is already PARKED on campus.");
                        return;
                    }
                }
            }

            // 3. Record session and lock slot to OCCUPIED
            conn.setAutoCommit(false);
            String insertSessionSql = "INSERT INTO parking_sessions (vehicle_id, slot_id, entry_time, fee) VALUES (?, ?, NOW(), 0.00)";
            String updateSlotSql = "UPDATE parking_slots SET status = 'OCCUPIED' WHERE slot_id = ?";

            try (PreparedStatement pstmt1 = conn.prepareStatement(insertSessionSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement pstmt2 = conn.prepareStatement(updateSlotSql)) {

                pstmt1.setInt(1, vehicleId);
                pstmt1.setInt(2, selectedSlot.id);
                pstmt1.executeUpdate();

                pstmt2.setInt(1, selectedSlot.id);
                pstmt2.executeUpdate();

                conn.commit();

                UITheme.showSuccess(this, "Entry Recorded",
                        "Vehicle Entry Recorded Successfully!\n"
                        + "Vehicle: " + vehNum + " (" + vehicleType + ")\n"
                        + "Driver: " + ownerName + "\n"
                        + "Assigned Slot: " + selectedSlot.number + " (" + selectedSlot.zoneName + ")\n"
                        + "Slot status is now OCCUPIED.");

                txtEntryVehicleNumber.setText("");
                loadAvailableSlots();
                loadActiveSessions();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            UITheme.showError(this, "Database Error", "Error recording entry:\n" + ex.getMessage());
        }
    }

    private void previewExitFee() {
        String vehNum = txtExitVehicleNumber.getText().trim().toUpperCase();
        if (vehNum.isEmpty()) return;

        String findSessionSql = "SELECT ps.session_id, ps.entry_time, v.vehicle_type, s.slot_number, z.zone_name "
                              + "FROM parking_sessions ps "
                              + "JOIN vehicles v ON ps.vehicle_id = v.vehicle_id "
                              + "JOIN parking_slots s ON ps.slot_id = s.slot_id "
                              + "JOIN parking_zones z ON s.zone_id = z.zone_id "
                              + "WHERE v.vehicle_number = ? AND ps.exit_time IS NULL";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(findSessionSql)) {

            pstmt.setString(1, vehNum);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String slotNumber = rs.getString("slot_number");
                    String zoneName = rs.getString("zone_name");
                    Timestamp entryTime = rs.getTimestamp("entry_time");
                    String vehicleType = rs.getString("vehicle_type");

                    long entryMillis = entryTime.getTime();
                    long exitMillis = System.currentTimeMillis();
                    long diffMillis = Math.max(0, exitMillis - entryMillis);
                    double diffHours = diffMillis / (1000.0 * 60.0 * 60.0);
                    int durationHours = (int) Math.ceil(diffHours);
                    if (durationHours < 1) durationHours = 1;

                    double rate = "Bike".equalsIgnoreCase(vehicleType) ? RATE_BIKE :
                                  ("Truck".equalsIgnoreCase(vehicleType) ? RATE_TRUCK : RATE_CAR);
                    double fee = durationHours * rate;

                    lblExitSlotDisplay.setText(slotNumber + " (" + zoneName + ")");
                    lblExitFeeDisplay.setText(durationHours + " hr(s) @ ₹" + (int)rate + "/hr = ₹" + String.format("%.2f", fee));
                } else {
                    lblExitSlotDisplay.setText("No active session");
                    lblExitFeeDisplay.setText("-");
                }
            }
        } catch (SQLException e) {
            System.err.println("Preview fee error: " + e.getMessage());
        }
    }

    private void processVehicleExit() {
        String vehNum = txtExitVehicleNumber.getText().trim().toUpperCase();
        if (vehNum.isEmpty()) {
            UITheme.showWarning(this, "Plate Required", "Please enter plate number to process departure.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String findSessionSql = "SELECT ps.session_id, ps.slot_id, ps.entry_time, v.vehicle_type, u.name, s.slot_number "
                                  + "FROM parking_sessions ps "
                                  + "JOIN vehicles v ON ps.vehicle_id = v.vehicle_id "
                                  + "JOIN users u ON v.user_id = u.user_id "
                                  + "JOIN parking_slots s ON ps.slot_id = s.slot_id "
                                  + "WHERE v.vehicle_number = ? AND ps.exit_time IS NULL";

            int sessionId = -1;
            int slotId = -1;
            Timestamp entryTime = null;
            String vehicleType = "";
            String ownerName = "";
            String slotNumber = "";

            try (PreparedStatement pstmt = conn.prepareStatement(findSessionSql)) {
                pstmt.setString(1, vehNum);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        sessionId = rs.getInt("session_id");
                        slotId = rs.getInt("slot_id");
                        entryTime = rs.getTimestamp("entry_time");
                        vehicleType = rs.getString("vehicle_type");
                        ownerName = rs.getString("name");
                        slotNumber = rs.getString("slot_number");
                    } else {
                        UITheme.showWarning(this, "Not Found", "No active session for: " + vehNum);
                        return;
                    }
                }
            }

            long diffMillis = Math.max(0, System.currentTimeMillis() - entryTime.getTime());
            int durationHours = (int) Math.ceil(diffMillis / (1000.0 * 60.0 * 60.0));
            if (durationHours < 1) durationHours = 1;

            double hourlyRate = "Bike".equalsIgnoreCase(vehicleType) ? RATE_BIKE :
                                ("Truck".equalsIgnoreCase(vehicleType) ? RATE_TRUCK : RATE_CAR);
            double totalFee = durationHours * hourlyRate;

            String confirmMsg = String.format(
                    "Confirm Vehicle Departure & Fee:\n\n"
                    + "Session ID: #%d\n"
                    + "Vehicle: %s (%s)\n"
                    + "Driver: %s\n"
                    + "Slot: %s\n"
                    + "Duration: %d hr(s) [rounded up]\n"
                    + "Tariff: ₹%.2f / hr\n"
                    + "----------------------------\n"
                    + "TOTAL PARKING FEE: ₹%.2f\n\n"
                    + "Process departure and release slot to AVAILABLE?",
                    sessionId, vehNum, vehicleType, ownerName, slotNumber, durationHours, hourlyRate, totalFee
            );

            boolean confirmed = UITheme.showConfirm(this, "Confirm Departure", confirmMsg);
            if (!confirmed) return;

            conn.setAutoCommit(false);
            String updateSessionSql = "UPDATE parking_sessions SET exit_time = NOW(), duration = ?, fee = ? WHERE session_id = ?";
            String freeSlotSql = "UPDATE parking_slots SET status = 'AVAILABLE' WHERE slot_id = ?";

            try (PreparedStatement pstmt1 = conn.prepareStatement(updateSessionSql);
                 PreparedStatement pstmt2 = conn.prepareStatement(freeSlotSql)) {

                pstmt1.setInt(1, durationHours);
                pstmt1.setDouble(2, totalFee);
                pstmt1.setInt(3, sessionId);
                pstmt1.executeUpdate();

                pstmt2.setInt(1, slotId);
                pstmt2.executeUpdate();

                conn.commit();

                UITheme.showSuccess(this, "Exit Processed",
                        "Vehicle departure processed successfully!\n"
                        + "Session #" + sessionId + " closed.\n"
                        + "Fee Invoiced: ₹" + String.format("%.2f", totalFee) + "\n"
                        + "Slot " + slotNumber + " is now AVAILABLE.");

                txtExitVehicleNumber.setText("");
                lblExitSlotDisplay.setText("-");
                lblExitFeeDisplay.setText("Select vehicle to preview fee");
                loadAvailableSlots();
                loadActiveSessions();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            UITheme.showError(this, "Database Error", "Error processing departure:\n" + ex.getMessage());
        }
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen - 2) + ".." : str;
    }
}
