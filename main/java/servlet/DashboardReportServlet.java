package servlet;

import java.io.IOException;
import java.util.ArrayList;
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
import model.StudentReportRow;
import model.Upload;
import model.User;

@WebServlet("/dashboard-report")
public class DashboardReportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final StudentDAO         studentDAO  =
        new StudentDAO();
    private final RequirementDAO     reqDAO      =
        new RequirementDAO();
    private final UploadDAO          uploadDAO   =
        new UploadDAO();
    private final StudentCategoryDAO categoryDAO =
        new StudentCategoryDAO(); 
    private final PrintLogDAO printLogDAO =
        new PrintLogDAO();

    
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
                // ✅ Ignore favicon requests
        if (request.getRequestURI().contains("favicon")) {
            return;
        }
        /* =========================
           AUTH
           ========================= */
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
               FETCH ALL DATA
               ========================= */
            List<Student> students =
                studentDAO.getAllStudents();

            System.out.println(
                "[DashboardReport] Students fetched: "
                + students.size());

            List<RequirementType> requirements =
                reqDAO.getAllRequirements();

            System.out.println(
                "[DashboardReport] Requirements fetched: "
                + requirements.size());

            /* =========================
               BUILD REPORT ROWS
               ========================= */
            List<StudentReportRow> rows =
                new ArrayList<>();

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

            /* =========================
               SET ATTRIBUTES
               ========================= */
            request.setAttribute("reportRows",      rows);
            request.setAttribute("requirements",    requirements);
            request.setAttribute("totalStudents",   total);
            request.setAttribute("totalComplete",   totalComplete);
            request.setAttribute("totalIncomplete", totalIncomplete);
            request.setAttribute("completionRate",  completionRate);
            request.setAttribute("generatedBy",
                currentUser.getUsername());
            request.setAttribute("generatedAt",
                new Date());

            request.getRequestDispatcher(
                "/dashboard-report.jsp")
                   .forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(
                request.getContextPath() + "/dashboard");
        }
        
        String refNo = printLogDAO.saveReportLog(
            currentUser.getUserId(),
            currentUser.getUsername()
        );

        request.setAttribute("refNo", refNo);

        System.out.println(
            "[DashboardReport] Request URI: "
            + request.getRequestURI());
        System.out.println(
            "[DashboardReport] User agent: "
            + request.getHeader("User-Agent"));
    }
}