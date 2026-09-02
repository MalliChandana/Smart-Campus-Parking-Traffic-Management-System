import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * DashboardFrame
 *
 * Smart Campus Parking & Traffic Management System
 *
 * PURE JAVA AWT IMPLEMENTATION
 *
 * IMPORTANT:
 * NO SWING COMPONENTS ARE USED.
 *
 * Uses:
 * Frame
 * Panel
 * Button
 * Label
 * List
 * MenuBar
 * Menu
 * MenuItem
 * CardLayout
 * BorderLayout
 * GridLayout
 * FlowLayout
 *
 * Main Features:
 * - Light modern dashboard
 * - Pastel navigation buttons
 * - AWT sidebar
 * - AWT menu bar
 * - CardLayout navigation
 * - KPI cards
 * - Parking occupancy chart
 * - Active parking sessions
 * - MySQL status
 * - Live clock
 * - Automatic dashboard refresh
 */
public class DashboardFrame extends Frame {

    // =========================================================
    // MAIN CARD CONTAINER
    // =========================================================

    private Panel cardsPanel;
    private CardLayout cardLayout;

    private Map<String, Button> navButtons =
            new HashMap<>();

    private String currentActiveCard =
            "DASHBOARD";


    // =========================================================
    // FEATURE PANELS
    // =========================================================

    private Panel homeDashboardPanel;

    private UserPanel userPanel;
    private VehiclePanel vehiclePanel;
    private ParkingPanel parkingPanel;
    private ReservationPanel reservationPanel;
    private EntryExitPanel entryExitPanel;
    private PaymentPanel paymentPanel;
    private PassPanel passPanel;
    private ViolationPanel violationPanel;
    private ReportPanel reportPanel;


    // =========================================================
    // TOP BAR
    // =========================================================

    private Label lblPageTitle;
    private Label lblPageSubtitle;
    private Label lblDbStatus;
    private Label lblClock;


    // =========================================================
    // KPI CARDS
    // =========================================================

    private UIComponents.StatCard cardTotalSlots;
    private UIComponents.StatCard cardAvailableSlots;
    private UIComponents.StatCard cardOccupiedSlots;
    private UIComponents.StatCard cardReservedSlots;
    private UIComponents.StatCard cardTotalRevenue;


    // =========================================================
    // OCCUPANCY
    // =========================================================

    private UIComponents.OccupancyDonutChart donutChart;


    // =========================================================
    // ACTIVE SESSIONS
    // =========================================================

    private List listActiveSessions;
    private Label lblActiveCount;


    // =========================================================
    // BACKGROUND TIMER
    // =========================================================

    private Timer backgroundTimer;


    // =========================================================
    // MAIN COLORS
    // =========================================================

    private static final Color BG =
            new Color(245, 248, 252);

    private static final Color WHITE =
            Color.WHITE;

    private static final Color BLUE =
            new Color(37, 99, 235);

    private static final Color BLUE_LIGHT =
            new Color(239, 246, 255);

    private static final Color GREEN =
            new Color(22, 163, 74);

    private static final Color GREEN_LIGHT =
            new Color(240, 253, 244);

    private static final Color ORANGE =
            new Color(234, 88, 12);

    private static final Color ORANGE_LIGHT =
            new Color(255, 247, 237);

    private static final Color RED =
            new Color(220, 38, 38);

    private static final Color RED_LIGHT =
            new Color(254, 242, 242);

    private static final Color PURPLE =
            new Color(124, 58, 237);

    private static final Color PURPLE_LIGHT =
            new Color(245, 243, 255);

    private static final Color TEAL =
            new Color(13, 148, 136);

    private static final Color TEAL_LIGHT =
            new Color(240, 253, 250);

    private static final Color TEXT =
            new Color(30, 41, 59);

    private static final Color MUTED =
            new Color(100, 116, 139);

    private static final Color BORDER =
            new Color(226, 232, 240);


    // =========================================================
    // PASTEL SIDEBAR COLORS
    // =========================================================

    private static final Color NAV_BLUE =
            new Color(239, 246, 255);

    private static final Color NAV_GREEN =
            new Color(240, 253, 244);

    private static final Color NAV_CYAN =
            new Color(240, 249, 255);

    private static final Color NAV_LAVENDER =
            new Color(245, 243, 255);

    private static final Color NAV_PURPLE =
            new Color(250, 245, 255);

    private static final Color NAV_YELLOW =
            new Color(255, 251, 235);

    private static final Color NAV_TEAL =
            new Color(240, 253, 250);

    private static final Color NAV_ROSE =
            new Color(255, 245, 247);

    private static final Color NAV_PEACH =
            new Color(255, 247, 237);


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public DashboardFrame() {

        super(
                "Smart Campus Parking & Traffic Management System"
        );

        initUI();

        refreshHomeDashboardMetrics();

        startAutoRefreshTimer();
    }


    // =========================================================
    // INITIALIZE UI
    // =========================================================

    private void initUI() {

        setSize(
                1250,
                800
        );

        setMinimumSize(
                new Dimension(
                        1050,
                        700
                )
        );

        setLocationRelativeTo(null);

        setBackground(BG);

        setLayout(
                new BorderLayout()
        );

        setupMenuBar();


        // -----------------------------------------------------
        // SIDEBAR
        // -----------------------------------------------------

        Panel sidebar =
                createSidebar();

        add(
                sidebar,
                BorderLayout.WEST
        );


        // -----------------------------------------------------
        // RIGHT SIDE
        // -----------------------------------------------------

        Panel rightContainer =
                new Panel(
                        new BorderLayout()
                );

        rightContainer.setBackground(BG);


        // -----------------------------------------------------
        // TOP BAR
        // -----------------------------------------------------

        Panel topBar =
                createTopBar();

        rightContainer.add(
                topBar,
                BorderLayout.NORTH
        );


        // -----------------------------------------------------
        // CARD LAYOUT
        // -----------------------------------------------------

        cardLayout =
                new CardLayout();

        cardsPanel =
                new Panel(cardLayout);

        cardsPanel.setBackground(BG);


        // -----------------------------------------------------
        // CREATE MODULES
        // -----------------------------------------------------

        homeDashboardPanel =
                createHomeDashboardPanel();

        userPanel =
                new UserPanel();

        vehiclePanel =
                new VehiclePanel();

        parkingPanel =
                new ParkingPanel();

        reservationPanel =
                new ReservationPanel();

        entryExitPanel =
                new EntryExitPanel();

        paymentPanel =
                new PaymentPanel();

        passPanel =
                new PassPanel();

        violationPanel =
                new ViolationPanel();

        reportPanel =
                new ReportPanel();


        // -----------------------------------------------------
        // ADD MODULES
        // -----------------------------------------------------

        cardsPanel.add(
                homeDashboardPanel,
                "DASHBOARD"
        );

        cardsPanel.add(
                userPanel,
                "USERS"
        );

        cardsPanel.add(
                vehiclePanel,
                "VEHICLES"
        );

        cardsPanel.add(
                parkingPanel,
                "PARKING"
        );

        cardsPanel.add(
                reservationPanel,
                "RESERVATIONS"
        );

        cardsPanel.add(
                entryExitPanel,
                "ENTRY_EXIT"
        );

        cardsPanel.add(
                paymentPanel,
                "PAYMENTS"
        );

        cardsPanel.add(
                passPanel,
                "PASSES"
        );

        cardsPanel.add(
                violationPanel,
                "VIOLATIONS"
        );

        cardsPanel.add(
                reportPanel,
                "REPORTS"
        );


        rightContainer.add(
                cardsPanel,
                BorderLayout.CENTER
        );

        add(
                rightContainer,
                BorderLayout.CENTER
        );


        // -----------------------------------------------------
        // WINDOW CLOSE
        // -----------------------------------------------------

        addWindowListener(
                new WindowAdapter() {

                    @Override
                    public void windowClosing(
                            WindowEvent e) {

                        if (
                                backgroundTimer != null
                        ) {
                            backgroundTimer.cancel();
                        }

                        dispose();

                        System.exit(0);
                    }
                }
        );
    }


    // =========================================================
    // TOP BAR
    // =========================================================

    private Panel createTopBar() {

        Panel topBar =
                new Panel(
                        new BorderLayout()
                ) {

                    @Override
                    public Insets getInsets() {

                        return new Insets(
                                14,
                                22,
                                14,
                                22
                        );
                    }

                    @Override
                    public void paint(
                            Graphics g) {

                        super.paint(g);

                        g.setColor(BORDER);

                        g.drawLine(
                                0,
                                getHeight() - 1,
                                getWidth(),
                                getHeight() - 1
                        );
                    }
                };

        topBar.setBackground(WHITE);


        // -----------------------------------------------------
        // TITLE
        // -----------------------------------------------------

        Panel titleBox =
                new Panel(
                        new GridLayout(
                                2,
                                1,
                                0,
                                3
                        )
                );

        titleBox.setBackground(WHITE);


        lblPageTitle =
                new Label(
                        "Dashboard Overview"
                );

        lblPageTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        24
                )
        );

        lblPageTitle.setForeground(TEXT);


        lblPageSubtitle =
                new Label(
                        "Monitor campus parking, vehicles, reservations and payments"
                );

        lblPageSubtitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        lblPageSubtitle.setForeground(MUTED);


        titleBox.add(
                lblPageTitle
        );

        titleBox.add(
                lblPageSubtitle
        );


        topBar.add(
                titleBox,
                BorderLayout.WEST
        );


        // -----------------------------------------------------
        // RIGHT INFORMATION
        // -----------------------------------------------------

        Panel rightControls =
                new Panel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                12,
                                4
                        )
                );

        rightControls.setBackground(WHITE);


        // Clock

        SimpleDateFormat sdf =
                new SimpleDateFormat(
                        "EEE, dd MMM yyyy  HH:mm"
                );

        lblClock =
                new Label(
                        sdf.format(
                                new Date()
                        )
                );

        lblClock.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        lblClock.setForeground(MUTED);

        rightControls.add(
                lblClock
        );


        // Database status

        boolean dbConnected =
                DBConnection.testConnection();

        lblDbStatus =
                new Label(
                        dbConnected
                                ? " ● MySQL Online "
                                : " ● MySQL Offline "
                );

        lblDbStatus.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        lblDbStatus.setForeground(
                dbConnected
                        ? GREEN
                        : RED
        );

        rightControls.add(
                lblDbStatus
        );


        // Operator

        Label operator =
                new Label(
                        "  Operator: Admin  "
                );

        operator.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        operator.setForeground(BLUE);

        rightControls.add(
                operator
        );


        topBar.add(
                rightControls,
                BorderLayout.EAST
        );


        return topBar;
    }


    // =========================================================
    // SIDEBAR
    // =========================================================

    private Panel createSidebar() {

        Panel sidebar =
                new Panel(
                        new BorderLayout()
                );

        sidebar.setBackground(
                WHITE
        );

        sidebar.setPreferredSize(
                new Dimension(
                        235,
                        600
                )
        );


        // -----------------------------------------------------
        // BRAND
        // -----------------------------------------------------

        Panel brand =
                new Panel(
                        new GridLayout(
                                3,
                                1,
                                0,
                                2
                        )
                ) {

                    @Override
                    public Insets getInsets() {

                        return new Insets(
                                22,
                                20,
                                18,
                                20
                        );
                    }
                };

        brand.setBackground(
                WHITE
        );


        Label logo =
                new Label(
                        "□  SMART CAMPUS"
                );

        logo.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        17
                )
        );

        logo.setForeground(BLUE);


        Label title =
                new Label(
                        "PARKING MANAGEMENT"
                );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        title.setForeground(TEXT);


        Label subtitle =
                new Label(
                        "Traffic & Parking Administration"
                );

        subtitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        10
                )
        );

        subtitle.setForeground(MUTED);


        brand.add(logo);
        brand.add(title);
        brand.add(subtitle);


        sidebar.add(
                brand,
                BorderLayout.NORTH
        );


        // -----------------------------------------------------
        // NAVIGATION
        // -----------------------------------------------------

        Panel navList =
                new Panel(
                        new GridLayout(
                                10,
                                1,
                                0,
                                7
                        )
                ) {

                    @Override
                    public Insets getInsets() {

                        return new Insets(
                                10,
                                8,
                                10,
                                8
                        );
                    }
                };

        navList.setBackground(
                WHITE
        );


        // -----------------------------------------------------
        // NAVIGATION BUTTONS
        // -----------------------------------------------------

        addNavButton(
                navList,
                "DASHBOARD",
                "□  Dashboard",
                NAV_BLUE,
                BLUE
        );


        addNavButton(
                navList,
                "USERS",
                "□  Users",
                NAV_GREEN,
                GREEN
        );


        addNavButton(
                navList,
                "VEHICLES",
                "□  Vehicles",
                NAV_CYAN,
                BLUE
        );


        addNavButton(
                navList,
                "PARKING",
                "□  Zones & Slots",
                NAV_LAVENDER,
                PURPLE
        );


        addNavButton(
                navList,
                "RESERVATIONS",
                "□  Reservations",
                NAV_PURPLE,
                PURPLE
        );


        addNavButton(
                navList,
                "ENTRY_EXIT",
                "□  Entry & Exit",
                NAV_YELLOW,
                ORANGE
        );


        addNavButton(
                navList,
                "PAYMENTS",
                "□  Payments",
                NAV_TEAL,
                TEAL
        );


        addNavButton(
                navList,
                "PASSES",
                "□  Parking Passes",
                NAV_ROSE,
                RED
        );


        addNavButton(
                navList,
                "VIOLATIONS",
                "□  Violations",
                NAV_PEACH,
                ORANGE
        );


        addNavButton(
                navList,
                "REPORTS",
                "□  Reports",
                NAV_BLUE,
                BLUE
        );


        sidebar.add(
                navList,
                BorderLayout.CENTER
        );


        // -----------------------------------------------------
        // LOGOUT
        // -----------------------------------------------------

        Panel logoutPanel =
                new Panel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                0,
                                12
                        )
                );

        logoutPanel.setBackground(
                WHITE
        );


        Button logout =
                new Button(
                        "  Sign Out  "
                );

        logout.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        logout.setBackground(
                RED
        );

        logout.setForeground(
                Color.WHITE
        );


        logout.addActionListener(
                e -> handleLogout()
        );


        logoutPanel.add(
                logout
        );


        sidebar.add(
                logoutPanel,
                BorderLayout.SOUTH
        );


        return sidebar;
    }


    // =========================================================
    // NAVIGATION BUTTON
    // =========================================================

    private void addNavButton(
            Panel container,
            String cardId,
            String text,
            Color pastelColor,
            Color accentColor
    ) {

        Button btn =
                new Button(text);


        btn.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );


        /*
         * Every navigation button gets
         * a different very light pastel color.
         */
        btn.setBackground(
                pastelColor
        );


        btn.setForeground(
                TEXT
        );


        btn.addActionListener(
                e -> showCard(cardId)
        );


        /*
         * Pure AWT mouse hover effect.
         */
        btn.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(
                            MouseEvent e) {

                        if (
                                !cardId.equals(
                                        currentActiveCard
                                )
                        ) {

                            btn.setBackground(
                                    accentColor
                            );

                            btn.setForeground(
                                    Color.WHITE
                            );
                        }
                    }


                    @Override
                    public void mouseExited(
                            MouseEvent e) {

                        if (
                                !cardId.equals(
                                        currentActiveCard
                                )
                        ) {

                            btn.setBackground(
                                    pastelColor
                            );

                            btn.setForeground(
                                    TEXT
                            );
                        }
                    }
                }
        );


        navButtons.put(
                cardId,
                btn
        );


        container.add(
                btn
        );
    }


    // =========================================================
    // SHOW CARD
    // =========================================================

    public void showCard(
            String cardId
    ) {

        currentActiveCard =
                cardId;


        cardLayout.show(
                cardsPanel,
                cardId
        );


        updateHeaderForCard(
                cardId
        );


        highlightNavButtons();


        // -----------------------------------------------------
        // REFRESH MODULE DATA
        // -----------------------------------------------------

        if (
                "DASHBOARD".equals(cardId)
        ) {

            refreshHomeDashboardMetrics();

        } else if (
                "USERS".equals(cardId)
        ) {

            userPanel.loadUserData();

        } else if (
                "VEHICLES".equals(cardId)
        ) {

            vehiclePanel.loadUserDropdown();

            vehiclePanel.loadVehicleData();

        } else if (
                "PARKING".equals(cardId)
        ) {

            parkingPanel.loadZonesFilter();

            parkingPanel.loadSlotsData(
                    "ALL",
                    "ALL",
                    ""
            );

        } else if (
                "RESERVATIONS".equals(cardId)
        ) {

            reservationPanel.loadInitialData();

        } else if (
                "ENTRY_EXIT".equals(cardId)
        ) {

            entryExitPanel.loadAvailableSlots();

            entryExitPanel.loadActiveSessions();

        } else if (
                "PAYMENTS".equals(cardId)
        ) {

            paymentPanel.loadPaymentsData();

        } else if (
                "PASSES".equals(cardId)
        ) {

            passPanel.loadDropdowns();

            passPanel.loadPassesData();

        } else if (
                "VIOLATIONS".equals(cardId)
        ) {

            violationPanel.loadVehicles();

            violationPanel.loadViolationsData();

        } else if (
                "REPORTS".equals(cardId)
        ) {

            reportPanel.generateOccupancyReport();
        }
    }


    // =========================================================
    // HIGHLIGHT NAVIGATION
    // =========================================================

    private void highlightNavButtons() {

        for (
                Map.Entry<String, Button> entry
                : navButtons.entrySet()
        ) {

            boolean active =
                    entry.getKey()
                            .equals(
                                    currentActiveCard
                            );


            Button btn =
                    entry.getValue();


            if (active) {

                btn.setBackground(
                        BLUE_LIGHT
                );

                btn.setForeground(
                        BLUE
                );

            } else {

                /*
                 * Restore pastel colors.
                 */
                btn.setForeground(
                        TEXT
                );
            }
        }
    }


    // =========================================================
    // UPDATE HEADER
    // =========================================================

    private void updateHeaderForCard(
            String cardId
    ) {

        switch (cardId) {

            case "DASHBOARD":

                lblPageTitle.setText(
                        "Dashboard Overview"
                );

                lblPageSubtitle.setText(
                        "Monitor campus parking, vehicles, reservations and payments"
                );

                break;


            case "USERS":

                lblPageTitle.setText(
                        "User Management"
                );

                lblPageSubtitle.setText(
                        "Manage students, faculty, staff and visitors"
                );

                break;


            case "VEHICLES":

                lblPageTitle.setText(
                        "Vehicle Registry"
                );

                lblPageSubtitle.setText(
                        "Register and manage campus vehicles"
                );

                break;


            case "PARKING":

                lblPageTitle.setText(
                        "Zones & Parking Slots"
                );

                lblPageSubtitle.setText(
                        "View and manage parking availability"
                );

                break;


            case "RESERVATIONS":

                lblPageTitle.setText(
                        "Parking Reservations"
                );

                lblPageSubtitle.setText(
                        "Reserve parking slots in advance"
                );

                break;


            case "ENTRY_EXIT":

                lblPageTitle.setText(
                        "Gate Entry & Exit"
                );

                lblPageSubtitle.setText(
                        "Record vehicle entry and calculate parking fees"
                );

                break;


            case "PAYMENTS":

                lblPageTitle.setText(
                        "Payments & Receipts"
                );

                lblPageSubtitle.setText(
                        "Manage parking payments and receipts"
                );

                break;


            case "PASSES":

                lblPageTitle.setText(
                        "Parking Passes"
                );

                lblPageSubtitle.setText(
                        "Manage long-term and visitor parking passes"
                );

                break;


            case "VIOLATIONS":

                lblPageTitle.setText(
                        "Parking Violations"
                );

                lblPageSubtitle.setText(
                        "Track violations and penalty payments"
                );

                break;


            case "REPORTS":

                lblPageTitle.setText(
                        "Reports & Analytics"
                );

                lblPageSubtitle.setText(
                        "Analyze parking occupancy and revenue"
                );

                break;
        }
    }


    // =========================================================
    // HOME DASHBOARD
    // =========================================================

    private Panel createHomeDashboardPanel() {

        Panel home =
                new Panel(
                        new BorderLayout(
                                12,
                                12
                        )
                ) {

                    @Override
                    public Insets getInsets() {

                        return new Insets(
                                16,
                                18,
                                18,
                                18
                        );
                    }
                };

        home.setBackground(BG);


        // -----------------------------------------------------
        // KPI ROW
        // -----------------------------------------------------

        Panel statsRow =
                new Panel(
                        new GridLayout(
                                1,
                                5,
                                10,
                                0
                        )
                );

        statsRow.setBackground(BG);

        statsRow.setPreferredSize(
                new Dimension(
                        900,
                        105
                )
        );


        cardTotalSlots =
                new UIComponents.StatCard(
                        "TOTAL SLOTS",
                        "18",
                        BLUE,
                        "3 Campus Zones"
                );


        cardAvailableSlots =
                new UIComponents.StatCard(
                        "AVAILABLE",
                        "13",
                        GREEN,
                        "Ready for Parking"
                );


        cardOccupiedSlots =
                new UIComponents.StatCard(
                        "OCCUPIED",
                        "3",
                        RED,
                        "Vehicles Parked"
                );


        cardReservedSlots =
                new UIComponents.StatCard(
                        "RESERVED",
                        "2",
                        ORANGE,
                        "Advance Bookings"
                );


        cardTotalRevenue =
                new UIComponents.StatCard(
                        "TOTAL REVENUE",
                        "₹160.00",
                        PURPLE,
                        "Paid Transactions"
                );


        statsRow.add(
                cardTotalSlots
        );

        statsRow.add(
                cardAvailableSlots
        );

        statsRow.add(
                cardOccupiedSlots
        );

        statsRow.add(
                cardReservedSlots
        );

        statsRow.add(
                cardTotalRevenue
        );


        home.add(
                statsRow,
                BorderLayout.NORTH
        );


        // -----------------------------------------------------
        // MAIN AREA
        // -----------------------------------------------------

        Panel mainArea =
                new Panel(
                        new BorderLayout(
                                12,
                                12
                        )
                );

        mainArea.setBackground(BG);


        // -----------------------------------------------------
        // QUICK ACTIONS
        // -----------------------------------------------------

        UITheme.CardPanel quickCard =
                new UITheme.CardPanel(
                        14,
                        16,
                        14,
                        16
                );

        quickCard.setBackground(
                WHITE
        );

        quickCard.setLayout(
                new BorderLayout(
                        8,
                        12
                )
        );

        quickCard.setPreferredSize(
                new Dimension(
                        245,
                        500
                )
        );


        Label quickTitle =
                new Label(
                        "Quick Actions"
                );

        quickTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        17
                )
        );

        quickTitle.setForeground(TEXT);


        quickCard.add(
                quickTitle,
                BorderLayout.NORTH
        );


        Panel quickButtons =
                new Panel(
                        new GridLayout(
                                6,
                                1,
                                0,
                                9
                        )
                );

        quickButtons.setBackground(
                WHITE
        );


        addQuickButton(
                quickButtons,
                "Register Vehicle",
                BLUE,
                e -> showCard("VEHICLES")
        );


        addQuickButton(
                quickButtons,
                "Make Reservation",
                PURPLE,
                e -> showCard("RESERVATIONS")
        );


        addQuickButton(
                quickButtons,
                "Vehicle Entry",
                GREEN,
                e -> showCard("ENTRY_EXIT")
        );


        addQuickButton(
                quickButtons,
                "Vehicle Exit",
                RED,
                e -> showCard("ENTRY_EXIT")
        );


        addQuickButton(
                quickButtons,
                "Process Payment",
                ORANGE,
                e -> showCard("PAYMENTS")
        );


        addQuickButton(
                quickButtons,
                "View Reports",
                TEAL,
                e -> showCard("REPORTS")
        );


        quickCard.add(
                quickButtons,
                BorderLayout.CENTER
        );


        mainArea.add(
                quickCard,
                BorderLayout.WEST
        );


        // -----------------------------------------------------
        // RIGHT AREA
        // -----------------------------------------------------

        Panel rightArea =
                new Panel(
                        new BorderLayout(
                                12,
                                12
                        )
                );

        rightArea.setBackground(BG);


        // -----------------------------------------------------
        // OCCUPANCY CARD
        // -----------------------------------------------------

        UITheme.CardPanel chartCard =
                new UITheme.CardPanel(
                        12,
                        16,
                        12,
                        16
                );

        chartCard.setBackground(
                WHITE
        );

        chartCard.setLayout(
                new BorderLayout(
                        8,
                        4
                )
        );

        chartCard.setPreferredSize(
                new Dimension(
                        600,
                        190
                )
        );


        Label chartTitle =
                new Label(
                        "Parking Capacity Overview"
                );

        chartTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        17
                )
        );

        chartTitle.setForeground(TEXT);


        chartCard.add(
                chartTitle,
                BorderLayout.NORTH
        );


        donutChart =
                new UIComponents.OccupancyDonutChart();


        chartCard.add(
                donutChart,
                BorderLayout.CENTER
        );


        rightArea.add(
                chartCard,
                BorderLayout.NORTH
        );


        // -----------------------------------------------------
        // ACTIVE SESSIONS
        // -----------------------------------------------------

        UITheme.CardPanel sessionsCard =
                new UITheme.CardPanel(
                        12,
                        16,
                        12,
                        16
                );

        sessionsCard.setBackground(
                WHITE
        );

        sessionsCard.setLayout(
                new BorderLayout(
                        8,
                        8
                )
        );


        Panel sessionHeader =
                new Panel(
                        new BorderLayout()
                );

        sessionHeader.setBackground(
                WHITE
        );


        Label sessionTitle =
                new Label(
                        "Live Active Parking Sessions"
                );

        sessionTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        17
                )
        );

        sessionTitle.setForeground(TEXT);


        sessionHeader.add(
                sessionTitle,
                BorderLayout.WEST
        );


        Panel sessionRight =
                new Panel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                8,
                                0
                        )
                );

        sessionRight.setBackground(
                WHITE
        );


        lblActiveCount =
                new Label(
                        "0 active"
                );

        lblActiveCount.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        lblActiveCount.setForeground(
                GREEN
        );


        sessionRight.add(
                lblActiveCount
        );


        Button refresh =
                new Button(
                        "Refresh"
                );

        refresh.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        refresh.setBackground(
                BLUE
        );

        refresh.setForeground(
                Color.WHITE
        );


        refresh.addActionListener(
                e -> {

                    refreshHomeDashboardMetrics();

                    UITheme.showSuccess(
                            this,
                            "Dashboard data refreshed."
                    );
                }
        );


        sessionRight.add(
                refresh
        );


        sessionHeader.add(
                sessionRight,
                BorderLayout.EAST
        );


        sessionsCard.add(
                sessionHeader,
                BorderLayout.NORTH
        );


        // -----------------------------------------------------
        // SESSION LIST HEADER
        // -----------------------------------------------------

        Label listHeader =
                new Label(
                        " SESSION ID   |   VEHICLE PLATE   |   TYPE   |   OWNER   |   SLOT   |   ZONE   |   ENTRY TIME"
                );

        listHeader.setFont(
                new Font(
                        "Monospaced",
                        Font.BOLD,
                        11
                )
        );

        listHeader.setForeground(
                BLUE
        );

        listHeader.setBackground(
                BLUE_LIGHT
        );


        listActiveSessions =
                new List(
                        8,
                        false
                );

        listActiveSessions.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        11
                )
        );

        listActiveSessions.setBackground(
                WHITE
        );


        Panel sessionList =
                new Panel(
                        new BorderLayout(
                                2,
                                2
                        )
                );

        sessionList.setBackground(
                WHITE
        );


        sessionList.add(
                listHeader,
                BorderLayout.NORTH
        );

        sessionList.add(
                listActiveSessions,
                BorderLayout.CENTER
        );


        sessionsCard.add(
                sessionList,
                BorderLayout.CENTER
        );


        rightArea.add(
                sessionsCard,
                BorderLayout.CENTER
        );


        mainArea.add(
                rightArea,
                BorderLayout.CENTER
        );


        home.add(
                mainArea,
                BorderLayout.CENTER
        );


        return home;
    }


    // =========================================================
    // QUICK ACTION BUTTON
    // =========================================================

    private void addQuickButton(
            Panel container,
            String text,
            Color color,
            ActionListener listener
    ) {

        Button button =
                new Button(
                        "  " + text + "  "
                );


        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );


        button.setBackground(
                color
        );

        button.setForeground(
                Color.WHITE
        );


        button.addActionListener(
                listener
        );


        container.add(
                button
        );
    }


    // =========================================================
    // REFRESH DASHBOARD
    // =========================================================

    public void refreshHomeDashboardMetrics() {

        try (
                Connection conn =
                        DBConnection.getConnection();

                Statement stmt =
                        conn.createStatement()
        ) {


            // -------------------------------------------------
            // SLOT COUNTS
            // -------------------------------------------------

            int totalSlots = 0;

            int availableSlots = 0;

            int occupiedSlots = 0;

            int reservedSlots = 0;


            try (
                    ResultSet rs =
                            stmt.executeQuery(
                                    "SELECT status, COUNT(*) AS cnt " +
                                            "FROM parking_slots " +
                                            "GROUP BY status"
                            )
            ) {

                while (rs.next()) {

                    String status =
                            rs.getString(
                                    "status"
                            );

                    int count =
                            rs.getInt(
                                    "cnt"
                            );


                    totalSlots += count;


                    if (
                            "AVAILABLE"
                                    .equalsIgnoreCase(
                                            status
                                    )
                    ) {

                        availableSlots =
                                count;

                    } else if (
                            "OCCUPIED"
                                    .equalsIgnoreCase(
                                            status
                                    )
                    ) {

                        occupiedSlots =
                                count;

                    } else if (
                            "RESERVED"
                                    .equalsIgnoreCase(
                                            status
                                    )
                    ) {

                        reservedSlots =
                                count;
                    }
                }
            }


            // -------------------------------------------------
            // UPDATE KPI CARDS
            // -------------------------------------------------

            if (
                    cardTotalSlots != null
            ) {

                cardTotalSlots.setValue(
                        String.valueOf(
                                totalSlots
                        )
                );
            }


            if (
                    cardAvailableSlots != null
            ) {

                cardAvailableSlots.setValue(
                        String.valueOf(
                                availableSlots
                        )
                );
            }


            if (
                    cardOccupiedSlots != null
            ) {

                cardOccupiedSlots.setValue(
                        String.valueOf(
                                occupiedSlots
                        )
                );
            }


            if (
                    cardReservedSlots != null
            ) {

                cardReservedSlots.setValue(
                        String.valueOf(
                                reservedSlots
                        )
                );
            }


            // -------------------------------------------------
            // UPDATE CHART
            // -------------------------------------------------

            if (
                    donutChart != null
            ) {

                donutChart.updateCounts(
                        availableSlots,
                        occupiedSlots,
                        reservedSlots
                );
            }


            // -------------------------------------------------
            // REVENUE
            // -------------------------------------------------

            double totalRevenue =
                    0.0;


            try (
                    ResultSet rs =
                            stmt.executeQuery(
                                    "SELECT IFNULL(SUM(amount),0.00) " +
                                            "FROM payments " +
                                            "WHERE status='PAID'"
                            )
            ) {

                if (rs.next()) {

                    totalRevenue =
                            rs.getDouble(1);
                }
            }


            if (
                    cardTotalRevenue != null
            ) {

                cardTotalRevenue.setValue(
                        "₹" +
                                String.format(
                                        "%.2f",
                                        totalRevenue
                                )
                );
            }


            // -------------------------------------------------
            // ACTIVE SESSIONS
            // -------------------------------------------------

            if (
                    listActiveSessions != null
            ) {

                listActiveSessions.removeAll();
            }


            String sql =
                    "SELECT ps.session_id, " +
                            "v.vehicle_number, " +
                            "v.vehicle_type, " +
                            "u.name, " +
                            "s.slot_number, " +
                            "z.zone_name, " +
                            "ps.entry_time " +
                            "FROM parking_sessions ps " +
                            "JOIN vehicles v " +
                            "ON ps.vehicle_id=v.vehicle_id " +
                            "JOIN users u " +
                            "ON v.user_id=u.user_id " +
                            "JOIN parking_slots s " +
                            "ON ps.slot_id=s.slot_id " +
                            "JOIN parking_zones z " +
                            "ON s.zone_id=z.zone_id " +
                            "WHERE ps.exit_time IS NULL " +
                            "ORDER BY ps.entry_time DESC";


            int activeCount = 0;


            try (
                    ResultSet rs =
                            stmt.executeQuery(sql)
            ) {

                while (rs.next()) {

                    activeCount++;


                    Timestamp timestamp =
                            rs.getTimestamp(
                                    "entry_time"
                            );


                    String entryTime =
                            timestamp != null
                                    ? timestamp.toString()
                                    : "-";


                    if (
                            entryTime.length() >= 19
                    ) {

                        entryTime =
                                entryTime.substring(
                                        11,
                                        19
                                );
                    }


                    String row =
                            String.format(
                                    " %-10d | %-16s | %-6s | %-12s | %-6s | %-6s | %s",
                                    rs.getInt(
                                            "session_id"
                                    ),
                                    rs.getString(
                                            "vehicle_number"
                                    ),
                                    rs.getString(
                                            "vehicle_type"
                                    ),
                                    rs.getString(
                                            "name"
                                    ),
                                    rs.getString(
                                            "slot_number"
                                    ),
                                    rs.getString(
                                            "zone_name"
                                    ),
                                    entryTime
                            );


                    if (
                            listActiveSessions != null
                    ) {

                        listActiveSessions.add(
                                row
                        );
                    }
                }
            }


            if (
                    lblActiveCount != null
            ) {

                lblActiveCount.setText(
                        activeCount +
                                " active"
                );
            }


        } catch (
                SQLException ex
        ) {

            System.err.println(
                    "Dashboard refresh error: "
                            + ex.getMessage()
            );
        }
    }


    // =========================================================
    // AUTO REFRESH
    // =========================================================

    private void startAutoRefreshTimer() {

        backgroundTimer =
                new Timer(true);


        backgroundTimer.scheduleAtFixedRate(
                new TimerTask() {

                    @Override
                    public void run() {

                        SimpleDateFormat sdf =
                                new SimpleDateFormat(
                                        "EEE, dd MMM yyyy  HH:mm"
                                );


                        if (
                                lblClock != null
                        ) {

                            lblClock.setText(
                                    sdf.format(
                                            new Date()
                                    )
                            );
                        }


                        if (
                                "DASHBOARD"
                                        .equals(
                                                currentActiveCard
                                        )
                        ) {

                            refreshHomeDashboardMetrics();
                        }
                    }

                },
                30000,
                30000
        );
    }


    // =========================================================
    // MENU BAR
    // =========================================================

    private void setupMenuBar() {

        MenuBar menuBar =
                new MenuBar();


        // -----------------------------------------------------
        // FILE
        // -----------------------------------------------------

        Menu file =
                new Menu(
                        "File"
                );


        MenuItem dashboard =
                new MenuItem(
                        "Dashboard"
                );


        dashboard.addActionListener(
                e -> showCard(
                        "DASHBOARD"
                )
        );


        file.add(
                dashboard
        );


        MenuItem refresh =
                new MenuItem(
                        "Refresh Dashboard"
                );


        refresh.addActionListener(
                e -> refreshHomeDashboardMetrics()
        );


        file.add(
                refresh
        );


        file.addSeparator();


        MenuItem signOut =
                new MenuItem(
                        "Sign Out"
                );


        signOut.addActionListener(
                e -> handleLogout()
        );


        file.add(
                signOut
        );


        MenuItem exit =
                new MenuItem(
                        "Exit"
                );


        exit.addActionListener(
                e -> {

                    if (
                            backgroundTimer != null
                    ) {

                        backgroundTimer.cancel();
                    }


                    dispose();

                    System.exit(0);
                }
        );


        file.add(
                exit
        );


        menuBar.add(
                file
        );


        // -----------------------------------------------------
        // MODULES
        // -----------------------------------------------------

        Menu modules =
                new Menu(
                        "Modules"
                );


        addMenuItem(
                modules,
                "Users",
                "USERS"
        );


        addMenuItem(
                modules,
                "Vehicles",
                "VEHICLES"
        );


        addMenuItem(
                modules,
                "Zones & Slots",
                "PARKING"
        );


        addMenuItem(
                modules,
                "Reservations",
                "RESERVATIONS"
        );


        addMenuItem(
                modules,
                "Entry & Exit",
                "ENTRY_EXIT"
        );


        addMenuItem(
                modules,
                "Payments",
                "PAYMENTS"
        );


        addMenuItem(
                modules,
                "Parking Passes",
                "PASSES"
        );


        addMenuItem(
                modules,
                "Violations",
                "VIOLATIONS"
        );


        addMenuItem(
                modules,
                "Reports",
                "REPORTS"
        );


        menuBar.add(
                modules
        );


        // -----------------------------------------------------
        // HELP
        // -----------------------------------------------------

        Menu help =
                new Menu(
                        "Help"
                );


        MenuItem about =
                new MenuItem(
                        "About System"
                );


        about.addActionListener(
                e -> UITheme.showSuccess(
                        this,
                        "About System",
                        "Smart Campus Parking & Traffic Management System\n"
                                + "Pure Java AWT Desktop Application."
                )
        );


        help.add(
                about
        );


        menuBar.add(
                help
        );


        setMenuBar(
                menuBar
        );
    }


    // =========================================================
    // MENU ITEM
    // =========================================================

    private void addMenuItem(
            Menu menu,
            String label,
            String cardId
    ) {

        MenuItem item =
                new MenuItem(
                        label
                );


        item.addActionListener(
                e -> showCard(
                        cardId
                )
        );


        menu.add(
                item
        );
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    private void handleLogout() {

        boolean confirmed =
                UITheme.showConfirm(
                        this,
                        "Confirm Sign Out",
                        "Are you sure you want to sign out?"
                );


        if (confirmed) {

            if (
                    backgroundTimer != null
            ) {

                backgroundTimer.cancel();
            }


            dispose();


            EventQueue.invokeLater(
                    () -> {

                        LoginFrame login =
                                new LoginFrame();

                        login.setVisible(
                                true
                        );
                    }
            );
        }
    }
}