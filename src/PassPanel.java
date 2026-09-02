import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * PassPanel - Pure Java AWT Parking Passes & Permits Module
 *
 * Pass Types: Monthly, Semester, Visitor, Student, Faculty, Daily
 *
 * Demonstrates:
 * 1. PreparedStatement for Pass creation (INSERT)
 * 2. PreparedStatement for Pass modification (UPDATE)
 * 3. PreparedStatement for Pass deletion (DELETE)
 * 4. PreparedStatement for Keyword search
 * 5. Statement for JOIN SELECT of passes with users and vehicles
 */
public class PassPanel extends Panel {

    private TextField txtPassId;
    private Choice cmbUsers;
    private Choice cmbVehicles;
    private Choice cmbPassType;
    private TextField txtStartDate;
    private TextField txtEndDate;
    private Choice cmbStatus;
    private TextField txtSearch;

    private Button btnCreatePass;
    private Button btnUpdatePass;
    private Button btnDeletePass;
    private Button btnClear;
    private Button btnSearch;
    private Button btnRefresh;

    private Label lblPassCount;
    private List listPasses;

    private static class UserItem {
        int id;
        String name;
        String role;
        UserItem(int id, String name, String role) { this.id = id; this.name = name; this.role = role; }
    }

    private static class VehicleItem {
        int id;
        int userId;
        String number;
        String type;
        VehicleItem(int id, int userId, String number, String type) { this.id = id; this.userId = userId; this.number = number; this.type = type; }
    }

    private static class PassRecord {
        int passId;
        String passType;
        String start;
        String end;
        String status;
        PassRecord(int passId, String passType, String start, String end, String status) {
            this.passId = passId; this.passType = passType; this.start = start; this.end = end; this.status = status;
        }
    }

    private java.util.List<UserItem> userList = new ArrayList<>();
    private java.util.List<VehicleItem> vehicleList = new ArrayList<>();
    private java.util.List<PassRecord> passRecords = new ArrayList<>();

    public PassPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.COLOR_BG);

        initUI();
        loadDropdowns();
        loadPassesData();
    }

    private void initUI() {
        // Top Header
        Panel headerPanel = UITheme.createHeaderPanel(
                "Campus Parking Passes & Long-Term Permits",
                "Issue, renew, search, and manage semester, monthly, faculty, student, and visitor parking permits."
        );
        add(headerPanel, BorderLayout.NORTH);

        // Center Split: Form (Left) + List (Right)
        Panel centerPanel = new Panel(new BorderLayout(12, 12));
        centerPanel.setBackground(UITheme.COLOR_BG);

        // Form Card Panel
        UITheme.CardPanel formCard = new UITheme.CardPanel(14, 16, 14, 16);
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(360, 520));

        Label lblFormTitle = new Label("CREATE / MANAGE PASS");
        lblFormTitle.setFont(UITheme.FONT_SECTION_TITLE);
        lblFormTitle.setForeground(UITheme.COLOR_PRIMARY_DARK);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        Panel fieldsPanel = new Panel(new GridLayout(7, 2, 8, 6));
        fieldsPanel.setBackground(Color.WHITE);

        fieldsPanel.add(new Label("Pass ID:"));
        txtPassId = UITheme.createTextField(10);
        txtPassId.setEditable(false);
        txtPassId.setBackground(new Color(241, 245, 249));
        fieldsPanel.add(txtPassId);

        fieldsPanel.add(new Label("Cardholder *:"));
        cmbUsers = new Choice();
        cmbUsers.setFont(UITheme.FONT_REGULAR);
        fieldsPanel.add(cmbUsers);

        fieldsPanel.add(new Label("Vehicle *:"));
        cmbVehicles = new Choice();
        cmbVehicles.setFont(UITheme.FONT_REGULAR);
        fieldsPanel.add(cmbVehicles);

        fieldsPanel.add(new Label("Pass Type *:"));
        cmbPassType = UITheme.createChoice(new String[]{"Monthly", "Semester", "Student", "Faculty", "Daily", "Visitor"});
        fieldsPanel.add(cmbPassType);

        fieldsPanel.add(new Label("Start Date *:"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        txtStartDate = UITheme.createTextField(10);
        txtStartDate.setText(sdf.format(new Date()));
        fieldsPanel.add(txtStartDate);

        fieldsPanel.add(new Label("End Date *:"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        txtEndDate = UITheme.createTextField(10);
        txtEndDate.setText(sdf.format(cal.getTime()));
        fieldsPanel.add(txtEndDate);

        fieldsPanel.add(new Label("Status *:"));
        cmbStatus = UITheme.createChoice(new String[]{"ACTIVE", "EXPIRED", "CANCELLED"});
        fieldsPanel.add(cmbStatus);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Action Buttons Grid
        Panel btnPanel = new Panel(new GridLayout(2, 2, 8, 8));
        btnPanel.setBackground(Color.WHITE);

        btnCreatePass = UITheme.createSuccessButton("Create Pass");
        btnUpdatePass = UITheme.createPrimaryButton("Update");
        btnDeletePass = UITheme.createDangerButton("Delete");
        btnClear = UITheme.createSecondaryButton("Clear");

        btnPanel.add(btnCreatePass);
        btnPanel.add(btnUpdatePass);
        btnPanel.add(btnDeletePass);
        btnPanel.add(btnClear);

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
        txtSearch = UITheme.createTextField(12);
        searchBar.add(txtSearch);

        btnSearch = UITheme.createPrimaryButton("Search");
        searchBar.add(btnSearch);
        btnRefresh = UITheme.createSecondaryButton("View All");
        searchBar.add(btnRefresh);
        listTop.add(searchBar, BorderLayout.WEST);

        lblPassCount = new Label("Issued Passes: 0");
        lblPassCount.setFont(UITheme.FONT_BOLD);
        lblPassCount.setForeground(UITheme.COLOR_PRIMARY_DARK);
        listTop.add(lblPassCount, BorderLayout.EAST);
        listCard.add(listTop, BorderLayout.NORTH);

        Label headerRow = new Label("  PASS ID |  USER NAME             |  ROLE        |  PLATE NUMBER  |  TYPE       |  START       |  END         |  STATUS");
        headerRow.setFont(UITheme.FONT_MONO);
        headerRow.setBackground(new Color(241, 245, 249));

        listPasses = new List(15, false);
        listPasses.setFont(UITheme.FONT_MONO);
        listPasses.setBackground(Color.WHITE);

        Panel listContainer = new Panel(new BorderLayout(2, 2));
        listContainer.add(headerRow, BorderLayout.NORTH);
        listContainer.add(listPasses, BorderLayout.CENTER);
        listCard.add(listContainer, BorderLayout.CENTER);

        centerPanel.add(listCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Event Listeners
        cmbUsers.addItemListener(e -> filterVehiclesForUser());
        btnCreatePass.addActionListener(e -> createPass());
        btnUpdatePass.addActionListener(e -> updatePass());
        btnDeletePass.addActionListener(e -> deletePass());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchPasses());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadDropdowns();
            loadPassesData();
        });

        listPasses.addItemListener(e -> {
            int idx = listPasses.getSelectedIndex();
            if (idx >= 0 && idx < passRecords.size()) {
                PassRecord rec = passRecords.get(idx);
                txtPassId.setText(String.valueOf(rec.passId));
                cmbPassType.select(rec.passType);
                txtStartDate.setText(rec.start);
                txtEndDate.setText(rec.end);
                cmbStatus.select(rec.status);
            }
        });
    }

    public void loadDropdowns() {
        cmbUsers.removeAll();
        userList.clear();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id, name, role FROM users ORDER BY name ASC")) {

            while (rs.next()) {
                int id = rs.getInt("user_id");
                String name = rs.getString("name");
                String role = rs.getString("role");
                userList.add(new UserItem(id, name, role));
                cmbUsers.add(name + " (" + role + ") [#" + id + "]");
            }
        } catch (SQLException ex) {
            System.err.println("Error loading pass users: " + ex.getMessage());
        }

        filterVehiclesForUser();
    }

    private void filterVehiclesForUser() {
        cmbVehicles.removeAll();
        vehicleList.clear();

        int uIdx = cmbUsers.getSelectedIndex();
        Integer userId = (uIdx >= 0 && uIdx < userList.size()) ? userList.get(uIdx).id : null;

        String sql = "SELECT vehicle_id, user_id, vehicle_number, vehicle_type FROM vehicles "
                   + (userId != null ? "WHERE user_id = ? " : "")
                   + "ORDER BY vehicle_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (userId != null) pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("vehicle_id");
                    int uid = rs.getInt("user_id");
                    String num = rs.getString("vehicle_number");
                    String typ = rs.getString("vehicle_type");
                    vehicleList.add(new VehicleItem(id, uid, num, typ));
                    cmbVehicles.add(num + " (" + typ + ")");
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error loading pass vehicles: " + ex.getMessage());
        }
    }

    public void loadPassesData() {
        listPasses.removeAll();
        passRecords.clear();

        String sql = "SELECT p.pass_id, u.name AS user_name, u.role AS user_role, "
                   + "v.vehicle_number, p.pass_type, p.start_date, p.end_date, p.status "
                   + "FROM parking_passes p "
                   + "JOIN users u ON p.user_id = u.user_id "
                   + "JOIN vehicles v ON p.vehicle_id = v.vehicle_id "
                   + "ORDER BY p.pass_id DESC";

        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                count++;
                int id = rs.getInt("pass_id");
                String pType = rs.getString("pass_type");
                String start = rs.getDate("start_date").toString();
                String end = rs.getDate("end_date").toString();
                String status = rs.getString("status");

                passRecords.add(new PassRecord(id, pType, start, end, status));

                String line = String.format("  #%-6d | %-22s | %-12s | %-14s | %-11s | %-12s | %-12s | %s",
                        id,
                        truncate(rs.getString("user_name"), 22),
                        rs.getString("user_role"),
                        rs.getString("vehicle_number"),
                        pType, start, end, status);
                listPasses.add(line);
            }
            lblPassCount.setText("Issued Passes: " + count);

        } catch (SQLException ex) {
            System.err.println("Error loading passes: " + ex.getMessage());
        }
    }

    private void createPass() {
        int uIdx = cmbUsers.getSelectedIndex();
        int vIdx = cmbVehicles.getSelectedIndex();
        String passType = cmbPassType.getSelectedItem();
        String startDate = txtStartDate.getText().trim();
        String endDate = txtEndDate.getText().trim();
        String status = cmbStatus.getSelectedItem();

        if (uIdx < 0 || vIdx < 0 || startDate.isEmpty() || endDate.isEmpty()) {
            UITheme.showWarning(this, "Missing Input", "Please fill in all pass fields.");
            return;
        }

        if (startDate.compareTo(endDate) > 0) {
            UITheme.showWarning(this, "Date Error", "Start Date cannot be later than End Date.");
            return;
        }

        int userId = userList.get(uIdx).id;
        int vehicleId = vehicleList.get(vIdx).id;

        String sql = "INSERT INTO parking_passes (user_id, vehicle_id, pass_type, start_date, end_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, vehicleId);
            pstmt.setString(3, passType);
            pstmt.setString(4, startDate);
            pstmt.setString(5, endDate);
            pstmt.setString(6, status);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                UITheme.showSuccess(this, "Pass Issued", "Parking Pass (" + passType + ") issued successfully!");
                clearFields();
                loadPassesData();
            }
        } catch (SQLException ex) {
            UITheme.showError(this, "Pass Error", "Failed to issue pass:\n" + ex.getMessage());
        }
    }

    private void updatePass() {
        String passIdStr = txtPassId.getText().trim();
        if (passIdStr.isEmpty()) {
            UITheme.showWarning(this, "No Selection", "Please click a pass from the list to modify.");
            return;
        }

        int passId = Integer.parseInt(passIdStr);
        String passType = cmbPassType.getSelectedItem();
        String startDate = txtStartDate.getText().trim();
        String endDate = txtEndDate.getText().trim();
        String status = cmbStatus.getSelectedItem();

        String sql = "UPDATE parking_passes SET pass_type = ?, start_date = ?, end_date = ?, status = ? WHERE pass_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, passType);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);
            pstmt.setString(4, status);
            pstmt.setInt(5, passId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                UITheme.showSuccess(this, "Pass Updated", "Pass #" + passId + " updated successfully.");
                clearFields();
                loadPassesData();
            }
        } catch (SQLException ex) {
            UITheme.showError(this, "Update Error", "Failed to update pass:\n" + ex.getMessage());
        }
    }

    private void deletePass() {
        String passIdStr = txtPassId.getText().trim();
        if (passIdStr.isEmpty()) {
            UITheme.showWarning(this, "No Selection", "Click a pass from the list first to delete.");
            return;
        }

        int passId = Integer.parseInt(passIdStr);
        boolean confirmed = UITheme.showConfirm(this, "Confirm Deletion", "Permanently remove parking pass #" + passId + "?");
        if (!confirmed) return;

        String sql = "DELETE FROM parking_passes WHERE pass_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, passId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                UITheme.showSuccess(this, "Pass Deleted", "Pass #" + passId + " has been deleted.");
                clearFields();
                loadPassesData();
            }
        } catch (SQLException ex) {
            UITheme.showError(this, "Deletion Error", "Could not delete pass:\n" + ex.getMessage());
        }
    }

    private void searchPasses() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            loadPassesData();
            return;
        }

        listPasses.removeAll();
        passRecords.clear();

        String sql = "SELECT p.pass_id, u.name AS user_name, u.role AS user_role, "
                   + "v.vehicle_number, p.pass_type, p.start_date, p.end_date, p.status "
                   + "FROM parking_passes p "
                   + "JOIN users u ON p.user_id = u.user_id "
                   + "JOIN vehicles v ON p.vehicle_id = v.vehicle_id "
                   + "WHERE u.name LIKE ? OR v.vehicle_number LIKE ? OR p.pass_type LIKE ? "
                   + "ORDER BY p.pass_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + query + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    int id = rs.getInt("pass_id");
                    String pType = rs.getString("pass_type");
                    String start = rs.getDate("start_date").toString();
                    String end = rs.getDate("end_date").toString();
                    String status = rs.getString("status");

                    passRecords.add(new PassRecord(id, pType, start, end, status));
                    String line = String.format("  #%-6d | %-22s | %-12s | %-14s | %-11s | %-12s | %-12s | %s",
                            id,
                            truncate(rs.getString("user_name"), 22),
                            rs.getString("user_role"),
                            rs.getString("vehicle_number"),
                            pType, start, end, status);
                    listPasses.add(line);
                }
                lblPassCount.setText("Matches: " + count);
                if (count == 0) {
                    UITheme.showWarning(this, "No Matches", "No passes found matching: '" + query + "'.");
                }
            }
        } catch (SQLException ex) {
            UITheme.showError(this, "Search Error", "Error searching passes:\n" + ex.getMessage());
        }
    }

    private void clearFields() {
        txtPassId.setText("");
        if (cmbUsers.getItemCount() > 0) cmbUsers.select(0);
        if (cmbPassType.getItemCount() > 0) cmbPassType.select(0);
        if (cmbStatus.getItemCount() > 0) cmbStatus.select(0);
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen - 2) + ".." : str;
    }
}
