package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dao.DashboardDAO;
import dao.RequirementDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.RequirementType;
import model.StudentReportRow;
import model.User;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /* =========================
       ✅ ONLY 2 DAOs needed now
       was: 5 DAOs before
       ========================= */
    private final DashboardDAO  dashboardDAO =
        new DashboardDAO();
    private final RequirementDAO reqDAO      =
        new RequirementDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /* =========================
           AUTH CHECK
           ========================= */
        HttpSession session =
            request.getSession(false);
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

        try {
            /* =========================
               ✅ QUERY 1
               Get total requirements
               (needed for completion logic)
               ========================= */
            List<RequirementType> requirements =
                reqDAO.getAllRequirements();
            int totalRequirements =
                requirements.size();

            /* =========================
               ✅ QUERY 2
               Dashboard stats
               (total, complete, pending)
               — replaces 3 separate queries
               ========================= */
            Map<String, Integer> stats =
                dashboardDAO.getDashboardStats(
                    totalRequirements);

            int totalStudents =
                stats.getOrDefault(
                    "totalStudents", 0);
            int completeStudents =
                stats.getOrDefault(
                    "complete", 0);
            int incompleteStudents =
                stats.getOrDefault(
                    "pending", 0);

            /* =========================
               ✅ QUERY 3
               Category breakdown
               — replaces nested Java loops
               ========================= */
            Map<String, Integer> categoryBreakdown =
                dashboardDAO.getCategoryBreakdown();

            /* =========================
               ✅ QUERY 4
               Report rows
               — replaces 712+ queries
               ========================= */
            List<StudentReportRow> reportRows =
                dashboardDAO.getReportRows(
                    totalRequirements);

            /* =========================
               COMPUTE TOTALS FROM ROWS
               (no extra queries needed)
               ========================= */
            int reportComplete   = 0;
            int reportIncomplete = 0;

            for (StudentReportRow row : reportRows) {
                if (row.isComplete) reportComplete++;
                else                reportIncomplete++;
            }

            int completionRate =
                (totalStudents > 0)
                ? (int) Math.round(
                    (reportComplete * 100.0)
                    / totalStudents)
                : 0;

            /* =========================
               SET ALL ATTRIBUTES
               ✅ Same attribute names
               — dashboard.jsp unchanged
               ========================= */
            request.setAttribute(
                "totalStudents",
                totalStudents);
            request.setAttribute(
                "completeRequirements",
                completeStudents);
            request.setAttribute(
                "incompleteRequirements",
                incompleteStudents);
            request.setAttribute(
                "categoryBreakdown",
                categoryBreakdown);
            request.setAttribute(
                "reportRows",
                reportRows);
            request.setAttribute(
                "requirements",
                requirements);
            request.setAttribute(
                "totalComplete",
                reportComplete);
            request.setAttribute(
                "totalIncomplete",
                reportIncomplete);
            request.setAttribute(
                "completionRate",
                completionRate);
            request.setAttribute(
                "generatedBy",
                currentUser.getUsername());
            request.setAttribute(
                "generatedAt",
                new java.util.Date());

            request.getRequestDispatcher(
                "/dashboard.jsp")
                   .forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();

            // ✅ Safe fallback — same as before
            request.setAttribute(
                "totalStudents",          0);
            request.setAttribute(
                "completeRequirements",   0);
            request.setAttribute(
                "incompleteRequirements", 0);
            request.setAttribute(
                "categoryBreakdown",
                new LinkedHashMap<>());
            request.setAttribute(
                "reportRows",
                new ArrayList<>());
            request.setAttribute(
                "totalComplete",          0);
            request.setAttribute(
                "totalIncomplete",        0);
            request.setAttribute(
                "completionRate",         0);
            request.setAttribute(
                "generatedBy",            "—");
            request.setAttribute(
                "generatedAt",
                new java.util.Date());

            request.getRequestDispatcher(
                "/dashboard.jsp")
                   .forward(request, response);
        }
    }
}