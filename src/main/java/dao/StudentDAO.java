package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kyrie.DBConnection;
import model.Student;

public class StudentDAO {

    /* =========================
       MAPPER
       ========================= */
    private Student map(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("student_id"),
                rs.getInt("user_id"),
                rs.getString("student_name"),
                rs.getDate("birth_date"),
                rs.getString("email"),
                rs.getInt("category_id"),
                rs.getString("remarks"),
                rs.getString("phone")
        );
    }

    /* =========================
       GET STUDENTS PAGINATED
       ========================= */
    public List<Student> getStudentsPaginated(
            int page, int pageSize) {

        List<Student> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;

        String sql = """
            SELECT * FROM students
            ORDER BY student_id
            LIMIT ? OFFSET ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, pageSize);
            ps.setInt(2, offset);

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
       SEARCH + FILTER
       ========================= */
    public List<Student> searchStudents(
            String search, Integer categoryId,
            int page, int pageSize) {

        List<Student> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT * FROM students WHERE 1=1 ");

        if (search != null && !search.trim().isEmpty()) {
            sql.append(
                "AND (student_name LIKE ? "
                + "OR email LIKE ?) ");
        }

        if (categoryId != null) {
            sql.append("AND category_id = ? ");
        }

        sql.append("ORDER BY student_id LIMIT ? OFFSET ?");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql.toString())) {

            int i = 1;

            if (search != null
                    && !search.trim().isEmpty()) {
                ps.setString(i++, "%" + search + "%");
                ps.setString(i++, "%" + search + "%");
            }

            if (categoryId != null) {
                ps.setInt(i++, categoryId);
            }

            ps.setInt(i++, pageSize);
            ps.setInt(i, (page - 1) * pageSize);

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
       COUNT FILTERED
       ========================= */
    public int countSearchStudents(
            String search, Integer categoryId) {

        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM students WHERE 1=1 ");

        if (search != null && !search.trim().isEmpty()) {
            sql.append(
                "AND (student_name LIKE ? "
                + "OR email LIKE ?) ");
        }

        if (categoryId != null) {
            sql.append("AND category_id = ? ");
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql.toString())) {

            int i = 1;

            if (search != null
                    && !search.trim().isEmpty()) {
                ps.setString(i++, "%" + search + "%");
                ps.setString(i++, "%" + search + "%");
            }

            if (categoryId != null) {
                ps.setInt(i++, categoryId);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /* =========================
       TOTAL COUNT
       ========================= */
    public int getTotalStudentCount() {

        String sql = "SELECT COUNT(*) FROM students";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /* =========================
       GET BY ID
       ========================= */
    public Student getStudentById(int id) {

        String sql =
            "SELECT * FROM students "
            + "WHERE student_id = ?";

        try (Connection con = DBConnection.getConnection();
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
       CREATE STUDENT
       ✅ FIXED — throws RuntimeException
       with "DUPLICATE_STUDENT" message
       so servlet can detect it cleanly
       ========================= */
    public boolean createStudent(Student s) {

        String sql = """
            INSERT INTO students
            (user_id, student_name, birth_date,
             email, category_id, remarks, phone)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setObject(1, s.getUserId());
            ps.setString(2, s.getStudentName());
            ps.setDate(3, s.getBirthDate());
            ps.setString(4, s.getEmail());
            ps.setObject(5, s.getCategoryId());
            ps.setString(6, s.getRemarks());
            ps.setString(7, s.getPhone());

            return ps.executeUpdate() > 0;

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            // ✅ Duplicate name + birthdate detected
            throw new RuntimeException(
                "DUPLICATE_STUDENT", e);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =========================
       UPDATE STUDENT
       ========================= */
    public boolean updateStudent(Student s) {

        String sql = """
            UPDATE students
            SET student_name=?, birth_date=?,
                email=?, category_id=?,
                remarks=?, phone=?
            WHERE student_id=?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, s.getStudentName());
            ps.setDate(2, s.getBirthDate());
            ps.setString(3, s.getEmail());
            ps.setObject(4, s.getCategoryId());
            ps.setString(5, s.getRemarks());
            ps.setString(6, s.getPhone());
            ps.setInt(7, s.getStudentId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /* =========================
       GET ALL STUDENTS
       ========================= */
    public List<Student> getAllStudents() {

        List<Student> list = new ArrayList<>();

        String sql =
            "SELECT * FROM students "
            + "ORDER BY student_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /* =========================
       DELETE STUDENT
       ========================= */
    public boolean deleteStudent(int id) {

        String sql =
            "DELETE FROM students "
            + "WHERE student_id=?";

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
    UPDATE STUDENT CATEGORY
    Used by BatchCategoryServlet
    ========================= */
    public boolean updateCategory(
            int studentId,
            int categoryId) {

        String sql =
            "UPDATE students "
            + "SET category_id = ? "
            + "WHERE student_id = ?";

        try (Connection con =
                DBConnection.getConnection();
            PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            ps.setInt(2, studentId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}