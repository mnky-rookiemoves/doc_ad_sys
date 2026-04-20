package servlet;

import java.io.IOException;
import java.util.Date;
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
import model.RequirementType;
import model.Student;
import model.StudentCategory;
import model.Upload;
import model.User;
import util.HashUtil;
import util.QRCodeUtil;

@WebServlet("/student-print")
public class StudentPrintServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /* =========================
       ✅ LOCAL IP CONFIGURATION
       Change IP here if Wi-Fi changes
       Find via: ipconfig → Wi-Fi IPv4
       ========================= */
    private static final String LOCAL_IP
            = "192.168.95.174";   // ✅ your Wi-Fi IP
    private static final String LOCAL_PORT
            = "8443";           // ✅ your Tomcat port

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

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /* =========================
           AUTH CHECK
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
           GET studentId PARAM
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
            int studentId
                    = Integer.parseInt(studentIdParam);

            /* =========================
               FETCH STUDENT
               ========================= */
            Student student
                    = studentDAO.getStudentById(studentId);

            if (student == null) {
                response.sendRedirect(
                        request.getContextPath()
                        + "/students");
                return;
            }

            /* =========================
               GENERATE REFERENCE NUMBER
               Format: VDM-YYYYMMDD-S023-MN0-XXXXXXX
               ✅ Generated ONCE
               ✅ Stored in session for PDF reuse
               ========================= */
            String refNo = printLogDAO.savePrintLog(
                    studentId,
                    currentUser.getUserId(),
                    currentUser.getUsername(),
                    "STUDENT_REPORT"
            );

            // ✅ Store in session for PDF reuse
            request.getSession().setAttribute(
                    "lastRefNo_" + studentId, refNo);

            /* =========================
               GENERATE HASH TOKEN
               ========================= */
            String rawToken = studentId
                    + "|"
                    + currentUser.getUserId();
            String hashToken
                    = HashUtil.sha256(rawToken);

            /* =========================
               ✅ BUILD VERIFY URL (QR)
               Uses LOCAL IP so phone can
               scan QR on same Wi-Fi network.
               ✅ Works in classroom/defense/LAN
               ✅ No internet needed
               ========================= */
            String verifyUrl
                    = "https://" + LOCAL_IP
                    + ":" + LOCAL_PORT
                    + request.getContextPath()
                    + "/verify?ref=" + refNo;

            /* =========================
               GENERATE QR CODE
               ========================= */
            String qrCodeBase64
                    = QRCodeUtil.generateBase64QRCode(
                            verifyUrl, 160);

            /* =========================
               FETCH CATEGORY NAME
               ========================= */
            String categoryName = "—";
            if (student.getCategoryId() > 0) {
                StudentCategory category
                        = categoryDAO.getById(
                                student.getCategoryId());
                if (category != null) {
                    categoryName
                            = category.getCategoryName();
                }
            }

            /* =========================
               FETCH REQUIREMENTS
               ========================= */
            List<RequirementType> requirements
                    = requirementDAO.getAllRequirements();

            /* =========================
               FETCH UPLOADS
               ========================= */
            List<Upload> uploads
                    = uploadDAO.getUploadsByStudent(
                            studentId);

            /* =========================
               COMPUTE COMPLETION
               ========================= */
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

            // ✅ Cap at total requirements
            int safeSubmitted = Math.min(
                    submittedCount,
                    requirements.size());

            boolean isComplete
                    = !requirements.isEmpty()
                    && safeSubmitted
                    >= requirements.size();

            /* =========================
               SET REQUEST ATTRIBUTES
               ========================= */
            Date printedAt = new Date();

            // ✅ Student data
            request.setAttribute("student",
                    student);
            request.setAttribute("categoryName",
                    categoryName);
            request.setAttribute("requirements",
                    requirements);
            request.setAttribute("uploads",
                    uploads);
            request.setAttribute("submittedCount",
                    safeSubmitted);
            request.setAttribute("totalRequirements",
                    requirements.size());
            request.setAttribute("isComplete",
                    isComplete);

            // ✅ Print metadata
            request.setAttribute("generatedBy",
                    currentUser.getUsername());
            request.setAttribute("printedBy",
                    currentUser.getUsername());
            request.setAttribute("printedAt",
                    printedAt);

            // ✅ Reference number
            request.setAttribute("refNo", refNo);

            // ✅ QR code
            // encodes: http://192.168.95.174:8081/
            //          doc_ad_sys/verify?ref=VDM-...
            request.setAttribute("qrCode",
                    qrCodeBase64);

            request.getRequestDispatcher(
                    "/student-print.jsp")
                    .forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/students");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(
                    request.getContextPath()
                    + "/students");
        }
    }
}
