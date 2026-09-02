import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Centralized Database Connection Manager
 *
 * Provides JDBC connection to the MySQL database.
 * The MySQL password is read from the DB_PASSWORD
 * environment variable instead of being stored in GitHub.
 */
public class DBConnection {

    // =========================================================
    // DATABASE CONFIGURATION
    // =========================================================

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";

    // Keep your actual database name here
    private static final String DB_NAME =
            "smart_campus_parking";

    // =========================================================
    // DATABASE URL
    // =========================================================

    private static final String DB_URL =
            "jdbc:mysql://"
                    + DB_HOST
                    + ":"
                    + DB_PORT
                    + "/"
                    + DB_NAME
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=UTC";

    // =========================================================
    // DATABASE USER
    // =========================================================

    private static final String DB_USER =
            "root";

    // =========================================================
    // DATABASE PASSWORD
    //
    // Reads the password from the Windows environment variable:
    //
    // DB_PASSWORD
    //
    // Your real password will NOT be stored in this Java file.
    // =========================================================

    private static final String PASSWORD =
            System.getenv("DB_PASSWORD");

    // =========================================================
    // JDBC DRIVER
    // =========================================================

    private static final String JDBC_DRIVER =
            "com.mysql.cj.jdbc.Driver";

    // =========================================================
    // LOAD JDBC DRIVER
    // =========================================================

    static {

        try {

            Class.forName(JDBC_DRIVER);

        } catch (ClassNotFoundException e) {

            try {

                // Fallback for older MySQL Connector versions
                Class.forName(
                        "com.mysql.jdbc.Driver");

            } catch (ClassNotFoundException ex) {

                System.err.println(
                        "CRITICAL: MySQL JDBC Driver "
                                + "not found in classpath!");

                ex.printStackTrace();
            }
        }
    }

    // =========================================================
    // GET DATABASE CONNECTION
    // =========================================================

    public static Connection getConnection()
            throws SQLException {

        // Check whether the environment variable exists
        if (PASSWORD == null || PASSWORD.trim().isEmpty()) {

            throw new SQLException(
                    "MySQL password is not configured.\n"
                            + "Please set the DB_PASSWORD "
                            + "environment variable.");
        }

        return DriverManager.getConnection(
                DB_URL,
                DB_USER,
                PASSWORD);
    }

    // =========================================================
    // TEST DATABASE CONNECTION
    // =========================================================

    public static boolean testConnection() {

        try (
                Connection conn =
                        getConnection()
        ) {

            return conn != null
                    && !conn.isClosed();

        } catch (SQLException e) {

            System.err.println(
                    "Database connection test failed: "
                            + e.getMessage());

            return false;
        }
    }

    // =========================================================
    // CLOSE CONNECTION SAFELY
    // =========================================================

    public static void closeConnection(
            Connection conn) {

        if (conn != null) {

            try {

                conn.close();

            } catch (SQLException e) {

                System.err.println(
                        "Error closing connection: "
                                + e.getMessage());
            }
        }
    }

    // =========================================================
    // GET DATABASE URL
    // =========================================================

    public static String getDbUrl() {

        return DB_URL;
    }

    // =========================================================
    // GET DATABASE USER
    // =========================================================

    public static String getDbUser() {

        return DB_USER;
    }
}