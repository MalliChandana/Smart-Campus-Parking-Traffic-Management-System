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
 * PaymentPanel - Pure Java AWT Payment Receipts & Financial Module
 *
 * Implements:
 * - Payment processing form (Session ID lookup, Amount, Method: Cash, Card, UPI)
 * - Official Payment Receipt Voucher popup dialog
 * - AWT List transaction log with live search & aggregate total revenue counter
 *
 * Demonstrates:
 * 1. PreparedStatement for Session validation and amount verification
 * 2. PreparedStatement for INSERT into payments
 * 3. PreparedStatement for Parameterized search
 * 4. Statement for full payment history & aggregate total revenue
 */
public class PaymentPanel extends Panel {

    private TextField txtSessionId;
    private TextField txtVehicleNumber;
    private TextField txtAmount;
    private Choice cmbPaymentMethod;
    private TextField txtSearch;

    private Button btnProcessPayment;
    private Button btnFetchSessionAmount;
    private Button btnViewReceipt;
    private Button btnClear;
    private Button btnSearch;
    private Button btnRefresh;

    private Label lblTotalRevenue;
    private List listPayments;

    private static class PaymentRecord {
        int payId;
        int sessionId;
        String vehNum;
        String owner;
        double amount;
        String method;
        String date;
        String status;

        PaymentRecord(int payId, int sessionId, String vehNum, String owner, double amount, String method, String date, String status) {
            this.payId = payId;
            this.sessionId = sessionId;
            this.vehNum = vehNum;
            this.owner = owner;
            this.amount = amount;
            this.method = method;
            this.date = date;
            this.status = status;
        }
    }

    private java.util.List<PaymentRecord> paymentRecords = new ArrayList<>();

    public PaymentPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.COLOR_BG);

        initUI();
        loadPaymentsData();
    }

    private void initUI() {
        // Top Header
        Panel headerPanel = UITheme.createHeaderPanel(
                "Payment Receipts & Financial Transactions",
                "Record parking session fees via Cash, Card, or UPI and generate verifiable receipt vouchers."
        );
        add(headerPanel, BorderLayout.NORTH);

        // Center Split: Form (Left) + List (Right)
        Panel centerPanel = new Panel(new BorderLayout(12, 12));
        centerPanel.setBackground(UITheme.COLOR_BG);

        // Form Card Panel
        UITheme.CardPanel formCard = new UITheme.CardPanel(14, 16, 14, 16);
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(360, 480));

        Label lblFormTitle = new Label("Process Parking Payment");
        lblFormTitle.setFont(UITheme.FONT_SECTION_TITLE);
        lblFormTitle.setForeground(UITheme.COLOR_PRIMARY_DARK);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        Panel fieldsPanel = new Panel(new GridLayout(5, 2, 8, 8));
        fieldsPanel.setBackground(Color.WHITE);

        fieldsPanel.add(new Label("Session ID *:"));
        Panel sessRow = new Panel(new BorderLayout(4, 0));
        txtSessionId = UITheme.createTextField(6);
        btnFetchSessionAmount = UITheme.createSecondaryButton("Lookup");
        btnFetchSessionAmount.addActionListener(e -> fetchSessionFee());
        sessRow.add(txtSessionId, BorderLayout.CENTER);
        sessRow.add(btnFetchSessionAmount, BorderLayout.EAST);
        fieldsPanel.add(sessRow);

        fieldsPanel.add(new Label("Vehicle Plate:"));
        txtVehicleNumber = UITheme.createTextField(10);
        txtVehicleNumber.setEditable(false);
        txtVehicleNumber.setBackground(new Color(241, 245, 249));
        fieldsPanel.add(txtVehicleNumber);

        fieldsPanel.add(new Label("Amount (₹) *:"));
        txtAmount = UITheme.createTextField(10);
        fieldsPanel.add(txtAmount);

        fieldsPanel.add(new Label("Payment Method *:"));
        cmbPaymentMethod = UITheme.createChoice(new String[]{"Cash", "Card", "UPI"});
        fieldsPanel.add(cmbPaymentMethod);

        fieldsPanel.add(new Label("Notes:"));
        Label lblNote = new Label("Session fees auto-calculated on gate departure");
        lblNote.setFont(UITheme.FONT_SMALL);
        lblNote.setForeground(UITheme.COLOR_TEXT_MUTED);
        fieldsPanel.add(lblNote);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Action Buttons
        Panel btnPanel = new Panel(new GridLayout(2, 1, 6, 6));
        btnProcessPayment = UITheme.createSuccessButton("PROCESS PAYMENT");
        btnClear = UITheme.createSecondaryButton("Clear Fields");
        btnPanel.add(btnProcessPayment);
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
        txtSearch = UITheme.createTextField(10);
        searchBar.add(txtSearch);

        btnSearch = UITheme.createPrimaryButton("Search");
        searchBar.add(btnSearch);

        btnRefresh = UITheme.createSecondaryButton("View All");
        searchBar.add(btnRefresh);

        btnViewReceipt = UITheme.createPrimaryButton("View Receipt");
        searchBar.add(btnViewReceipt);

        listTop.add(searchBar, BorderLayout.WEST);

        lblTotalRevenue = new Label("Total: ₹0.00");
        lblTotalRevenue.setFont(UITheme.FONT_BOLD);
        lblTotalRevenue.setForeground(UITheme.COLOR_AVAILABLE);
        listTop.add(lblTotalRevenue, BorderLayout.EAST);
        listCard.add(listTop, BorderLayout.NORTH);

        Label headerRow = new Label("  PAY ID  |  SESSION  |  PLATE NUMBER       |  OWNER NAME             |  AMOUNT (₹)  |  METHOD   |  DATE        |  STATUS");
        headerRow.setFont(UITheme.FONT_MONO);
        headerRow.setBackground(new Color(241, 245, 249));

        listPayments = new List(15, false);
        listPayments.setFont(UITheme.FONT_MONO);
        listPayments.setBackground(Color.WHITE);

        Panel listContainer = new Panel(new BorderLayout(2, 2));
        listContainer.add(headerRow, BorderLayout.NORTH);
        listContainer.add(listPayments, BorderLayout.CENTER);
        listCard.add(listContainer, BorderLayout.CENTER);

        centerPanel.add(listCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Event Handlers
        btnFetchSessionAmount.addActionListener(e -> fetchSessionFee());
        btnProcessPayment.addActionListener(e -> processPayment());
        btnViewReceipt.addActionListener(e -> showSelectedReceipt());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchPayments());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadPaymentsData();
        });
    }

    private void fetchSessionFee() {
        String sessStr = txtSessionId.getText().trim();
        if (sessStr.isEmpty()) {
            UITheme.showWarning(this, "Session Required", "Enter a Session ID to lookup.");
            return;
        }

        try {
            int sessId = Integer.parseInt(sessStr);
            String sql = "SELECT ps.fee, v.vehicle_number FROM parking_sessions ps "
                       + "JOIN vehicles v ON ps.vehicle_id = v.vehicle_id WHERE ps.session_id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, sessId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        txtAmount.setText(String.format("%.2f", rs.getDouble("fee")));
                        txtVehicleNumber.setText(rs.getString("vehicle_number"));
                    } else {
                        UITheme.showWarning(this, "Not Found", "Parking Session #" + sessId + " does not exist.");
                    }
                }
            }
        } catch (NumberFormatException ex) {
            UITheme.showWarning(this, "Invalid ID", "Session ID must be an integer.");
        } catch (SQLException ex) {
            UITheme.showError(this, "Database Error", "Error fetching fee:\n" + ex.getMessage());
        }
    }

    public void loadPaymentsData() {
        listPayments.removeAll();
        paymentRecords.clear();

        String sql = "SELECT p.payment_id, p.session_id, v.vehicle_number, u.name AS owner_name, "
                   + "p.amount, p.payment_method, p.payment_date, p.status "
                   + "FROM payments p "
                   + "JOIN parking_sessions ps ON p.session_id = ps.session_id "
                   + "JOIN vehicles v ON ps.vehicle_id = v.vehicle_id "
                   + "JOIN users u ON v.user_id = u.user_id "
                   + "ORDER BY p.payment_id DESC";

        double totalSum = 0.0;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int payId = rs.getInt("payment_id");
                int sessId = rs.getInt("session_id");
                String plate = rs.getString("vehicle_number");
                String owner = rs.getString("owner_name");
                double amt = rs.getDouble("amount");
                String method = rs.getString("payment_method");
                String date = rs.getTimestamp("payment_date").toString();
                String status = rs.getString("status");

                if ("PAID".equalsIgnoreCase(status)) totalSum += amt;
                paymentRecords.add(new PaymentRecord(payId, sessId, plate, owner, amt, method, date, status));

                String line = String.format("  %-7d | %-9d | %-19s | %-23s | %-12.2f | %-9s | %-11s | %s",
                        payId, sessId, plate, truncate(owner, 23), amt, method, date.substring(0, 10), status);
                listPayments.add(line);
            }
            lblTotalRevenue.setText("Total Revenue: ₹" + String.format("%.2f", totalSum));

        } catch (SQLException ex) {
            System.err.println("Error loading payments: " + ex.getMessage());
        }
    }

    private void processPayment() {
        String sessStr = txtSessionId.getText().trim();
        String amtStr = txtAmount.getText().trim();
        String method = cmbPaymentMethod.getSelectedItem();

        if (sessStr.isEmpty() || amtStr.isEmpty()) {
            UITheme.showWarning(this, "Missing Input", "Enter Session ID and Payment Amount.");
            return;
        }

        try {
            int sessionId = Integer.parseInt(sessStr);
            double amount = Double.parseDouble(amtStr);
            if (amount < 0) {
                UITheme.showWarning(this, "Invalid Amount", "Amount cannot be negative.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String checkSql = "SELECT v.vehicle_number, u.name FROM parking_sessions ps "
                                + "JOIN vehicles v ON ps.vehicle_id = v.vehicle_id "
                                + "JOIN users u ON v.user_id = u.user_id WHERE ps.session_id = ?";
                String vehiclePlate = "";
                String ownerName = "";

                try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                    checkPstmt.setInt(1, sessionId);
                    try (ResultSet rs = checkPstmt.executeQuery()) {
                        if (rs.next()) {
                            vehiclePlate = rs.getString("vehicle_number");
                            ownerName = rs.getString("name");
                        } else {
                            UITheme.showError(this, "Not Found", "Session #" + sessionId + " does not exist.");
                            return;
                        }
                    }
                }

                String insertSql = "INSERT INTO payments (session_id, amount, payment_method, payment_date, status) VALUES (?, ?, ?, NOW(), 'PAID')";
                int paymentId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setInt(1, sessionId);
                    pstmt.setDouble(2, amount);
                    pstmt.setString(3, method);
                    pstmt.executeUpdate();

                    try (ResultSet keys = pstmt.getGeneratedKeys()) {
                        if (keys.next()) paymentId = keys.getInt(1);
                    }
                }

                // Show receipt voucher
                showReceiptDialog(paymentId, sessionId, vehiclePlate, ownerName, amount, method, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

                clearFields();
                loadPaymentsData();
            }

        } catch (NumberFormatException e) {
            UITheme.showWarning(this, "Format Error", "Session ID and Amount must be valid numbers.");
        } catch (SQLException ex) {
            UITheme.showError(this, "Database Error", "Error recording payment:\n" + ex.getMessage());
        }
    }

    private void showReceiptDialog(int payId, int sessId, String plate, String owner, double amount, String method, String date) {
        String receiptText = "==================================================\n"
                           + "           SMART CAMPUS PARKING SYSTEM\n"
                           + "            OFFICIAL PAYMENT RECEIPT\n"
                           + "==================================================\n"
                           + " Receipt / Payment ID: #" + payId + "\n"
                           + " Parking Session ID  : #" + sessId + "\n"
                           + " Vehicle License Plate: " + plate + "\n"
                           + " Driver / Owner      : " + owner + "\n"
                           + " Payment Method      : " + method + "\n"
                           + " Transaction Status  : PAID (Verified)\n"
                           + " Date & Timestamp    : " + date + "\n"
                           + "--------------------------------------------------\n"
                           + " TOTAL AMOUNT PAID   : ₹" + String.format("%.2f", amount) + "\n"
                           + "==================================================\n"
                           + " Thank you for using Campus Parking Services!";

        UITheme.showSuccess(this, "Payment Receipt Voucher #" + payId, receiptText);
    }

    private void showSelectedReceipt() {
        int idx = listPayments.getSelectedIndex();
        if (idx < 0 || idx >= paymentRecords.size()) {
            UITheme.showWarning(this, "No Selection", "Please click a payment record from the list to view receipt.");
            return;
        }

        PaymentRecord rec = paymentRecords.get(idx);
        showReceiptDialog(rec.payId, rec.sessionId, rec.vehNum, rec.owner, rec.amount, rec.method, rec.date);
    }

    private void searchPayments() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            loadPaymentsData();
            return;
        }

        listPayments.removeAll();
        paymentRecords.clear();

        String sql = "SELECT p.payment_id, p.session_id, v.vehicle_number, u.name AS owner_name, "
                   + "p.amount, p.payment_method, p.payment_date, p.status "
                   + "FROM payments p "
                   + "JOIN parking_sessions ps ON p.session_id = ps.session_id "
                   + "JOIN vehicles v ON ps.vehicle_id = v.vehicle_id "
                   + "JOIN users u ON v.user_id = u.user_id "
                   + "WHERE v.vehicle_number LIKE ? OR p.payment_method LIKE ? OR u.name LIKE ? "
                   + "ORDER BY p.payment_id DESC";

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
                    int payId = rs.getInt("payment_id");
                    int sessId = rs.getInt("session_id");
                    String plate = rs.getString("vehicle_number");
                    String owner = rs.getString("owner_name");
                    double amt = rs.getDouble("amount");
                    String method = rs.getString("payment_method");
                    String date = rs.getTimestamp("payment_date").toString();
                    String status = rs.getString("status");

                    paymentRecords.add(new PaymentRecord(payId, sessId, plate, owner, amt, method, date, status));
                    String line = String.format("  %-7d | %-9d | %-19s | %-23s | %-12.2f | %-9s | %-11s | %s",
                            payId, sessId, plate, truncate(owner, 23), amt, method, date.substring(0, 10), status);
                    listPayments.add(line);
                }
                if (count == 0) {
                    UITheme.showWarning(this, "No Matches", "No payments found matching: '" + query + "'.");
                }
            }
        } catch (SQLException ex) {
            UITheme.showError(this, "Search Error", "Error executing search:\n" + ex.getMessage());
        }
    }

    private void clearFields() {
        txtSessionId.setText("");
        txtVehicleNumber.setText("");
        txtAmount.setText("");
        if (cmbPaymentMethod.getItemCount() > 0) cmbPaymentMethod.select(0);
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen - 2) + ".." : str;
    }
}
