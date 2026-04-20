package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kyrie.DBConnection;
import model.PrintLog;

public class PrintLogDAO {

    /* =========================
       GENERATE REFERENCE NUMBER
       Format: VDM-YYYYMMDD-S{id}-MN0-{rand}
       ========================= */
    public String generateRefNo(
            int studentId,
            String docType) {

        String date = new SimpleDateFormat("yyyyMMdd")
            .format(new Date());

        // ✅ 7‑digit random number (1,000,000 – 9,999,999)
        int rand = (int)(Math.random() * 9000000) + 1000000;

        if ("REPORT".equals(docType)) {
            return String.format(
                "VDM-RPT-%s-MN0-%04d",
                date, rand);
        }

        return String.format(
            "VDM-%s-S%03d-MN0-%04d",
            date, studentId, rand);
    }

    /* =========================
       SAVE PRINT LOG
       ========================= */
    public String savePrintLog(
        int studentId,
        int printedBy,
        String printedByName,
        String docType) {

    String sql = """
        INSERT INTO print_logs
        (ref_no, student_id, document_type,
         printed_by, printed_by_name,
         printed_at, campus_code)
        VALUES (?, ?, ?, ?, ?, NOW(), 'MN0')
    """;

    int attempts = 0;
    final int MAX_ATTEMPTS = 5;

    while (attempts < MAX_ATTEMPTS) {
        attempts++;

        // ✅ Generate new ref every attempt
        String refNo = generateRefNo(studentId, docType);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, refNo);
            ps.setInt(2, studentId);
            ps.setString(3, docType);
            ps.setInt(4, printedBy);
            ps.setString(5, printedByName);

            ps.executeUpdate();

            // ✅ SUCCESS
            return refNo;

        } catch (SQLIntegrityConstraintViolationException dup) {
            // ✅ Duplicate ref_no → retry
            System.out.println(
                "Duplicate ref detected, retrying... "
                + refNo);
        } catch (Exception e) {
            e.printStackTrace();
            break;
        }
    }

    // ❌ If still failing after retries
    throw new RuntimeException(
        "Unable to generate unique reference number "
        + "after " + MAX_ATTEMPTS + " attempts.");
}

    /* =========================
       SAVE REPORT LOG (no student)
       ========================= */
    public String saveReportLog(
            int printedBy,
            String printedByName) {

        String refNo = generateRefNo(0, "REPORT");

        String sql = """
            INSERT INTO print_logs
            (ref_no, student_id, document_type,
             printed_by, printed_by_name,
             printed_at, campus_code)
            VALUES (?, NULL, 'ALL_STUDENTS_REPORT',
                ?, ?, NOW(), 'MN0')
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, refNo);
            ps.setInt(2, printedBy);
            ps.setString(3, printedByName);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return refNo;
    }

    /* =========================
       FIND BY REF NO (for verify)
       ========================= */
    public PrintLog findByRefNo(String refNo) {

        String sql = """
            SELECT pl.*,
                   s.student_name,
                   s.email
            FROM print_logs pl
            LEFT JOIN students s
                ON pl.student_id = s.student_id
            WHERE pl.ref_no = ?
        """;

        try (Connection con =
                DBConnection.getConnection();
             PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, refNo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                PrintLog log = new PrintLog();
                log.setRefNo(
                    rs.getString("ref_no"));
                log.setDocumentType(
                    rs.getString("document_type"));
                log.setPrintedByName(
                    rs.getString("printed_by_name"));
                log.setPrintedAt(
                    rs.getTimestamp("printed_at"));
                log.setCampusCode(
                    rs.getString("campus_code"));
                log.setStudentName(
                    rs.getString("student_name"));
                log.setEmail(
                    rs.getString("email"));
                return log;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /* =========================
   LOG PRINT (legacy method)
   Called by StudentPrintServlet
   and StudentPDFServlet
   ========================= */
public void logPrint(int userId,
                     String username,
                     int studentId,
                     String hashToken) {

    String sql = """
        INSERT INTO print_logs
        (ref_no, student_id, document_type,
         printed_by, printed_by_name,
         printed_at, campus_code)
        VALUES (?, ?, 'STUDENT_REPORT',
                ?, ?, NOW(), 'MN0')
    """;

    // ✅ Generate ref number
    String refNo = generateRefNo(
        studentId, "STUDENT_REPORT");

    try (Connection con =
            DBConnection.getConnection();
         PreparedStatement ps =
            con.prepareStatement(sql)) {

        ps.setString(1, refNo);
        ps.setInt(2, studentId);
        ps.setInt(3, userId);
        ps.setString(4, username);

        ps.executeUpdate();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

@Override
public String toString() {
    return "PrintLogDAO []";
}
}