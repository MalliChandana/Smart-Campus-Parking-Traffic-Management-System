import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * UserPanel - Pure Java AWT User Management Directory Module
 *
 * Implements:
 * - User registration form
 * - Phone & Email validation
 * - AWT List directory with item selection
 *
 * Demonstrates:
 * 1. Statement: For general user listing and count queries.
 * 2. PreparedStatement: For parameterized INSERT, UPDATE, DELETE, and search queries.
 */
public class UserPanel extends Panel {

    private TextField txtUserId;
    private TextField txtName;
    private TextField txtEmail;
    private TextField txtPhone;
    private Choice cmbRole;
    private TextField txtSearch;

    private Button btnAdd;
    private Button btnUpdate;
    private Button btnDelete;
    private Button btnClear;
    private Button btnSearch;
    private Button btnRefresh;

    private Label lblUserCount;
    private List listUsers;
    private java.util.List<Integer> userIds = new ArrayList<>();

    public UserPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.COLOR_BG);

        initUI();
        loadUserData();
    }

    private void initUI() {
        // Top Header
        Panel headerPanel = UITheme.createHeaderPanel(
                "Campus User Management Directory",
                "Register, search, update, and manage drivers, students, faculty members, and campus visitors."
        );
        add(headerPanel, BorderLayout.NORTH);

        // Center Split: Form (Left) + List (Right)
        Panel centerPanel = new Panel(new BorderLayout(12, 12));
        centerPanel.setBackground(UITheme.COLOR_BG);

        // Form Card Panel
        UITheme.CardPanel formCard = new UITheme.CardPanel(14, 16, 14, 16);
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(360, 480));

        Label lblFormTitle = new Label("User Registration & Profile");
        lblFormTitle.setFont(UITheme.FONT_SECTION_TITLE);
        lblFormTitle.setForeground(UITheme.COLOR_PRIMARY_DARK);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        Panel fieldsPanel = new Panel(new GridLayout(5, 2, 8, 8));
        fieldsPanel.setBackground(Color.WHITE);

        fieldsPanel.add(new Label("User ID:"));
        txtUserId = UITheme.createTextField(10);
        txtUserId.setEditable(false);
        txtUserId.setBackground(new Color(241, 245, 249));
        fieldsPanel.add(txtUserId);

        fieldsPanel.add(new Label("Full Name *:"));
        txtName = UITheme.createTextField(14);
        fieldsPanel.add(txtName);

        fieldsPanel.add(new Label("Email Address *:"));
        txtEmail = UITheme.createTextField(14);
        fieldsPanel.add(txtEmail);

        fieldsPanel.add(new Label("Phone Number *:"));
        txtPhone = UITheme.createTextField(14);
        fieldsPanel.add(txtPhone);

        fieldsPanel.add(new Label("Campus Role *:"));
        cmbRole = UITheme.createChoice(new String[]{"Student", "Faculty", "Staff", "Visitor"});
        fieldsPanel.add(cmbRole);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons Panel
        Panel btnPanel = new Panel(new GridLayout(2, 2, 8, 8));
        btnPanel.setBackground(Color.WHITE);

        btnAdd = UITheme.createSuccessButton("Add User");
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

        lblUserCount = new Label("Total Users: 0");
        lblUserCount.setFont(UITheme.FONT_BOLD);
        lblUserCount.setForeground(UITheme.COLOR_PRIMARY_DARK);
        listTop.add(lblUserCount, BorderLayout.EAST);
        listCard.add(listTop, BorderLayout.NORTH);

        // Header and List
        Label headerRow = new Label("  ID  |  NAME                   |  EMAIL                    |  PHONE        |  ROLE");
        headerRow.setFont(UITheme.FONT_MONO);
        headerRow.setBackground(new Color(241, 245, 249));

        listUsers = new List(15, false);
        listUsers.setFont(UITheme.FONT_MONO);
        listUsers.setBackground(Color.WHITE);

        Panel listContainer = new Panel(new BorderLayout(2, 2));
        listContainer.add(headerRow, BorderLayout.NORTH);
        listContainer.add(listUsers, BorderLayout.CENTER);
        listCard.add(listContainer, BorderLayout.CENTER);

        centerPanel.add(listCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Event Handlers
        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchUsers());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadUserData();
        });

        listUsers.addItemListener(e -> {
            int idx = listUsers.getSelectedIndex();
            if (idx >= 0 && idx < userIds.size()) {
                loadSelectedUserDetails(userIds.get(idx));
            }
        });
    }

    public void loadUserData() {
        listUsers.removeAll();
        userIds.clear();
        String sql = "SELECT user_id, name, email, phone, role FROM users ORDER BY user_id ASC";

        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                count++;
                int id = rs.getInt("user_id");
                userIds.add(id);

                String line = String.format("  %-4d | %-22s | %-24s | %-12s | %s",
                        id,
                        truncate(rs.getString("name"), 22),
                        truncate(rs.getString("email"), 24),
                        rs.getString("phone"),
                        rs.getString("role"));
                listUsers.add(line);
            }
            lblUserCount.setText("Total Users: " + count);

        } catch (SQLException ex) {
            UITheme.showError(this, "Database Error", "Error loading users:\n" + ex.getMessage());
        }
    }

    private void loadSelectedUserDetails(int userId) {
        String sql = "SELECT user_id, name, email, phone, role FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    txtUserId.setText(String.valueOf(rs.getInt("user_id")));
                    txtName.setText(rs.getString("name"));
                    txtEmail.setText(rs.getString("email"));
                    txtPhone.setText(rs.getString("phone"));
                    cmbRole.select(rs.getString("role"));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching user: " + ex.getMessage());
        }
    }

    private void addUser() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String role = cmbRole.getSelectedItem();

        if (!validateInputs(name, email, phone)) return;

        String sql = "INSERT INTO users (name, email, phone, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, role);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                UITheme.showSuccess(this, "User Added", "User '" + name + "' added successfully!");
                clearFields();
                loadUserData();
            }

        } catch (SQLException ex) {
            UITheme.showError(this, "Database Error", "Failed to add user:\n" + ex.getMessage());
        }
    }

    private void updateUser() {
        String idStr = txtUserId.getText().trim();
        if (idStr.isEmpty()) {
            UITheme.showWarning(this, "No User Selected", "Please select a user from the list to update.");
            return;
        }

        int userId = Integer.parseInt(idStr);
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String role = cmbRole.getSelectedItem();

        if (!validateInputs(name, email, phone)) return;

        String sql = "UPDATE users SET name = ?, email = ?, phone = ?, role = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, role);
            pstmt.setInt(5, userId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                UITheme.showSuccess(this, "User Updated", "User #" + userId + " updated successfully.");
                clearFields();
                loadUserData();
            }

        } catch (SQLException ex) {
            UITheme.showError(this, "Database Error", "Failed to update user:\n" + ex.getMessage());
        }
    }

    private void deleteUser() {
        String idStr = txtUserId.getText().trim();
        if (idStr.isEmpty()) {
            UITheme.showWarning(this, "No Selection", "Please click a user in the list to select for deletion.");
            return;
        }

        int userId = Integer.parseInt(idStr);
        String userName = txtName.getText().trim();

        boolean confirmed = UITheme.showConfirm(this, "Confirm Deletion",
                "Delete user #" + userId + " (" + userName + ")?\nThis may remove linked vehicles.");
        if (!confirmed) return;

        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                UITheme.showSuccess(this, "User Deleted", "User #" + userId + " deleted.");
                clearFields();
                loadUserData();
            }

        } catch (SQLException ex) {
            UITheme.showError(this, "Deletion Error", "Cannot delete user. Ensure related records are cleared:\n" + ex.getMessage());
        }
    }

    private void searchUsers() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            loadUserData();
            return;
        }

        listUsers.removeAll();
        userIds.clear();

        String sql = "SELECT user_id, name, email, phone, role FROM users "
                   + "WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? OR role LIKE ? "
                   + "ORDER BY user_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + query + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setString(4, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    int id = rs.getInt("user_id");
                    userIds.add(id);
                    String line = String.format("  %-4d | %-22s | %-24s | %-12s | %s",
                            id,
                            truncate(rs.getString("name"), 22),
                            truncate(rs.getString("email"), 24),
                            rs.getString("phone"),
                            rs.getString("role"));
                    listUsers.add(line);
                }
                lblUserCount.setText("Matches: " + count);
                if (count == 0) {
                    UITheme.showWarning(this, "No Matches", "No users found matching: '" + query + "'.");
                }
            }

        } catch (SQLException ex) {
            UITheme.showError(this, "Search Error", "Error executing user search:\n" + ex.getMessage());
        }
    }

    private boolean validateInputs(String name, String email, String phone) {
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            UITheme.showWarning(this, "Validation Error", "All fields marked with * are required.");
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
            UITheme.showWarning(this, "Invalid Email", "Please enter a valid email address.");
            return false;
        }
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 10) {
            UITheme.showWarning(this, "Invalid Phone", "Please enter a valid 10-digit phone number.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        txtUserId.setText("");
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        if (cmbRole.getItemCount() > 0) cmbRole.select(0);
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen - 2) + ".." : str;
    }
}
