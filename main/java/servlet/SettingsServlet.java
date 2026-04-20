package servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet("/settings")
public class SettingsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /* =========================
       GET — Show settings page
       ========================= */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

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

        // ✅ Get current theme from session
        String theme =
            (String) session.getAttribute("theme");
        if (theme == null) theme = "normal";

        request.setAttribute("currentTheme", theme);

        request.getRequestDispatcher("/settings.jsp")
               .forward(request, response);
    }

    /* =========================
       POST — Save theme
       ========================= */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

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

        // ✅ Validate and save theme
        String theme =
            request.getParameter("theme");

        if (theme == null
                || (!theme.equals("normal")
                    && !theme.equals("light")
                    && !theme.equals("dark"))) {
            theme = "normal";
        }

        session.setAttribute("theme", theme);

        response.sendRedirect(
            request.getContextPath()
            + "/settings?success=Theme+applied!");
    }
}