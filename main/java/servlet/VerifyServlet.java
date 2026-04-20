package servlet;

import java.io.IOException;

import dao.PrintLogDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.PrintLog;

@WebServlet("/verify")
public class VerifyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final PrintLogDAO printLogDAO =
        new PrintLogDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String refNo =
            request.getParameter("ref");

        PrintLog log = null;

        if (refNo != null
                && !refNo.trim().isEmpty()) {
            log = printLogDAO
                .findByRefNo(refNo.trim());
        }

        request.setAttribute("refNo",   refNo);
        request.setAttribute("printLog", log);

        request.getRequestDispatcher("/verify.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}