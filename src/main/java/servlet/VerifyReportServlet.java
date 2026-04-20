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
import model.RequirementType;
import model.Student;
import model.Upload;
import util.HashUtil;

@WebServlet("/verify-report")
public class VerifyReportServlet extends HttpServlet {

    private StudentDAO studentDAO = new StudentDAO();
    private RequirementDAO requirementDAO = new RequirementDAO();
    private UploadDAO uploadDAO = new UploadDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
       String studentIdParam = request.getParameter("studentId");
                String token = request.getParameter("token");

                if (studentIdParam == null || token == null) {
                    response.sendError(400, "Invalid verification link");
                    return;
                }

                int studentId = Integer.parseInt(studentIdParam);

                String expected =
                    HashUtil.sha256(studentId + "|" + studentIdParam);

                if (!token.equals(expected)) {
                    response.sendError(403, "Verification failed");
                    return;
                }

            Student student = studentDAO.getStudentById(studentId);
            
            List<RequirementType> requirements =
                    requirementDAO.getAllRequirements();
            List<Upload> uploads =
                    uploadDAO.getUploadsByStudent(studentId);

            int submittedCount = 0;
            for (RequirementType r : requirements) {
                for (Upload u : uploads) {
                    if (u.getRequirementId() == r.getRequirementId()
                            && u.getFileName() != null) {
                        submittedCount++;
                        break;
                    }
                }
            }

            boolean isComplete =
                    !requirements.isEmpty()
                    && submittedCount == requirements.size();

            request.setAttribute("student", student);
            request.setAttribute("submittedCount", submittedCount);
            request.setAttribute("totalRequirements", requirements.size());
            request.setAttribute("isComplete", isComplete);

            request.getRequestDispatcher("/verify-report.jsp")
                   .forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid verification link");
        }
    }
}