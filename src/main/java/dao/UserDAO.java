package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kyrie.DBConnection;
import model.User;

public class UserDAO {

    /* =========================
       MAPPER
       ========================= */
    private User map(ResultSet rs)
            throws Exception {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(
            rs.getString("username"));
        u.setPassword(
            rs.getString("password"));
        u.setEmail(rs.getString("email"));
        u.setFullName(
            rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setRole(rs.getString("role"));
        u.setActive(
            rs.getInt("is_active") == 1);
        u.setCreatedAt(
            rs.getTimestamp("created_at"));
        u.setLastLogin(
            rs.getTimestamp("last_login"));
        return u;
    }

    /* =========================
       GET ALL USERS
       ========================= */
    public List<User> getAllUsers() {

        List<User> list = new ArrayList<>();

        String sql = """
            SELECT * FROM users
            ORDER BY
                CASE role
                    WHEN 'superadmin' THEN 1
                    WHEN 'admin'      THEN 2
                    WHEN 'staff'      THEN 3
                    ELSE 4
                END,
                username
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql);
             ResultSet rs =
                ps.executeQuery()) {

            while (rs.next())
                list.add(map(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /* =========================
       GET BY ID
       ========================= */
    public User getUserById(int id) {

        String sql =
            "SELECT * FROM users "
            + "WHERE user_id = ?";

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /* =========================
       GET BY USERNAME
       ========================= */
    public User getUserByUsername(
            String username) {

        String sql =
            "SELECT * FROM users "
            + "WHERE username = ?";

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /* =========================
       ✅ GET BY EMAIL
       Used by ForgotPasswordServlet
       ========================= */
    public User getUserByEmail(String email) {

        String sql =
            "SELECT * FROM users "
            + "WHERE email     = ? "
            + "AND   is_active = 1 "
            + "LIMIT 1";

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /* =========================
       CREATE USER
       ========================= */
    public boolean createUser(User u) {

        String sql = """
            INSERT INTO users
                (username, password, email,
                 full_name, phone, role,
                 is_active, created_at)
            VALUES (?, ?, ?, ?, ?, ?, 1, NOW())
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getFullName());
            ps.setString(5, u.getPhone());
            ps.setString(6, u.getRole());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =========================
       UPDATE PROFILE
       (own profile only)
       ========================= */
    public boolean updateProfile(
            int userId,
            String fullName,
            String email,
            String phone) {

        String sql = """
            UPDATE users
            SET full_name = ?,
                email     = ?,
                phone     = ?
            WHERE user_id = ?
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =========================
       ✅ UPDATE PASSWORD
       Used by ResetPasswordServlet
       and ChangePasswordServlet
       ========================= */
    public boolean updatePassword(
            int userId,
            String hashedPassword) {

        String sql = """
            UPDATE users
            SET password = ?
            WHERE user_id = ?
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =========================
       SUPERADMIN — UPDATE USER
       (role + active status)
       ========================= */
    public boolean adminUpdateUser(
            int userId,
            String fullName,
            String email,
            String phone,
            String role,
            boolean isActive) {

        String sql = """
            UPDATE users
            SET full_name = ?,
                email     = ?,
                phone     = ?,
                role      = ?,
                is_active = ?
            WHERE user_id = ?
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, role);
            ps.setInt(5, isActive ? 1 : 0);
            ps.setInt(6, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =========================
       DELETE USER
       ✅ Nullifies student + log
       references before deleting
       ========================= */
    public boolean deleteUser(int userId) {

        String nullifyStudents =
            "UPDATE students "
            + "SET user_id = NULL "
            + "WHERE user_id = ?";

        String nullifyLogs =
            "UPDATE activity_log "
            + "SET user_id = NULL "
            + "WHERE user_id = ?";

        String deleteUser =
            "DELETE FROM users "
            + "WHERE user_id = ?";

        try (Connection con =
                DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                // Step 1 — Nullify students
                try (PreparedStatement ps1 =
                        con.prepareStatement(
                            nullifyStudents)) {
                    ps1.setInt(1, userId);
                    ps1.executeUpdate();
                }

                // Step 2 — Nullify logs
                try (PreparedStatement ps2 =
                        con.prepareStatement(
                            nullifyLogs)) {
                    ps2.setInt(1, userId);
                    ps2.executeUpdate();
                }

                // Step 3 — Delete user
                try (PreparedStatement ps3 =
                        con.prepareStatement(
                            deleteUser)) {
                    ps3.setInt(1, userId);
                    int rows =
                        ps3.executeUpdate();
                    con.commit();
                    return rows > 0;
                }

            } catch (Exception e) {
                con.rollback();
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =========================
       UPDATE LAST LOGIN
       ========================= */
    public boolean updateLastLogin(
            int userId) {

        String sql = """
            UPDATE users
            SET last_login = NOW()
            WHERE user_id = ?
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =========================
       CHECK USERNAME EXISTS
       ========================= */
    public boolean usernameExists(
            String username) {

        String sql = """
            SELECT COUNT(*) FROM users
            WHERE username = ?
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =========================
       GET PASSWORD BY ID
       ========================= */
    public String getPasswordById(
            int userId) {

        String sql =
            "SELECT password FROM users "
            + "WHERE user_id = ?";

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("password");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /* =========================
       AUTHENTICATE USER
       ========================= */
    public User authenticateUser(
            String username,
            String password) {

        String hashed = hashPassword(password);

        String sql = """
            SELECT * FROM users
            WHERE username  = ?
            AND   password  = ?
            AND   is_active = 1
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hashed);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User u = map(rs);
                updateLastLogin(u.getUserId());
                return u;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /* =========================
       PASSWORD HASHING — SHA-256
       ========================= */
    public static String hashPassword(
            String password) {

        if (password == null) return "";

        try {
            java.security.MessageDigest md =
                java.security.MessageDigest
                    .getInstance("SHA-256");

            byte[] hash = md.digest(
                password.getBytes(
                    java.nio.charset
                        .StandardCharsets.UTF_8));

            StringBuilder sb =
                new StringBuilder();
            for (byte b : hash) {
                sb.append(
                    String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return password;
        }
    }
}