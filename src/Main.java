import java.awt.EventQueue;

/**
 * Main - Pure Java AWT Application Entry Point
 *
 * Smart Campus Parking and Traffic Management System
 * Built strictly with pure Java AWT and MySQL JDBC.
 *
 * Demo Credentials:
 * Username: admin
 * Password: admin123
 */
public class Main {

    public static void main(String[] args) {
        // Initialize AWT look and feel / antialiasing settings
        UITheme.initializeLookAndFeel();

        // Launch UI on the AWT Event Dispatch Thread
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Test database connectivity
                boolean connected = DBConnection.testConnection();
                if (!connected) {
                    System.out.println("Notice: Could not connect to MySQL database 'smart_campus_parking'.");
                    System.out.println("Ensure MySQL Server is active on port 3306.");
                }

                // Launch Login Frame
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            }
        });
    }
}
