package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kyrie.DBConnection;
import model.Student;

public class StudentDAO {

    public boolean addStudent(Student student) {
        String sql = "INSERT INTO students (user_id, student_name, birth_date, email, category_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                System.err.println("Database connection is null");
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, student.getUserId());
                ps.setString(2, student.getStudentName());

                if (student.getBirthDate() != null && !student.getBirthDate().isEmpty()) {
                    ps.setDate(3, Date.valueOf(student.getBirthDate()));
                } else {
                    ps.setNull(3, java.sql.Types.DATE);
                }

                ps.setString(4, student.getEmail());
                ps.setInt(5, student.getCategoryId());

                int rows = ps.executeUpdate();
                System.out.println("✓ Student added: " + student.getStudentName());
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<Student> getStudents(String search, String searchField, Integer categoryId) {
        List<Student> students = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT s.student_id, s.user_id, s.student_name, s.birth_date, s.email, s.category_id, c.category_name ");
        sql.append("FROM students s ");
        sql.append("LEFT JOIN student_categories c ON s.category_id = c.category_id ");
        sql.append("WHERE 1=1 ");

        boolean hasSearch = (search != null && !search.trim().isEmpty());
        if (searchField == null || searchField.isEmpty()) {
            searchField = "all";
        }

        if (categoryId != null) {
            sql.append("AND s.category_id = ? ");
        }

        if (hasSearch) {
            if ("name".equalsIgnoreCase(searchField)) {
                sql.append("AND s.student_name LIKE ? ");
            } else if ("email".equalsIgnoreCase(searchField)) {
                sql.append("AND s.email LIKE ? ");
            } else {
                sql.append("AND (s.student_name LIKE ? OR s.email LIKE ?) ");
            }
        }

        sql.append("ORDER BY s.student_id DESC ");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int idx = 1;

            if (categoryId != null) {
                ps.setInt(idx++, categoryId);
            }

            if (hasSearch) {
                String like = "%" + search.trim() + "%";
                if ("name".equalsIgnoreCase(searchField)) {
                    ps.setString(idx++, like);
                } else if ("email".equalsIgnoreCase(searchField)) {
                    ps.setString(idx++, like);
                } else {
                    ps.setString(idx++, like);
                    ps.setString(idx++, like);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student s = new Student();
                    s.setStudentId(rs.getInt("student_id"));
                    s.setUserId(rs.getInt("user_id"));
                    s.setStudentName(rs.getString("student_name"));

                    Date bd = rs.getDate("birth_date");
                    if (bd != null) s.setBirthDate(bd.toString());

                    s.setEmail(rs.getString("email"));
                    s.setCategoryId(rs.getInt("category_id"));
                    s.setCategoryName(rs.getString("category_name"));

                    students.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    public Student getStudentById(int studentId) {
        String sql = "SELECT s.*, c.category_name FROM students s " +
                     "LEFT JOIN student_categories c ON s.category_id = c.category_id " +
                     "WHERE s.student_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = new Student();
                    s.setStudentId(rs.getInt("student_id"));
                    s.setUserId(rs.getInt("user_id"));
                    s.setStudentName(rs.getString("student_name"));
                    s.setBirthDate(rs.getString("birth_date"));
                    s.setEmail(rs.getString("email"));
                    s.setCategoryId(rs.getInt("category_id"));
                    s.setCategoryName(rs.getString("category_name"));
                    return s;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateStudent(Student s) {
        String sql = "UPDATE students SET student_name=?, birth_date=?, email=?, category_id=? WHERE student_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, s.getStudentName());
            ps.setString(2, s.getBirthDate());
            ps.setString(3, s.getEmail());
            ps.setInt(4, s.getCategoryId());
            ps.setInt(5, s.getStudentId());

            System.out.println("✓ Student updated: " + s.getStudentName());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteStudent(int studentId) {
        String sql = "DELETE FROM students WHERE student_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            System.out.println("✓ Student deleted: ID " + studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<Student> getAllStudents() {
        return getStudents(null, "all", null);
    }

    // ============ PHASE 2 METHODS (For Future Use) ============

    /**
     * Get count of incomplete documents for a specific student
     * @param studentId Student ID
     * @return Count of incomplete documents
     */
    public int getIncompleteDocumentsCount(int studentId) {
        String sql = "SELECT COUNT(*) as incomplete_count " +
                     "FROM student_requirements " +
                     "WHERE student_id = ? AND (status IS NULL OR status != 'approved')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);  // ✅ FIXED: Use setInt

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("incomplete_count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting incomplete documents count for student " + studentId);
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Get total count of documents required/submitted for a student
     * @param studentId Student ID
     * @return Total document count
     */
    public int getTotalDocumentsCount(int studentId) {
        String sql = "SELECT COUNT(*) as total_count " +
                     "FROM student_requirements " +
                     "WHERE student_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);  // ✅ FIXED: Use setInt

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total documents count for student " + studentId);
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Get document completion percentage for a student
     * @param studentId Student ID
     * @return Percentage of completed documents (0-100)
     */
    public int getDocumentCompletionPercentage(int studentId) {
        int total = getTotalDocumentsCount(studentId);
        if (total == 0) return 0;

        int approved = getApprovedDocumentsCount(studentId);
        return (approved * 100) / total;
    }

    /**
     * Get count of approved documents for a student
     * @param studentId Student ID
     * @return Count of approved documents
     */
    public int getApprovedDocumentsCount(int studentId) {
        String sql = "SELECT COUNT(*) as approved_count " +
                     "FROM student_requirements " +
                     "WHERE student_id = ? AND status = 'approved'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);  // ✅ FIXED: Use setInt

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("approved_count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting approved documents count for student " + studentId);
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Get total count of students with at least one incomplete document
     * @return Count of students with incomplete documents
     */
    public int getTotalStudentsWithIncompleteDocuments() {
        String sql = "SELECT COUNT(DISTINCT s.student_id) as incomplete_students " +
                     "FROM students s " +
                     "INNER JOIN student_requirements sr ON s.student_id = sr.student_id " +
                     "WHERE sr.status IS NULL OR sr.status != 'approved'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("incomplete_students");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total students with incomplete documents");
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Get all students sorted by document completion status
     * @return List of students with document status info
     */
    public List<Student> getStudentsWithDocumentStatus(String search, String searchField, Integer categoryId) {
        List<Student> students = getStudents(search, searchField, categoryId);

        // Enrich each student with document status
        for (Student s : students) {
            int total = getTotalDocumentsCount(s.getStudentId());
            int incomplete = getIncompleteDocumentsCount(s.getStudentId());
            s.setTotalDocuments(total);
            s.setIncompleteDocuments(incomplete);
            s.setCompletionPercentage(getDocumentCompletionPercentage(s.getStudentId()));
        }

        return students;
    }

    /**
     * Get document status details for a specific student
     * @param studentId Student ID
     * @return Array: [totalDocuments, approvedDocuments, incompleteDocuments]
     */
    public int[] getStudentDocumentStatusArray(int studentId) {
        int total = getTotalDocumentsCount(studentId);
        int approved = getApprovedDocumentsCount(studentId);
        int incomplete = total - approved;

        return new int[]{total, approved, incomplete};
    }
}