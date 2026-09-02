import java.awt.*;

/**
 * UITheme
 *
 * Smart Campus Parking & Traffic Management System
 *
 * PURE JAVA AWT THEME
 *
 * IMPORTANT:
 * - Uses only java.awt.*
 * - No javax.swing.*
 * - No JFrame
 * - No JPanel
 * - No JButton
 * - No JLabel
 * - No JTextField
 * - No JOptionPane
 */
public class UITheme {

    // =========================================================================
    // 1. MAIN COLOR SYSTEM
    // =========================================================================

    // Main application colors
    public static final Color COLOR_PRIMARY_DARK =
            new Color(30, 41, 59);

    public static final Color COLOR_SIDEBAR_BG =
            new Color(30, 41, 59);

    public static final Color COLOR_SIDEBAR_HOVER =
            new Color(51, 65, 85);

    public static final Color COLOR_SIDEBAR_ACTIVE =
            new Color(96, 165, 250);

    // Primary blue
    public static final Color COLOR_PRIMARY =
            new Color(79, 127, 176);

    public static final Color COLOR_PRIMARY_LIGHT =
            new Color(147, 197, 253);

    public static final Color COLOR_PRIMARY_SOFT =
            new Color(239, 246, 255);


    // =========================================================================
    // 2. BACKGROUND & CARD COLORS
    // =========================================================================

    // Very light application background
    public static final Color COLOR_BG =
            new Color(244, 247, 250);

    // White content cards
    public static final Color COLOR_CARD =
            new Color(255, 255, 255);

    // Soft borders
    public static final Color COLOR_BORDER =
            new Color(214, 223, 232);

    public static final Color COLOR_BORDER_DARK =
            new Color(190, 201, 214);


    // =========================================================================
    // 3. TEXT COLORS
    // =========================================================================

    public static final Color COLOR_TEXT_MAIN =
            new Color(51, 65, 85);

    public static final Color COLOR_TEXT_MUTED =
            new Color(100, 116, 139);

    public static final Color COLOR_TEXT_LIGHT =
            new Color(248, 250, 252);


    // =========================================================================
    // 4. STATUS COLORS
    // =========================================================================

    // AVAILABLE
    public static final Color COLOR_AVAILABLE =
            new Color(72, 140, 133);

    public static final Color COLOR_AVAILABLE_BG =
            new Color(226, 241, 239);


    // OCCUPIED
    public static final Color COLOR_OCCUPIED =
            new Color(176, 102, 112);

    public static final Color COLOR_OCCUPIED_BG =
            new Color(247, 234, 236);


    // RESERVED
    public static final Color COLOR_RESERVED =
            new Color(112, 130, 170);

    public static final Color COLOR_RESERVED_BG =
            new Color(232, 236, 247);


    // =========================================================================
    // 5. APPLICATION FEEDBACK COLORS
    // =========================================================================

    public static final Color COLOR_SUCCESS =
            new Color(72, 140, 133);

    public static final Color COLOR_DANGER =
            new Color(176, 102, 112);

    public static final Color COLOR_WARNING =
            new Color(112, 130, 170);


    // =========================================================================
    // 6. EXTRA ACCENT COLORS
    // =========================================================================

    public static final Color COLOR_PURPLE =
            new Color(124, 92, 170);

    public static final Color COLOR_PURPLE_BG =
            new Color(242, 237, 248);

    public static final Color COLOR_TEAL =
            new Color(58, 137, 140);

    public static final Color COLOR_TEAL_BG =
            new Color(232, 245, 245);

    public static final Color COLOR_ORANGE =
            new Color(196, 132, 67);

    public static final Color COLOR_ORANGE_BG =
            new Color(250, 242, 232);

    public static final Color COLOR_BLUE_BG =
            new Color(235, 242, 250);


    // =========================================================================
    // 7. TYPOGRAPHY
    // =========================================================================

    public static final Font FONT_PAGE_TITLE =
            new Font("Segoe UI", Font.BOLD, 18);

    public static final Font FONT_SECTION_TITLE =
            new Font("Segoe UI", Font.BOLD, 14);

    public static final Font FONT_BOLD =
            new Font("Segoe UI", Font.BOLD, 13);

    public static final Font FONT_REGULAR =
            new Font("Segoe UI", Font.PLAIN, 13);

    public static final Font FONT_SMALL =
            new Font("Segoe UI", Font.PLAIN, 11);

    public static final Font FONT_SMALL_BOLD =
            new Font("Segoe UI", Font.BOLD, 11);

    public static final Font FONT_MONO =
            new Font("Monospaced", Font.PLAIN, 12);


    // =========================================================================
    // 8. INITIALIZE AWT RENDERING
    // =========================================================================

    /**
     * Initializes global AWT rendering settings.
     *
     * IMPORTANT:
     * This method contains no Swing code.
     */
    public static void initializeLookAndFeel() {

        System.setProperty(
                "awt.useSystemAAFontSettings",
                "on"
        );
    }


    // =========================================================================
    // 9. REUSABLE CARD PANEL
    // =========================================================================

    /**
     * Reusable card panel using only AWT Panel.
     */
    public static class CardPanel extends Panel {

        private final int top;
        private final int left;
        private final int bottom;
        private final int right;


        public CardPanel(int padding) {

            this(
                    padding,
                    padding,
                    padding,
                    padding
            );
        }


        public CardPanel(
                int top,
                int left,
                int bottom,
                int right
        ) {

            this.top = top;
            this.left = left;
            this.bottom = bottom;
            this.right = right;

            setBackground(COLOR_CARD);
        }


        @Override
        public Insets getInsets() {

            return new Insets(
                    top,
                    left,
                    bottom,
                    right
            );
        }


        @Override
        public void paint(Graphics g) {

            super.paint(g);

            g.setColor(COLOR_BORDER);

            int width = getWidth() - 1;
            int height = getHeight() - 1;

            if (width > 0 && height > 0) {

                g.drawRect(
                        0,
                        0,
                        width,
                        height
                );
            }
        }
    }


    // =========================================================================
    // 10. CARD FACTORIES
    // =========================================================================

    /**
     * Creates a standard card.
     */
    public static CardPanel createCardPanel() {

        return new CardPanel(
                12,
                14,
                12,
                14
        );
    }


    /**
     * Creates a card with custom padding.
     */
    public static CardPanel createCardPanel(
            int topBottomPadding,
            int leftRightPadding
    ) {

        return new CardPanel(
                topBottomPadding,
                leftRightPadding,
                topBottomPadding,
                leftRightPadding
        );
    }


    // =========================================================================
    // 11. HEADER PANEL
    // =========================================================================

    /**
     * Creates a reusable page header.
     */
    public static Panel createHeaderPanel(
            String title,
            String subtitle
    ) {

        CardPanel panel =
                new CardPanel(
                        10,
                        14,
                        10,
                        14
                );

        panel.setLayout(
                new GridLayout(
                        2,
                        1,
                        0,
                        2
                )
        );


        Label lblTitle =
                new Label(title);

        lblTitle.setFont(
                FONT_PAGE_TITLE
        );

        lblTitle.setForeground(
                COLOR_PRIMARY_DARK
        );


        Label lblSubtitle =
                new Label(subtitle);

        lblSubtitle.setFont(
                FONT_SMALL
        );

        lblSubtitle.setForeground(
                COLOR_TEXT_MUTED
        );


        panel.add(lblTitle);
        panel.add(lblSubtitle);

        return panel;
    }


    // =========================================================================
    // 12. BUTTON FACTORY
    // =========================================================================

    /**
     * Creates a styled AWT Button.
     */
    public static Button createStyledButton(
            String text,
            Color background,
            Color foreground
    ) {

        Button button =
                new Button(text);

        button.setFont(
                FONT_BOLD
        );

        button.setBackground(
                background
        );

        button.setForeground(
                foreground
        );

        return button;
    }


    /**
     * Primary blue button.
     */
    public static Button createPrimaryButton(
            String text
    ) {

        return createStyledButton(
                text,
                COLOR_PRIMARY,
                Color.WHITE
        );
    }


    /**
     * Success button.
     */
    public static Button createSuccessButton(
            String text
    ) {

        return createStyledButton(
                text,
                COLOR_SUCCESS,
                Color.WHITE
        );
    }


    /**
     * Danger button.
     */
    public static Button createDangerButton(
            String text
    ) {

        return createStyledButton(
                text,
                COLOR_DANGER,
                Color.WHITE
        );
    }


    /**
     * Warning button.
     */
    public static Button createWarningButton(
            String text
    ) {

        return createStyledButton(
                text,
                COLOR_WARNING,
                Color.WHITE
        );
    }


    /**
     * Purple button.
     */
    public static Button createPurpleButton(
            String text
    ) {

        return createStyledButton(
                text,
                COLOR_PURPLE,
                Color.WHITE
        );
    }


    /**
     * Teal button.
     */
    public static Button createTealButton(
            String text
    ) {

        return createStyledButton(
                text,
                COLOR_TEAL,
                Color.WHITE
        );
    }


    /**
     * Secondary white button.
     */
    public static Button createSecondaryButton(
            String text
    ) {

        return createStyledButton(
                text,
                Color.WHITE,
                COLOR_TEXT_MAIN
        );
    }


    // =========================================================================
    // 13. FORM COMPONENT FACTORIES
    // =========================================================================

    /**
     * Creates a styled AWT TextField.
     */
    public static TextField createTextField(
            int columns
    ) {

        TextField textField =
                new TextField(columns);

        textField.setFont(
                FONT_REGULAR
        );

        textField.setBackground(
                Color.WHITE
        );

        textField.setForeground(
                COLOR_TEXT_MAIN
        );

        return textField;
    }


    /**
     * Creates a styled AWT password TextField.
     */
    public static TextField createPasswordField(
            int columns
    ) {

        TextField textField =
                new TextField(columns);

        textField.setFont(
                FONT_REGULAR
        );

        textField.setEchoChar('*');

        textField.setBackground(
                Color.WHITE
        );

        textField.setForeground(
                COLOR_TEXT_MAIN
        );

        return textField;
    }


    /**
     * Creates an AWT Choice dropdown.
     */
    public static Choice createChoice(
            String[] items
    ) {

        Choice choice =
                new Choice();

        choice.setFont(
                FONT_REGULAR
        );

        for (String item : items) {

            choice.add(item);
        }

        return choice;
    }


    // =========================================================================
    // 14. LABEL FACTORIES
    // =========================================================================

    /**
     * Creates a normal AWT Label.
     */
    public static Label createLabel(
            String text
    ) {

        Label label =
                new Label(text);

        label.setFont(
                FONT_REGULAR
        );

        label.setForeground(
                COLOR_TEXT_MAIN
        );

        return label;
    }


    /**
     * Creates a bold AWT Label.
     */
    public static Label createBoldLabel(
            String text
    ) {

        Label label =
                new Label(text);

        label.setFont(
                FONT_BOLD
        );

        label.setForeground(
                COLOR_TEXT_MAIN
        );

        return label;
    }


    /**
     * Creates a muted AWT Label.
     */
    public static Label createMutedLabel(
            String text
    ) {

        Label label =
                new Label(text);

        label.setFont(
                FONT_SMALL
        );

        label.setForeground(
                COLOR_TEXT_MUTED
        );

        return label;
    }


    /**
     * Creates a section title.
     */
    public static Label createSectionTitle(
            String text
    ) {

        Label label =
                new Label(text);

        label.setFont(
                FONT_SECTION_TITLE
        );

        label.setForeground(
                COLOR_PRIMARY_DARK
        );

        return label;
    }


    // =========================================================================
    // 15. AWT MESSAGE DIALOGS
    // =========================================================================

    /**
     * Displays a success message.
     */
    public static void showSuccess(
            Component parent,
            String message
    ) {

        showSuccess(
                parent,
                "Success",
                message
        );
    }


    /**
     * Displays a success message with title.
     */
    public static void showSuccess(
            Component parent,
            String title,
            String message
    ) {

        AWTDialog.showMessage(
                parent,
                title,
                message
        );
    }


    /**
     * Displays an error message.
     */
    public static void showError(
            Component parent,
            String message
    ) {

        showError(
                parent,
                "Error",
                message
        );
    }


    /**
     * Displays an error message with title.
     */
    public static void showError(
            Component parent,
            String title,
            String message
    ) {

        AWTDialog.showMessage(
                parent,
                title,
                message
        );
    }


    /**
     * Displays a warning message.
     */
    public static void showWarning(
            Component parent,
            String message
    ) {

        showWarning(
                parent,
                "Warning",
                message
        );
    }


    /**
     * Displays a warning message with title.
     */
    public static void showWarning(
            Component parent,
            String title,
            String message
    ) {

        AWTDialog.showMessage(
                parent,
                title,
                message
        );
    }


    /**
     * Displays a confirmation dialog.
     */
    public static boolean showConfirm(
            Component parent,
            String title,
            String question
    ) {

        return AWTDialog.showConfirm(
                parent,
                title,
                question
        );
    }
}