package servlet;

import java.io.IOException;
import java.util.List;

import dao.StudentRequirementDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.StudentRequirement;
import model.User;

@WebServlet("/student-requirements") // ✅ FIXED MAPPING
public class StudentRequirementServlet extends HttpServlet {

    private StudentRequirementDAO dao = new StudentRequirementDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 🔒 SESSION CHECK
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // 📌 GET STUDENT ID
        String studentId = request.getParameter("studentId");

        // 🚫 BLOCK DIRECT ACCESS (IMPORTANT)
        if (studentId == null || studentId.isEmpty()) {
            response.sendRedirect("students?error=Select+a+student+first");
            return;
        }

        try {
            // 📥 FETCH DATA
            List<StudentRequirement> list = dao.getRequirementsByStudent(studentId);

            // 📤 SEND TO JSP
            request.setAttribute("requirements", list);
            request.setAttribute("studentId", studentId);

            request.getRequestDispatcher("/student-requirements.jsp")
                   .forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("students?error=Failed+to+load+requirements");
        }
    }
}