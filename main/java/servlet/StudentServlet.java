package servlet;

import java.io.IOException;
import java.sql.Date;
import java.util.List;
import java.util.Map;

import dao.ActivityLogDAO;
import dao.RequirementDAO;
import dao.StudentCategoryDAO;
import dao.StudentDAO;
import dao.UploadDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.RequirementType;
import model.Student;
import model.StudentCategory;
import model.User;
import util.EmailUtil;

@WebServlet("/students")
public class StudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /* =========================
       DAOs
       ========================= */
    private final StudentDAO studentDAO
            = new StudentDAO();
    private final StudentCategoryDAO categoryDAO
            = new StudentCategoryDAO();
    private final UploadDAO uploadDAO
            = new UploadDAO();
    private final RequirementDAO requirementDAO
            = new RequirementDAO();
    private final ActivityLogDAO logDAO
            = new ActivityLogDAO();

    /* =========================
       ✅ LOCAL IP CONFIGURATION
       Same as other servlets
       ========================= */
    private static final String LOCAL_IP
            = "192.168.95.174";
    private static final String LOCAL_PORT
            = "8443";

    /* =========================
       DISPLAY STUDENTS
       ========================= */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // ===== AUTH =====
        HttpSession session
                = request.getSession(false);
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

        // ===== DELETE HANDLER =====
        String action
                = request.getParameter("action");
        if ("delete".equals(action)) {
            handleDelete(request, response);
            return;
        }

        // ===== PARAMETERS =====
        String search
                = request.getParameter("search");

        String categoryParam
                = request.getParameter("categoryId");
        Integer categoryId = null;
        if (categoryParam != null
                && !categoryParam.trim().isEmpty()) {
            try {
                categoryId = Integer.parseInt(
                        categoryParam.trim());
            } catch (Exception e) {
                categoryId = null;
            }
        }

        // ===== PAGINATION DEFAULTS =====
        int pageSize = 10;
        int currentPage = 1;

        try {
            String ps
                    = request.getParameter("pageSize");
            if (ps != null
                    && !ps.trim().isEmpty()) {
                int val = Integer.parseInt(
                        ps.trim());
                if (val == 10 || val == 25
                        || val == 50
                        || val == 100) {
                    pageSize = val;
                }
            }

            String p
                    = request.getParameter("page");
            if (p != null
                    && !p.trim().isEmpty()) {
                currentPage = Math.max(1,
                        Integer.parseInt(p.trim()));
            }

        } catch (Exception ignored) {
        }

        // ===== DETERMINE FILTER MODE =====
        boolean hasSearch = (search != null
                && !search.trim().isEmpty());
        boolean hasCategory = (categoryId != null);
        boolean isFiltering
                = hasSearch || hasCategory;

        // ===== COUNT TOTAL =====
        int totalStudents;
        if (isFiltering) {
            totalStudents
                    = studentDAO.countSearchStudents(
                            search, categoryId);
        } else {
            totalStudents
                    = studentDAO.getTotalStudentCount();
        }

        // ===== PAGINATION CALC =====
        int totalPages = (int) Math.ceil(
                (double) totalStudents / pageSize);
        if (totalPages == 0) {
            totalPages = 1;
        }
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        int offset = (currentPage - 1) * pageSize;
        int startRow = totalStudents == 0
                ? 0 : offset + 1;
        int endRow = Math.min(
                offset + pageSize, totalStudents);

        // ===== FETCH DATA =====
        List<Student> students;
        if (isFiltering) {
            students = studentDAO.searchStudents(
                    search, categoryId,
                    currentPage, pageSize);
        } else {
            students
                    = studentDAO.getStudentsPaginated(
                            currentPage, pageSize);
        }

        // ===== LOAD CATEGORIES =====
        List<StudentCategory> categories
                = categoryDAO.getAllCategories();

        // ===== SET ATTRIBUTES =====
        request.setAttribute(
                "studentList", students);
        request.setAttribute(
                "categoryList", categories);
        request.setAttribute(
                "pageSize", pageSize);
        request.setAttribute(
                "currentPage", currentPage);
        request.setAttribute(
                "totalPages", totalPages);
        request.setAttribute(
                "startRow", startRow);
        request.setAttribute(
                "endRow", endRow);
        request.setAttribute(
                "totalStudents", totalStudents);

        Map<Integer, Integer> uploadedCountMap
                = uploadDAO.countUploadsPerStudent();
        request.setAttribute(
                "uploadedCountMap", uploadedCountMap);

        int totalRequirements
                = requirementDAO.getAllRequirements()
                        .size();
        request.setAttribute(
                "totalRequirements", totalRequirements);

        request.getRequestDispatcher(
                "/students.jsp")
                .forward(request, response);
    }

    /* =========================
       ADD / UPDATE
       ========================= */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session
                = request.getSession(false);
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

        String action
                = request.getParameter("action");

        if ("update".equals(action)) {
            handleUpdate(
                    request, response, currentUser);

        } else if ("save".equals(action)) {
            handleSave(
                    request, response, currentUser);

        } else {
            response.sendRedirect(
                    request.getContextPath()
                    + "/students?error=Invalid+action");
        }
    }

    /* =========================
       SAVE
       ========================= */
    private void handleSave(
            HttpServletRequest request,
            HttpServletResponse response,
            User user) throws IOException {

        try {
            String name
                    = request.getParameter("studentName");
            String email
                    = request.getParameter("email");
            String categoryStr
                    = request.getParameter("categoryId");

            /* =====================
               VALIDATION
               ===================== */
            if (name == null
                    || name.trim().isEmpty()
                    || email == null
                    || email.trim().isEmpty()
                    || categoryStr == null
                    || categoryStr.trim()
                            .isEmpty()) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/students?error="
                        + "Missing+required+fields");
                return;
            }

            /* =====================
               BIRTHDATE
               ===================== */
            Date birthDate = null;
            String birthStr
                    = request.getParameter("birthDate");
            if (birthStr != null
                    && !birthStr.trim().isEmpty()) {
                birthDate
                        = Date.valueOf(birthStr.trim());
            }

            /* =====================
               BUILD STUDENT
               ===================== */
            int categoryId = Integer.parseInt(
                    categoryStr.trim());

            Student s = new Student(
                    0, user.getUserId(),
                    name.trim(), birthDate,
                    email.trim(), categoryId,
                    request.getParameter("remarks"),
                    request.getParameter("phone")
            );

            /* =====================
               SAVE
               ===================== */
            boolean saved
                    = studentDAO.createStudent(s);

            if (saved) {

                /* ===================
                   ✅ LOG ACTIVITY
                   =================== */
                logDAO.log(
                        user.getUserId(),
                        user.getUsername(),
                        "ADD",
                        "Students",
                        "Added new student: \""
                        + name.trim() + "\""
                        + " | Email: "
                        + email.trim());

                /* ===================
                   ✅ WELCOME EMAIL
                   Notify student on
                   record creation
                   =================== */
                try {
                    if (email != null
                            && !email.trim()
                                    .isEmpty()) {

                        // Category name
                        String catName = "—";
                        StudentCategory cat
                                = categoryDAO.getById(
                                        categoryId);
                        if (cat != null) {
                            catName
                                    = cat.getCategoryName();
                        }

                        // Requirement names
                        List<RequirementType> reqs
                                = requirementDAO
                                        .getAllRequirements();
                        List<String> reqNames
                                = new java.util.ArrayList<>();
                        for (RequirementType r
                                : reqs) {
                            reqNames.add(
                                    r.getRequirementName());
                        }

                        EmailUtil.sendStudentWelcome(
                                email.trim(),
                                name.trim(),
                                catName,
                                reqNames,
                                "https://"
                                + LOCAL_IP
                                + ":"
                                + LOCAL_PORT
                                + request.getContextPath()
                                + "/uploads?studentId="
                                + s.getStudentId());
                    }
                } catch (Exception emailEx) {
                    System.err.println(
                            "[EMAIL] ⚠️ Welcome "
                            + "email failed: "
                            + emailEx.getMessage());
                }

                response.sendRedirect(
                        request.getContextPath()
                        + "/students?success="
                        + "Student+added+successfully.");

            } else {
                response.sendRedirect(
                        request.getContextPath()
                        + "/students?error="
                        + "Failed+to+save+student.");
            }

        } catch (Exception e) {

            boolean isDuplicate
                    = (e.getCause() instanceof java.sql.SQLIntegrityConstraintViolationException)
                    || (e.getMessage() != null
                    && e.getMessage().toLowerCase()
                            .contains("duplicate entry"));

            boolean isBadDate
                    = e instanceof IllegalArgumentException;

            if (isDuplicate) {
                response.sendRedirect(
                        request.getContextPath()
                        + "/students?error="
                        + "A+student+with+the+same+"
                        + "name+and+birthdate+"
                        + "already+exists.");

            } else if (isBadDate) {
                response.sendRedirect(
                        request.getContextPath()
                        + "/students?error="
                        + "Invalid+date+format.+"
                        + "Please+use+YYYY-MM-DD.");

            } else {
                e.printStackTrace();
                response.sendRedirect(
                        request.getContextPath()
                        + "/students?error="
                        + "Failed+to+save+student.+"
                        + "Please+try+again.");
            }
        }
    }

    /* =========================
       UPDATE
       ========================= */
    private void handleUpdate(
            HttpServletRequest request,
            HttpServletResponse response,
            User user) throws IOException {

        try {
            int studentId = Integer.parseInt(
                    request.getParameter("studentId"));
            String name
                    = request.getParameter("studentName");
            String email
                    = request.getParameter("email");
            int categoryId = Integer.parseInt(
                    request.getParameter("categoryId"));

            Date birthDate = null;
            String birthStr
                    = request.getParameter("birthDate");
            if (birthStr != null
                    && !birthStr.trim().isEmpty()) {
                birthDate
                        = Date.valueOf(birthStr.trim());
            }

            /* =====================
               ✅ GET OLD DATA FIRST
               For email comparison
               ===================== */
            Student oldStudent
                    = studentDAO.getStudentById(
                            studentId);

            /* =====================
               BUILD UPDATED STUDENT
               ===================== */
            Student s = new Student(
                    studentId, user.getUserId(),
                    name.trim(), birthDate,
                    email.trim(), categoryId,
                    request.getParameter("remarks"),
                    request.getParameter("phone")
            );

            boolean updated
                    = studentDAO.updateStudent(s);

            if (updated) {

                /* ===================
                   ✅ LOG ACTIVITY
                   =================== */
                logDAO.log(
                        user.getUserId(),
                        user.getUsername(),
                        "UPDATE",
                        "Students",
                        "Updated student: \""
                        + name.trim() + "\""
                        + " (ID: " + studentId + ")"
                        + " | Email: "
                        + email.trim());

                /* ===================
                   ✅ EMAIL NOTIFICATION
                   Only if category or
                   email changed
                   =================== */
                try {
                    if (oldStudent != null
                            && email != null
                            && !email.trim()
                                    .isEmpty()) {

                        boolean categoryChanged
                                = oldStudent.getCategoryId()
                                != categoryId;

                        boolean emailChanged
                                = oldStudent.getEmail()
                                != null
                                && !oldStudent.getEmail()
                                        .equalsIgnoreCase(
                                                email.trim());

                        // ✅ Category changed
                        if (categoryChanged) {

                            String newCatName = "—";
                            StudentCategory newCat
                                    = categoryDAO.getById(
                                            categoryId);
                            if (newCat != null) {
                                newCatName
                                        = newCat
                                                .getCategoryName();
                            }

                            String oldCatName = "—";
                            StudentCategory oldCat
                                    = categoryDAO.getById(
                                            oldStudent
                                                    .getCategoryId());
                            if (oldCat != null) {
                                oldCatName
                                        = oldCat
                                                .getCategoryName();
                            }

                            EmailUtil
                                    .sendCategoryUpdated(
                                            email.trim(),
                                            name.trim(),
                                            oldCatName,
                                            newCatName);
                        }

                        // ✅ Email changed
                        if (emailChanged) {
                            EmailUtil
                                    .sendEmailChanged(
                                            oldStudent
                                                    .getEmail(),
                                            name.trim(),
                                            email.trim());
                        }
                    }

                } catch (Exception emailEx) {
                    System.err.println(
                            "[EMAIL] ⚠️ Update "
                            + "notify: "
                            + emailEx.getMessage());
                }
            }

            response.sendRedirect(
                    "students?success=updated");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(
                    "students?error=Update+failed");
        }
    }

    /* =========================
       DELETE
       ========================= */
    private void handleDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        try {
            HttpSession session
                    = request.getSession(false);
            User user = (session != null)
                    ? (User) session.getAttribute(
                            "currentUser")
                    : null;

            int studentId = Integer.parseInt(
                    request.getParameter("studentId"));

            Student existing
                    = studentDAO.getStudentById(studentId);
            String studentName
                    = (existing != null)
                            ? existing.getStudentName()
                            : "Unknown";

            boolean deleted
                    = studentDAO.deleteStudent(studentId);

            if (deleted && user != null) {
                logDAO.log(
                        user.getUserId(),
                        user.getUsername(),
                        "DELETE",
                        "Students",
                        "Deleted student: \""
                        + studentName + "\""
                        + " (ID: " + studentId
                        + ")");
            }

            response.sendRedirect(
                    "students?success=deleted");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(
                    "students?error=Delete+failed");
        }
    }
}
