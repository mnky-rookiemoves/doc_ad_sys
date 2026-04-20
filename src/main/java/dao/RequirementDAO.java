package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kyrie.DBConnection;
import model.RequirementType;

public class RequirementDAO {

    // ✅ EXISTING — keep as-is
    public List<RequirementType> getAllRequirements() {

        List<RequirementType> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM requirement_types"
             );
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RequirementType r = new RequirementType();
                r.setRequirementId(rs.getInt("requirement_id"));
                r.setRequirementName(rs.getString("requirement_name"));
                r.setDescription(rs.getString("description"));
                list.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ✅ NEW — GET SINGLE by ID
    public RequirementType getRequirementById(int id) {

        String sql = """
            SELECT * FROM requirement_types
            WHERE requirement_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                RequirementType r = new RequirementType();
                r.setRequirementId(rs.getInt("requirement_id"));
                r.setRequirementName(rs.getString("requirement_name"));
                r.setDescription(rs.getString("description"));
                return r;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ✅ NEW — ADD
    public boolean addRequirement(RequirementType r) {

        String sql = """
            INSERT INTO requirement_types
                (requirement_name, description)
            VALUES (?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.getRequirementName());
            ps.setString(2, r.getDescription());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ✅ NEW — UPDATE
    public boolean updateRequirement(RequirementType r) {

        String sql = """
            UPDATE requirement_types
            SET requirement_name = ?,
                description      = ?
            WHERE requirement_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.getRequirementName());
            ps.setString(2, r.getDescription());
            ps.setInt(3, r.getRequirementId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ✅ NEW — DELETE
    public boolean deleteRequirement(int id) {

        String sql = """
            DELETE FROM requirement_types
            WHERE requirement_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}