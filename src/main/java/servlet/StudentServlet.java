package servlet;

import java.io.IOException;

import dao.StudentCategoryDAO;
import dao.StudentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Student;
import model.User;

@WebServlet("/students")
public class StudentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        
        if ("delete".equals(action)) {
            String studentIdStr = request.getParameter("studentId");
            if (studentIdStr != null && !studentIdStr.isEmpty()) {
                try {
                    int studentId = Integer.parseInt(studentIdStr);
                    
                    if ("admin".equalsIgnoreCase(currentUser.getRole())) {
                        StudentDAO sdao = new StudentDAO();
                        boolean deleted = sdao.deleteStudent(studentId);
                        
                        if (deleted) {
                            request.setAttribute("success", "Student deleted successfully!");
                            System.out.println("✓ Student ID " + studentId + " deleted by " + currentUser.getUsername());
                        } else {
                            request.setAttribute("error", "Failed to delete student.");
                        }
                    } else {
                        request.setAttribute("error", "Permission denied.");
                    }
                } catch (NumberFormatException e) {
                    request.setAttribute("error", "Invalid student ID.");
                }
            }
        }

        try {
            StudentDAO sdao = new StudentDAO();
            var studentList = sdao.getAllStudents();
            
            System.out.println("DEBUG: ===== LOADING STUDENTS =====");
            System.out.println("DEBUG: studentList size = " + (studentList != null ? studentList.size() : "NULL"));
            if (studentList != null && !studentList.isEmpty()) {
                System.out.println("DEBUG: First student: " + studentList.get(0).getStudentName());
            }
            
            request.setAttribute("studentList", studentList);
            StudentCategoryDAO cdao = new StudentCategoryDAO();
            request.setAttribute("categoryList", cdao.getAllCategories());
            
        } catch (Exception e) {
            System.err.println("✗ ERROR LOADING DATA: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Failed to load student data: " + e.getMessage());
        }

        request.getRequestDispatcher("/students.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        User currentUser = (User) request.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if (!"admin".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/students?error=Permission+denied");
            return;
        }

        String studentName = request.getParameter("studentName");
        String email = request.getParameter("email");

        if (studentName == null || studentName.trim().isEmpty() || 
            email == null || email.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/students?error=Name+and+email+required");
            return;
        }

        String birthDate = request.getParameter("birthDate");
        String categoryIdStr = request.getParameter("categoryId");
        Integer categoryId = null;
        
        if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
            try {
                categoryId = Integer.parseInt(categoryIdStr);
            } catch (NumberFormatException e) {
                System.err.println("✗ Invalid category ID: " + categoryIdStr);
            }
        }

        Student student = new Student();
        student.setUserId(currentUser.getUserId());
        student.setStudentName(studentName.trim());
        student.setEmail(email.trim());
        student.setBirthDate(birthDate);
        student.setCategoryId(categoryId);

        try {
            StudentDAO sdao = new StudentDAO();
            boolean success = sdao.addStudent(student);
            
            if (success) {
                System.out.println("✓ Student added: " + studentName);
                response.sendRedirect(request.getContextPath() + "/students?success=Student+added+successfully");
            } else {
                System.err.println("✗ Failed to add student: " + studentName);
                response.sendRedirect(request.getContextPath() + "/students?error=Failed+to+add+student");
            }
        } catch (Exception e) {
            System.err.println("✗ EXCEPTION adding student: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/students?error=Error+adding+student");
        }
    }
}