package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

@WebServlet("/batch-print")
public class BatchPrintServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final StudentDAO studentDAO
            = new StudentDAO();
    private final RequirementDAO reqDAO
            = new RequirementDAO();
    private final UploadDAO uploadDAO
            = new UploadDAO();
    private final StudentCategoryDAO categoryDAO
            = new StudentCategoryDAO();
    private final PrintLogDAO printLogDAO
            = new PrintLogDAO();

    /* =========================
       MODEL
       ========================= */
    public static class BatchStudentRow {

        public Student student;
        public String categoryName;
        public List<RequirementType> requirements;
        public List<Upload> uploads;
        public int submittedCount;
        public boolean isComplete;
        public String refNo;
        public String qrCode;
    }

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
           GET SELECTED IDS
           ========================= */
        String idsParam
                = request.getParameter("ids");

        if (idsParam == null
                || idsParam.trim().isEmpty()) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/students?error="
                    + "No+students+selected.");
            return;
        }

        try {
            String[] idArray
                    = idsParam.split(",");

            List<RequirementType> requirements
                    = reqDAO.getAllRequirements();

            List<BatchStudentRow> batchRows
                    = new ArrayList<>();

            /* =========================
               BUILD BATCH DATA
               ========================= */
            for (String idStr : idArray) {

                int studentId;
                try {
                    studentId = Integer.parseInt(
                            idStr.trim());
                } catch (NumberFormatException e) {
                    continue;
                }

                Student student
                        = studentDAO.getStudentById(
                                studentId);

                if (student == null) {
                    continue;
                }

                /* =========================
                   CATEGORY
                   ========================= */
                String catName = "—";
                if (student.getCategoryId() > 0) {
                    StudentCategory cat
                            = categoryDAO.getById(
                                    student.getCategoryId());
                    if (cat != null) {
                        catName = cat.getCategoryName();
                    }
                }

                /* =========================
                   UPLOADS
                   ========================= */
                List<Upload> uploads
                        = uploadDAO.getUploadsByStudent(
                                studentId);

                /* =========================
                   COUNT SUBMITTED
                   ========================= */
                int submitted = 0;
                for (RequirementType r
                        : requirements) {
                    for (Upload u : uploads) {
                        if (u.getRequirementId()
                                == r.getRequirementId()
                                && u.getFileName()
                                != null) {
                            submitted++;
                            break;
                        }
                    }
                }

                int safeSubmitted = Math.min(
                        submitted,
                        requirements.size());

                boolean complete
                        = !requirements.isEmpty()
                        && safeSubmitted
                        >= requirements.size();

                /* =========================
                   REFERENCE NUMBER
                   ========================= */
                String refNo
                        = printLogDAO.savePrintLog(
                                studentId,
                                currentUser.getUserId(),
                                currentUser.getUsername(),
                                "STUDENT_REPORT"
                        );

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

                String qrCode = "";
                try {
                    qrCode = QRCodeUtil
                            .generateBase64QRCode(
                                    baseUrl, 100);
                } catch (Exception qrEx) {
                    qrEx.printStackTrace();
                }

                /* =========================
                   BUILD ROW
                   ========================= */
                BatchStudentRow row
                        = new BatchStudentRow();
                row.student = student;
                row.categoryName = catName;
                row.requirements = requirements;
                row.uploads = uploads;
                row.submittedCount = safeSubmitted;
                row.isComplete = complete;
                row.refNo = refNo;
                row.qrCode = qrCode;

                batchRows.add(row);

            }

            /* =========================
               SET ATTRIBUTES
               ========================= */
            request.setAttribute(
                    "batchRows", batchRows);
            request.setAttribute(
                    "requirements", requirements);
            request.setAttribute(
                    "generatedBy",
                    currentUser.getUsername());
            request.setAttribute(
                    "generatedAt",
                    new java.util.Date());

            request.getRequestDispatcher(
                    "/batch-print.jsp")
                    .forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(
                    request.getContextPath()
                    + "/students?error="
                    + "Batch+print+failed.");
        }
    }
}
