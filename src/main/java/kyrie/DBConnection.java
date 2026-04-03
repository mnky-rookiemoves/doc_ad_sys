package kyrie;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Database Configuration
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "doc_admission_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";  // MySQL root password (empty by default)

    // JDBC URL with UTF-8 encoding and timezone
    private static final String URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME 
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";

    // Driver initialization
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ MySQL JDBC Driver not found");
            e.printStackTrace();
        }
    }

    /**
     * Get a connection to the database
     * @return Connection object if successful, null if failed
     */
    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
            System.out.println("✓ Database connection established successfully");
            return connection;
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed");
            System.err.println("Error: " + e.getMessage());
            System.err.println("URL: " + URL);
            System.err.println("User: " + DB_USER);
            System.err.println("Make sure:");
            System.err.println("  1. MySQL server is running");
            System.err.println("  2. Database 'doc_admission_db' exists");
            System.err.println("  3. Database credentials are correct");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Close a database connection safely
     * @param connection Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✓ Database connection closed");
            } catch (SQLException e) {
                System.err.println("✗ Error closing database connection");
                e.printStackTrace();
            }
        }
    }

    /**
     * Test the database connection
     */
    public static void testConnection() {
        System.out.println("\n📋 Testing Database Connection...");
        System.out.println("Database: " + DB_NAME);
        System.out.println("Host: " + DB_HOST + ":" + DB_PORT);
        System.out.println("User: " + DB_USER);
        
        Connection con = getConnection();
        if (con != null) {
            System.out.println("✓✓✓ Database connection test PASSED ✓✓✓\n");
            closeConnection(con);
        } else {
            System.err.println("✗✗✗ Database connection test FAILED ✗✗✗\n");
        }
    }
}