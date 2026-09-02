import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * ParkingPanel
 *
 * PURE JAVA AWT implementation.
 *
 * Smart Campus Parking - Location & Slot Map
 *
 * College Parking Locations:
 * 1. Hospital
 * 2. Rectangle
 * 3. AHS
 * 4. SCAD
 * 5. Temple
 *
 * NO SWING / NO JFrame / NO JPanel.
 */
public class ParkingPanel extends Panel {

    // =========================================================
    // COLORS
    // =========================================================

    private static final Color BG =
            new Color(244, 247, 251);

    private static final Color WHITE =
            Color.WHITE;

    private static final Color NAVY =
            new Color(15, 23, 42);

    private static final Color DARK_BLUE =
            new Color(30, 58, 138);

    private static final Color BLUE =
            new Color(37, 99, 235);

    private static final Color TEXT =
            new Color(30, 41, 59);

    private static final Color MUTED =
            new Color(100, 116, 139);

    private static final Color BORDER =
            new Color(220, 226, 235);

    private static final Color AVAILABLE =
            new Color(22, 163, 74);

    private static final Color AVAILABLE_LIGHT =
            new Color(220, 252, 231);

    private static final Color OCCUPIED =
            new Color(220, 38, 38);

    private static final Color OCCUPIED_LIGHT =
            new Color(254, 226, 226);

    private static final Color RESERVED =
            new Color(234, 138, 0);

    private static final Color RESERVED_LIGHT =
            new Color(255, 237, 200);

    // =========================================================
    // FONTS
    // =========================================================

    private static final Font FONT_TITLE =
            new Font("Segoe UI", Font.BOLD, 25);

    private static final Font FONT_SUBTITLE =
            new Font("Segoe UI", Font.PLAIN, 14);

    private static final Font FONT_SECTION =
            new Font("Segoe UI", Font.BOLD, 18);

    private static final Font FONT_CARD_TITLE =
            new Font("Segoe UI", Font.BOLD, 12);

    private static final Font FONT_CARD_VALUE =
            new Font("Segoe UI", Font.BOLD, 25);

    private static final Font FONT_NORMAL =
            new Font("Segoe UI", Font.PLAIN, 13);

    private static final Font FONT_BOLD =
            new Font("Segoe UI", Font.BOLD, 13);

    private static final Font FONT_SMALL =
            new Font("Segoe UI", Font.PLAIN, 11);

    // =========================================================
    // FILTER CONTROLS
    // =========================================================

    private Choice cmbZoneFilter;
    private Choice cmbStatusFilter;
    private TextField txtSearchSlot;

    private Button btnSearch;
    private Button btnReset;
    private Button btnToggleView;

    // =========================================================
    // SUMMARY LABELS
    // =========================================================

    private Label lblTotal;
    private Label lblAvailable;
    private Label lblOccupied;
    private Label lblReserved;

    private Label lblShowing;

    // =========================================================
    // CONTAINERS
    // =========================================================

    private Panel mainContent;
    private Panel gridContainer;
    private Panel listContainer;

    private ScrollPane scrollPane;

    private List listSlots;

    private boolean isGridView = true;

    // =========================================================
    // DATA
    // =========================================================

    public static class SlotData {

        int id;
        String slotNumber;
        int zoneId;
        String zoneName;
        String location;
        String status;

        SlotData(
                int id,
                String slotNumber,
                int zoneId,
                String zoneName,
                String location,
                String status) {

            this.id = id;
            this.slotNumber = slotNumber;
            this.zoneId = zoneId;
            this.zoneName = zoneName;
            this.location = location;
            this.status = status;
        }
    }

    private java.util.List<SlotData> currentSlots =
            new ArrayList<>();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ParkingPanel() {

        setLayout(new BorderLayout());
        setBackground(BG);

        buildInterface();

        loadZonesFilter();

        loadSlotsData(
                "ALL",
                "ALL",
                "");
    }

    // =========================================================
    // MAIN INTERFACE
    // =========================================================

    private void buildInterface() {

        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------

        Panel header =
                new Panel(new BorderLayout());

        header.setBackground(WHITE);

        header.setPreferredSize(
                new Dimension(1000, 105));

        Panel titleArea =
                new Panel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                25,
                                18));

        titleArea.setBackground(WHITE);

        Label icon =
                new Label("P");

        icon.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        27));

        icon.setForeground(BLUE);

        Panel titleText =
                new Panel(
                        new GridLayout(
                                2,
                                1,
                                0,
                                2));

        titleText.setBackground(WHITE);

        Label title =
                new Label(
                        "Parking Locations & Slot Map");

        title.setFont(FONT_TITLE);
        title.setForeground(NAVY);

        Label subtitle =
                new Label(
                        "Manage campus parking availability across Hospital, Rectangle, AHS, SCAD and Temple");

        subtitle.setFont(FONT_SUBTITLE);
        subtitle.setForeground(MUTED);

        titleText.add(title);
        titleText.add(subtitle);

        titleArea.add(icon);
        titleArea.add(titleText);

        header.add(
                titleArea,
                BorderLayout.WEST);

        // -----------------------------------------------------
        // RIGHT INFORMATION
        // -----------------------------------------------------

        Panel rightInfo =
                new Panel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                20,
                                30));

        rightInfo.setBackground(WHITE);

        Label date =
                new Label(
                        "● 01 Sep 2026");

        date.setFont(FONT_BOLD);
        date.setForeground(MUTED);

        Label database =
                new Label(
                        "● MySQL Online");

        database.setFont(FONT_BOLD);
        database.setForeground(AVAILABLE);

        Label operator =
                new Label(
                        "Operator: Admin");

        operator.setFont(FONT_BOLD);
        operator.setForeground(TEXT);

        rightInfo.add(date);
        rightInfo.add(database);
        rightInfo.add(operator);

        header.add(
                rightInfo,
                BorderLayout.EAST);

        add(
                header,
                BorderLayout.NORTH);

        // -----------------------------------------------------
        // MAIN AREA
        // -----------------------------------------------------

        mainContent =
                new Panel(
                        new BorderLayout(
                                0,
                                12));

        mainContent.setBackground(BG);

        mainContent.add(
                createSummaryPanel(),
                BorderLayout.NORTH);

        mainContent.add(
                createFilterPanel(),
                BorderLayout.CENTER);

        add(
                mainContent,
                BorderLayout.CENTER);
    }

    // =========================================================
    // SUMMARY PANEL
    // =========================================================

    private Panel createSummaryPanel() {

        Panel summary =
                new Panel(
                        new GridLayout(
                                1,
                                4,
                                12,
                                0));

        summary.setBackground(BG);

        lblTotal =
                new Label("0");

        lblAvailable =
                new Label("0");

        lblOccupied =
                new Label("0");

        lblReserved =
                new Label("0");

        summary.add(
                createStatCard(
                        "TOTAL SLOTS",
                        lblTotal,
                        BLUE,
                        "Campus Capacity"));

        summary.add(
                createStatCard(
                        "AVAILABLE",
                        lblAvailable,
                        AVAILABLE,
                        "Ready for Parking"));

        summary.add(
                createStatCard(
                        "OCCUPIED",
                        lblOccupied,
                        OCCUPIED,
                        "Currently Parked"));

        summary.add(
                createStatCard(
                        "RESERVED",
                        lblReserved,
                        RESERVED,
                        "Advance Bookings"));

        return summary;
    }

    // =========================================================
    // STAT CARD
    // =========================================================

    private Panel createStatCard(
            String title,
            Label value,
            Color accent,
            String description) {

        Panel card =
                new Panel(
                        new BorderLayout());

        card.setBackground(WHITE);

        Panel leftBar =
                new Panel();

        leftBar.setBackground(accent);

        leftBar.setPreferredSize(
                new Dimension(
                        5,
                        80));

        card.add(
                leftBar,
                BorderLayout.WEST);

        Panel content =
                new Panel(
                        new GridLayout(
                                3,
                                1,
                                0,
                                2));

        content.setBackground(WHITE);

        Label lblTitle =
                new Label(title);

        lblTitle.setFont(
                FONT_CARD_TITLE);

        lblTitle.setForeground(MUTED);

        value.setFont(
                FONT_CARD_VALUE);

        value.setForeground(accent);

        Label lblDescription =
                new Label(description);

        lblDescription.setFont(FONT_SMALL);
        lblDescription.setForeground(MUTED);

        content.add(lblTitle);
        content.add(value);
        content.add(lblDescription);

        Panel padding =
                new Panel(
                        new BorderLayout());

        padding.setBackground(WHITE);

        padding.add(
                content,
                BorderLayout.CENTER);

        card.add(
                padding,
                BorderLayout.CENTER);

        return card;
    }

    // =========================================================
    // FILTER PANEL
    // =========================================================

    private Panel createFilterPanel() {

        Panel wrapper =
                new Panel(
                        new BorderLayout(
                                0,
                                10));

        wrapper.setBackground(BG);

        // -----------------------------------------------------
        // FILTER CARD
        // -----------------------------------------------------

        Panel filterCard =
                new Panel(
                        new BorderLayout());

        filterCard.setBackground(WHITE);

        // -----------------------------------------------------
        // HEADING
        // -----------------------------------------------------

        Panel heading =
                new Panel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                15,
                                12));

        heading.setBackground(WHITE);

        Label filterTitle =
                new Label(
                        "CAMPUS PARKING LOCATION MANAGEMENT");

        filterTitle.setFont(FONT_SECTION);
        filterTitle.setForeground(NAVY);

        heading.add(filterTitle);

        lblShowing =
                new Label(
                        "Showing all parking slots");

        lblShowing.setFont(FONT_NORMAL);
        lblShowing.setForeground(MUTED);

        heading.add(lblShowing);

        filterCard.add(
                heading,
                BorderLayout.NORTH);

        // -----------------------------------------------------
        // CONTROLS
        // -----------------------------------------------------

        Panel controls =
                new Panel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                8));

        controls.setBackground(WHITE);

        Label zoneLabel =
                new Label(
                        "Location");

        zoneLabel.setFont(FONT_BOLD);
        zoneLabel.setForeground(TEXT);

        cmbZoneFilter =
                new Choice();

        cmbZoneFilter.setFont(FONT_NORMAL);

        Label statusLabel =
                new Label(
                        "Status");

        statusLabel.setFont(FONT_BOLD);
        statusLabel.setForeground(TEXT);

        cmbStatusFilter =
                new Choice();

        cmbStatusFilter.add("ALL");
        cmbStatusFilter.add("AVAILABLE");
        cmbStatusFilter.add("OCCUPIED");
        cmbStatusFilter.add("RESERVED");

        cmbStatusFilter.setFont(FONT_NORMAL);

        Label searchLabel =
                new Label(
                        "Slot Number");

        searchLabel.setFont(FONT_BOLD);
        searchLabel.setForeground(TEXT);

        txtSearchSlot =
                new TextField(10);

        txtSearchSlot.setFont(FONT_NORMAL);

        btnSearch =
                createButton(
                        "Apply Filter",
                        BLUE);

        btnReset =
                createButton(
                        "Reset",
                        new Color(
                                71,
                                85,
                                105));

        btnToggleView =
                createButton(
                        "☷  List View",
                        DARK_BLUE);

        controls.add(zoneLabel);
        controls.add(cmbZoneFilter);

        controls.add(statusLabel);
        controls.add(cmbStatusFilter);

        controls.add(searchLabel);
        controls.add(txtSearchSlot);

        controls.add(btnSearch);
        controls.add(btnReset);
        controls.add(btnToggleView);

        filterCard.add(
                controls,
                BorderLayout.CENTER);

        wrapper.add(
                filterCard,
                BorderLayout.NORTH);

        // -----------------------------------------------------
        // VISUAL GRID
        // -----------------------------------------------------

        scrollPane =
                new ScrollPane(
                        ScrollPane.SCROLLBARS_AS_NEEDED);

        scrollPane.setBackground(BG);

        gridContainer =
                new Panel(
                        new GridLayout(
                                0,
                                1,
                                0,
                                14));

        gridContainer.setBackground(BG);

        scrollPane.add(gridContainer);

        wrapper.add(
                scrollPane,
                BorderLayout.CENTER);

        // -----------------------------------------------------
        // LIST VIEW
        // -----------------------------------------------------

        listContainer =
                new Panel(
                        new BorderLayout());

        listContainer.setBackground(WHITE);

        Label listHeader =
                new Label(
                        " SLOT ID       LOCATION              AREA                    SLOT          STATUS");

        listHeader.setFont(
                new Font(
                        "Monospaced",
                        Font.BOLD,
                        13));

        listHeader.setForeground(NAVY);

        listHeader.setBackground(
                new Color(
                        226,
                        232,
                        240));

        listSlots =
                new List(
                        18,
                        false);

        listSlots.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        12));

        listContainer.add(
                listHeader,
                BorderLayout.NORTH);

        listContainer.add(
                listSlots,
                BorderLayout.CENTER);

        // -----------------------------------------------------
        // EVENTS
        // -----------------------------------------------------

        btnSearch.addActionListener(
                e -> applyFilter());

        btnReset.addActionListener(
                e -> {

                    cmbZoneFilter.select(0);
                    cmbStatusFilter.select(0);
                    txtSearchSlot.setText("");

                    loadSlotsData(
                            "ALL",
                            "ALL",
                            "");
                });

        btnToggleView.addActionListener(
                e -> toggleView());

        txtSearchSlot.addActionListener(
                e -> applyFilter());

        return wrapper;
    }

    // =========================================================
    // BUTTON CREATION
    // =========================================================

    private Button createButton(
            String text,
            Color background) {

        Button button =
                new Button(text);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12));

        button.setBackground(background);
        button.setForeground(Color.WHITE);

        return button;
    }

    // =========================================================
    // LOAD LOCATIONS
    // =========================================================

    public void loadZonesFilter() {

        cmbZoneFilter.removeAll();

        cmbZoneFilter.add(
                "ALL LOCATIONS");

        String sql =
                "SELECT zone_name " +
                        "FROM parking_zones " +
                        "ORDER BY zone_name ASC";

        try (
                Connection conn =
                        DBConnection.getConnection();

                Statement stmt =
                        conn.createStatement();

                ResultSet rs =
                        stmt.executeQuery(sql)
        ) {

            while (rs.next()) {

                String location =
                        rs.getString(
                                "zone_name");

                if (location != null
                        && !location.trim().isEmpty()) {

                    cmbZoneFilter.add(
                            location);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error loading parking locations: "
                            + e.getMessage());
        }
    }

    // =========================================================
    // APPLY FILTER
    // =========================================================

    private void applyFilter() {

        String zone =
                cmbZoneFilter.getSelectedItem();

        String status =
                cmbStatusFilter.getSelectedItem();

        String search =
                txtSearchSlot
                        .getText()
                        .trim();

        loadSlotsData(
                zone,
                status,
                search);
    }

    // =========================================================
    // LOAD SLOT DATA
    // =========================================================

    public void loadSlotsData(
            String zoneFilter,
            String statusFilter,
            String searchSlot) {

        StringBuilder sql =
                new StringBuilder();

        sql.append(
                "SELECT s.slot_id, " +
                        "s.slot_number, " +
                        "s.status, " +
                        "z.zone_id, " +
                        "z.zone_name, " +
                        "z.location " +
                        "FROM parking_slots s " +
                        "JOIN parking_zones z " +
                        "ON s.zone_id = z.zone_id " +
                        "WHERE 1=1 ");

        java.util.List<Object> params =
                new ArrayList<>();

        // -----------------------------------------------------
        // LOCATION FILTER
        // -----------------------------------------------------

        if (
                zoneFilter != null
                        && !zoneFilter.equals(
                        "ALL LOCATIONS")
                        && !zoneFilter.equals(
                        "ALL")
        ) {

            sql.append(
                    "AND z.zone_name = ? ");

            params.add(zoneFilter);
        }

        // -----------------------------------------------------
        // STATUS FILTER
        // -----------------------------------------------------

        if (
                statusFilter != null
                        && !statusFilter.equals(
                        "ALL")
        ) {

            sql.append(
                    "AND s.status = ? ");

            params.add(statusFilter);
        }

        // -----------------------------------------------------
        // SLOT SEARCH
        // -----------------------------------------------------

        if (
                searchSlot != null
                        && !searchSlot.isEmpty()
        ) {

            sql.append(
                    "AND s.slot_number LIKE ? ");

            params.add(
                    "%" + searchSlot + "%");
        }

        sql.append(
                "ORDER BY z.zone_name ASC, "
                        + "s.slot_number ASC");

        currentSlots.clear();

        listSlots.removeAll();

        int total = 0;
        int available = 0;
        int occupied = 0;
        int reserved = 0;

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement pstmt =
                        conn.prepareStatement(
                                sql.toString())
        ) {

            for (
                    int i = 0;
                    i < params.size();
                    i++
            ) {

                pstmt.setObject(
                        i + 1,
                        params.get(i));
            }

            try (
                    ResultSet rs =
                            pstmt.executeQuery()
            ) {

                while (rs.next()) {

                    total++;

                    SlotData slot =
                            new SlotData(
                                    rs.getInt(
                                            "slot_id"),

                                    rs.getString(
                                            "slot_number"),

                                    rs.getInt(
                                            "zone_id"),

                                    rs.getString(
                                            "zone_name"),

                                    rs.getString(
                                            "location"),

                                    rs.getString(
                                            "status"));

                    currentSlots.add(slot);

                    if (
                            "AVAILABLE"
                                    .equalsIgnoreCase(
                                            slot.status)
                    ) {

                        available++;

                    } else if (
                            "OCCUPIED"
                                    .equalsIgnoreCase(
                                            slot.status)
                    ) {

                        occupied++;

                    } else if (
                            "RESERVED"
                                    .equalsIgnoreCase(
                                            slot.status)
                    ) {

                        reserved++;
                    }

                    String line =
                            String.format(
                                    " %-8d | %-20s | %-22s | %-8s | %s",
                                    slot.id,
                                    slot.zoneName,
                                    slot.location,
                                    slot.slotNumber,
                                    slot.status);

                    listSlots.add(line);
                }
            }

            lblTotal.setText(
                    String.valueOf(total));

            lblAvailable.setText(
                    String.valueOf(available));

            lblOccupied.setText(
                    String.valueOf(occupied));

            lblReserved.setText(
                    String.valueOf(reserved));

            lblShowing.setText(
                    "Showing "
                            + total
                            + " parking slot"
                            + (total == 1
                            ? ""
                            : "s"));

            renderVisualSlotGrid();

        } catch (SQLException e) {

            showAWTError(
                    "Database Error",
                    "Unable to load parking slots.\n\n"
                            + e.getMessage());
        }
    }

    // =========================================================
    // RENDER LOCATION GRID
    // =========================================================

    private void renderVisualSlotGrid() {

        gridContainer.removeAll();

        if (currentSlots.isEmpty()) {

            Panel empty =
                    createEmptyPanel();

            gridContainer.add(empty);

            gridContainer.validate();
            gridContainer.repaint();

            return;
        }

        /*
         * IMPORTANT:
         *
         * We no longer use:
         *
         * Zone A
         * Zone B
         * Zone C
         *
         * Instead, the application automatically
         * groups slots according to the actual
         * location names stored in the database.
         */

        java.util.List<String> locations =
                new ArrayList<>();

        for (SlotData slot : currentSlots) {

            if (slot.zoneName == null) {
                continue;
            }

            boolean alreadyExists = false;

            for (String location : locations) {

                if (location.equalsIgnoreCase(
                        slot.zoneName)) {

                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {

                locations.add(
                        slot.zoneName);
            }
        }

        for (String location : locations) {

            Panel locationCard =
                    createLocationCard(
                            location);

            gridContainer.add(
                    locationCard);
        }

        gridContainer.validate();
        gridContainer.repaint();

        scrollPane.validate();
        scrollPane.repaint();
    }

    // =========================================================
    // CREATE LOCATION CARD
    // =========================================================

    private Panel createLocationCard(
            String locationName) {

        Panel locationCard =
                new Panel(
                        new BorderLayout());

        locationCard.setBackground(
                WHITE);

        // -----------------------------------------------------
        // LOCATION HEADER
        // -----------------------------------------------------

        Panel locationHeader =
                new Panel(
                        new BorderLayout());

        locationHeader.setBackground(
                WHITE);

        Panel left =
                new Panel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                15,
                                10));

        left.setBackground(WHITE);

        Label locationTitle =
                new Label(
                        locationName.toUpperCase());

        locationTitle.setFont(
                FONT_SECTION);

        locationTitle.setForeground(
                NAVY);

        Label locationDescription =
                new Label(
                        getLocationDescription(
                                locationName));

        locationDescription.setFont(
                FONT_SMALL);

        locationDescription.setForeground(
                MUTED);

        left.add(locationTitle);
        left.add(locationDescription);

        locationHeader.add(
                left,
                BorderLayout.WEST);

        locationCard.add(
                locationHeader,
                BorderLayout.NORTH);

        // -----------------------------------------------------
        // SLOT TILES
        // -----------------------------------------------------

        Panel slotsPanel =
                new Panel(
                        new GridLayout(
                                0,
                                4,
                                12,
                                12));

        slotsPanel.setBackground(
                WHITE);

        int count = 0;

        for (SlotData slot : currentSlots) {

            if (
                    slot.zoneName
                            .equalsIgnoreCase(
                                    locationName)
            ) {

                slotsPanel.add(
                        new SlotTile(slot));

                count++;
            }
        }

        Panel padding =
                new Panel(
                        new BorderLayout());

        padding.setBackground(
                WHITE);

        padding.add(
                slotsPanel,
                BorderLayout.CENTER);

        locationCard.add(
                padding,
                BorderLayout.CENTER);

        int rows =
                (count + 3) / 4;

        locationCard.setPreferredSize(
                new Dimension(
                        900,
                        120 + rows * 100));

        return locationCard;
    }

    // =========================================================
    // LOCATION DESCRIPTION
    // =========================================================

    private String getLocationDescription(
            String location) {

        if (location == null) {
            return "Campus Parking Area";
        }

        if (location.equalsIgnoreCase(
                "Hospital")) {

            return "Hospital Parking Area";
        }

        if (location.equalsIgnoreCase(
                "Rectangle")) {

            return "Rectangle Campus Area";
        }

        if (location.equalsIgnoreCase(
                "AHS")) {

            return "AHS Campus Area";
        }

        if (location.equalsIgnoreCase(
                "SCAD")) {

            return "SCAD Campus Area";
        }

        if (location.equalsIgnoreCase(
                "Temple")) {

            return "Temple Parking Area";
        }

        return "Campus Parking Area";
    }

    // =========================================================
    // SLOT TILE
    // =========================================================

    private class SlotTile extends Canvas {

        private SlotData slot;

        private boolean hover = false;

        SlotTile(SlotData slot) {

            this.slot = slot;

            setBackground(
                    getStatusBackground(
                            slot.status));

            setPreferredSize(
                    new Dimension(
                            190,
                            82));

            addMouseListener(
                    new MouseAdapter() {

                        @Override
                        public void mouseEntered(
                                MouseEvent e) {

                            hover = true;

                            repaint();
                        }

                        @Override
                        public void mouseExited(
                                MouseEvent e) {

                            hover = false;

                            repaint();
                        }

                        @Override
                        public void mouseClicked(
                                MouseEvent e) {

                            showSlotManagementDialog(
                                    slot);
                        }
                    });
        }

        @Override
        public void paint(Graphics g) {

            super.paint(g);

            Graphics2D g2 =
                    (Graphics2D) g;

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Color statusColor =
                    getStatusColor(
                            slot.status);

            Color lightColor =
                    getStatusBackground(
                            slot.status);

            // -------------------------------------------------
            // BACKGROUND
            // -------------------------------------------------

            g2.setColor(
                    hover
                            ? lightColor.darker()
                            : lightColor);

            g2.fillRect(
                    0,
                    0,
                    getWidth(),
                    getHeight());

            // -------------------------------------------------
            // LEFT STATUS BAR
            // -------------------------------------------------

            g2.setColor(
                    statusColor);

            g2.fillRect(
                    0,
                    0,
                    6,
                    getHeight());

            // -------------------------------------------------
            // SLOT NUMBER
            // -------------------------------------------------

            g2.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            19));

            g2.setColor(NAVY);

            g2.drawString(
                    slot.slotNumber,
                    18,
                    30);

            // -------------------------------------------------
            // STATUS
            // -------------------------------------------------

            g2.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            11));

            g2.setColor(
                    statusColor);

            g2.drawString(
                    slot.status,
                    18,
                    51);

            // -------------------------------------------------
            // LOCATION
            // -------------------------------------------------

            g2.setFont(
                    FONT_SMALL);

            g2.setColor(
                    MUTED);

            String location =
                    slot.location;

            if (location == null) {
                location = "";
            }

            if (location.length() > 22) {

                location =
                        location.substring(
                                0,
                                22)
                                + "...";
            }

            g2.drawString(
                    location,
                    18,
                    69);

            // -------------------------------------------------
            // STATUS INDICATOR
            // -------------------------------------------------

            g2.setColor(
                    statusColor);

            g2.fillOval(
                    getWidth() - 27,
                    16,
                    11,
                    11);
        }
    }

    // =========================================================
    // STATUS COLOR
    // =========================================================

    private Color getStatusColor(
            String status) {

        if (
                "AVAILABLE"
                        .equalsIgnoreCase(
                                status)
        ) {

            return AVAILABLE;
        }

        if (
                "OCCUPIED"
                        .equalsIgnoreCase(
                                status)
        ) {

            return OCCUPIED;
        }

        return RESERVED;
    }

    // =========================================================
    // STATUS BACKGROUND
    // =========================================================

    private Color getStatusBackground(
            String status) {

        if (
                "AVAILABLE"
                        .equalsIgnoreCase(
                                status)
        ) {

            return AVAILABLE_LIGHT;
        }

        if (
                "OCCUPIED"
                        .equalsIgnoreCase(
                                status)
        ) {

            return OCCUPIED_LIGHT;
        }

        return RESERVED_LIGHT;
    }

    // =========================================================
    // EMPTY RESULT
    // =========================================================

    private Panel createEmptyPanel() {

        Panel panel =
                new Panel(
                        new GridBagLayout());

        panel.setBackground(
                WHITE);

        Label title =
                new Label(
                        "No Parking Slots Found");

        title.setFont(
                FONT_SECTION);

        title.setForeground(
                NAVY);

        Label message =
                new Label(
                        "Try changing the location, status or search filter.");

        message.setFont(
                FONT_NORMAL);

        message.setForeground(
                MUTED);

        Panel content =
                new Panel(
                        new GridLayout(
                                2,
                                1,
                                0,
                                8));

        content.setBackground(
                WHITE);

        content.add(title);
        content.add(message);

        panel.add(content);

        return panel;
    }

    // =========================================================
    // TOGGLE GRID / LIST
    // =========================================================

    private void toggleView() {

        isGridView = !isGridView;

        Component componentToRemove;
        Component componentToAdd;

        if (isGridView) {

            componentToRemove =
                    listContainer;

            componentToAdd =
                    scrollPane;

        } else {

            componentToRemove =
                    scrollPane;

            componentToAdd =
                    listContainer;
        }

        mainContent.remove(
                componentToRemove);

        mainContent.add(
                componentToAdd,
                BorderLayout.CENTER);

        btnToggleView.setLabel(
                isGridView
                        ? "☷  List View"
                        : "▦  Visual Grid");

        mainContent.validate();
        mainContent.repaint();
    }

    // =========================================================
    // SLOT MANAGEMENT DIALOG
    // =========================================================

    private void showSlotManagementDialog(
            SlotData slot) {

        Dialog dialog =
                new Dialog(
                        getParentFrame(),
                        "Slot "
                                + slot.slotNumber,
                        true);

        dialog.setLayout(
                new BorderLayout());

        dialog.setBackground(
                WHITE);

        dialog.setSize(
                430,
                310);

        dialog.setLocationRelativeTo(
                getParentFrame());

        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------

        Panel header =
                new Panel(
                        new GridLayout(
                                2,
                                1));

        header.setBackground(
                NAVY);

        Label title =
                new Label(
                        "Parking Slot "
                                + slot.slotNumber);

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        20));

        title.setForeground(
                Color.WHITE);

        Label subtitle =
                new Label(
                        slot.zoneName
                                + "  •  "
                                + slot.location);

        subtitle.setFont(
                FONT_SMALL);

        subtitle.setForeground(
                new Color(
                        203,
                        213,
                        225));

        header.add(title);
        header.add(subtitle);

        dialog.add(
                header,
                BorderLayout.NORTH);

        // -----------------------------------------------------
        // DETAILS
        // -----------------------------------------------------

        Panel details =
                new Panel(
                        new GridLayout(
                                4,
                                2,
                                12,
                                12));

        details.setBackground(
                WHITE);

        addDetail(
                details,
                "Slot ID",
                String.valueOf(
                        slot.id));

        addDetail(
                details,
                "Location",
                slot.zoneName);

        addDetail(
                details,
                "Area",
                slot.location);

        addDetail(
                details,
                "Current Status",
                slot.status);

        Panel detailsWrapper =
                new Panel(
                        new BorderLayout());

        detailsWrapper.setBackground(
                WHITE);

        detailsWrapper.add(
                details,
                BorderLayout.CENTER);

        dialog.add(
                detailsWrapper,
                BorderLayout.CENTER);

        // -----------------------------------------------------
        // BUTTONS
        // -----------------------------------------------------

        Panel buttons =
                new Panel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                10,
                                12));

        buttons.setBackground(
                new Color(
                        248,
                        250,
                        252));

        Button close =
                createButton(
                        "Close",
                        new Color(
                                71,
                                85,
                                105));

        String newStatus;

        if (
                "AVAILABLE"
                        .equalsIgnoreCase(
                                slot.status)
        ) {

            newStatus = "OCCUPIED";

        } else {

            newStatus = "AVAILABLE";
        }

        Button update =
                createButton(
                        "Set " + newStatus,
                        getStatusColor(
                                newStatus));

        buttons.add(close);
        buttons.add(update);

        close.addActionListener(
                e -> dialog.dispose());

        update.addActionListener(
                e -> {

                    dialog.dispose();

                    updateSlotStatus(
                            slot.id,
                            newStatus);
                });

        dialog.add(
                buttons,
                BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // =========================================================
    // DETAIL ROW
    // =========================================================

    private void addDetail(
            Panel panel,
            String name,
            String value) {

        Label label =
                new Label(name);

        label.setFont(FONT_BOLD);
        label.setForeground(MUTED);

        Label valueLabel =
                new Label(value);

        valueLabel.setFont(FONT_NORMAL);
        valueLabel.setForeground(TEXT);

        panel.add(label);
        panel.add(valueLabel);
    }

    // =========================================================
    // GET PARENT FRAME
    // =========================================================

    private Frame getParentFrame() {

        Container parent =
                getParent();

        while (
                parent != null
                        && !(parent instanceof Frame)
        ) {

            parent =
                    parent.getParent();
        }

        if (parent instanceof Frame) {

            return (Frame) parent;
        }

        return null;
    }

    // =========================================================
    // UPDATE SLOT STATUS
    // =========================================================

    private void updateSlotStatus(
            int slotId,
            String newStatus) {

        String sql =
                "UPDATE parking_slots "
                        + "SET status = ? "
                        + "WHERE slot_id = ?";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            pstmt.setString(
                    1,
                    newStatus);

            pstmt.setInt(
                    2,
                    slotId);

            int rows =
                    pstmt.executeUpdate();

            if (rows > 0) {

                showAWTMessage(
                        "Slot Updated",
                        "Slot status changed to "
                                + newStatus
                                + ".");

                applyFilter();
            }

        } catch (SQLException ex) {

            showAWTError(
                    "Update Failed",
                    "Could not update slot status.\n\n"
                            + ex.getMessage());
        }
    }

    // =========================================================
    // AWT SUCCESS MESSAGE
    // =========================================================

    private void showAWTMessage(
            String title,
            String message) {

        Dialog dialog =
                new Dialog(
                        getParentFrame(),
                        title,
                        true);

        dialog.setLayout(
                new BorderLayout());

        dialog.setBackground(
                WHITE);

        Label messageLabel =
                new Label(message);

        messageLabel.setFont(
                FONT_NORMAL);

        messageLabel.setForeground(
                TEXT);

        Panel center =
                new Panel(
                        new GridBagLayout());

        center.setBackground(
                WHITE);

        center.add(messageLabel);

        dialog.add(
                center,
                BorderLayout.CENTER);

        Panel bottom =
                new Panel(
                        new FlowLayout(
                                FlowLayout.RIGHT));

        bottom.setBackground(
                WHITE);

        Button ok =
                createButton(
                        "OK",
                        AVAILABLE);

        ok.addActionListener(
                e -> dialog.dispose());

        bottom.add(ok);

        dialog.add(
                bottom,
                BorderLayout.SOUTH);

        dialog.setSize(
                360,
                170);

        dialog.setLocationRelativeTo(
                getParentFrame());

        dialog.setVisible(true);
    }

    // =========================================================
    // AWT ERROR MESSAGE
    // =========================================================

    private void showAWTError(
            String title,
            String message) {

        Dialog dialog =
                new Dialog(
                        getParentFrame(),
                        title,
                        true);

        dialog.setLayout(
                new BorderLayout());

        dialog.setBackground(
                WHITE);

        Label messageLabel =
                new Label(message);

        messageLabel.setFont(
                FONT_NORMAL);

        messageLabel.setForeground(
                TEXT);

        Panel center =
                new Panel(
                        new GridBagLayout());

        center.setBackground(
                WHITE);

        center.add(messageLabel);

        dialog.add(
                center,
                BorderLayout.CENTER);

        Panel bottom =
                new Panel(
                        new FlowLayout(
                                FlowLayout.RIGHT));

        bottom.setBackground(
                WHITE);

        Button ok =
                createButton(
                        "OK",
                        OCCUPIED);

        ok.addActionListener(
                e -> dialog.dispose());

        bottom.add(ok);

        dialog.add(
                bottom,
                BorderLayout.SOUTH);

        dialog.setSize(
                430,
                190);

        dialog.setLocationRelativeTo(
                getParentFrame());

        dialog.setVisible(true);
    }
}