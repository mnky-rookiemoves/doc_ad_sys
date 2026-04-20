package servlet;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import dao.ActivityLogDAO;
import dao.RequirementDAO;
import dao.StudentDAO;
import dao.UploadDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import kyrie.AppConfig;
import model.RequirementType;
import model.Student;
import model.Upload;
import model.User;
import util.EmailUtil;

@WebServlet("/upload-file")
@MultipartConfig(
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 12 * 1024 * 1024
)
public class UploadFileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /* =========================
       DAOs
       ========================= */
    private final ActivityLogDAO logDAO
            = new ActivityLogDAO();
    private final UploadDAO uploadDAO
            = new UploadDAO();
    private final StudentDAO studentDAO
            = new StudentDAO();
    private final RequirementDAO requirementDAO
            = new RequirementDAO();

    /* =========================
       VALIDATION CONSTANTS
       ========================= */
    private static final java.util.Set<String> ALLOWED_EXTENSIONS
            = new java.util.HashSet<>(
                    java.util.Arrays.asList(
                            "jpg", "jpeg",
                            "png", "pdf"));

    private static final java.util.Set<String> ALLOWED_MIME_TYPES
            = new java.util.HashSet<>(
                    java.util.Arrays.asList(
                            "image/jpeg",
                            "image/png",
                            "application/pdf"));

    private static final Pattern VALID_FILENAME
            = Pattern.compile(
                    "^[a-zA-Z0-9 _.\\-]+$");

    // ✅ LOCAL_IP and LOCAL_PORT REMOVED!
    // AppConfig.getBaseUrl(request) handles
    // URLs dynamically!

    /* =========================
       ADMIN EMAIL
       ========================= */
    private static final String ADMIN_EMAIL
            = "vandam.system@gmail.com";

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /* =========================
           AUTH CHECK
           ========================= */
        HttpSession session
                = request.getSession(false);
        User user = (session != null)
                ? (User) session.getAttribute(
                        "currentUser")
                : null;

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        /* =========================
           PARAM VALIDATION
           ========================= */
        String studentIdParam
                = request.getParameter("studentId");
        String requirementIdParam
                = request.getParameter("requirementId");

        String redirectBase
                = "uploads?studentId="
                + studentIdParam;

        if (studentIdParam == null
                || studentIdParam.trim().isEmpty()
                || requirementIdParam == null
                || requirementIdParam.trim()
                        .isEmpty()) {
            response.sendRedirect(
                    "students?error="
                    + "Missing+parameters");
            return;
        }

        int studentId;
        int requirementId;

        try {
            studentId = Integer.parseInt(
                    studentIdParam.trim());
            requirementId = Integer.parseInt(
                    requirementIdParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(
                    "students?error="
                    + "Invalid+parameters");
            return;
        }

        /* =========================
           GET FILE PART
           ========================= */
        Part filePart;
        try {
            filePart = request.getPart("file");
        } catch (Exception e) {
            response.sendRedirect(redirectBase
                    + "&error=File+upload+failed."
                    + "+Please+try+again.");
            return;
        }

        if (filePart == null
                || filePart.getSize() == 0) {
            response.sendRedirect(redirectBase
                    + "&error=No+file+selected."
                    + "+Please+choose+a+file.");
            return;
        }

        /* =========================
           EXTRACT METADATA
           ========================= */
        String originalFileName
                = extractFileName(filePart);

        if (originalFileName == null
                || originalFileName.trim()
                        .isEmpty()) {
            response.sendRedirect(redirectBase
                    + "&error=Could+not+read+"
                    + "file+name.");
            return;
        }

        String mimeType
                = filePart.getContentType();
        long fileSize = filePart.getSize();

        String extension = "";
        int dotIndex = originalFileName
                .lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFileName
                    .substring(dotIndex + 1)
                    .toLowerCase();
        }

        String cleanName = originalFileName;
        int lastSlash = Math.max(
                originalFileName.lastIndexOf('/'),
                originalFileName.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            cleanName = originalFileName
                    .substring(lastSlash + 1);
        }

        /* =========================
           VALIDATION
           ========================= */
        if (!ALLOWED_EXTENSIONS
                .contains(extension)) {
            response.sendRedirect(redirectBase
                    + "&error=Invalid+file+type."
                    + "+Only+JPG,+JPEG,+PNG,"
                    + "+and+PDF+are+allowed.");
            return;
        }

        if (mimeType == null
                || !ALLOWED_MIME_TYPES
                        .contains(mimeType)) {
            response.sendRedirect(redirectBase
                    + "&error=Invalid+file+format."
                    + "+Only+JPG,+JPEG,+PNG,"
                    + "+and+PDF+are+allowed.");
            return;
        }

        String nameWithoutExt = dotIndex >= 0
                ? cleanName.substring(0, dotIndex)
                : cleanName;

        if (!VALID_FILENAME.matcher(
                nameWithoutExt).matches()) {
            response.sendRedirect(redirectBase
                    + "&error=File+name+contains+"
                    + "invalid+characters.");
            return;
        }

        if (fileSize > 10 * 1024 * 1024) {
            response.sendRedirect(redirectBase
                    + "&error=File+too+large."
                    + "+Maximum+size+is+10MB.");
            return;
        }

        /* =========================
           READ FILE BYTES
           ========================= */
        byte[] fileBytes;
        try (InputStream is
                = filePart.getInputStream()) {
            fileBytes = is.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            response.sendRedirect(redirectBase
                    + "&error=Failed+to+read+"
                    + "file+content.");
            return;
        }

        /* =========================
           SAVE OR UPDATE IN DB
           ========================= */
        try {
            boolean exists
                    = uploadDAO.existsUpload(
                            studentId,
                            requirementId);
            boolean saved;

            if (exists) {
                saved = uploadDAO
                        .updateFileByStudentAndRequirement(
                                studentId,
                                requirementId,
                                cleanName,
                                fileBytes,
                                mimeType,
                                fileSize,
                                user.getUserId());
            } else {
                Upload upload = new Upload();
                upload.setStudentId(
                        String.valueOf(studentId));
                upload.setRequirementId(
                        requirementId);
                upload.setFileName(cleanName);
                upload.setFileContent(fileBytes);
                upload.setMimeType(mimeType);
                upload.setFileSize(fileSize);
                saved = uploadDAO.save(upload);
            }

            if (saved) {

                /* =========================
                   LOG ACTIVITY
                   ========================= */
                logDAO.log(
                        user.getUserId(),
                        user.getUsername(),
                        exists ? "REPLACE"
                                : "UPLOAD",
                        "Uploads",
                        (exists ? "Replaced"
                                : "Uploaded")
                        + " file \""
                        + cleanName + "\""
                        + " for Student ID: "
                        + studentId
                        + " | Requirement ID: "
                        + requirementId);

                /* =========================
                   EMAIL NOTIFICATIONS
                   ========================= */
                try {
                    Student student
                            = studentDAO
                                    .getStudentById(
                                            studentId);

                    List<RequirementType> allRequirements
                            = requirementDAO
                                    .getAllRequirements();

                    String requirementName
                            = "Requirement #"
                            + requirementId;
                    for (RequirementType r
                            : allRequirements) {
                        if (r.getRequirementId()
                                == requirementId) {
                            requirementName
                                    = r.getRequirementName();
                            break;
                        }
                    }

                    SimpleDateFormat dtf
                            = new SimpleDateFormat(
                                    "MMM dd, yyyy"
                                    + " hh:mm a");
                    String uploadedAt
                            = dtf.format(new Date());

                    /* =====================
                       EMAIL 1
                       Notify admin
                       ===================== */
                    EmailUtil.sendFileUploaded(
                            ADMIN_EMAIL,
                            student != null
                                    ? student
                                            .getStudentName()
                                    : "Student #"
                                    + studentId,
                            requirementName,
                            user.getUsername(),
                            uploadedAt);

                    /* =====================
                       EMAIL 2
                       Notify student if
                       just completed
                       ✅ FIXED — Uses
                       AppConfig!
                       ===================== */
                    if (student != null
                            && student.getEmail()
                            != null
                            && !student.getEmail()
                                    .trim()
                                    .isEmpty()) {

                        List<Upload> currentUploads
                                = uploadDAO
                                        .getUploadsByStudent(
                                                studentId);

                        int submittedCount = 0;
                        for (RequirementType r
                                : allRequirements) {
                            for (Upload u
                                    : currentUploads) {
                                if (u.getRequirementId()
                                        == r.getRequirementId()
                                        && u.getFileName()
                                        != null) {
                                    submittedCount++;
                                    break;
                                }
                            }
                        }

                        int total
                                = allRequirements
                                        .size();
                        boolean isComplete
                                = total > 0
                                && submittedCount
                                >= total;

                        if (isComplete) {
                            // ✅ FIXED — Uses
                            // AppConfig, no
                            // double context path!
                            String verifyUrl
                                    = AppConfig
                                            .getBaseUrl(
                                                    request)
                                    + "verify";

                            EmailUtil
                                    .sendSubmissionComplete(
                                            student
                                                    .getEmail(),
                                            student
                                                    .getStudentName(),
                                            "See verify page",
                                            submittedCount,
                                            total,
                                            verifyUrl);
                        }
                    }

                } catch (Exception emailEx) {
                    System.err.println(
                            "[EMAIL] ⚠️ Failed: "
                            + emailEx.getMessage());
                }

                response.sendRedirect(
                        redirectBase
                        + "&success=File+uploaded"
                        + "+successfully.");

            } else {
                response.sendRedirect(
                        redirectBase
                        + "&error=Failed+to+save"
                        + "+file.+Please+try+again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(redirectBase
                    + "&error=An+unexpected+"
                    + "error+occurred.");
        }
    }

    /* =========================
       HELPER — Extract filename
       ========================= */
    private String extractFileName(Part part) {
        String contentDisp
                = part.getHeader(
                        "content-disposition");
        if (contentDisp == null) {
            return null;
        }

        for (String token
                : contentDisp.split(";")) {
            token = token.trim();
            if (token.startsWith("filename")) {
                return token.substring(
                        token.indexOf('=') + 1)
                        .trim()
                        .replace("\"", "");
            }
        }
        return null;
    }
}
