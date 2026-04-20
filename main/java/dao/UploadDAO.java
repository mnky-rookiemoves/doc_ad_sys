package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kyrie.DBConnection;
import model.Upload;

public class UploadDAO {

    // ✅ GET UPLOADS BY STUDENT — only rows with actual files
    public List<Upload> getUploadsByStudent(int studentId) {

        List<Upload> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM student_requirements
            WHERE student_id = ?
            AND file_name IS NOT NULL
            AND file_content IS NOT NULL
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Upload u = new Upload();
                u.setStudentRequirementId(rs.getInt("student_requirement_id"));
                u.setStudentId(String.valueOf(rs.getInt("student_id")));
                u.setRequirementId(rs.getInt("requirement_id"));
                u.setFileName(rs.getString("file_name"));
                u.setStatus(rs.getString("status"));
                u.setUploadedAt(rs.getTimestamp("uploaded_at"));
                list.add(u);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ✅ GET FILE BLOB FOR VIEWING — fetches binary content + metadata
    public Upload getFileContent(int studentId, int requirementId) {

        String sql = """
            SELECT file_name, file_content, mime_type
            FROM student_requirements
            WHERE student_id = ?
            AND requirement_id = ?
            AND file_name IS NOT NULL
            AND file_content IS NOT NULL
            LIMIT 1
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, requirementId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Upload u = new Upload();
                u.setFileName(rs.getString("file_name"));
                u.setFileContent(rs.getBytes("file_content"));
                u.setMimeType(rs.getString("mime_type"));
                return u;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ✅ INSERT (FIRST TIME)
    public boolean save(Upload u) {

        String sql = """
            INSERT INTO student_requirements
            (student_id, requirement_id, file_name, file_content,
             mime_type, file_size, status, uploaded_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(u.getStudentId()));
            ps.setInt(2, u.getRequirementId());
            ps.setString(3, u.getFileName());
            ps.setBytes(4, u.getFileContent());
            ps.setString(5, u.getMimeType());
            ps.setLong(6, u.getFileSize());
            ps.setString(7, "Uploaded");

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ✅ UPDATE EXISTING RECORD
    public boolean updateFile(int studentRequirementId,
                              String fileName,
                              byte[] fileContent,
                              String mimeType,
                              long fileSize,
                              int uploadedBy) {

        String sql = """
            UPDATE student_requirements
            SET file_name    = ?,
                file_content = ?,
                mime_type    = ?,
                file_size    = ?,
                uploaded_by  = ?,
                status       = 'Uploaded',
                uploaded_at  = NOW()
            WHERE student_requirement_id = ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fileName);
            ps.setBytes(2, fileContent);
            ps.setString(3, mimeType);
            ps.setLong(4, fileSize);
            ps.setInt(5, uploadedBy);
            ps.setInt(6, studentRequirementId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ✅ COUNT UPLOADS PER STUDENT — only actual files
    public Map<Integer, Integer> countUploadsPerStudent() {

        Map<Integer, Integer> map = new HashMap<>();

        String sql = """
            SELECT student_id, COUNT(*) AS total
            FROM student_requirements
            WHERE file_name IS NOT NULL
            AND file_content IS NOT NULL
            GROUP BY student_id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getInt("student_id"), rs.getInt("total"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    // ✅ CHECK IF UPLOAD ROW EXISTS for student + requirement
    public boolean existsUpload(int studentId, int requirementId) {

        String sql = """
            SELECT COUNT(*)
            FROM student_requirements
            WHERE student_id = ?
            AND requirement_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, requirementId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ✅ UPDATE FILE by studentId + requirementId
    //    (used when replacing an existing upload)
    public boolean updateFileByStudentAndRequirement(
            int    studentId,
            int    requirementId,
            String fileName,
            byte[] fileContent,
            String mimeType,
            long   fileSize,
            int    uploadedBy) {

        String sql = """
            UPDATE student_requirements
            SET file_name    = ?,
                file_content = ?,
                mime_type    = ?,
                file_size    = ?,
                uploaded_by  = ?,
                status       = 'Uploaded',
                uploaded_at  = NOW()
            WHERE student_id     = ?
            AND   requirement_id = ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fileName);
            ps.setBytes(2, fileContent);
            ps.setString(3, mimeType);
            ps.setLong(4, fileSize);
            ps.setInt(5, uploadedBy);
            ps.setInt(6, studentId);
            ps.setInt(7, requirementId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}