package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kyrie.DBConnection;
import model.StudentCategory;

public class StudentCategoryDAO {

    public List<StudentCategory> getAllCategories() {
        List<StudentCategory> categories = new ArrayList<>();
        String sql = "SELECT category_id, category_name FROM student_categories ORDER BY category_name ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                StudentCategory cat = new StudentCategory(
                    rs.getInt("category_id"),
                    rs.getString("category_name")
                );
                categories.add(cat);
            }
            System.out.println("✓ Loaded " + categories.size() + " categories");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    public boolean addCategory(String categoryName) {
        String sql = "INSERT INTO student_categories (category_name) VALUES (?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoryName);
            System.out.println("✓ Category added: " + categoryName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteCategory(int categoryId) {
        String sql = "DELETE FROM student_categories WHERE category_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}