package kyrie;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // ── Railway Environment Variables with local fallback ──────────
    private static final String HOST = System.getenv("MYSQLHOST") != null
            ? System.getenv("MYSQLHOST")
            : "localhost";

    private static final String PORT = System.getenv("MYSQLPORT") != null
            ? System.getenv("MYSQLPORT")
            : "3306";

    private static final String DATABASE = System.getenv("MYSQLDATABASE") != null
            ? System.getenv("MYSQLDATABASE")
            : "doc_admission_db";

    private static final String USER = System.getenv("MYSQLUSER") != null
            ? System.getenv("MYSQLUSER")
            : "root";

    private static final String PASSWORD = System.getenv("MYSQLPASSWORD") != null
            ? System.getenv("MYSQLPASSWORD")
            : "NewPassword123!";

    // ── Build Connection URL ────────────────────────────────────────
    private static final String URL
            = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?allowMultiQueries=true"
            + "&autoReconnect=true"
            + "&useSSL=false"
            + "&serverTimezone=UTC"
            + "&allowPublicKeyRetrieval=true";

    // ── Load MySQL Driver ───────────────────────────────────────────
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] ❌ MySQL Driver not found: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Get Connection ──────────────────────────────────────────────
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("[DBConnection] ❌ Failed to connect!");
            System.err.println("  HOST     : " + HOST);
            System.err.println("  PORT     : " + PORT);
            System.err.println("  DATABASE : " + DATABASE);
            System.err.println("  USER     : " + USER);
            throw e;
        }
    }
}
