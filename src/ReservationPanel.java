import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * ReservationPanel - Pure Java AWT Slot Advance Reservations Module
 *
 * Implements:
 * - 5-Step guided booking flow
 * - Dynamic slot availability filtering
 * - Overlap conflict checking
 * - Atomic transaction locking slot to RESERVED
 * - AWT List log of existing reservations
 *
 * Demonstrates:
 * 1. PreparedStatement for Conflict Checking
 * 2. PreparedStatement for INSERT into reservations
 * 3. PreparedStatement for UPDATE parking_slots status to 'RESERVED'
 * 4. PreparedStatement for cancelling reservation and reverting slot to 'AVAILABLE'
 */
public class ReservationPanel extends Panel {

    private Choice cmbUsers;
    private Choice cmbVehicles;
    private Choice cmbZones;
    private Choice cmbSlots;
    private TextField txtReservationDate;
    private TextField txtStartTime;
    private TextField txtEndTime;

    private Button btnReserve;
    private Button btnCancelReservation;
    private Button btnClear;
    private Button btnRefresh;

    private Label lblResCount;
    private List listReservations;

    // Helper models
    private static class UserOption {
        int id;
        String name;
        String role;
        UserOption(int id, String name, String role) { this.id = id; this.name = name; this.role = role; }
    }

    private static class VehicleOption {
        int id;
        int userId;
        String number;
        String type;
        VehicleOption(int id, int userId, String number, String type) { this.id = id; this.userId = userId; this.number = number; this.type = type; }
    }

    private static class ZoneOption {
        int id;
        String name;
        ZoneOption(int id, String name) { this.id = id; this.name = name; }
    }

    private static class SlotOption {
        int id;
        String slotNumber;
        SlotOption(int id, String slotNumber) { this.id = id; this.slotNumber = slotNumber; }
    }

    private static class ResRecord {
        int id;
        String slotNumber;
        String status;
        ResRecord(int id, String slotNumber, String status) { this.id = id; this.slotNumber = slotNumber; this.status = status; }
    }

    private java.util.List<UserOption> userList = new ArrayList<>();
    private java.util.List<VehicleOption> vehicleList = new ArrayList<>();
    private java.util.List<ZoneOption> zoneList = new ArrayList<>();
    private java.util.List<SlotOption> slotList = new ArrayList<>();
    private java.util.List<ResRecord> resRecords = new ArrayList<>();

    public ReservationPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.COLOR_BG);

        initUI();
        loadInitialData();
    }

    private void initUI() {
        // Top Header
        Panel headerPanel = UITheme.createHeaderPanel(
                "Advance Parking Slot Reservations",
                "Reserve dedicated parking bays in advance with automated conflict checking and slot locking."
        );
        add(headerPanel, BorderLayout.NORTH);

        // Center Split: Form (Left) + List (Right)
        Panel centerPanel = new Panel(new BorderLayout(12, 12));
        centerPanel.setBackground(UITheme.COLOR_BG);

        // Left 5-Step Form Panel
        UITheme.CardPanel formCard = new UITheme.CardPanel(14, 16, 14, 16);
        formCard.setLayout(new BorderLayout(8, 8));
        formCard.setPreferredSize(new Dimension(380, 520));

        Label lblFormTitle = new Label("New Reservation (5-Step Flow)");
        lblFormTitle.setFont(UITheme.FONT_SECTION_TITLE);
        lblFormTitle.setForeground(UITheme.COLOR_PRIMARY_DARK);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        Panel stepsPanel = new Panel(new GridLayout(5, 1, 0, 8));
        stepsPanel.setBackground(Color.WHITE);

        // STEP 1: Select User
        Panel step1 = new Panel(new GridLayout(2, 1, 0, 2));
        Label lblStep1 = new Label("STEP 1: Select Campus User");
        lblStep1.setFont(UITheme.FONT_SMALL_BOLD);
        lblStep1.setForeground(UITheme.COLOR_PRIMARY);
        cmbUsers = new Choice();
        cmbUsers.setFont(UITheme.FONT_REGULAR);
        step1.add(lblStep1);
        step1.add(cmbUsers);
        stepsPanel.add(step1);

        // STEP 2: Select Vehicle
        Panel step2 = new Panel(new GridLayout(2, 1, 0, 2));
        Label lblStep2 = new Label("STEP 2: Select Registered Vehicle");
        lblStep2.setFont(UITheme.FONT_SMALL_BOLD);
        lblStep2.setForeground(UITheme.COLOR_PRIMARY);
        cmbVehicles = new Choice();
        cmbVehicles.setFont(UITheme.FONT_REGULAR);
        step2.add(lblStep2);
        step2.add(cmbVehicles);
        stepsPanel.add(step2);

        // STEP 3: Zone & Available Slot
        Panel step3 = new Panel(new GridLayout(2, 1, 0, 2));
        Label lblStep3 = new Label("STEP 3: Select Zone & Available Slot");
        lblStep3.setFont(UITheme.FONT_SMALL_BOLD);
        lblStep3.setForeground(UITheme.COLOR_PRIMARY);

        Panel zoneSlotRow = new Panel(new GridLayout(1, 2, 6, 0));
        cmbZones = new Choice();
        cmbSlots = new Choice();
        zoneSlotRow.add(cmbZones);
        zoneSlotRow.add(cmbSlots);
        step3.add(lblStep3);
        step3.add(zoneSlotRow);
        stepsPanel.add(step3);

        // STEP 4: Date & Time Interval
        Panel step4 = new Panel(new GridLayout(2, 1, 0, 2));
        Label lblStep4 = new Label("STEP 4: Date (YYYY-MM-DD) & Time (HH:MM)");
        lblStep4.setFont(UITheme.FONT_SMALL_BOLD);
        lblStep4.setForeground(UITheme.COLOR_PRIMARY);

        Panel dateTimeRow = new Panel(new GridLayout(1, 3, 4, 0));
        String todayStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        txtReservationDate = UITheme.createTextField(10);
        txtReservationDate.setText(todayStr);
        txtStartTime = UITheme.createTextField(6);
        txtStartTime.setText("09:00:00");
        txtEndTime = UITheme.createTextField(6);
        txtEndTime.setText("13:00:00");
        dateTimeRow.add(txtReservationDate);
        dateTimeRow.add(txtStartTime);
        dateTimeRow.add(txtEndTime);
        step4.add(lblStep4);
        step4.add(dateTimeRow);
        stepsPanel.add(step4);

        // STEP 5: Confirm Button
        Panel step5 = new Panel(new BorderLayout(4, 4));
        Label lblStep5 = new Label("STEP 5: Confirm & Book");
        lblStep5.setFont(UITheme.FONT_SMALL_BOLD);
        lblStep5.setForeground(UITheme.COLOR_PRIMARY);
        btnReserve = UITheme.createSuccessButton("Confirm Reservation");
        step5.add(lblStep5, BorderLayout.NORTH);
        step5.add(btnReserve, BorderLayout.CENTER);
        stepsPanel.add(step5);

        formCard.add(stepsPanel, BorderLayout.CENTER);

        // Bottom Actions
        Panel bottomActions = new Panel(new GridLayout(1, 2, 8, 0));
        btnCancelReservation = UITheme.createDangerButton("Cancel Booking");
        btnClear = UITheme.createSecondaryButton("Clear");
        bottomActions.add(btnCancelReservation);
        bottomActions.add(btnClear);
        formCard.add(bottomActions, BorderLayout.SOUTH);

        centerPanel.add(formCard, BorderLayout.WEST);

        // Right List Card Panel
        UITheme.CardPanel listCard = new UITheme.CardPanel(14, 16, 14, 16);
        listCard.setLayout(new BorderLayout(8, 8));

        Panel listTop = new Panel(new BorderLayout());
        Label lblListTitle = new Label("Slot Reservations Directory");
        lblListTitle.setFont(UITheme.FONT_SECTION_TITLE);
        lblListTitle.setForeground(UITheme.COLOR_PRIMARY_DARK);
        listTop.add(lblListTitle, BorderLayout.WEST);

        Panel rightTop = new Panel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        lblResCount = new Label("Total: 0");
        lblResCount.setFont(UITheme.FONT_BOLD);
        rightTop.add(lblResCount);
        btnRefresh = UITheme.createSecondaryButton("Refresh");
        rightTop.add(btnRefresh);
        listTop.add(rightTop, BorderLayout.EAST);
        listCard.add(listTop, BorderLayout.NORTH);

        Label headerRow = new Label("  ID   |  PLATE NUMBER  |  OWNER NAME    |  ZONE    |  SLOT  |  DATE        |  START    |  END      |  STATUS");
        headerRow.setFont(UITheme.FONT_MONO);
        headerRow.setBackground(new Color(241, 245, 249));

        listReservations = new List(15, false);
        listReservations.setFont(UITheme.FONT_MONO);
        listReservations.setBackground(Color.WHITE);

        Panel listContainer = new Panel(new BorderLayout(2, 2));
        listContainer.add(headerRow, BorderLayout.NORTH);
        listContainer.add(listReservations, BorderLayout.CENTER);
        listCard.add(listContainer, BorderLayout.CENTER);

        centerPanel.add(listCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Event Listeners
        cmbUsers.addItemListener(e -> filterVehiclesForUser());
        cmbZones.addItemListener(e -> loadAvailableSlotsForZone());
        btnReserve.addActionListener(e -> processReservation());
        btnCancelReservation.addActionListener(e -> cancelSelectedReservation());
        btnClear.addActionListener(e -> clearFields());
        btnRefresh.addActionListener(e -> loadInitialData());
    }

    public void loadInitialData() {
        loadUsers();
        loadZones();
        loadReservationsList();
    }

    private void loadUsers() {
        cmbUsers.removeAll();
        userList.clear();

        String sql = "SELECT user_id, name, role FROM users ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("user_id");
                String name = rs.getString("name");
                String role = rs.getString("role");
                userList.add(new UserOption(id, name, role));
                cmbUsers.add(name + " (" + role + ") [#" + id + "]");
            }
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }

        filterVehiclesForUser();
    }

    private void filterVehiclesForUser() {
        cmbVehicles.removeAll();
        vehicleList.clear();

        int userIdx = cmbUsers.getSelectedIndex();
        Integer userId = (userIdx >= 0 && userIdx < userList.size()) ? userList.get(userIdx).id : null;

        String sql = "SELECT vehicle_id, user_id, vehicle_number, vehicle_type FROM vehicles "
                   + (userId != null ? "WHERE user_id = ? " : "")
                   + "ORDER BY vehicle_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (userId != null) {
                pstmt.setInt(1, userId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("vehicle_id");
                    int uId = rs.getInt("user_id");
                    String num = rs.getString("vehicle_number");
                    String typ = rs.getString("vehicle_type");
                    vehicleList.add(new VehicleOption(id, uId, num, typ));
                    cmbVehicles.add(num + " (" + typ + ")");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering vehicles: " + e.getMessage());
        }
    }

    private void loadZones() {
        cmbZones.removeAll();
        zoneList.clear();

        String sql = "SELECT zone_id, zone_name FROM parking_zones ORDER BY zone_name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("zone_id");
                String name = rs.getString("zone_name");
                zoneList.add(new ZoneOption(id, name));
                cmbZones.add(name);
            }
        } catch (SQLException e) {
            System.err.println("Error loading zones: " + e.getMessage());
        }

        loadAvailableSlotsForZone();
    }

    private void loadAvailableSlotsForZone() {
        cmbSlots.removeAll();
        slotList.clear();

        int zoneIdx = cmbZones.getSelectedIndex();
        if (zoneIdx < 0 || zoneIdx >= zoneList.size()) return;
        int zoneId = zoneList.get(zoneIdx).id;

        String sql = "SELECT slot_id, slot_number FROM parking_slots WHERE zone_id = ? AND status = 'AVAILABLE' ORDER BY slot_number ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, zoneId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("slot_id");
                    String num = rs.getString("slot_number");
                    slotList.add(new SlotOption(id, num));
                    cmbSlots.add(num + " [AVAILABLE]");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading slots: " + e.getMessage());
        }
    }

    public void loadReservationsList() {
        listReservations.removeAll();
        resRecords.clear();

        String sql = "SELECT r.reservation_id, v.vehicle_number, u.name AS owner_name, z.zone_name, s.slot_number, "
                   + "r.reservation_date, r.start_time, r.end_time, r.status "
                   + "FROM reservations r "
                   + "JOIN vehicles v ON r.vehicle_id = v.vehicle_id "
                   + "JOIN users u ON v.user_id = u.user_id "
                   + "JOIN parking_slots s ON r.slot_id = s.slot_id "
                   + "JOIN parking_zones z ON s.zone_id = z.zone_id "
                   + "ORDER BY r.reservation_id DESC";

        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                count++;
                int resId = rs.getInt("reservation_id");
                String slotNum = rs.getString("slot_number");
                String status = rs.getString("status");
                resRecords.add(new ResRecord(resId, slotNum, status));

                String line = String.format("  #%-4d | %-14s | %-14s | %-8s | %-5s | %-11s | %-8s | %-8s | %s",
                        resId,
                        rs.getString("vehicle_number"),
                        truncate(rs.getString("owner_name"), 14),
                        rs.getString("zone_name"),
                        slotNum,
                        rs.getDate("reservation_date").toString(),
                        rs.getTime("start_time").toString(),
                        rs.getTime("end_time").toString(),
                        status);
                listReservations.add(line);
            }
            lblResCount.setText("Total: " + count);

        } catch (SQLException ex) {
            UITheme.showError(this, "Database Error", "Error loading reservations:\n" + ex.getMessage());
        }
    }

    private boolean isSlotConflict(int slotId, String dateStr, String startTimeStr, String endTimeStr) {
        String sql = "SELECT COUNT(*) FROM reservations "
                   + "WHERE slot_id = ? AND reservation_date = ? AND status = 'ACTIVE' "
                   + "AND ((start_time < ? AND end_time > ?) OR (start_time >= ? AND start_time < ?))";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, slotId);
            pstmt.setString(2, dateStr);
            pstmt.setString(3, endTimeStr);
            pstmt.setString(4, startTimeStr);
            pstmt.setString(5, startTimeStr);
            pstmt.setString(6, endTimeStr);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Conflict check error: " + e.getMessage());
        }
        return false;
    }

    private void processReservation() {
        int vehIdx = cmbVehicles.getSelectedIndex();
        int slotIdx = cmbSlots.getSelectedIndex();
        String dateStr = txtReservationDate.getText().trim();
        String startStr = txtStartTime.getText().trim();
        String endStr = txtEndTime.getText().trim();

        if (vehIdx < 0 || vehIdx >= vehicleList.size()) {
            UITheme.showWarning(this, "Vehicle Required", "Please select a vehicle registered to the selected user.");
            return;
        }

        if (slotIdx < 0 || slotIdx >= slotList.size()) {
            UITheme.showWarning(this, "Slot Required", "Please select a zone with available slots.");
            return;
        }

        if (dateStr.isEmpty() || startStr.isEmpty() || endStr.isEmpty()) {
            UITheme.showWarning(this, "Time Required", "Please specify reservation date, start time, and end time.");
            return;
        }

        if (startStr.compareTo(endStr) >= 0) {
            UITheme.showWarning(this, "Invalid Time", "Start time must precede end time.");
            return;
        }

        VehicleOption vehicle = vehicleList.get(vehIdx);
        SlotOption slot = slotList.get(slotIdx);

        if (isSlotConflict(slot.id, dateStr, startStr, endStr)) {
            UITheme.showError(this, "Conflicting Booking",
                    "Slot " + slot.slotNumber + " already has an ACTIVE reservation on " + dateStr + " during that time.");
            return;
        }

        String insertSql = "INSERT INTO reservations (vehicle_id, slot_id, reservation_date, start_time, end_time, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        String updateSlotSql = "UPDATE parking_slots SET status = 'RESERVED' WHERE slot_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Atomic transaction
            try (PreparedStatement pstmt1 = conn.prepareStatement(insertSql);
                 PreparedStatement pstmt2 = conn.prepareStatement(updateSlotSql)) {

                pstmt1.setInt(1, vehicle.id);
                pstmt1.setInt(2, slot.id);
                pstmt1.setString(3, dateStr);
                pstmt1.setString(4, startStr);
                pstmt1.setString(5, endStr);
                pstmt1.executeUpdate();

                pstmt2.setInt(1, slot.id);
                pstmt2.executeUpdate();

                conn.commit();

                UITheme.showSuccess(this, "Reservation Confirmed",
                        "Reservation confirmed successfully!\n"
                        + "Vehicle: " + vehicle.number + "\n"
                        + "Slot: " + slot.slotNumber + " [RESERVED]\n"
                        + "Date: " + dateStr + " (" + startStr + " - " + endStr + ")");
                loadInitialData();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            UITheme.showError(this, "Booking Failed", "Could not complete reservation:\n" + ex.getMessage());
        }
    }

    private void cancelSelectedReservation() {
        int idx = listReservations.getSelectedIndex();
        if (idx < 0 || idx >= resRecords.size()) {
            UITheme.showWarning(this, "No Selection", "Please click a reservation from the list to cancel.");
            return;
        }

        ResRecord record = resRecords.get(idx);
        if (!"ACTIVE".equalsIgnoreCase(record.status)) {
            UITheme.showWarning(this, "Cannot Cancel", "Only ACTIVE reservations can be cancelled.");
            return;
        }

        boolean confirmed = UITheme.showConfirm(this, "Confirm Cancellation",
                "Cancel reservation #" + record.id + " for slot " + record.slotNumber + "?\nSlot will be restored to AVAILABLE.");
        if (!confirmed) return;

        String cancelSql = "UPDATE reservations SET status = 'CANCELLED' WHERE reservation_id = ?";
        String freeSlotSql = "UPDATE parking_slots SET status = 'AVAILABLE' WHERE slot_number = ? AND status = 'RESERVED'";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt1 = conn.prepareStatement(cancelSql);
                 PreparedStatement pstmt2 = conn.prepareStatement(freeSlotSql)) {

                pstmt1.setInt(1, record.id);
                pstmt1.executeUpdate();

                pstmt2.setString(1, record.slotNumber);
                pstmt2.executeUpdate();

                conn.commit();

                UITheme.showSuccess(this, "Reservation Cancelled", "Reservation #" + record.id + " cancelled. Slot is now AVAILABLE.");
                loadInitialData();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            UITheme.showError(this, "Error", "Failed to cancel reservation:\n" + ex.getMessage());
        }
    }

    private void clearFields() {
        if (cmbUsers.getItemCount() > 0) cmbUsers.select(0);
        loadAvailableSlotsForZone();
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen - 2) + ".." : str;
    }
}
