package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import kyrie.DBConnection;
import model.RequirementType;

public class RequirementTypeDAO {

    // ================= GET ALL =================
    public List<RequirementType> getAll() {
        List<RequirementType> list = new ArrayList<>();
        String sql = "SELECT * FROM requirement_types";

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new RequirementType(
                    rs.getInt("requirement_id"),
                    rs.getString("requirement_name"),
                    rs.getString("description")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= GET BY ID =================
    public RequirementType getById(int id) {
        String sql = "SELECT * FROM requirement_types WHERE requirement_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new RequirementType(
                        rs.getInt("requirement_id"),
                        rs.getString("requirement_name"),
                        rs.getString("description")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= ADD =================
    public boolean add(RequirementType r) {
        String sql = "INSERT INTO requirement_types (requirement_name, description) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, r.getRequirementName());
            ps.setString(2, r.getDescription());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= UPDATE =================
    public boolean update(RequirementType r) {
        String sql = "UPDATE requirement_types SET requirement_name=?, description=? WHERE requirement_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, r.getRequirementName());
            ps.setString(2, r.getDescription());
            ps.setInt(3, r.getRequirementId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= DELETE =================
    public boolean delete(int id) {
        String sql = "DELETE FROM requirement_types WHERE requirement_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}