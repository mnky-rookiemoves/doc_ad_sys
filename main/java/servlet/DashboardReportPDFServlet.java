package servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import dao.RequirementDAO;
import dao.StudentCategoryDAO;
import dao.StudentDAO;
import dao.UploadDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.RequirementType;
import model.Student;
import model.StudentCategory;
import model.Upload;
import model.User;
import model.StudentReportRow;

import java.awt.Color;
import java.text.SimpleDateFormat;

@WebServlet("/dashboard-report-pdf")
public class DashboardReportPDFServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final StudentDAO         studentDAO  =
        new StudentDAO();
    private final RequirementDAO     reqDAO      =
        new RequirementDAO();
    private final UploadDAO          uploadDAO   =
        new UploadDAO();
    private final StudentCategoryDAO categoryDAO =
        new StudentCategoryDAO();

    private static final Color MAROON     =
        new Color(109, 15, 15);
    private static final Color LIGHT_GRAY =
        new Color(245, 245, 245);
    private static final Color GREEN      =
        new Color(46, 125, 50);
    private static final Color RED        =
        new Color(198, 40, 40);

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User currentUser = (session != null)
                ? (User) session.getAttribute("currentUser")
                : null;

        if (currentUser == null) {
            response.sendRedirect(
                request.getContextPath() + "/login.jsp");
            return;
        }

        try {
            /* =========================
               FETCH DATA
               ========================= */
            List<Student> students =
                studentDAO.getAllStudents();
            List<RequirementType> requirements =
                reqDAO.getAllRequirements();

            List<StudentReportRow> rows = new ArrayList<>();
            int totalComplete   = 0;
            int totalIncomplete = 0;

            for (Student s : students) {
                List<Upload> uploads =
                    uploadDAO.getUploadsByStudent(
                        s.getStudentId());

                int submitted = 0;
                for (RequirementType r : requirements) {
                    for (Upload u : uploads) {
                        if (u.getRequirementId()
                                == r.getRequirementId()
                            && u.getFileName() != null) {
                            submitted++;
                            break;
                        }
                    }
                }

                boolean complete =
                    !requirements.isEmpty()
                    && submitted == requirements.size();

                String catName = "—";
                if (s.getCategoryId() > 0) {
                    StudentCategory cat =
                        categoryDAO.getById(
                            s.getCategoryId());
                    if (cat != null)
                        catName = cat.getCategoryName();
                }

                StudentReportRow row =
                    new StudentReportRow();
                row.studentId         = s.getStudentId();
                row.studentName       = s.getStudentName();
                row.categoryName      = catName;
                row.email             =
                    s.getEmail() != null
                    ? s.getEmail() : "—";
                row.submittedCount    = submitted;
                row.totalRequirements = requirements.size();
                row.isComplete        = complete;
                rows.add(row);

                if (complete) totalComplete++;
                else          totalIncomplete++;
            }

            int total = students.size();
            int completionRate = (total > 0)
                ? (int) Math.round(
                    (totalComplete * 100.0) / total)
                : 0;

            Date generatedAt = new Date();
            SimpleDateFormat dtf =
                new SimpleDateFormat(
                    "MMM dd, yyyy hh:mm a");

            /* =========================
               PDF SETUP
               ========================= */
            response.setContentType("application/pdf");
            response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"All_Students_Report_"
                + new SimpleDateFormat("yyyyMMdd")
                    .format(generatedAt)
                + ".pdf\""
            );

            OutputStream out =
                response.getOutputStream();
            Document doc = new Document(
                PageSize.A4, 72, 72, 72, 72);
            PdfWriter.getInstance(doc, out);
            doc.open();

            /* =========================
               FONTS
               ========================= */
            Font titleFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 18, MAROON);
            Font subFont = FontFactory.getFont(
                FontFactory.HELVETICA, 10,
                new Color(80, 80, 80));
            Font headerFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 10,
                Color.WHITE);
            Font labelFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 9);
            Font valueFont = FontFactory.getFont(
                FontFactory.HELVETICA, 9);
            Font smallFont = FontFactory.getFont(
                FontFactory.HELVETICA, 8,
                new Color(80, 80, 80));
            Font verifiedFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 10, MAROON);
            Font verifiedSubFont = FontFactory.getFont(
                FontFactory.HELVETICA_OBLIQUE, 8,
                new Color(80, 80, 80));

            /* =========================
               GENERATED ON HEADER
               ========================= */
            PdfPTable genTable = new PdfPTable(1);
            genTable.setWidthPercentage(100);
            genTable.setSpacingAfter(6);

            PdfPCell genCell = new PdfPCell(
                new Phrase(
                    "Generated on: "
                    + dtf.format(generatedAt)
                    + "   |   Generated by: "
                    + currentUser.getUsername(),
                    smallFont));
            genCell.setBackgroundColor(LIGHT_GRAY);
            genCell.setBorder(Rectangle.BOX);
            genCell.setPadding(6);
            genTable.addCell(genCell);
            doc.add(genTable);

            /* =========================
               TITLE BOX
               ========================= */
            PdfPTable titleTable = new PdfPTable(1);
            titleTable.setWidthPercentage(100);
            titleTable.setSpacingAfter(16);

            PdfPCell titleCell = new PdfPCell();
            titleCell.setPadding(16);
            titleCell.setBorder(Rectangle.BOX);

            Paragraph titlePara = new Paragraph(
                "All Students Submission Report",
                titleFont);
            titlePara.setSpacingAfter(4);
            titleCell.addElement(titlePara);

            Paragraph subPara = new Paragraph(
                "VANDAM Document Admission System"
                + " — Read-Only Summary",
                subFont);
            titleCell.addElement(subPara);
            titleTable.addCell(titleCell);
            doc.add(titleTable);

            /* =========================
               STATS TABLE
               ========================= */
            PdfPTable statsTable = new PdfPTable(4);
            statsTable.setWidthPercentage(100);
            statsTable.setWidths(
                new float[]{1.5f, 1f, 1.5f, 1f});
            statsTable.setSpacingAfter(16);

            addStatRow(statsTable,
                "Total Students",
                String.valueOf(total),
                "Completion Rate",
                completionRate + "%",
                labelFont, valueFont, GREEN);

            addStatRow(statsTable,
                "Completed",
                String.valueOf(totalComplete),
                "Incomplete",
                String.valueOf(totalIncomplete),
                labelFont, valueFont, RED);

            doc.add(statsTable);

            /* =========================
               MAIN TABLE HEADER
               ========================= */
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{
                    0.4f,  // #
                    1.8f,  // Student Name
                    1.4f,  // Category
                    2.2f,  // Email
                    0.9f,  // Submitted
                    1.2f   // Status
                });
            table.setSpacingAfter(14);

            String[] headers = {
                "#", "Student Name", "Category",
                "Email", "Submitted", "Status"
            };

            for (String h : headers) {
                PdfPCell hc = new PdfPCell(
                    new Phrase(h, headerFont));
                hc.setBackgroundColor(MAROON);
                hc.setPadding(7);
                hc.setHorizontalAlignment(
                    Element.ALIGN_LEFT);
                table.addCell(hc);
            }

            /* =========================
               TABLE ROWS
               ========================= */
            int rowNum = 1;
            for (StudentReportRow row : rows) {

                Color rowBg = (rowNum % 2 == 0)
                    ? LIGHT_GRAY : Color.WHITE;

                addCell(table,
                    String.valueOf(rowNum++),
                    valueFont, rowBg);
                addCell(table,
                    row.studentName,
                    valueFont, rowBg);
                addCell(table,
                    row.categoryName,
                    valueFont, rowBg);
                addCell(table,
                    row.email,
                    valueFont, rowBg);
                addCell(table,
                    row.submittedCount
                    + " / "
                    + row.totalRequirements,
                    valueFont, rowBg);

                Font statusFont = row.isComplete
                    ? FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        9, GREEN)
                    : FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        9, RED);
                addCell(table,
                    row.isComplete
                    ? "COMPLETED"
                    : "INCOMPLETE",
                    statusFont, rowBg);
            }

            doc.add(table);

            /* =========================
               VERIFIED FOOTER
               ========================= */
            PdfPTable verified = new PdfPTable(1);
            verified.setWidthPercentage(100);
            verified.setSpacingAfter(14);

            PdfPCell vCell = new PdfPCell();
            vCell.setBorder(
                Rectangle.TOP | Rectangle.BOTTOM);
            vCell.setPadding(10);
            vCell.setHorizontalAlignment(
                Element.ALIGN_CENTER);

            Paragraph vTitle = new Paragraph(
                "✔ VERIFIED BY SYSTEM",
                verifiedFont);
            vTitle.setAlignment(Element.ALIGN_CENTER);
            vCell.addElement(vTitle);

            Paragraph vSub = new Paragraph(
                "This report was generated electronically"
                + " by the VANDAM Document Admission"
                + " System and is read-only.",
                verifiedSubFont);
            vSub.setAlignment(Element.ALIGN_CENTER);
            vCell.addElement(vSub);

            verified.addCell(vCell);
            doc.add(verified);

            /* =========================
               PRINT META
               ========================= */
            doc.add(new Paragraph(
                "Generated By: "
                + currentUser.getUsername(),
                smallFont));
            doc.add(new Paragraph(
                "Generated At: "
                + dtf.format(generatedAt),
                smallFont));
            doc.add(Chunk.NEWLINE);

            /* =========================
               SIGNATURE
               ========================= */
            doc.add(new Paragraph(
                "Prepared by: "
                + "_____________________________",
                valueFont));
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph(
                "Date: _____________________________",
                valueFont));

            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(
                request.getContextPath() + "/dashboard");
        }
    }

    /* =========================
       HELPERS
       ========================= */
    private void addCell(PdfPTable table,
                         String text,
                         Font font,
                         Color bg) {
        PdfPCell cell = new PdfPCell(
            new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }

    private void addStatRow(PdfPTable table,
                            String l1, String v1,
                            String l2, String v2,
                            Font lf, Font vf,
                            Color valueColor) {

        PdfPCell lc1 = new PdfPCell(new Phrase(l1, lf));
        lc1.setBackgroundColor(LIGHT_GRAY);
        lc1.setPadding(7);
        table.addCell(lc1);

        PdfPCell vc1 = new PdfPCell(new Phrase(v1, vf));
        vc1.setPadding(7);
        table.addCell(vc1);

        PdfPCell lc2 = new PdfPCell(new Phrase(l2, lf));
        lc2.setBackgroundColor(LIGHT_GRAY);
        lc2.setPadding(7);
        table.addCell(lc2);

        Font v2Font = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 9, valueColor);
        PdfPCell vc2 = new PdfPCell(
            new Phrase(v2, v2Font));
        vc2.setPadding(7);
        table.addCell(vc2);
    }
}