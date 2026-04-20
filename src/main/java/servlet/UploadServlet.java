package servlet;

import java.io.IOException;
import java.util.List;

import dao.RequirementDAO;
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
import model.Upload;
import model.User;

@WebServlet("/uploads")
public class UploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private RequirementDAO requirementDAO = new RequirementDAO();
    private UploadDAO      uploadDAO      = new UploadDAO();
    private StudentDAO     studentDAO     = new StudentDAO();  // ✅ ADDED

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        /* ===============================
         * 🔒 SESSION CHECK
         * =============================== */
        HttpSession session = request.getSession(false);
        User user = (session != null)
                ? (User) session.getAttribute("currentUser")
                : null;

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        /* ===============================
         * ✅ STUDENT ID VALIDATION
         * =============================== */
        String studentIdParam = request.getParameter("studentId");

        if (studentIdParam == null || studentIdParam.trim().isEmpty()) {
            response.sendRedirect("students?error=Select+a+student+first");
            return;
        }

        int studentId;
        try {
            studentId = Integer.parseInt(studentIdParam);
        } catch (NumberFormatException e) {
            response.sendRedirect("students?error=Invalid+student+ID");
            return;
        }

        /* ===============================
         * 📥 LOAD DATA
         * =============================== */
        try {
            // ✅ Fetch full Student object
            Student student = studentDAO.getStudentById(studentId);

            // If student doesn't exist, bounce back
            if (student == null) {
                response.sendRedirect("students?error=Student+not+found");
                return;
            }

            // All requirements
            List<RequirementType> requirements =
                    requirementDAO.getAllRequirements();

            // Uploaded files for this student (real files only)
            List<Upload> uploads =
                    uploadDAO.getUploadsByStudent(studentId);

            /* ===============================
             * 📤 SEND DATA TO JSP
             * =============================== */
            request.setAttribute("student",      student);   // ✅ full object
            request.setAttribute("studentId",    studentId);
            request.setAttribute("requirements", requirements);
            request.setAttribute("uploadList",   uploads);

            request.getRequestDispatcher("/uploads.jsp")
                   .forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("students?error=Failed+to+load+uploads");
        }
    }
}