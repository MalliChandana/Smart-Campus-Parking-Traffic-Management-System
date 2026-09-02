import java.awt.*;
import java.awt.event.*;

/**
 * LoginFrame
 * Smart Campus Parking and Traffic Management System
 */
public class LoginFrame extends Frame {

    private TextField txtUsername;
    private TextField txtPassword;
    private Button btnLogin;
    private Button btnDemoFill;
    private Label lblStatus;

    public LoginFrame() {
        super("Smart Campus Parking Management - Admin Sign In");
        initUI();
    }

    private void initUI() {
        setSize(460, 480);
        setResizable(false);
        setLocationRelativeTo(null);
        setBackground(UITheme.COLOR_BG);
        setLayout(new BorderLayout());

        // =========================================================
        // TOP BANNER
        // =========================================================
        Panel bannerPanel = new Panel(new GridLayout(3, 1, 0, 2)) {
            @Override
            public Insets getInsets() {
                return new Insets(24, 16, 20, 16);
            }
        };

        bannerPanel.setBackground(UITheme.COLOR_PRIMARY_DARK);

        Label lblTitle1 = new Label(
                "SMART CAMPUS PARKING SYSTEM",
                Label.CENTER
        );
        lblTitle1.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle1.setForeground(Color.WHITE);

        Label lblTitle2 = new Label(
                "Traffic & Security Administration",
                Label.CENTER
        );
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle2.setForeground(new Color(147, 197, 253));

        Label lblSubtitle = new Label(
                "Campus Parking Management Portal",
                Label.CENTER
        );
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(new Color(203, 213, 225));

        bannerPanel.add(lblTitle1);
        bannerPanel.add(lblTitle2);
        bannerPanel.add(lblSubtitle);

        add(bannerPanel, BorderLayout.NORTH);

        // =========================================================
        // CENTER LOGIN CARD
        // =========================================================
        Panel centerWrapper = new Panel(new GridBagLayout());
        centerWrapper.setBackground(UITheme.COLOR_BG);

        UITheme.CardPanel formCard =
                new UITheme.CardPanel(20, 24, 20, 24);

        formCard.setLayout(new GridBagLayout());
        formCard.setPreferredSize(new Dimension(380, 260));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Authentication Title
        Label lblAuth = new Label("Administrator Sign In");
        lblAuth.setFont(UITheme.FONT_SECTION_TITLE);
        lblAuth.setForeground(UITheme.COLOR_PRIMARY_DARK);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        formCard.add(lblAuth, gbc);

        // =========================================================
        // USERNAME
        // =========================================================
        Label lblUser = new Label("Username:");
        lblUser.setFont(UITheme.FONT_BOLD);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;

        formCard.add(lblUser, gbc);

        txtUsername = UITheme.createTextField(15);

        gbc.gridx = 1;
        formCard.add(txtUsername, gbc);

        // =========================================================
        // PASSWORD
        // =========================================================
        Label lblPass = new Label("Password:");
        lblPass.setFont(UITheme.FONT_BOLD);

        gbc.gridx = 0;
        gbc.gridy = 2;

        formCard.add(lblPass, gbc);

        txtPassword = UITheme.createPasswordField(15);

        gbc.gridx = 1;
        formCard.add(txtPassword, gbc);

        // =========================================================
        // STATUS MESSAGE
        // =========================================================
        lblStatus = new Label(" ");
        lblStatus.setFont(UITheme.FONT_SMALL_BOLD);
        lblStatus.setForeground(UITheme.COLOR_TEXT_MUTED);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;

        formCard.add(lblStatus, gbc);

        // =========================================================
        // LOGIN BUTTON
        // =========================================================
        btnLogin = UITheme.createPrimaryButton("  Sign In  ");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));

        gbc.gridy = 4;

        formCard.add(btnLogin, gbc);

        centerWrapper.add(formCard);

        add(centerWrapper, BorderLayout.CENTER);

        // =========================================================
        // BOTTOM PANEL
        // =========================================================
        Panel bottomPanel =
                new Panel(new FlowLayout(FlowLayout.CENTER, 10, 12));

        bottomPanel.setBackground(UITheme.COLOR_BG);

        btnDemoFill = UITheme.createSecondaryButton(
                "Use Demo Account"
        );

        btnDemoFill.setFont(UITheme.FONT_SMALL);

        bottomPanel.add(btnDemoFill);

        add(bottomPanel, BorderLayout.SOUTH);

        // =========================================================
        // EVENT LISTENERS
        // =========================================================

        btnLogin.addActionListener(e -> handleLogin());

        btnDemoFill.addActionListener(e -> {
            txtUsername.setText("admin");
            txtPassword.setText("admin123");

            lblStatus.setText(
                    "Demo account loaded. Click Sign In to continue."
            );

            lblStatus.setForeground(UITheme.COLOR_PRIMARY);
        });

        // Press Enter to log in
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };

        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        // Close application
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    // =============================================================
    // LOGIN LOGIC
    // =============================================================
    private void handleLogin() {

        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText().trim();

        // Validation
        if (user.isEmpty() || pass.isEmpty()) {

            lblStatus.setText(
                    "Please enter your username and password."
            );

            lblStatus.setForeground(UITheme.COLOR_TEXT_MUTED);

            UITheme.showWarning(
                    this,
                    "Validation",
                    "Please enter both Username and Password."
            );

            return;
        }

        // Demo Authentication
        if ("admin".equalsIgnoreCase(user)
                && "admin123".equals(pass)) {

            lblStatus.setText(
                    "Access granted. Opening the dashboard..."
            );

            lblStatus.setForeground(UITheme.COLOR_SUCCESS);

            EventQueue.invokeLater(() -> {

                DashboardFrame dashboard =
                        new DashboardFrame();

                dashboard.setVisible(true);
            });

            dispose();

        } else {

            lblStatus.setText(
                    "Invalid username or password."
            );

            lblStatus.setForeground(UITheme.COLOR_TEXT_MUTED);

            UITheme.showError(
                    this,
                    "Authentication Failed",
                    "Invalid Username or Password.\n\n"
                            + "Please check your login credentials."
            );
        }
    }
}