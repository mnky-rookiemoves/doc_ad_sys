package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kyrie.DBConnection;
import model.StudentReportRow;

public class DashboardDAO {

    /* =========================
       DASHBOARD STATS
       ✅ ONE query
       ✅ Fixed varchar→int cast
       ========================= */
    public Map<String, Integer> getDashboardStats(
            int totalRequirements) {

        Map<String, Integer> stats =
            new HashMap<>();

        String sql = """
            SELECT
                COUNT(DISTINCT s.student_id)
                    AS total_students,
                COUNT(DISTINCT
                    CASE
                        WHEN COALESCE(
                            sub.uploaded, 0)
                            >= ?
                        AND ? > 0
                        THEN s.student_id
                    END
                ) AS complete_students
            FROM students s
            LEFT JOIN (
                SELECT
                    CAST(student_id AS UNSIGNED)
                        AS student_id,
                    COUNT(DISTINCT requirement_id)
                        AS uploaded
                FROM student_requirements
                WHERE file_name    IS NOT NULL
                AND   file_content IS NOT NULL
                GROUP BY student_id
            ) sub
            ON s.student_id = sub.student_id
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, totalRequirements);
            ps.setInt(2, totalRequirements);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int total =
                    rs.getInt("total_students");
                int complete =
                    rs.getInt("complete_students");

                stats.put("totalStudents", total);
                stats.put("complete",      complete);
                stats.put("pending",
                    total - complete);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return stats;
    }

    /* =========================
       CATEGORY BREAKDOWN
       ✅ ONE query
       ========================= */
    public Map<String, Integer>
            getCategoryBreakdown() {

        Map<String, Integer> breakdown =
            new LinkedHashMap<>();

        String sql = """
            SELECT
                c.category_name,
                COUNT(s.student_id) AS total
            FROM student_categories c
            LEFT JOIN students s
                ON s.category_id =
                   c.category_id
            GROUP BY
                c.category_id,
                c.category_name
            ORDER BY c.category_name
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql);
             ResultSet rs =
                ps.executeQuery()) {

            while (rs.next()) {
                int total = rs.getInt("total");
                if (total > 0) {
                    breakdown.put(
                        rs.getString(
                            "category_name"),
                        total);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return breakdown;
    }

    /* =========================
       REPORT ROWS
       ✅ ONE query
       ✅ Fixed varchar→int cast
       ========================= */
    public List<StudentReportRow>
            getReportRows(int totalRequirements) {

        List<StudentReportRow> rows =
            new ArrayList<>();

        String sql = """
            SELECT
                s.student_id,
                s.student_name,
                s.email,
                COALESCE(
                    c.category_name, '—')
                    AS category_name,
                COALESCE(
                    sub.uploaded, 0)
                    AS submitted_count
            FROM students s
            LEFT JOIN student_categories c
                ON s.category_id =
                   c.category_id
            LEFT JOIN (
                SELECT
                    CAST(student_id AS UNSIGNED)
                        AS student_id,
                    COUNT(DISTINCT requirement_id)
                        AS uploaded
                FROM student_requirements
                WHERE file_name    IS NOT NULL
                AND   file_content IS NOT NULL
                GROUP BY student_id
            ) sub
            ON s.student_id = sub.student_id
            ORDER BY s.student_name
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql);
             ResultSet rs =
                ps.executeQuery()) {

            while (rs.next()) {

                int submitted = Math.min(
                    rs.getInt("submitted_count"),
                    totalRequirements);

                boolean complete =
                    totalRequirements > 0
                    && submitted
                        >= totalRequirements;

                StudentReportRow row =
                    new StudentReportRow();

                row.studentId =
                    rs.getInt("student_id");
                row.studentName =
                    rs.getString("student_name");
                row.categoryName =
                    rs.getString("category_name");
                row.email =
                    rs.getString("email") != null
                    ? rs.getString("email")
                    : "—";
                row.submittedCount    = submitted;
                row.totalRequirements =
                    totalRequirements;
                row.isComplete        = complete;

                rows.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rows;
    }

    /* =========================
       UPLOAD COUNT MAP
       ✅ ONE query
       ✅ Fixed varchar→int cast
       ========================= */
    public Map<Integer, Integer>
            getUploadCountMap() {

        Map<Integer, Integer> countMap =
            new HashMap<>();

        String sql = """
            SELECT
                CAST(student_id AS UNSIGNED)
                    AS student_id,
                COUNT(DISTINCT requirement_id)
                    AS uploaded
            FROM student_requirements
            WHERE file_name    IS NOT NULL
            AND   file_content IS NOT NULL
            GROUP BY student_id
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql);
             ResultSet rs =
                ps.executeQuery()) {

            while (rs.next()) {
                countMap.put(
                    rs.getInt("student_id"),
                    rs.getInt("uploaded"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return countMap;
    }
}