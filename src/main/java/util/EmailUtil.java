package util;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import java.util.List;

public class EmailUtil {

    // ✅ Resend API Key from Railway
    private static final String API_KEY
            = System.getenv("RESEND_API_KEY") != null
            ? System.getenv("RESEND_API_KEY")
            : "";

    private static final String FROM_EMAIL
            = "onboarding@resend.dev";

    private static final String FROM_NAME
            = "VANDAM Document Admission System";

    /* =========================
       CORE SEND METHOD
       ✅ Resend HTTPS API
       ✅ Works on Railway!
       ✅ Background thread
       ========================= */
    public static void sendEmail(
            String toEmail,
            String subject,
            String htmlBody) {

        new Thread(() -> {
            try {
                Resend resend = new Resend(API_KEY);

                CreateEmailOptions params
                        = CreateEmailOptions.builder()
                                .from(FROM_NAME
                                        + " <" + FROM_EMAIL + ">")
                                .to(toEmail)
                                .subject(subject)
                                .html(htmlBody)
                                .build();

                CreateEmailResponse response
                        = resend.emails().send(params);

                System.out.println(
                        "[EMAIL] ✅ Sent → "
                        + toEmail
                        + " | ID: "
                        + response.getId());

            } catch (Exception e) {
                System.err.println(
                        "[EMAIL] ⚠️ Failed → "
                        + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }


    /* =========================
       TEMPLATE 1
       ✅ STUDENT WELCOME
       ========================= */
    public static void sendStudentWelcome(
            String toEmail,
            String studentName,
            String categoryName,
            List<String> requirementNames,
            String requirementsUrl) {

        String subject
                = "🎓 Welcome to VANDAM — "
                + "Document Submission Guide";

        StringBuilder reqRows
                = new StringBuilder();
        int num = 1;
        for (String req : requirementNames) {
            reqRows.append(
                    String.format("""
                    <tr>
                        <td style="
                            padding:8px 10px;
                            border:1px solid #ddd;
                            text-align:center;
                            width:40px;
                            background:#f9f9f9;">
                            <b>%d</b>
                        </td>
                        <td style="
                            padding:8px 10px;
                            border:1px solid #ddd;">
                            %s
                        </td>
                        <td style="
                            padding:8px 10px;
                            border:1px solid #ddd;
                            color:#c62828;
                            font-weight:bold;
                            width:110px;">
                            Pending ⏳
                        </td>
                    </tr>
                    """, num++, req));
        }

        String body = String.format("""
            <div style="font-family:Arial,
                sans-serif;max-width:600px;
                margin:0 auto;border:1px
                solid #eee;border-radius:8px;
                overflow:hidden;">
                <div style="background:#6d0f0f;
                    padding:28px;
                    text-align:center;">
                    <div style="font-size:40px;
                        margin-bottom:8px;">
                        🎓
                    </div>
                    <h2 style="color:#fff;
                        margin:0;font-size:22px;">
                        Welcome to VANDAM!
                    </h2>
                    <p style="color:#ffcccc;
                        margin:6px 0 0 0;
                        font-size:13px;">
                        Document Admission System
                        — Sta. Mesa Campus (MN0)
                    </p>
                </div>
                <div style="padding:28px;
                    background:#f9f9f9;">
                    <p style="font-size:15px;
                        margin-bottom:6px;">
                        Dear <b>%s</b>,
                    </p>
                    <p style="font-size:13px;
                        color:#555;
                        margin-bottom:20px;">
                        Your admission record
                        has been successfully
                        created in the VANDAM
                        Document Admission System.
                    </p>
                    <table style="width:100%%;
                        border-collapse:collapse;
                        font-size:13px;
                        margin-bottom:24px;">
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                width:160px;
                                border:1px solid #ddd;">
                                Student Name
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;
                                font-weight:bold;">
                                %s
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                Category
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;">
                                %s
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                Campus
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;">
                                Sta. Mesa (MN0)
                            </td>
                        </tr>
                    </table>
                    <p style="font-size:14px;
                        font-weight:bold;
                        color:#6d0f0f;
                        margin-bottom:10px;">
                        📋 Required Documents
                        to Submit:
                    </p>
                    <p style="font-size:12px;
                        color:#777;
                        margin-bottom:12px;">
                        Please coordinate with
                        the registrar's office
                        to submit the following:
                    </p>
                    <table style="width:100%%;
                        border-collapse:collapse;
                        font-size:13px;
                        margin-bottom:24px;">
                        <thead>
                            <tr>
                                <th style="
                                    background:#6d0f0f;
                                    color:#fff;
                                    padding:8px 10px;
                                    border:1px solid #999;
                                    text-align:center;
                                    width:40px;">
                                    #
                                </th>
                                <th style="
                                    background:#6d0f0f;
                                    color:#fff;
                                    padding:8px 10px;
                                    border:1px solid #999;
                                    text-align:left;">
                                    Requirement
                                </th>
                                <th style="
                                    background:#6d0f0f;
                                    color:#fff;
                                    padding:8px 10px;
                                    border:1px solid #999;
                                    text-align:left;
                                    width:110px;">
                                    Status
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>
                    <div style="
                        background:#fff8e1;
                        border:1px solid #f9a825;
                        border-radius:4px;
                        padding:12px 16px;
                        font-size:12px;
                        color:#555;
                        margin-bottom:8px;">
                        ⚠️ <b>Note:</b> All
                        requirements must be
                        submitted to complete
                        your admission process.
                    </div>
                </div>
                <div style="padding:14px;
                    text-align:center;
                    font-size:11px;color:#999;
                    background:#fff;
                    border-top:1px solid #eee;">
                    VANDAM Document Admission
                    System &nbsp;|&nbsp;
                    Sta. Mesa Campus (MN0)
                </div>
            </div>
            """,
                studentName,
                studentName,
                categoryName,
                reqRows.toString());

        sendEmail(toEmail, subject, body);
    }

    /* =========================
       TEMPLATE 2
       ✅ FILE UPLOADED
       ========================= */
    public static void sendFileUploaded(
            String toEmail,
            String studentName,
            String requirementName,
            String uploadedBy,
            String uploadedAt) {

        String subject
                = "📂 Document Uploaded — VANDAM";

        String body = String.format("""
            <div style="font-family:Arial,
                sans-serif;max-width:600px;
                margin:0 auto;border:1px
                solid #eee;border-radius:8px;
                overflow:hidden;">
                <div style="background:#6d0f0f;
                    padding:24px;
                    text-align:center;">
                    <h2 style="color:#fff;
                        margin:0;font-size:20px;">
                        📂 Document Uploaded
                    </h2>
                </div>
                <div style="padding:24px;
                    background:#f9f9f9;">
                    <p style="font-size:14px;
                        margin-bottom:16px;">
                        A new document has been
                        uploaded in VANDAM.
                    </p>
                    <table style="width:100%%;
                        border-collapse:collapse;
                        font-size:13px;">
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                width:160px;
                                border:1px solid #ddd;">
                                Student
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;">
                                %s
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                Requirement
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;">
                                %s
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                Uploaded By
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;">
                                %s
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                Date
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;">
                                %s
                            </td>
                        </tr>
                    </table>
                </div>
                <div style="padding:14px;
                    text-align:center;
                    font-size:11px;color:#999;
                    background:#fff;
                    border-top:1px solid #eee;">
                    VANDAM Document Admission
                    System &nbsp;|&nbsp;
                    Sta. Mesa Campus (MN0)
                </div>
            </div>
            """,
                studentName,
                requirementName,
                uploadedBy,
                uploadedAt);

        sendEmail(toEmail, subject, body);
    }

    /* =========================
       TEMPLATE 3
       ✅ SUBMISSION COMPLETE
       ========================= */
    public static void sendSubmissionComplete(
            String toEmail,
            String studentName,
            String refNo,
            int submitted,
            int total,
            String verifyUrl) {

        String subject
                = "✅ Submission Complete — VANDAM";

        String body = String.format("""
            <div style="font-family:Arial,
                sans-serif;max-width:600px;
                margin:0 auto;border:1px
                solid #eee;border-radius:8px;
                overflow:hidden;">
                <div style="background:#6d0f0f;
                    padding:24px;
                    text-align:center;">
                    <h2 style="color:#fff;
                        margin:0;font-size:20px;">
                        ✅ Submission Complete
                    </h2>
                </div>
                <div style="padding:24px;
                    background:#f9f9f9;">
                    <p style="font-size:14px;">
                        Dear <b>%s</b>,
                    </p>
                    <p style="font-size:13px;
                        color:#555;
                        margin-bottom:20px;">
                        Your document submission
                        is now <b style="
                        color:#2e7d32;">
                        COMPLETE</b>.
                    </p>
                    <table style="width:100%%;
                        border-collapse:collapse;
                        font-size:13px;
                        margin-bottom:20px;">
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                width:160px;
                                border:1px solid #ddd;">
                                Reference No
                            </td>
                            <td style="padding:10px;
                                font-family:
                                'Courier New',monospace;
                                color:#6d0f0f;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                %s
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                Submitted
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;">
                                %d of %d requirements
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                Status
                            </td>
                            <td style="padding:10px;
                                color:#2e7d32;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                COMPLETED ✅
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                Campus
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;">
                                Sta. Mesa (MN0)
                            </td>
                        </tr>
                    </table>
                    <div style="text-align:center;">
                        <a href="%s"
                           style="
                               display:inline-block;
                               padding:12px 28px;
                               background:#6d0f0f;
                               color:#fff;
                               text-decoration:none;
                               border-radius:4px;
                               font-weight:bold;
                               font-size:13px;">
                            🔍 Verify Document
                        </a>
                    </div>
                </div>
                <div style="padding:14px;
                    text-align:center;
                    font-size:11px;color:#999;
                    background:#fff;
                    border-top:1px solid #eee;">
                    VANDAM Document Admission
                    System &nbsp;|&nbsp;
                    Sta. Mesa Campus (MN0)
                </div>
            </div>
            """,
                studentName,
                refNo,
                submitted, total,
                verifyUrl);

        sendEmail(toEmail, subject, body);
    }

    /* =========================
       TEMPLATE 4
       ✅ ACCOUNT CREATED
       ========================= */
    public static void sendAccountCreated(
            String toEmail,
            String username,
            String role,
            String loginUrl) {

        String subject
                = "👤 Account Created — VANDAM";

        String body = String.format("""
            <div style="font-family:Arial,
                sans-serif;max-width:600px;
                margin:0 auto;border:1px
                solid #eee;border-radius:8px;
                overflow:hidden;">
                <div style="background:#6d0f0f;
                    padding:24px;
                    text-align:center;">
                    <h2 style="color:#fff;
                        margin:0;font-size:20px;">
                        👤 Account Created
                    </h2>
                </div>
                <div style="padding:24px;
                    background:#f9f9f9;">
                    <p style="font-size:14px;
                        margin-bottom:16px;">
                        Your account has been
                        created in the VANDAM
                        Document Admission System.
                    </p>
                    <table style="width:100%%;
                        border-collapse:collapse;
                        font-size:13px;
                        margin-bottom:20px;">
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                width:140px;
                                border:1px solid #ddd;">
                                Username
                            </td>
                            <td style="padding:10px;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                %s
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                Role
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;">
                                %s
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                Campus
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;">
                                Sta. Mesa (MN0)
                            </td>
                        </tr>
                    </table>
                    <p style="color:#c62828;
                        font-size:13px;
                        margin-bottom:20px;">
                        ⚠️ Please change your
                        password after first login.
                    </p>
                    <div style="text-align:center;">
                        <a href="%s"
                           style="
                               display:inline-block;
                               padding:12px 28px;
                               background:#6d0f0f;
                               color:#fff;
                               text-decoration:none;
                               border-radius:4px;
                               font-weight:bold;
                               font-size:13px;">
                            🔐 Login Now
                        </a>
                    </div>
                </div>
                <div style="padding:14px;
                    text-align:center;
                    font-size:11px;color:#999;
                    background:#fff;
                    border-top:1px solid #eee;">
                    VANDAM Document Admission
                    System &nbsp;|&nbsp;
                    Sta. Mesa Campus (MN0)
                </div>
            </div>
            """,
                username,
                role,
                loginUrl);

        sendEmail(toEmail, subject, body);
    }

    /* =========================
       TEMPLATE 5
       ✅ PASSWORD CHANGED
       ========================= */
    public static void sendPasswordChanged(
            String toEmail,
            String username,
            String changedAt,
            String ipAddress) {

        String subject
                = "🔒 Password Changed — VANDAM";

        String body = String.format("""
            <div style="font-family:Arial,
                sans-serif;max-width:600px;
                margin:0 auto;border:1px
                solid #eee;border-radius:8px;
                overflow:hidden;">
                <div style="background:#6d0f0f;
                    padding:24px;
                    text-align:center;">
                    <h2 style="color:#fff;
                        margin:0;font-size:20px;">
                        🔒 Password Changed
                    </h2>
                </div>
                <div style="padding:24px;
                    background:#f9f9f9;">
                    <p style="font-size:14px;
                        margin-bottom:16px;">
                        Your password was changed
                        successfully.
                    </p>
                    <table style="width:100%%;
                        border-collapse:collapse;
                        font-size:13px;
                        margin-bottom:20px;">
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                width:140px;
                                border:1px solid #ddd;">
                                Username
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;">
                                %s
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                Changed At
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;">
                                %s
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                IP Address
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;">
                                %s
                            </td>
                        </tr>
                    </table>
                    <p style="color:#c62828;
                        font-size:13px;">
                        ⚠️ If you did not make
                        this change, contact your
                        administrator immediately.
                    </p>
                </div>
                <div style="padding:14px;
                    text-align:center;
                    font-size:11px;color:#999;
                    background:#fff;
                    border-top:1px solid #eee;">
                    VANDAM Document Admission
                    System &nbsp;|&nbsp;
                    Sta. Mesa Campus (MN0)
                </div>
            </div>
            """,
                username,
                changedAt,
                ipAddress);

        sendEmail(toEmail, subject, body);
    }

    /* =========================
       TEMPLATE 6
       ✅ CATEGORY UPDATED
       ========================= */
    public static void sendCategoryUpdated(
            String toEmail,
            String studentName,
            String oldCategory,
            String newCategory) {

        String subject
                = "📁 Category Updated — VANDAM";

        String body = String.format("""
            <div style="font-family:Arial,
                sans-serif;max-width:600px;
                margin:0 auto;border:1px
                solid #eee;border-radius:8px;
                overflow:hidden;">
                <div style="background:#6d0f0f;
                    padding:24px;
                    text-align:center;">
                    <h2 style="color:#fff;
                        margin:0;font-size:20px;">
                        📁 Category Updated
                    </h2>
                </div>
                <div style="padding:24px;
                    background:#f9f9f9;">
                    <p style="font-size:14px;">
                        Dear <b>%s</b>,
                    </p>
                    <p style="font-size:13px;
                        color:#555;
                        margin-bottom:20px;">
                        Your student category
                        has been updated in
                        VANDAM.
                    </p>
                    <table style="width:100%%;
                        border-collapse:collapse;
                        font-size:13px;
                        margin-bottom:20px;">
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                width:180px;
                                border:1px solid #ddd;">
                                Previous Category
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;
                                color:#c62828;">
                                %s
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                New Category
                            </td>
                            <td style="padding:10px;
                                border:1px solid #ddd;
                                color:#2e7d32;
                                font-weight:bold;">
                                %s ✅
                            </td>
                        </tr>
                    </table>
                    <p style="font-size:12px;
                        color:#777;">
                        If you have questions,
                        please contact the
                        registrar's office.
                    </p>
                </div>
                <div style="padding:14px;
                    text-align:center;
                    font-size:11px;color:#999;
                    background:#fff;
                    border-top:1px solid #eee;">
                    VANDAM Document Admission
                    System &nbsp;|&nbsp;
                    Sta. Mesa Campus (MN0)
                </div>
            </div>
            """,
                studentName,
                oldCategory,
                newCategory);

        sendEmail(toEmail, subject, body);
    }

    /* =========================
       TEMPLATE 7
       ✅ EMAIL CHANGED
       ========================= */
    public static void sendEmailChanged(
            String toOldEmail,
            String studentName,
            String newEmail) {

        String subject
                = "🔒 Email Address Updated — VANDAM";

        String body = String.format("""
            <div style="font-family:Arial,
                sans-serif;max-width:600px;
                margin:0 auto;border:1px
                solid #eee;border-radius:8px;
                overflow:hidden;">
                <div style="background:#6d0f0f;
                    padding:24px;
                    text-align:center;">
                    <h2 style="color:#fff;
                        margin:0;font-size:20px;">
                        🔒 Email Address Updated
                    </h2>
                </div>
                <div style="padding:24px;
                    background:#f9f9f9;">
                    <p style="font-size:14px;">
                        Dear <b>%s</b>,
                    </p>
                    <p style="font-size:13px;
                        color:#555;
                        margin-bottom:20px;">
                        Your registered email
                        address has been updated
                        in VANDAM.
                    </p>
                    <table style="width:100%%;
                        border-collapse:collapse;
                        font-size:13px;
                        margin-bottom:20px;">
                        <tr>
                            <td style="padding:10px;
                                background:#f0f0f0;
                                font-weight:bold;
                                width:160px;
                                border:1px solid #ddd;">
                                New Email
                            </td>
                            <td style="padding:10px;
                                font-weight:bold;
                                border:1px solid #ddd;">
                                %s
                            </td>
                        </tr>
                    </table>
                    <p style="color:#c62828;
                        font-size:13px;">
                        ⚠️ If you did not
                        request this change,
                        contact the registrar's
                        office immediately.
                    </p>
                </div>
                <div style="padding:14px;
                    text-align:center;
                    font-size:11px;color:#999;
                    background:#fff;
                    border-top:1px solid #eee;">
                    VANDAM Document Admission
                    System &nbsp;|&nbsp;
                    Sta. Mesa Campus (MN0)
                </div>
            </div>
            """,
                studentName,
                newEmail);

        sendEmail(toOldEmail, subject, body);
    }

    /* =========================
       TEMPLATE 8
       ✅ PASSWORD RESET
       ========================= */
    public static void sendPasswordReset(
            String toEmail,
            String username,
            String resetUrl) {

        String subject
                = "🔑 Password Reset — VANDAM";

        String body = String.format("""
            <div style="font-family:Arial,
                sans-serif;max-width:600px;
                margin:0 auto;border:1px
                solid #eee;border-radius:8px;
                overflow:hidden;">
                <div style="background:#6d0f0f;
                    padding:24px;
                    text-align:center;">
                    <h2 style="color:#fff;
                        margin:0;font-size:20px;">
                        🔑 Password Reset Request
                    </h2>
                </div>
                <div style="padding:24px;
                    background:#f9f9f9;">
                    <p style="font-size:14px;">
                        Dear <b>%s</b>,
                    </p>
                    <p style="font-size:13px;
                        color:#555;
                        margin-bottom:20px;">
                        We received a request to
                        reset your password for
                        the VANDAM Document
                        Admission System.
                    </p>
                    <div style="
                        background:#fff8e1;
                        border:1px solid #f9a825;
                        border-radius:4px;
                        padding:12px 16px;
                        font-size:12px;
                        color:#555;
                        margin-bottom:20px;">
                        ⏳ This link expires in
                        <b>30 minutes</b>.
                    </div>
                    <div style="
                        text-align:center;
                        margin-bottom:20px;">
                        <a href="%s"
                           style="
                               display:inline-block;
                               padding:12px 28px;
                               background:#6d0f0f;
                               color:#fff;
                               text-decoration:none;
                               border-radius:4px;
                               font-weight:bold;
                               font-size:14px;">
                            🔑 Reset My Password
                        </a>
                    </div>
                    <p style="font-size:12px;
                        color:#999;
                        text-align:center;">
                        If you did not request
                        this, ignore this email.
                        Your password will not
                        be changed.
                    </p>
                    <hr style="border:none;
                        border-top:1px solid #eee;
                        margin:16px 0;">
                    <p style="font-size:11px;
                        color:#bbb;
                        word-break:break-all;">
                        Or copy this link:<br>
                        %s
                    </p>
                </div>
                <div style="padding:14px;
                    text-align:center;
                    font-size:11px;color:#999;
                    background:#fff;
                    border-top:1px solid #eee;">
                    VANDAM Document Admission
                    System &nbsp;|&nbsp;
                    Sta. Mesa Campus (MN0)
                </div>
            </div>
            """,
                username,
                resetUrl,
                resetUrl);

        sendEmail(toEmail, subject, body);
    }
}
