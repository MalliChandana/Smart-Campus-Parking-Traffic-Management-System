import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * VehiclePanel - Pure Java AWT Vehicle Registry Module
 *
 * Implements:
 * - Vehicle registration linked to registered campus owners
 * - Type classification: Bike, Car, Truck
 * - License plate validation & duplicate plate prevention
 * - AWT List directory
 *
 * Demonstrates:
 * 1. Statement: For JOIN SELECT queries between vehicles and users.
 * 2. PreparedStatement: For parameterized INSERT, UPDATE, DELETE, and search queries.
 */
public class VehiclePanel extends Panel {

    private TextField txtVehicleId;
    private TextField txtVehicleNumber;
    private Choice cmbVehicleType;
    private Choice cmbUser;
    private TextField txtSearch;

    private Button btnAdd;
    private Button btnUpdate;
    private Button btnDelete;
    private Button btnClear;
    private Button btnSearch;
    private Button btnRefresh;

    private Label lblVehicleCount;
    private List listVehicles;
    private java.util.List<Integer> vehicleIds = new ArrayList<>();
    private java.util.List<Integer> userIds = new ArrayList<>();

    public VehiclePanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.COLOR_BG);

        initUI();
        loadUserDropdown();
        loadVehicleData();
    }

    private void initUI() {
        // Top Header
        Panel headerPanel = UITheme.createHeaderPanel(
                "Campus Vehicle Registry & Classification",
                "Register student, faculty, and visitor vehicles (Bike, Car, Truck) with license plate validation."
        );
        add(headerPanel, BorderLayout.NORTH);

        // Center Split: Form (Left) + List (Right)
        Panel centerPanel = new Panel(new BorderLayout(12, 12));
        centerPanel.setBackground(UITheme.COLOR_BG);

        // Form Card Panel
        UITheme.CardPanel formCard = new UITheme.CardPanel(14, 16, 14, 16);
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(360, 480));

        Label lblFormTitle = new Label("Vehicle Registration Form");
        lblFormTitle.setFont(UITheme.FONT_SECTION_TITLE);
        lblFormTitle.setForeground(UITheme.COLOR_PRIMARY_DARK);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        Panel fieldsPanel = new Panel(new GridLayout(5, 2, 8, 8));
        fieldsPanel.setBackground(Color.WHITE);

        fieldsPanel.add(new Label("Vehicle ID:"));
        txtVehicleId = UITheme.createTextField(10);
        txtVehicleId.setEditable(false);
        txtVehicleId.setBackground(new Color(241, 245, 249));
        fieldsPanel.add(txtVehicleId);

        fieldsPanel.add(new Label("Plate Number *:"));
        txtVehicleNumber = UITheme.createTextField(14);
        fieldsPanel.add(txtVehicleNumber);

        fieldsPanel.add(new Label("Vehicle Type *:"));
        cmbVehicleType = UITheme.createChoice(new String[]{"Car", "Bike", "Truck"});
        fieldsPanel.add(cmbVehicleType);

        fieldsPanel.add(new Label("Owner (User) *:"));
        cmbUser = new Choice();
        cmbUser.setFont(UITheme.FONT_REGULAR);
        fieldsPanel.add(cmbUser);

        fieldsPanel.add(new Label("Tariff Rules:"));
        Label lblTariff = new Label("Bike ₹10/hr | Car ₹20/hr | Truck ₹30/hr");
        lblTariff.setFont(UITheme.FONT_SMALL);
        lblTariff.setForeground(UITheme.COLOR_TEXT_MUTED);
        fieldsPanel.add(lblTariff);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons Panel
        Panel btnPanel = new Panel(new GridLayout(2, 2, 8, 8));
        btnPanel.setBackground(Color.WHITE);

        btnAdd = UITheme.createSuccessButton("Register");
        btnUpdate = UITheme.createPrimaryButton("Update");
        btnDelete = UITheme.createDangerButton("Delete");
        btnClear = UITheme.createSecondaryButton("Clear");

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        formCard.add(btnPanel, BorderLayout.SOUTH);
        centerPanel.add(formCard, BorderLayout.WEST);

        // List Card Panel
        UITheme.CardPanel listCard = new UITheme.CardPanel(14, 16, 14, 16);
        listCard.setLayout(new BorderLayout(10, 10));

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

        lblVehicleCount = new Label("Registered Vehicles: 0");
        lblVehicleCount.setFont(UITheme.FONT_BOLD);
        lblVehicleCount.setForeground(UITheme.COLOR_PRIMARY_DARK);
        listTop.add(lblVehicleCount, BorderLayout.EAST);
        listCard.add(listTop, BorderLayout.NORTH);

        // Header and List
        Label headerRow = new Label("  ID  |  PLATE NUMBER       |  TYPE   |  OWNER NAME             |  ROLE        |  PHONE");
        headerRow.setFont(UITheme.FONT_MONO);
        headerRow.setBackground(new Color(241, 245, 249));

        listVehicles = new List(15, false);
        listVehicles.setFont(UITheme.FONT_MONO);
        listVehicles.setBackground(Color.WHITE);

        Panel listContainer = new Panel(new BorderLayout(2, 2));
        listContainer.add(headerRow, BorderLayout.NORTH);
        listContainer.add(listVehicles, BorderLayout.CENTER);
        listCard.add(listContainer, BorderLayout.CENTER);

        centerPanel.add(listCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Event Listeners
        btnAdd.addActionListener(e -> addVehicle());
        btnUpdate.addActionListener(e -> updateVehicle());
        btnDelete.addActionListener(e -> deleteVehicle());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchVehicles());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadUserDropdown();
            loadVehicleData();
        });

        listVehicles.addItemListener(e -> {
            int idx = listVehicles.getSelectedIndex();
            if (idx >= 0 && idx < vehicleIds.size()) {
                loadSelectedVehicleDetails(vehicleIds.get(idx));
            }
        });
    }

    public void loadUserDropdown() {
        cmbUser.removeAll();
        userIds.clear();
        String sql = "SELECT user_id, name, role FROM users ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("user_id");
                userIds.add(id);
                cmbUser.add(rs.getString("name") + " (" + rs.getString("role") + ") [#" + id + "]");
            }
        } catch (SQLException e) {
            System.err.println("Error loading users for vehicle: " + e.getMessage());
        }
    }

    public void loadVehicleData() {
        listVehicles.removeAll();
        vehicleIds.clear();

        String sql = "SELECT v.vehicle_id, v.vehicle_number, v.vehicle_type, u.name AS owner_name, u.role AS owner_role, u.phone "
                   + "FROM vehicles v JOIN users u ON v.user_id = u.user_id ORDER BY v.vehicle_id ASC";

        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                count++;
                int id = rs.getInt("vehicle_id");
                vehicleIds.add(id);

                String line = String.format("  %-4d | %-19s | %-7s | %-23s | %-12s | %s",
                        id,
                        rs.getString("vehicle_number"),
                        rs.getString("vehicle_type"),
                        truncate(rs.getString("owner_name"), 23),
                        rs.getString("owner_role"),
                        rs.getString("phone"));
                listVehicles.add(line);
            }
            lblVehicleCount.setText("Registered Vehicles: " + count);

        } catch (SQLException ex) {
            UITheme.showError(this, "Database Error", "Error loading vehicles:\n" + ex.getMessage());
        }
    }

    private void loadSelectedVehicleDetails(int vehicleId) {
        String sql = "SELECT vehicle_id, user_id, vehicle_number, vehicle_type FROM vehicles WHERE vehicle_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    txtVehicleId.setText(String.valueOf(rs.getInt("vehicle_id")));
                    txtVehicleNumber.setText(rs.getString("vehicle_number"));
                    cmbVehicleType.select(rs.getString("vehicle_type"));

                    int uId = rs.getInt("user_id");
                    for (int i = 0; i < userIds.size(); i++) {
                        if (userIds.get(i) == uId) {
                            cmbUser.select(i);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching vehicle details: " + ex.getMessage());
        }
    }

    private void addVehicle() {
        String number = txtVehicleNumber.getText().trim().toUpperCase();
        String type = cmbVehicleType.getSelectedItem();
        int userIdx = cmbUser.getSelectedIndex();

        if (number.isEmpty() || userIdx < 0 || userIdx >= userIds.size()) {
            UITheme.showWarning(this, "Missing Input", "Please enter plate number and select an owner.");
            return;
        }

        int userId = userIds.get(userIdx);
        String sql = "INSERT INTO vehicles (user_id, vehicle_number, vehicle_type) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, number);
            pstmt.setString(3, type);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                UITheme.showSuccess(this, "Vehicle Registered", "Vehicle '" + number + "' registered successfully!");
                clearFields();
                loadVehicleData();
            }

        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate") || ex.getMessage().contains("UNIQUE")) {
                UITheme.showError(this, "Duplicate Plate", "Vehicle '" + number + "' is already registered.");
            } else {
                UITheme.showError(this, "Database Error", "Failed to register vehicle:\n" + ex.getMessage());
            }
        }
    }

    private void updateVehicle() {
        String idStr = txtVehicleId.getText().trim();
        if (idStr.isEmpty()) {
            UITheme.showWarning(this, "No Selection", "Please click a vehicle from the list to update.");
            return;
        }

        int vehicleId = Integer.parseInt(idStr);
        String number = txtVehicleNumber.getText().trim().toUpperCase();
        String type = cmbVehicleType.getSelectedItem();
        int userIdx = cmbUser.getSelectedIndex();

        if (number.isEmpty() || userIdx < 0 || userIdx >= userIds.size()) {
            UITheme.showWarning(this, "Missing Input", "Please enter plate number and select an owner.");
            return;
        }

        int userId = userIds.get(userIdx);
        String sql = "UPDATE vehicles SET user_id = ?, vehicle_number = ?, vehicle_type = ? WHERE vehicle_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, number);
            pstmt.setString(3, type);
            pstmt.setInt(4, vehicleId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                UITheme.showSuccess(this, "Vehicle Updated", "Vehicle #" + vehicleId + " updated successfully.");
                clearFields();
                loadVehicleData();
            }

        } catch (SQLException ex) {
            UITheme.showError(this, "Database Error", "Failed to update vehicle:\n" + ex.getMessage());
        }
    }

    private void deleteVehicle() {
        String idStr = txtVehicleId.getText().trim();
        if (idStr.isEmpty()) {
            UITheme.showWarning(this, "No Selection", "Please click a vehicle from the list to delete.");
            return;
        }

        int vehicleId = Integer.parseInt(idStr);
        String plate = txtVehicleNumber.getText().trim();

        boolean confirmed = UITheme.showConfirm(this, "Confirm Deletion",
                "Delete vehicle #" + vehicleId + " (" + plate + ")?\nEnsure this vehicle has no active sessions or violations.");
        if (!confirmed) return;

        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vehicleId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                UITheme.showSuccess(this, "Vehicle Deleted", "Vehicle #" + vehicleId + " has been deleted.");
                clearFields();
                loadVehicleData();
            }

        } catch (SQLException ex) {
            UITheme.showError(this, "Deletion Error", "Cannot delete vehicle. Check for linked records:\n" + ex.getMessage());
        }
    }

    private void searchVehicles() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            loadVehicleData();
            return;
        }

        listVehicles.removeAll();
        vehicleIds.clear();

        String sql = "SELECT v.vehicle_id, v.vehicle_number, v.vehicle_type, u.name AS owner_name, u.role AS owner_role, u.phone "
                   + "FROM vehicles v JOIN users u ON v.user_id = u.user_id "
                   + "WHERE v.vehicle_number LIKE ? OR v.vehicle_type LIKE ? OR u.name LIKE ? "
                   + "ORDER BY v.vehicle_id ASC";

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
                    int id = rs.getInt("vehicle_id");
                    vehicleIds.add(id);
                    String line = String.format("  %-4d | %-19s | %-7s | %-23s | %-12s | %s",
                            id,
                            rs.getString("vehicle_number"),
                            rs.getString("vehicle_type"),
                            truncate(rs.getString("owner_name"), 23),
                            rs.getString("owner_role"),
                            rs.getString("phone"));
                    listVehicles.add(line);
                }
                lblVehicleCount.setText("Matches: " + count);
                if (count == 0) {
                    UITheme.showWarning(this, "No Matches", "No vehicles found matching: '" + query + "'.");
                }
            }

        } catch (SQLException ex) {
            UITheme.showError(this, "Search Error", "Error searching vehicles:\n" + ex.getMessage());
        }
    }

    private void clearFields() {
        txtVehicleId.setText("");
        txtVehicleNumber.setText("");
        if (cmbVehicleType.getItemCount() > 0) cmbVehicleType.select(0);
        if (cmbUser.getItemCount() > 0) cmbUser.select(0);
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen - 2) + ".." : str;
    }
}
