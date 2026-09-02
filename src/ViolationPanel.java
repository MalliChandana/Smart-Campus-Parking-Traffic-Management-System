import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * ViolationPanel - Pure Java AWT Traffic Violations & Campus Security Module
 *
 * Implements:
 * - Violation ticketing form
 * - Preset violation types & default penalty fines:
 *   - Unauthorized Parking (₹500.00)
 *   - Overtime Parking     (₹200.00)
 *   - Blocking Driveway    (₹1000.00)
 *   - Speeding on Campus   (₹800.00)
 *   - No Parking Pass      (₹300.00)
 * - AWT List directory with total penalties metric
 *
 * Demonstrates:
 * 1. PreparedStatement for INSERT into traffic_violations
 * 2. PreparedStatement for DELETE
 * 3. PreparedStatement for Search
 * 4. Statement for Aggregate fine sums
 */
public class ViolationPanel extends Panel {

    private TextField txtViolationId;
    private Choice cmbVehicles;
    private Choice cmbViolationType;
    private TextField txtFineAmount;
    private TextArea txtDescription;
    private TextField txtSearch;

    private Button btnIssueTicket;
    private Button btnDeleteTicket;
    private Button btnClear;
    private Button btnSearch;
    private Button btnRefresh;

    private Label lblTotalFines;
    private List listViolations;

    private static class VehicleOption {
        int id;
        String number;
        String type;
        VehicleOption(int id, String number, String type) { this.id = id; this.number = number; this.type = type; }
    }

    private static class ViolationRecord {
        int id;
        String plate;
        String type;
        double fine;
        String date;
        ViolationRecord(int id, String plate, String type, double fine, String date) {
            this.id = id; this.plate = plate; this.type = type; this.fine = fine; this.date = date;
        }
    }

    private java.util.List<VehicleOption> vehicleList = new ArrayList<>();
    private java.util.List<ViolationRecord> violationRecords = new ArrayList<>();

    public ViolationPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.COLOR_BG);

        initUI();
        loadVehicles();
        loadViolationsData();
    }

    private void initUI() {
        // Top Header
        Panel headerPanel = UITheme.createHeaderPanel(
                "Campus Traffic & Parking Violations",
                "Log campus traffic violations, issue penalty fines, and enforce parking rules."
        );
        add(headerPanel, BorderLayout.NORTH);

        // Center Split: Form (Left) + List (Right)
        Panel centerPanel = new Panel(new BorderLayout(12, 12));
        centerPanel.setBackground(UITheme.COLOR_BG);

        // Form Card Panel
        UITheme.CardPanel formCard = new UITheme.CardPanel(14, 16, 14, 16);
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(360, 520));

        Label lblFormTitle = new Label("Issue Violation Ticket");
        lblFormTitle.setFont(UITheme.FONT_SECTION_TITLE);
        lblFormTitle.setForeground(UITheme.COLOR_PRIMARY_DARK);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        Panel fieldsPanel = new Panel(new GridLayout(4, 2, 8, 8));
        fieldsPanel.setBackground(Color.WHITE);

        fieldsPanel.add(new Label("Ticket ID:"));
        txtViolationId = UITheme.createTextField(10);
        txtViolationId.setEditable(false);
        txtViolationId.setBackground(new Color(241, 245, 249));
        fieldsPanel.add(txtViolationId);

        fieldsPanel.add(new Label("Offending Vehicle *:"));
        cmbVehicles = new Choice();
        cmbVehicles.setFont(UITheme.FONT_REGULAR);
        fieldsPanel.add(cmbVehicles);

        fieldsPanel.add(new Label("Violation Type *:"));
        cmbViolationType = UITheme.createChoice(new String[]{
                "Wrong Parking",
                "Overstay",
                "Reserved Slot Violation",
                "Unauthorized Parking"
        });
        fieldsPanel.add(cmbViolationType);

        fieldsPanel.add(new Label("Penalty Fine (₹) *:"));
        txtFineAmount = UITheme.createTextField(10);
        txtFineAmount.setText("200.00");
        fieldsPanel.add(txtFineAmount);

        // Description Area
        Panel descWrapper = new Panel(new BorderLayout(4, 4));
        descWrapper.setBackground(Color.WHITE);
        Label lblDesc = new Label("Incident Remarks / Location Description:");
        lblDesc.setFont(UITheme.FONT_SMALL);
        lblDesc.setForeground(UITheme.COLOR_TEXT_MUTED);
        descWrapper.add(lblDesc, BorderLayout.NORTH);

        txtDescription = new TextArea("Parked in restricted faculty zone without authorization.", 4, 30, TextArea.SCROLLBARS_VERTICAL_ONLY);
        txtDescription.setFont(UITheme.FONT_REGULAR);
        descWrapper.add(txtDescription, BorderLayout.CENTER);

        Panel centerForm = new Panel(new BorderLayout(8, 8));
        centerForm.setBackground(Color.WHITE);
        centerForm.add(fieldsPanel, BorderLayout.NORTH);
        centerForm.add(descWrapper, BorderLayout.CENTER);
        formCard.add(centerForm, BorderLayout.CENTER);

        // Buttons Panel
        Panel btnPanel = new Panel(new GridLayout(2, 1, 6, 6));
        btnIssueTicket = UITheme.createDangerButton("ISSUE VIOLATION TICKET");
        Panel subBtns = new Panel(new GridLayout(1, 2, 6, 0));
        btnDeleteTicket = UITheme.createSecondaryButton("Delete Ticket");
        btnClear = UITheme.createSecondaryButton("Clear Fields");
        subBtns.add(btnDeleteTicket);
        subBtns.add(btnClear);
        btnPanel.add(btnIssueTicket);
        btnPanel.add(subBtns);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        centerPanel.add(formCard, BorderLayout.WEST);

        // Right List Card Panel
        UITheme.CardPanel listCard = new UITheme.CardPanel(14, 16, 14, 16);
        listCard.setLayout(new BorderLayout(8, 8));

        Panel listTop = new Panel(new BorderLayout(8, 0));
        listTop.setBackground(Color.WHITE);

        Panel searchBar = new Panel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBar.setBackground(Color.WHITE);
        searchBar.add(new Label("Search:"));
        txtSearch = UITheme.createTextField(10);
        searchBar.add(txtSearch);

        btnSearch = UITheme.createPrimaryButton("Search");
        searchBar.add(btnSearch);
        btnRefresh = UITheme.createSecondaryButton("View All");
        searchBar.add(btnRefresh);
        listTop.add(searchBar, BorderLayout.WEST);

        lblTotalFines = new Label("Total Fines: ₹0.00");
        lblTotalFines.setFont(UITheme.FONT_BOLD);
        lblTotalFines.setForeground(UITheme.COLOR_DANGER);
        listTop.add(lblTotalFines, BorderLayout.EAST);
        listCard.add(listTop, BorderLayout.NORTH);

        Label headerRow = new Label("  TICKET ID |  PLATE NUMBER  |  TYPE     |  VIOLATION TYPE         |  FINE (₹)    |  DATE");
        headerRow.setFont(UITheme.FONT_MONO);
        headerRow.setBackground(new Color(241, 245, 249));

        listViolations = new List(15, false);
        listViolations.setFont(UITheme.FONT_MONO);
        listViolations.setBackground(Color.WHITE);

        Panel listContainer = new Panel(new BorderLayout(2, 2));
        listContainer.add(headerRow, BorderLayout.NORTH);
        listContainer.add(listViolations, BorderLayout.CENTER);
        listCard.add(listContainer, BorderLayout.CENTER);

        centerPanel.add(listCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Event Listeners
        cmbViolationType.addItemListener(e -> updateDefaultFine());
        btnIssueTicket.addActionListener(e -> issueTicket());
        btnDeleteTicket.addActionListener(e -> deleteTicket());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchViolations());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadVehicles();
            loadViolationsData();
        });

        listViolations.addItemListener(e -> {
            int idx = listViolations.getSelectedIndex();
            if (idx >= 0 && idx < violationRecords.size()) {
                ViolationRecord rec = violationRecords.get(idx);
                txtViolationId.setText(String.valueOf(rec.id));
                txtFineAmount.setText(String.format("%.2f", rec.fine));
            }
        });
    }

    private void updateDefaultFine() {
        String type = cmbViolationType.getSelectedItem();
        if ("Wrong Parking".equalsIgnoreCase(type)) txtFineAmount.setText("200.00");
        else if ("Overstay".equalsIgnoreCase(type)) txtFineAmount.setText("150.00");
        else if ("Reserved Slot Violation".equalsIgnoreCase(type)) txtFineAmount.setText("300.00");
        else if ("Unauthorized Parking".equalsIgnoreCase(type)) txtFineAmount.setText("500.00");
    }

    public void loadVehicles() {
        cmbVehicles.removeAll();
        vehicleList.clear();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT vehicle_id, vehicle_number, vehicle_type FROM vehicles ORDER BY vehicle_number ASC")) {

            while (rs.next()) {
                int id = rs.getInt("vehicle_id");
                String num = rs.getString("vehicle_number");
                String typ = rs.getString("vehicle_type");
                vehicleList.add(new VehicleOption(id, num, typ));
                cmbVehicles.add(num + " (" + typ + ")");
            }
        } catch (SQLException e) {
            System.err.println("Error loading vehicles: " + e.getMessage());
        }
    }

    public void loadViolationsData() {
        listViolations.removeAll();
        violationRecords.clear();

        String sql = "SELECT tv.violation_id, v.vehicle_number, v.vehicle_type, "
                   + "tv.violation_type, tv.fine, tv.violation_date "
                   + "FROM violations tv "
                   + "JOIN vehicles v ON tv.vehicle_id = v.vehicle_id "
                   + "ORDER BY tv.violation_id DESC";

        double sumFines = 0.0;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("violation_id");
                String plate = rs.getString("vehicle_number");
                String vType = rs.getString("vehicle_type");
                String violType = rs.getString("violation_type");
                double fine = rs.getDouble("fine");
                String date = rs.getDate("violation_date").toString();

                sumFines += fine;
                violationRecords.add(new ViolationRecord(id, plate, violType, fine, date));

                String line = String.format("  #%-8d | %-14s | %-9s | %-23s | %-12.2f | %s",
                        id, plate, vType, truncate(violType, 23), fine, date);
                listViolations.add(line);
            }
            lblTotalFines.setText("Total Penalties: ₹" + String.format("%.2f", sumFines));

        } catch (SQLException ex) {
            System.err.println("Error loading violations: " + ex.getMessage());
        }
    }

    private void issueTicket() {
        int vIdx = cmbVehicles.getSelectedIndex();
        String violType = cmbViolationType.getSelectedItem();
        String fineStr = txtFineAmount.getText().trim();
        String desc = txtDescription.getText().trim();

        if (vIdx < 0 || fineStr.isEmpty()) {
            UITheme.showWarning(this, "Missing Input", "Please select a vehicle and specify penalty fine.");
            return;
        }

        try {
            double fine = Double.parseDouble(fineStr);
            int vehicleId = vehicleList.get(vIdx).id;

            String sql = "INSERT INTO violations (vehicle_id, violation_type, description, fine, violation_date) VALUES (?, ?, ?, ?, CURDATE())";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, vehicleId);
                pstmt.setString(2, violType);
                pstmt.setString(3, desc);
                pstmt.setDouble(4, fine);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    UITheme.showSuccess(this, "Ticket Issued",
                            "Traffic Violation Ticket Issued!\n"
                            + "Offense: " + violType + "\n"
                            + "Penalty Fine: ₹" + String.format("%.2f", fine));
                    clearFields();
                    loadViolationsData();
                }
            }
        } catch (NumberFormatException e) {
            UITheme.showWarning(this, "Invalid Fine", "Penalty fine must be a valid numeric amount.");
        } catch (SQLException ex) {
            UITheme.showError(this, "Database Error", "Error issuing ticket:\n" + ex.getMessage());
        }
    }

    private void deleteTicket() {
        String idStr = txtViolationId.getText().trim();
        if (idStr.isEmpty()) {
            UITheme.showWarning(this, "No Selection", "Click a ticket from the list to delete.");
            return;
        }

        int ticketId = Integer.parseInt(idStr);
        boolean confirmed = UITheme.showConfirm(this, "Confirm Deletion", "Permanently remove violation ticket #" + ticketId + "?");
        if (!confirmed) return;

        String sql = "DELETE FROM violations WHERE violation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ticketId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                UITheme.showSuccess(this, "Ticket Deleted", "Violation ticket #" + ticketId + " removed.");
                clearFields();
                loadViolationsData();
            }
        } catch (SQLException ex) {
            UITheme.showError(this, "Deletion Error", "Could not delete ticket:\n" + ex.getMessage());
        }
    }

    private void searchViolations() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            loadViolationsData();
            return;
        }

        listViolations.removeAll();
        violationRecords.clear();

        String sql = "SELECT tv.violation_id, v.vehicle_number, v.vehicle_type, "
                   + "tv.violation_type, tv.fine, tv.violation_date "
                   + "FROM violations tv "
                   + "JOIN vehicles v ON tv.vehicle_id = v.vehicle_id "
                   + "WHERE v.vehicle_number LIKE ? OR tv.violation_type LIKE ? "
                   + "ORDER BY tv.violation_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + query + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    int id = rs.getInt("violation_id");
                    String plate = rs.getString("vehicle_number");
                    String vType = rs.getString("vehicle_type");
                    String violType = rs.getString("violation_type");
                    double fine = rs.getDouble("fine");
                    String date = rs.getDate("violation_date").toString();

                    violationRecords.add(new ViolationRecord(id, plate, violType, fine, date));
                    String line = String.format("  #%-8d | %-14s | %-9s | %-23s | %-12.2f | %s",
                            id, plate, vType, truncate(violType, 23), fine, date);
                    listViolations.add(line);
                }
                if (count == 0) {
                    UITheme.showWarning(this, "No Matches", "No violations found matching: '" + query + "'.");
                }
            }
        } catch (SQLException ex) {
            UITheme.showError(this, "Search Error", "Error searching violations:\n" + ex.getMessage());
        }
    }

    private void clearFields() {
        txtViolationId.setText("");
        txtDescription.setText("");
        if (cmbVehicles.getItemCount() > 0) cmbVehicles.select(0);
        if (cmbViolationType.getItemCount() > 0) cmbViolationType.select(0);
        updateDefaultFine();
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen - 2) + ".." : str;
    }
}
