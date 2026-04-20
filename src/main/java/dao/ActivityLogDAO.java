package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kyrie.DBConnection;
import model.ActivityLog;

public class ActivityLogDAO {

    /* =========================
       INSERT ACTIVITY LOG
       ========================= */
    public boolean log(int userId, String username,
                       String action, String module,
                       String description) {

            String sql = """
                        INSERT INTO activity_log
                        (user_id, username, action, module, description, log_time)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """;

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, username);
            ps.setString(3, action);
            ps.setString(4, module);
            ps.setString(5, description);

            // ✅ Java system time (UTC+8 — matches your Windows clock)
            ps.setTimestamp(6,
                new java.sql.Timestamp(System.currentTimeMillis()));

                return ps.executeUpdate() > 0;

            } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =========================
       GET LOGS (PAGINATED)
       ========================= */
    public List<ActivityLog> getLogs(int page, int pageSize) {

        List<ActivityLog> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM activity_log
            ORDER BY log_time DESC
            LIMIT ? OFFSET ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ActivityLog log = new ActivityLog();
                log.setLogId(rs.getInt("log_id"));
                log.setUserId(rs.getInt("user_id"));
                log.setUsername(rs.getString("username"));
                log.setAction(rs.getString("action"));
                log.setModule(rs.getString("module"));
                log.setDescription(rs.getString("description"));
                log.setLogTime(rs.getTimestamp("log_time")); // ✅ Timestamp
                list.add(log);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /* =========================
       GET LOGS WITH FILTERS
       ========================= */
    public List<ActivityLog> getLogsFiltered(String module,
                                             String username,
                                             int page,
                                             int pageSize) {

        List<ActivityLog> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT *
            FROM activity_log
            WHERE 1=1
        """);

        if (module != null && !module.isEmpty())
            sql.append(" AND module = ?");

        if (username != null && !username.isEmpty())
            sql.append(" AND username LIKE ?");

        sql.append(" ORDER BY log_time DESC LIMIT ? OFFSET ?");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int i = 1;

            if (module != null && !module.isEmpty())
                ps.setString(i++, module);

            if (username != null && !username.isEmpty())
                ps.setString(i++, "%" + username + "%");

            ps.setInt(i++, pageSize);
            ps.setInt(i, (page - 1) * pageSize);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ActivityLog log = new ActivityLog();
                log.setLogId(rs.getInt("log_id"));
                log.setUserId(rs.getInt("user_id"));
                log.setUsername(rs.getString("username"));
                log.setAction(rs.getString("action"));
                log.setModule(rs.getString("module"));
                log.setDescription(rs.getString("description"));
                log.setLogTime(rs.getTimestamp("log_time"));
                list.add(log);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /* =========================
       COUNT LOGS (FOR PAGINATION)
       ========================= */
    public int countLogs(String module, String username) {

        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM activity_log WHERE 1=1"
        );

        if (module != null && !module.isEmpty())
            sql.append(" AND module = ?");

        if (username != null && !username.isEmpty())
            sql.append(" AND username LIKE ?");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int i = 1;

            if (module != null && !module.isEmpty())
                ps.setString(i++, module);

            if (username != null && !username.isEmpty())
                ps.setString(i, "%" + username + "%");

            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}