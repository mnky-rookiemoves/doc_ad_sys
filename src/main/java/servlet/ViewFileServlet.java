package servlet;

import java.io.IOException;

import dao.UploadDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Upload;
import model.User;

@WebServlet("/view-file")
public class ViewFileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UploadDAO uploadDAO = new UploadDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
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
         * ✅ PARAM VALIDATION
         * =============================== */
        String studentIdParam     = request.getParameter("studentId");
        String requirementIdParam = request.getParameter("requirementId");

        if (studentIdParam == null || requirementIdParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Missing studentId or requirementId.");
            return;
        }

        int studentId;
        int requirementId;

        try {
            studentId     = Integer.parseInt(studentIdParam);
            requirementId = Integer.parseInt(requirementIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Invalid studentId or requirementId.");
            return;
        }

        /* ===============================
         * 📥 FETCH FILE FROM DB
         * =============================== */
        Upload upload = uploadDAO.getFileContent(studentId, requirementId);

        if (upload == null || upload.getFileContent() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                               "File not found.");
            return;
        }

        /* ===============================
         * 📤 STREAM FILE TO BROWSER
         * =============================== */
        String mimeType  = upload.getMimeType();
        String fileName  = upload.getFileName();
        byte[] content   = upload.getFileContent();

        // Fallback MIME type
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "application/octet-stream";
        }

        // Set headers — inline means browser opens it directly
        response.setContentType(mimeType);
        response.setContentLength(content.length);
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + fileName + "\"");

        // Write bytes to response
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }
}