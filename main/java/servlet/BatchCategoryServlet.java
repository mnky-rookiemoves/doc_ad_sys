package servlet;

import java.io.IOException;

import dao.ActivityLogDAO;
import dao.StudentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet("/batch-category")
public class BatchCategoryServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final StudentDAO     studentDAO =
        new StudentDAO();
    private final ActivityLogDAO logDAO     =
        new ActivityLogDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /* =========================
           AUTH
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

        /* =========================
           GET PARAMS
           ========================= */
        String idsParam =
            request.getParameter("ids");
        String categoryIdParam =
            request.getParameter("categoryId");

        if (idsParam == null
                || idsParam.trim().isEmpty()
                || categoryIdParam == null
                || categoryIdParam.trim().isEmpty()) {
            response.sendRedirect(
                request.getContextPath()
                + "/students?error="
                + "Invalid+batch+update+request.");
            return;
        }

        try {
            int categoryId =
                Integer.parseInt(
                    categoryIdParam.trim());

            String[] idArray =
                idsParam.split(",");

            int successCount = 0;

            /* =========================
               UPDATE EACH STUDENT
               ========================= */
            for (String idStr : idArray) {
                try {
                    int studentId =
                        Integer.parseInt(
                            idStr.trim());

                    boolean updated =
                        studentDAO
                            .updateCategory(
                                studentId,
                                categoryId);

                    if (updated) successCount++;

                } catch (NumberFormatException e) {
                    // skip bad IDs
                }
            }

            /* =========================
               LOG ACTIVITY
               ========================= */
            logDAO.log(
                currentUser.getUserId(),
                currentUser.getUsername(),
                "UPDATE",
                "Students",
                "Batch category update — "
                + successCount
                + " student(s) updated "
                + "to category ID: "
                + categoryId
            );

            /* =========================
               REDIRECT WITH SUCCESS
               ========================= */
            response.sendRedirect(
                request.getContextPath()
                + "/students?success="
                + successCount
                + "+student(s)+category+"
                + "updated+successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(
                request.getContextPath()
                + "/students?error="
                + "Batch+category+update+failed.");
        }
    }
}