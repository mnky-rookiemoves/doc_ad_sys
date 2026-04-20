package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kyrie.DBConnection;
import model.StudentRequirement;

public class StudentRequirementDAO {

    /* =========================
       MAPPER
       ========================= */
    private StudentRequirement map(ResultSet rs)
            throws SQLException {

        StudentRequirement sr = new StudentRequirement();

        sr.setStudentRequirementId(
            rs.getInt("student_requirement_id"));
        sr.setStudentId(rs.getString("student_id"));
        sr.setRequirementId(rs.getInt("requirement_id"));
        sr.setFileName(rs.getString("file_name"));
        sr.setFilePath(rs.getString("file_path"));
        sr.setMimeType(rs.getString("mime_type"));
        sr.setFileSize(rs.getLong("file_size"));
        sr.setStatus(rs.getString("status"));
        sr.setUploadDate(rs.getDate("upload_date"));
        sr.setUploadedAt(rs.getTimestamp("uploaded_at"));

        // Safe optional column (from JOIN)
        try {
            sr.setRequirementName(
                rs.getString("requirement_name"));
        } catch (SQLException ignored) {}

        return sr;
    }

    /* =========================
       GET BY STUDENT
       ========================= */
    public List<StudentRequirement> getRequirementsByStudent(
            String studentId) {

        List<StudentRequirement> list = new ArrayList<>();

        String sql = """
            SELECT sr.*, r.requirement_name
            FROM student_requirements sr
            LEFT JOIN requirements r
                ON sr.requirement_id = r.requirement_id
            WHERE sr.student_id = ?
            ORDER BY sr.student_requirement_id
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /* =========================
       GET BY USER
       ========================= */
    public List<StudentRequirement> getRequirementsByUser(
            int userId) {

        List<StudentRequirement> list = new ArrayList<>();

        String sql = """
            SELECT sr.*, r.requirement_name
            FROM student_requirements sr
            JOIN students s
                ON sr.student_id = s.student_id
            LEFT JOIN requirements r
                ON sr.requirement_id = r.requirement_id
            WHERE s.user_id = ?
            ORDER BY sr.student_requirement_id
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /* =========================
       DASHBOARD COUNTS
       ========================= */

    // ✅ Students where uploads >= total requirements
    public int getTotalCompleteStudents() {

        String sql = """
            SELECT COUNT(*) FROM students s
            WHERE (
                SELECT COUNT(*)
                FROM student_requirements sr
                WHERE sr.student_id = s.student_id
                AND sr.file_name    IS NOT NULL
                AND sr.file_content IS NOT NULL
            ) >= (
                SELECT COUNT(*) FROM requirement_types
            )
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    // ✅ Students where uploads < total requirements
    public int getTotalIncompleteStudents() {

        String sql = """
            SELECT COUNT(*) FROM students s
            WHERE (
                SELECT COUNT(*)
                FROM student_requirements sr
                WHERE sr.student_id = s.student_id
                AND sr.file_name    IS NOT NULL
                AND sr.file_content IS NOT NULL
            ) < (
                SELECT COUNT(*) FROM requirement_types
            )
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /* =========================
       UPDATE STATUS
       ========================= */
    public boolean updateStatus(int id, String status) {

        String sql = """
            UPDATE student_requirements
            SET status = ?
            WHERE student_requirement_id = ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =========================
       DELETE
       ========================= */
    public boolean delete(int id) {

        String sql = """
            DELETE FROM student_requirements
            WHERE student_requirement_id = ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =========================
       INSERT
       ========================= */
    public boolean insert(StudentRequirement sr) {

        String sql = """
            INSERT INTO student_requirements
            (student_id, requirement_id, file_name,
             file_path, mime_type, file_size,
             status, upload_date, uploaded_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, sr.getStudentId());
            ps.setInt(2, sr.getRequirementId());
            ps.setString(3, sr.getFileName());
            ps.setString(4, sr.getFilePath());
            ps.setString(5, sr.getMimeType());
            ps.setLong(6, sr.getFileSize());
            ps.setString(7, sr.getStatus());
            ps.setDate(8, sr.getUploadDate());
            ps.setTimestamp(9, sr.getUploadedAt());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =========================
       ✅ NEW — GET SUBMITTED COUNT
       (capped at total requirements)
       ========================= */
    public int getSubmittedCount(int studentId) {

        String sql = """
            SELECT
                LEAST(
                    (SELECT COUNT(*)
                     FROM student_requirements sr
                     WHERE sr.student_id = ?
                     AND sr.file_name IS NOT NULL),
                    (SELECT COUNT(*)
                     FROM requirement_types)
                ) AS safe_count
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getInt("safe_count");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /* =========================
       ✅ NEW — GET COMPLETION %
       (always 0–100, never over)
       ========================= */
    public int getCompletionPercent(int studentId) {

        String sql = """
            SELECT
                LEAST(100,
                    ROUND(
                        LEAST(
                            (SELECT COUNT(*)
                             FROM student_requirements sr
                             WHERE sr.student_id = ?
                             AND sr.file_name IS NOT NULL),
                            (SELECT COUNT(*)
                             FROM requirement_types)
                        ) * 100.0
                        / NULLIF(
                            (SELECT COUNT(*)
                             FROM requirement_types), 0)
                    )
                ) AS safe_percent
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
                return rs.getInt("safe_percent");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}