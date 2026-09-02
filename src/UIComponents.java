import java.awt.*;

/**
 * UIComponents - Pure Java AWT Custom Components
 *
 * Implements:
 * - StatCard: Custom AWT Panel displaying metric title, value, subtext, and colored accent bar
 * - OccupancyDonutChart: Custom AWT Canvas painting a donut occupancy chart and zone legend
 *
 * ZERO javax.swing dependencies.
 */
public class UIComponents {

    // =========================================================================
    // 1. STAT CARD (AWT KPI METRIC PANEL)
    // =========================================================================
    public static class StatCard extends Panel {
        private final Label lblTitle;
        private final Label lblValue;
        private final Label lblSubtext;
        private final Color accentColor;

        public StatCard(String title, String initialVal, Color accentColor, String subtext) {
            this.accentColor = accentColor;
            setLayout(new GridLayout(3, 1, 0, 2));
            setBackground(Color.WHITE);

            lblTitle = new Label(title.toUpperCase());
            lblTitle.setFont(UITheme.FONT_SMALL_BOLD);
            lblTitle.setForeground(UITheme.COLOR_TEXT_MUTED);

            lblValue = new Label(initialVal);
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblValue.setForeground(UITheme.COLOR_TEXT_MAIN);

            lblSubtext = new Label(subtext);
            lblSubtext.setFont(UITheme.FONT_SMALL);
            lblSubtext.setForeground(UITheme.COLOR_TEXT_MUTED);

            add(lblTitle);
            add(lblValue);
            add(lblSubtext);
        }

        public void setValue(String val) {
            lblValue.setText(val);
        }

        public void setSubtext(String sub) {
            lblSubtext.setText(sub);
        }

        @Override
        public Insets getInsets() {
            return new Insets(10, 14, 10, 12);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            int w = getWidth();
            int h = getHeight();

            // Border
            g.setColor(UITheme.COLOR_BORDER);
            g.drawRect(0, 0, w - 1, h - 1);

            // Left Color Accent Bar
            g.setColor(accentColor);
            g.fillRect(1, 1, 4, h - 2);
        }
    }

    // =========================================================================
    // 2. OCCUPANCY DONUT CHART (AWT CANVAS)
    // =========================================================================
    public static class OccupancyDonutChart extends Canvas {
        private int available = 13;
        private int occupied = 3;
        private int reserved = 2;

        public OccupancyDonutChart() {
            setBackground(Color.WHITE);
            setSize(320, 150);
        }

        public void updateCounts(int avail, int occ, int res) {
            this.available = avail;
            this.occupied = occ;
            this.reserved = res;
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            int w = getWidth();
            int h = getHeight();

            int total = available + occupied + reserved;
            if (total == 0) total = 18;

            int diameter = Math.min(110, h - 20);
            int cx = 70;
            int cy = h / 2;
            int x = cx - diameter / 2;
            int y = cy - diameter / 2;

            int angleAvail = (int) Math.round((double) available / total * 360.0);
            int angleOcc = (int) Math.round((double) occupied / total * 360.0);
            int angleRes = 360 - angleAvail - angleOcc;

            // Draw donut segments
            int startAngle = 90;

            // Available (Green)
            g.setColor(UITheme.COLOR_AVAILABLE);
            g.fillArc(x, y, diameter, diameter, startAngle, -angleAvail);
            startAngle -= angleAvail;

            // Occupied (Red)
            g.setColor(UITheme.COLOR_OCCUPIED);
            g.fillArc(x, y, diameter, diameter, startAngle, -angleOcc);
            startAngle -= angleOcc;

            // Reserved (Amber)
            g.setColor(UITheme.COLOR_RESERVED);
            g.fillArc(x, y, diameter, diameter, startAngle, -angleRes);

            // Inner hole for donut
            int hole = (int) (diameter * 0.60);
            int hx = cx - hole / 2;
            int hy = cy - hole / 2;
            g.setColor(Color.WHITE);
            g.fillOval(hx, hy, hole, hole);

            // Center % text
            int occPct = (int) Math.round(((double) (occupied + reserved) / total) * 100.0);
            g.setColor(UITheme.COLOR_PRIMARY_DARK);
            g.setFont(new Font("Segoe UI", Font.BOLD, 15));
            String pctStr = occPct + "%";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(pctStr, cx - fm.stringWidth(pctStr) / 2, cy + 5);

            // Right Legend
            int lx = cx + diameter / 2 + 20;
            int ly = cy - 35;

            drawLegendRow(g, lx, ly, UITheme.COLOR_AVAILABLE, "Available: " + available + " slots (" + (available * 100 / total) + "%)");
            drawLegendRow(g, lx, ly + 25, UITheme.COLOR_OCCUPIED, "Occupied: " + occupied + " slots (" + (occupied * 100 / total) + "%)");
            drawLegendRow(g, lx, ly + 50, UITheme.COLOR_RESERVED, "Reserved: " + reserved + " slots (" + (reserved * 100 / total) + "%)");
        }

        private void drawLegendRow(Graphics g, int x, int y, Color color, String text) {
            g.setColor(color);
            g.fillRect(x, y, 10, 10);
            g.setColor(UITheme.COLOR_TEXT_MAIN);
            g.setFont(UITheme.FONT_REGULAR);
            g.drawString(text, x + 16, y + 10);
        }
    }
}
