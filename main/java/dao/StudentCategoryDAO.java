package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import kyrie.DBConnection;
import model.StudentCategory;

public class StudentCategoryDAO {

    // ✅ GET ALL CATEGORIES
    public List<StudentCategory> getAllCategories() {
        List<StudentCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM student_categories ORDER BY category_id ASC";

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new StudentCategory(
                    rs.getInt("category_id"),
                    rs.getString("category_name")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ✅ ADD CATEGORY
    public boolean addCategory(StudentCategory category) {
        String sql = "INSERT INTO student_categories (category_name) VALUES (?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, category.getCategoryName());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ✅ DELETE CATEGORY
    public boolean deleteCategory(int id) {
        String sql = "DELETE FROM student_categories WHERE category_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ✅ UPDATE CATEGORY (for Edit Modal)
    public boolean updateCategory(int id, String name) {
        String sql = "UPDATE student_categories SET category_name = ? WHERE category_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public StudentCategory getById(int id) {
    StudentCategory category = null;

    String sql = "SELECT * FROM student_categories WHERE category_id = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                category = new StudentCategory(
                    rs.getInt("category_id"),
                    rs.getString("category_name")
                );
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return category;
}
}