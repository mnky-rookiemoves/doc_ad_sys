package servlet;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import dao.PrintLogDAO;
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
import kyrie.AppConfig;
import model.RequirementType;
import model.Student;
import model.StudentCategory;
import model.Upload;
import model.User;
import util.QRCodeUtil;

@WebServlet("/student-pdf")
public class StudentPDFServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final StudentDAO studentDAO
            = new StudentDAO();
    private final RequirementDAO requirementDAO
            = new RequirementDAO();
    private final UploadDAO uploadDAO
            = new UploadDAO();
    private final StudentCategoryDAO categoryDAO
            = new StudentCategoryDAO();
    private final PrintLogDAO printLogDAO
            = new PrintLogDAO();

    /* =========================
       COLORS
       ========================= */
    private static final Color MAROON
            = new Color(109, 15, 15);
    private static final Color LIGHT_GRAY
            = new Color(245, 245, 245);
    private static final Color BORDER_GRAY
            = new Color(200, 200, 200);
    private static final Color GREEN
            = new Color(46, 125, 50);
    private static final Color RED
            = new Color(198, 40, 40);
    private static final Color WHITE
            = Color.WHITE;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /* =========================
           AUTH
           ========================= */
        HttpSession session
                = request.getSession(false);
        User currentUser = (session != null)
                ? (User) session.getAttribute(
                        "currentUser")
                : null;

        if (currentUser == null) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/login.jsp");
            return;
        }

        /* =========================
           PARAM
           ========================= */
        String studentIdParam
                = request.getParameter("studentId");

        if (studentIdParam == null
                || studentIdParam.trim().isEmpty()) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/students");
            return;
        }

        try {
            int studentId = Integer.parseInt(
                    studentIdParam);

            /* =========================
               FETCH STUDENT
               ========================= */
            Student student
                    = studentDAO.getStudentById(
                            studentId);

            if (student == null) {
                response.sendRedirect(
                        request.getContextPath()
                        + "/students");
                return;
            }

            /* =========================
               CATEGORY NAME
               ========================= */
            String categoryName = "—";
            if (student.getCategoryId() > 0) {
                StudentCategory cat
                        = categoryDAO.getById(
                                student.getCategoryId());
                if (cat != null) {
                    categoryName
                            = cat.getCategoryName();
                }
            }

            /* =========================
               REQUIREMENTS + UPLOADS
               ========================= */
            List<RequirementType> requirements
                    = requirementDAO.getAllRequirements();
            List<Upload> uploads
                    = uploadDAO.getUploadsByStudent(
                            studentId);

            int submittedCount = 0;
            for (RequirementType r : requirements) {
                for (Upload u : uploads) {
                    if (u.getRequirementId()
                            == r.getRequirementId()
                            && u.getFileName() != null) {
                        submittedCount++;
                        break;
                    }
                }
            }

            int safeSubmitted = Math.min(
                    submittedCount,
                    requirements.size());

            boolean isComplete
                    = !requirements.isEmpty()
                    && safeSubmitted
                    >= requirements.size();

            /* =========================
               REFERENCE NUMBER
               ========================= */
            String refNo = (String) session
                    .getAttribute(
                            "lastRefNo_" + studentId);

            if (refNo == null
                    || refNo.isEmpty()) {
                refNo = printLogDAO.savePrintLog(
                        studentId,
                        currentUser.getUserId(),
                        currentUser.getUsername(),
                        "STUDENT_REPORT"
                );
            }

            /* =========================
               DATE + FORMAT
               ========================= */
            Date printedAt = new Date();
            SimpleDateFormat dtf
                    = new SimpleDateFormat(
                            "MMM dd, yyyy hh:mm a");
            String formattedDate
                    = dtf.format(printedAt);

            /* =========================
               QR CODE
               ✅ FIXED — No double
               context path!
               ========================= */
            String baseUrl
                    = AppConfig.getBaseUrl(request)
                    + "verify?ref=" + refNo;
            // ✅ Removed duplicate
            // getContextPath()!

            String qrBase64
                    = QRCodeUtil.generateBase64QRCode(
                            baseUrl, 110);

            /* =========================
               PDF SETUP
               ========================= */
            response.setContentType(
                    "application/pdf");
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=\""
                    + "Student_Report_"
                    + studentId + ".pdf\""
            );

            OutputStream out
                    = response.getOutputStream();
            Document doc = new Document(
                    PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            /* =========================
               FONTS
               ========================= */
            Font titleFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    18, MAROON);
            Font labelFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 9);
            Font valueFont = FontFactory.getFont(
                    FontFactory.HELVETICA, 9);
            Font smallFont = FontFactory.getFont(
                    FontFactory.HELVETICA, 8,
                    new Color(80, 80, 80));
            Font headerFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    10, WHITE);
            Font completeFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    13, GREEN);
            Font incompleteFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    13, RED);
            Font verifiedFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    10, MAROON);
            Font verifiedSubFont = FontFactory.getFont(
                    FontFactory.HELVETICA_OBLIQUE, 8,
                    new Color(80, 80, 80));
            Font refFont = FontFactory.getFont(
                    FontFactory.COURIER_BOLD,
                    10, MAROON);

            /* =========================
               REF NO BOX
               ========================= */
            PdfPTable refTable = new PdfPTable(
                    new float[]{1.5f, 1.5f});
            refTable.setWidthPercentage(100);
            refTable.setSpacingAfter(4);

            PdfPCell refCell = new PdfPCell();
            refCell.setBorder(Rectangle.BOX);
            refCell.setPadding(8);
            refCell.setBackgroundColor(LIGHT_GRAY);

            Paragraph refLabel = new Paragraph(
                    "Reference No:",
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            9));
            refLabel.setSpacingAfter(3);

            Paragraph refValue = new Paragraph(
                    refNo,
                    FontFactory.getFont(
                            FontFactory.COURIER_BOLD,
                            12, MAROON));

            refCell.addElement(refLabel);
            refCell.addElement(refValue);
            refTable.addCell(refCell);

            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.BOX);
            infoCell.setPadding(8);
            infoCell.setBackgroundColor(LIGHT_GRAY);
            infoCell.setHorizontalAlignment(
                    Element.ALIGN_RIGHT);
            infoCell.setVerticalAlignment(
                    Element.ALIGN_MIDDLE);

            Font boldSmall = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 9);
            Font normalSmall = FontFactory.getFont(
                    FontFactory.HELVETICA, 9,
                    new Color(60, 60, 60));

            Paragraph campusPara = new Paragraph();
            campusPara.setAlignment(
                    Element.ALIGN_RIGHT);
            campusPara.add(new Chunk(
                    "Campus: ", boldSmall));
            campusPara.add(new Chunk(
                    "Sta. Mesa (MN0)", normalSmall));
            campusPara.setSpacingAfter(3);

            Paragraph printedPara = new Paragraph();
            printedPara.setAlignment(
                    Element.ALIGN_RIGHT);
            printedPara.add(new Chunk(
                    "Printed by: ", boldSmall));
            printedPara.add(new Chunk(
                    currentUser.getUsername(),
                    normalSmall));
            printedPara.add(new Chunk(
                    "  |  ", normalSmall));
            printedPara.add(new Chunk(
                    "Date: ", boldSmall));
            printedPara.add(new Chunk(
                    formattedDate, normalSmall));

            infoCell.addElement(campusPara);
            infoCell.addElement(printedPara);
            refTable.addCell(infoCell);
            doc.add(refTable);

            /* =========================
               TITLE + QR HEADER
               ========================= */
            Font titleFontLarge = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    20, MAROON);

            PdfPTable header = new PdfPTable(
                    new float[]{3f, 1f});
            header.setWidthPercentage(100);
            header.setSpacingAfter(10);

            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(Rectangle.BOX);
            titleCell.setPadding(14);
            titleCell.setVerticalAlignment(
                    Element.ALIGN_MIDDLE);

            Paragraph titlePara = new Paragraph(
                    "Student Submission Report",
                    titleFontLarge);
            titlePara.setAlignment(
                    Element.ALIGN_LEFT);
            titleCell.addElement(titlePara);
            header.addCell(titleCell);

            PdfPCell qrCell = new PdfPCell();
            qrCell.setPadding(10);
            qrCell.setBorder(Rectangle.BOX);
            qrCell.setHorizontalAlignment(
                    Element.ALIGN_CENTER);
            qrCell.setVerticalAlignment(
                    Element.ALIGN_MIDDLE);

            Image qrImage = Image.getInstance(
                    Base64.getDecoder()
                            .decode(qrBase64));
            qrImage.scaleToFit(90, 90);
            qrImage.setAlignment(
                    Element.ALIGN_CENTER);
            qrCell.addElement(qrImage);
            header.addCell(qrCell);
            doc.add(header);

            /* =========================
               STUDENT INFO TABLE
               ========================= */
            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setWidths(new float[]{1.5f, 3f});
            info.setSpacingAfter(10);

            addInfoRow(info, "Student ID",
                    String.valueOf(
                            student.getStudentId()),
                    labelFont, valueFont);
            addInfoRow(info, "Student Name",
                    student.getStudentName(),
                    labelFont, valueFont);
            addInfoRow(info, "Category",
                    categoryName,
                    labelFont, valueFont);
            addInfoRow(info, "Email",
                    student.getEmail(),
                    labelFont, valueFont);
            addInfoRow(info, "Generated By",
                    currentUser.getUsername(),
                    labelFont, valueFont);
            doc.add(info);

            /* =========================
               REQUIREMENTS STATUS
               ========================= */
            Paragraph reqTitle = new Paragraph(
                    "Requirements Status",
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            12, MAROON));
            reqTitle.setAlignment(
                    Element.ALIGN_CENTER);
            reqTitle.setSpacingAfter(4);
            doc.add(reqTitle);

            PdfPTable reqTable = new PdfPTable(3);
            reqTable.setWidthPercentage(100);
            reqTable.setWidths(
                    new float[]{0.5f, 3f, 1.5f});
            reqTable.setSpacingAfter(8);

            String[] headers = {
                "#", "Requirement", "Status"};
            for (String h : headers) {
                PdfPCell hc = new PdfPCell(
                        new Phrase(h, headerFont));
                hc.setBackgroundColor(MAROON);
                hc.setPadding(5);
                hc.setBorder(Rectangle.BOX);
                reqTable.addCell(hc);
            }

            int rowNum = 1;
            for (RequirementType r : requirements) {

                boolean ok = false;
                for (Upload u : uploads) {
                    if (u.getRequirementId()
                            == r.getRequirementId()
                            && u.getFileName()
                            != null) {
                        ok = true;
                        break;
                    }
                }

                Color rowBg = (rowNum % 2 == 0)
                        ? LIGHT_GRAY : WHITE;

                PdfPCell numCell = new PdfPCell(
                        new Phrase(
                                String.valueOf(rowNum++),
                                valueFont));
                numCell.setPadding(5);
                numCell.setBackgroundColor(rowBg);
                reqTable.addCell(numCell);

                PdfPCell nameCell = new PdfPCell(
                        new Phrase(
                                r.getRequirementName(),
                                valueFont));
                nameCell.setPadding(5);
                nameCell.setBackgroundColor(rowBg);
                reqTable.addCell(nameCell);

                Font statusFont = ok
                        ? FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD,
                                9, GREEN)
                        : FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD,
                                9, RED);

                PdfPCell statusCell = new PdfPCell(
                        new Phrase(
                                ok ? "Submitted"
                                        : "Not Submitted",
                                statusFont));
                statusCell.setPadding(5);
                statusCell.setBackgroundColor(rowBg);
                reqTable.addCell(statusCell);
            }
            doc.add(reqTable);

            /* =========================
               COMPLETION STATUS
               ========================= */
            Paragraph completion = new Paragraph();
            completion.setAlignment(
                    Element.ALIGN_CENTER);
            completion.add(new Chunk(
                    "Completion Status: ",
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            13)));
            completion.add(new Chunk(
                    isComplete
                            ? "COMPLETED"
                            : "INCOMPLETE",
                    isComplete
                            ? completeFont
                            : incompleteFont));
            completion.setSpacingAfter(2);
            doc.add(completion);

            Paragraph submittedPara = new Paragraph(
                    "Submitted "
                    + safeSubmitted
                    + " of "
                    + requirements.size()
                    + " requirements.",
                    valueFont);
            submittedPara.setAlignment(
                    Element.ALIGN_CENTER);
            submittedPara.setSpacingAfter(16);
            doc.add(submittedPara);

            /* =========================
               VERIFIED FOOTER
               ========================= */
            PdfPTable verified = new PdfPTable(1);
            verified.setWidthPercentage(100);
            verified.setSpacingAfter(14);

            PdfPCell vCell = new PdfPCell();
            vCell.setBorder(
                    Rectangle.TOP
                    | Rectangle.BOTTOM);
            vCell.setPadding(6);
            vCell.setHorizontalAlignment(
                    Element.ALIGN_CENTER);

            Paragraph vTitle = new Paragraph(
                    "✔ VERIFIED BY SYSTEM",
                    verifiedFont);
            vTitle.setAlignment(
                    Element.ALIGN_CENTER);

            Paragraph vSub = new Paragraph(
                    "This document contains a secure "
                    + "QR code and was generated "
                    + "electronically by the\n"
                    + "VANDAM Document Admission System",
                    verifiedSubFont);
            vSub.setAlignment(Element.ALIGN_CENTER);

            vCell.addElement(vTitle);
            vCell.addElement(vSub);
            verified.addCell(vCell);
            doc.add(verified);

            /* =========================
               PRINT META
               ========================= */
            doc.add(new Paragraph(
                    "Printed By: "
                    + currentUser.getUsername(),
                    smallFont));
            doc.add(new Paragraph(
                    "Printed At: " + formattedDate,
                    smallFont));

            /* =========================
               SIGNATURE
               ========================= */
            doc.add(new Paragraph(
                    "Prepared by: "
                    + "_____________________________",
                    valueFont));
            doc.add(new Paragraph(
                    "Date: "
                    + "_____________________________",
                    valueFont));

            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(
                    request.getContextPath()
                    + "/students");
        }
    }

    /* =========================
       HELPER: INFO ROW
       ========================= */
    private void addInfoRow(
            PdfPTable table,
            String label,
            String value,
            Font labelFont,
            Font valueFont) {

        PdfPCell labelCell = new PdfPCell(
                new Phrase(label, labelFont));
        labelCell.setBackgroundColor(LIGHT_GRAY);
        labelCell.setBorder(Rectangle.BOX);
        labelCell.setPadding(7);

        PdfPCell valueCell = new PdfPCell(
                new Phrase(
                        value != null ? value : "—",
                        valueFont));
        valueCell.setBorder(Rectangle.BOX);
        valueCell.setPadding(7);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
