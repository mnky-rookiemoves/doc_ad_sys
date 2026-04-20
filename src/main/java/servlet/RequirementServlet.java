package servlet;

import java.io.IOException;
import java.util.List;

import dao.ActivityLogDAO;
import dao.RequirementDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.RequirementType;
import model.User;

@WebServlet("/requirements")
public class RequirementServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private RequirementDAO requirementDAO = new RequirementDAO();
    private ActivityLogDAO logDAO         = new ActivityLogDAO();

    /* =========================
       GET — VIEW (all users)
       ========================= */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // ── AUTH — allow ALL logged-in users ──
        User currentUser = getUser(request, response);
        if (currentUser == null) return;

        try {
            List<RequirementType> list =
                requirementDAO.getAllRequirements();

            request.setAttribute("requirementList", list);

            // Pass success/error from redirect params
            request.setAttribute("successMsg",
                request.getParameter("success"));
            request.setAttribute("errorMsg",
                request.getParameter("error"));

            request.getRequestDispatcher("/requirements.jsp")
                   .forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(
                request.getContextPath()
                + "/dashboard?error=Failed+to+load+requirements");
        }
    }

    /* =========================
       POST — ADD / UPDATE / DELETE
       Admin only
       ========================= */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // ── AUTH ──
        User currentUser = getUser(request, response);
        if (currentUser == null) return;

        // ── Block staff from POST ──
        if (!isAdmin(currentUser)) {
            response.sendRedirect(
                request.getContextPath() + "/requirements");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action) {
            case "add"    -> handleAdd(request, response, currentUser);
            case "update" -> handleUpdate(request, response, currentUser);
            case "delete" -> handleDelete(request, response, currentUser);
            default       -> response.sendRedirect(
                                 request.getContextPath()
                                 + "/requirements");
        }
    }

    /* =========================
       ADD
       ========================= */
    private void handleAdd(HttpServletRequest request,
                           HttpServletResponse response,
                           User user) throws IOException {

        String name = request.getParameter("name");
        String desc = request.getParameter("description");

        if (name == null || name.trim().isEmpty()) {
            response.sendRedirect(
                request.getContextPath()
                + "/requirements?error=Requirement+name+is+required.");
            return;
        }

        try {
            RequirementType r = new RequirementType();
            r.setRequirementName(name.trim());
            r.setDescription(desc != null ? desc.trim() : "");

            boolean saved = requirementDAO.addRequirement(r);

            if (saved) {
                logDAO.log(
                    user.getUserId(),
                    user.getUsername(),
                    "ADD",
                    "Requirements",
                    "Added requirement: \""
                        + name.trim() + "\""
                );
                response.sendRedirect(
                    request.getContextPath()
                    + "/requirements?success=Requirement+added+successfully.");
            } else {
                response.sendRedirect(
                    request.getContextPath()
                    + "/requirements?error=Failed+to+add+requirement.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(
                request.getContextPath()
                + "/requirements?error=An+error+occurred.");
        }
    }

    /* =========================
       UPDATE
       ========================= */
    private void handleUpdate(HttpServletRequest request,
                              HttpServletResponse response,
                              User user) throws IOException {

        String name = request.getParameter("name");
        String desc = request.getParameter("description");

        if (name == null || name.trim().isEmpty()) {
            response.sendRedirect(
                request.getContextPath()
                + "/requirements?error=Requirement+name+is+required.");
            return;
        }

        try {
            int id = Integer.parseInt(
                         request.getParameter("id"));

            // Fetch old name for log
            RequirementType old =
                requirementDAO.getRequirementById(id);
            String oldName = (old != null)
                ? old.getRequirementName() : "Unknown";

            RequirementType r = new RequirementType();
            r.setRequirementId(id);
            r.setRequirementName(name.trim());
            r.setDescription(desc != null ? desc.trim() : "");

            boolean updated = requirementDAO.updateRequirement(r);

            if (updated) {
                logDAO.log(
                    user.getUserId(),
                    user.getUsername(),
                    "UPDATE",
                    "Requirements",
                    "Updated requirement: \""
                        + oldName + "\" → \""
                        + name.trim() + "\""
                );
                response.sendRedirect(
                    request.getContextPath()
                    + "/requirements?success=Requirement+updated+successfully.");
            } else {
                response.sendRedirect(
                    request.getContextPath()
                    + "/requirements?error=Failed+to+update+requirement.");
            }

        } catch (NumberFormatException e) {
            response.sendRedirect(
                request.getContextPath()
                + "/requirements?error=Invalid+requirement+ID.");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(
                request.getContextPath()
                + "/requirements?error=An+error+occurred.");
        }
    }

    /* =========================
       DELETE
       ========================= */
    private void handleDelete(HttpServletRequest request,
                              HttpServletResponse response,
                              User user) throws IOException {
        try {
            int id = Integer.parseInt(
                         request.getParameter("id"));

            // Fetch name before delete for log
            RequirementType r =
                requirementDAO.getRequirementById(id);
            String reqName = (r != null)
                ? r.getRequirementName() : "Unknown";

            boolean deleted = requirementDAO.deleteRequirement(id);

            if (deleted) {
                logDAO.log(
                    user.getUserId(),
                    user.getUsername(),
                    "DELETE",
                    "Requirements",
                    "Deleted requirement: \""
                        + reqName + "\""
                        + " (ID: " + id + ")"
                );
                response.sendRedirect(
                    request.getContextPath()
                    + "/requirements?success=Requirement+deleted+successfully.");
            } else {
                response.sendRedirect(
                    request.getContextPath()
                    + "/requirements?error=Failed+to+delete+requirement.");
            }

        } catch (NumberFormatException e) {
            response.sendRedirect(
                request.getContextPath()
                + "/requirements?error=Invalid+requirement+ID.");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(
                request.getContextPath()
                + "/requirements?error=An+error+occurred.");
        }
    }

    /* =========================
       AUTH HELPERS
       ========================= */
    private User getUser(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(
                request.getContextPath() + "/login.jsp");
            return null;
        }

        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect(
                request.getContextPath() + "/login.jsp");
        }
        return user;
    }
        private boolean isAdmin(User user) {
            if (user.getRole() == null) return false;
            String role = user.getRole().toLowerCase();
            return role.equals("admin") || role.equals("superadmin");
        }
}