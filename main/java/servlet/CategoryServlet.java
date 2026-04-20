package servlet;

import java.io.IOException;
import java.util.List;

import dao.ActivityLogDAO;
import dao.StudentCategoryDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.StudentCategory;
import model.User;

@WebServlet("/categories")
public class CategoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final StudentCategoryDAO categoryDAO =
        new StudentCategoryDAO();
    private final ActivityLogDAO logDAO =
        new ActivityLogDAO();

    /* =========================
       GET — VIEW + EDIT MODE
       ========================= */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User currentUser = getUser(request, response);
        if (currentUser == null) return;

        // ✅ FIXED — includes superadmin
        boolean adminAccess = isAdmin(currentUser);

        String action = request.getParameter("action");

        // ── EDIT MODE — admin/superadmin only ──
        if ("edit".equals(action) || "update".equals(action)) {
            if (!adminAccess) {
                response.sendRedirect(
                    request.getContextPath() + "/categories");
                return;
            }

            try {
                int id = Integer.parseInt(
                    request.getParameter("id"));
                StudentCategory cat = categoryDAO.getById(id);
                if (cat != null) {
                    request.setAttribute("editCategory", cat);
                }
            } catch (NumberFormatException e) {
                response.sendRedirect(
                    request.getContextPath() + "/categories");
                return;
            }
        }

        // ── LOAD LIST ──
        List<StudentCategory> list =
            categoryDAO.getAllCategories();
        request.setAttribute("categoryList", list);
        request.setAttribute("isAdmin", adminAccess);

        // ── MESSAGES ──
        request.setAttribute("successMsg",
            request.getParameter("success"));
        request.setAttribute("errorMsg",
            request.getParameter("error"));

        request.getRequestDispatcher("/categories.jsp")
               .forward(request, response);
    }

    /* =========================
       POST — ADD / UPDATE / DELETE
       ========================= */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        User currentUser = getUser(request, response);
        if (currentUser == null) return;

        if (!isAdmin(currentUser)) {
            response.sendRedirect(
                request.getContextPath() + "/categories");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action) {
            case "add"            -> addCategory(
                                        request, response,
                                        currentUser);
            case "edit", "update" -> updateCategory(
                                        request, response,
                                        currentUser);
            case "delete"         -> deleteCategory(
                                        request, response,
                                        currentUser);
            default               -> response.sendRedirect(
                                        request.getContextPath()
                                        + "/categories");
        }
    }

    /* =========================
       ADD
       ========================= */
    private void addCategory(HttpServletRequest request,
                             HttpServletResponse response,
                             User user) throws IOException {

        String name = request.getParameter("name");

        if (name == null || name.trim().isEmpty()) {
            response.sendRedirect(
                request.getContextPath()
                + "/categories?error=Category+name+is+required.");
            return;
        }

        boolean saved = categoryDAO.addCategory(
            new StudentCategory(0, name.trim()));

        if (saved) {
            logDAO.log(
                user.getUserId(),
                user.getUsername(),
                "ADD",
                "Categories",
                "Added category: \"" + name.trim() + "\""
            );
            response.sendRedirect(
                request.getContextPath()
                + "/categories?success=Category+added+successfully.");
        } else {
            response.sendRedirect(
                request.getContextPath()
                + "/categories?error=Failed+to+add+category.");
        }
    }

    /* =========================
       UPDATE
       ========================= */
    private void updateCategory(HttpServletRequest request,
                                HttpServletResponse response,
                                User user) throws IOException {

        String name = request.getParameter("name");

        if (name == null || name.trim().isEmpty()) {
            response.sendRedirect(
                request.getContextPath()
                + "/categories?error=Category+name+is+required.");
            return;
        }

        try {
            int id = Integer.parseInt(
                request.getParameter("id"));

            StudentCategory old = categoryDAO.getById(id);
            String oldName = (old != null)
                ? old.getCategoryName() : "Unknown";

            boolean updated =
                categoryDAO.updateCategory(id, name.trim());

            if (updated) {
                logDAO.log(
                    user.getUserId(),
                    user.getUsername(),
                    "UPDATE",
                    "Categories",
                    "Updated category: \""
                        + oldName + "\" → \""
                        + name.trim() + "\""
                );
                response.sendRedirect(
                    request.getContextPath()
                    + "/categories?success="
                    + "Category+updated+successfully.");
            } else {
                response.sendRedirect(
                    request.getContextPath()
                    + "/categories?error="
                    + "Failed+to+update+category.");
            }

        } catch (NumberFormatException e) {
            response.sendRedirect(
                request.getContextPath()
                + "/categories?error=Invalid+category+ID.");
        }
    }

    /* =========================
       DELETE
       ========================= */
    private void deleteCategory(HttpServletRequest request,
                                HttpServletResponse response,
                                User user) throws IOException {

        try {
            int id = Integer.parseInt(
                request.getParameter("id"));

            StudentCategory cat = categoryDAO.getById(id);
            String catName = (cat != null)
                ? cat.getCategoryName() : "Unknown";

            boolean deleted = categoryDAO.deleteCategory(id);

            if (deleted) {
                logDAO.log(
                    user.getUserId(),
                    user.getUsername(),
                    "DELETE",
                    "Categories",
                    "Deleted category: \""
                        + catName + "\""
                        + " (ID: " + id + ")"
                );
                response.sendRedirect(
                    request.getContextPath()
                    + "/categories?success="
                    + "Category+deleted+successfully.");
            } else {
                response.sendRedirect(
                    request.getContextPath()
                    + "/categories?error="
                    + "Failed+to+delete+category.");
            }

        } catch (NumberFormatException e) {
            response.sendRedirect(
                request.getContextPath()
                + "/categories?error=Invalid+category+ID.");
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

    // ✅ FIXED — superadmin + admin both allowed
    private boolean isAdmin(User user) {
        if (user.getRole() == null) return false;
        String role = user.getRole().toLowerCase();
        return role.equals("admin")
            || role.equals("superadmin");
    }
}