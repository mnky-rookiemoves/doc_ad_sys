package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

import kyrie.DBConnection;

public class PasswordResetDAO {

    /* =========================
       SAVE TOKEN
       ========================= */
    public boolean saveToken(
            int userId,
            String token,
            LocalDateTime expiresAt) {

        String sql = """
            INSERT INTO password_reset_tokens
            (user_id, token, expires_at)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE
                token      = VALUES(token),
                expires_at = VALUES(expires_at),
                used       = 0
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, token);
            ps.setObject(3, expiresAt);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /* =========================
       VALIDATE TOKEN
       Returns userId if valid
       Returns -1 if invalid
       ========================= */
    public int validateToken(String token) {

        String sql = """
            SELECT user_id
            FROM password_reset_tokens
            WHERE token      = ?
            AND   used       = 0
            AND   expires_at > NOW()
            LIMIT 1
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // ❌ invalid
    }

    /* =========================
       MARK TOKEN AS USED
       ========================= */
    public boolean markUsed(String token) {

        String sql = """
            UPDATE password_reset_tokens
            SET used = 1
            WHERE token = ?
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, token);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /* =========================
       CLEANUP EXPIRED TOKENS
       ========================= */
    public void cleanupExpired() {

        String sql = """
            DELETE FROM password_reset_tokens
            WHERE expires_at < NOW()
            OR used = 1
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}