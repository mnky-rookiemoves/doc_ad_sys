package servlet;

import dao.StudentDAO;
import dao.StudentRequirementDAO;
import model.Student;
import model.StudentRequirement;
import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet("/api/student-details")
public class StudentDetailsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Check if user is logged in
            User currentUser = (User) request.getSession().getAttribute("currentUser");
            if (currentUser == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"success\": false, \"error\": \"Unauthorized\"}");
                out.flush();
                return;
            }

            String studentIdStr = request.getParameter("studentId");
            if (studentIdStr == null || studentIdStr.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"error\": \"Student ID is required\"}");
                out.flush();
                return;
            }

            int studentId = Integer.parseInt(studentIdStr.trim());

            // Get student details
            StudentDAO studentDAO = new StudentDAO();
            Student student = studentDAO.getStudentById(studentId);

            if (student == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\": false, \"error\": \"Student not found\"}");
                out.flush();
                return;
            }

            // Get document requirements
            StudentRequirementDAO reqDAO = new StudentRequirementDAO();
            List<StudentRequirement> requirements = reqDAO.getRequirementsByStudent(studentId);
            Map<String, Integer> statusCount = reqDAO.getRequirementCountByStatus(studentId);

            // Build response
            StudentDetailsResponse responseData = new StudentDetailsResponse();
            responseData.setSuccess(true);
            responseData.setStudent(student);
            responseData.setRequirements(requirements);
            responseData.setStatusCount(statusCount);
            responseData.setTotalRequired(reqDAO.getTotalRequiredDocuments(studentId));
            responseData.setTotalApproved(reqDAO.getApprovedRequirementsCount(studentId));

            String jsonResponse = gson.toJson(responseData);
            System.out.println("✓ Student details fetched: " + student.getStudentName());
            out.print(jsonResponse);
            out.flush();

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"error\": \"Invalid Student ID\"}");
            out.flush();
            System.err.println("Invalid student ID format: " + e.getMessage());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"error\": \"Server error: " + e.getMessage() + "\"}");
            out.flush();
            System.err.println("Error fetching student details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Response class
    public static class StudentDetailsResponse {
        private boolean success;
        private Student student;
        private List<StudentRequirement> requirements;
        private Map<String, Integer> statusCount;
        private int totalRequired;
        private int totalApproved;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public Student getStudent() { return student; }
        public void setStudent(Student student) { this.student = student; }

        public List<StudentRequirement> getRequirements() { return requirements; }
        public void setRequirements(List<StudentRequirement> requirements) { this.requirements = requirements; }

        public Map<String, Integer> getStatusCount() { return statusCount; }
        public void setStatusCount(Map<String, Integer> statusCount) { this.statusCount = statusCount; }

        public int getTotalRequired() { return totalRequired; }
        public void setTotalRequired(int totalRequired) { this.totalRequired = totalRequired; }

        public int getTotalApproved() { return totalApproved; }
        public void setTotalApproved(int totalApproved) { this.totalApproved = totalApproved; }
    }
}