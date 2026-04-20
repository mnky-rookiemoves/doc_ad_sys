package servlet;

import java.io.IOException;

import com.google.gson.Gson;

import dao.StudentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Student;

public class StudentDetailsServlet extends HttpServlet {
    private StudentDAO studentDAO = new StudentDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String studentIdParam = request.getParameter("id");
        if (studentIdParam != null) {
            int studentId = Integer.parseInt(studentIdParam);
            Student student = studentDAO.getStudentById(studentId);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(student));
        }
    }
}
