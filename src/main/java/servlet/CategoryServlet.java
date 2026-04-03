package servlet;

import dao.StudentCategoryDAO;
import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CategoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private boolean isAdmin(User user) {
        return user != null && user.getRole() != null
                && "admin".equalsIgnoreCase(user.getRole());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if (!isAdmin(currentUser)) {
            response.sendRedirect(request.getContextPath() + "/home.jsp");
            return;
        }

        StudentCategoryDAO dao = new StudentCategoryDAO();
        request.setAttribute("categoryList", dao.getAllCategories());
        request.getRequestDispatcher("/categories.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null || !isAdmin(currentUser)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        String categoryName = request.getParameter("categoryName");

        StudentCategoryDAO dao = new StudentCategoryDAO();

        if ("add".equalsIgnoreCase(action)) {
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                dao.addCategory(categoryName.trim());
            }
        }

        response.sendRedirect(request.getContextPath() + "/categories");
    }
}