package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kyrie.DBConnection;
import model.StudentRequirement;

public class StudentRequirementDAO {

    /**
     * Get all requirements for a specific student
     * @param studentId Student ID
     * @return List of student requirements
     */
    public List<StudentRequirement> getRequirementsByStudent(int studentId) {
        List<StudentRequirement> requirements = new ArrayList<>();
        
        String sql = "SELECT sr.student_requirement_id, sr.student_id, sr.requirement_id, " +
                     "sr.file_name, sr.status, sr.upload_date, sr.uploaded_at, " +
                     "rt.requirement_name, rt.description " +
                     "FROM student_requirements sr " +
                     "LEFT JOIN requirement_types rt ON sr.requirement_id = rt.requirement_id " +
                     "WHERE sr.student_id = ? " +
                     "ORDER BY rt.requirement_name ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(studentId));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentRequirement sr = new StudentRequirement();
                    sr.setStudentRequirementId(rs.getInt("student_requirement_id"));
                    sr.setStudentId(rs.getInt("student_id"));
                    sr.setRequirementId(rs.getInt("requirement_id"));
                    sr.setFileName(rs.getString("file_name"));
                    sr.setStatus(rs.getString("status"));
                    sr.setUploadDate(rs.getString("upload_date"));
                    sr.setUploadedAt(rs.getString("uploaded_at"));
                    sr.setRequirementName(rs.getString("requirement_name"));
                    sr.setDescription(rs.getString("description"));

                    requirements.add(sr);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching requirements for student " + studentId);
            e.printStackTrace();
        }

        return requirements;
    }

    /**
     * Get count of requirements by status for a student
     * @param studentId Student ID
     * @return Map with status as key and count as value
     */
    public Map<String, Integer> getRequirementCountByStatus(int studentId) {
        Map<String, Integer> statusCount = new HashMap<>();
        
        String sql = "SELECT COALESCE(status, 'not_submitted') as status, COUNT(*) as count " +
                     "FROM student_requirements " +
                     "WHERE student_id = ? " +
                     "GROUP BY COALESCE(status, 'not_submitted')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(studentId));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("count");
                    statusCount.put(status, count);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching requirement count by status for student " + studentId);
            e.printStackTrace();
        }

        return statusCount;
    }

    /**
     * Get total required documents for a student (based on category)
     * @param studentId Student ID
     * @return Total count of required documents
     */
    public int getTotalRequiredDocuments(int studentId) {
        String sql = "SELECT COUNT(DISTINCT sr.requirement_id) as total " +
                     "FROM student_requirements sr " +
                     "WHERE sr.student_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(studentId));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total required documents for student " + studentId);
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Get approved document count for a student
     * @param studentId Student ID
     * @return Count of approved documents
     */
    public int getApprovedRequirementsCount(int studentId) {
        String sql = "SELECT COUNT(*) as approved " +
                     "FROM student_requirements " +
                     "WHERE student_id = ? AND status = 'approved'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(studentId));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("approved");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching approved requirements count for student " + studentId);
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Get requirement details by ID
     * @param requirementId Requirement ID
     * @return StudentRequirement object
     */
    public StudentRequirement getRequirementById(int requirementId) {
        String sql = "SELECT sr.*, rt.requirement_name, rt.description " +
                     "FROM student_requirements sr " +
                     "LEFT JOIN requirement_types rt ON sr.requirement_id = rt.requirement_id " +
                     "WHERE sr.student_requirement_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, requirementId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StudentRequirement sr = new StudentRequirement();
                    sr.setStudentRequirementId(rs.getInt("student_requirement_id"));
                    sr.setStudentId(rs.getInt("student_id"));
                    sr.setRequirementId(rs.getInt("requirement_id"));
                    sr.setFileName(rs.getString("file_name"));
                    sr.setStatus(rs.getString("status"));
                    sr.setUploadDate(rs.getString("upload_date"));
                    sr.setUploadedAt(rs.getString("uploaded_at"));
                    sr.setRequirementName(rs.getString("requirement_name"));
                    sr.setDescription(rs.getString("description"));

                    return sr;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching requirement " + requirementId);
            e.printStackTrace();
        }

        return null;
    }
}