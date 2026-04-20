package servlet;

import java.io.IOException;
import java.util.List;

import dao.ActivityLogDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ActivityLog;
import model.User;

@WebServlet("/activity-log")
public class ActivityLogServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ActivityLogDAO logDAO = new ActivityLogDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        /* =========================
           AUTH CHECK
           ========================= */
        HttpSession session = request.getSession(false);
        User currentUser = (session != null)
                ? (User) session.getAttribute("currentUser")
                : null;

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        /* =========================
           FILTER PARAMETERS
           ========================= */
        String module   = trim(request.getParameter("module"));
        String username = trim(request.getParameter("username"));

        boolean isFiltering =
                (module != null && !module.isEmpty()) ||
                (username != null && !username.isEmpty());

        /* =========================
           PAGINATION PARAMETERS
           ========================= */
        int pageSize = 20;   // default
        int page     = 1;    // default

        try {
            String ps = request.getParameter("pageSize");
            if (ps != null) {
                int val = Integer.parseInt(ps);
                if (val == 10 || val == 20 || val == 50) {
                    pageSize = val;
                }
            }

            String p = request.getParameter("page");
            if (p != null) {
                page = Math.max(1, Integer.parseInt(p));
            }
        } catch (Exception ignored) {}

        /* =========================
           COUNT TOTAL LOGS
           ========================= */
        int totalLogs = logDAO.countLogs(module, username);
        int totalPages = (int) Math.ceil((double) totalLogs / pageSize);
        if (totalPages < 1) totalPages = 1;

        // ✅ Clamp page safely
        if (page > totalPages) {
            page = totalPages;
        }

        /* =========================
           FETCH LOGS
           ========================= */
        List<ActivityLog> logs = isFiltering
                ? logDAO.getLogsFiltered(module, username, page, pageSize)
                : logDAO.getLogs(page, pageSize);

        /* =========================
           ROW RANGE (DISPLAY INFO)
           ========================= */
        int offset   = (page - 1) * pageSize;
        int startRow = totalLogs == 0 ? 0 : offset + 1;
        int endRow   = Math.min(offset + pageSize, totalLogs);

        /* =========================
           UX FLAGS
           ========================= */
        boolean highlightNew =
                "true".equals(request.getParameter("new"));

        /* =========================
           SET REQUEST ATTRIBUTES
           ========================= */
        request.setAttribute("logs", logs);

        request.setAttribute("totalLogs", totalLogs);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.setAttribute("startRow", startRow);
        request.setAttribute("endRow", endRow);

        request.setAttribute("filterModule", module);
        request.setAttribute("filterUsername", username);

        request.setAttribute("highlightNew", highlightNew);

        /* =========================
           FORWARD
           ========================= */
        request.getRequestDispatcher("/activity-log.jsp")
               .forward(request, response);
    }

    /* =========================
       HELPER
       ========================= */
    private String trim(String s) {
        return (s == null) ? null : s.trim();
    }
}
