<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, model.Student, model.StudentCategory, model.User" %>
<%@ include file="/includes/head.jsp" %>

<%
    // ===== AUTH =====
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    boolean isAdmin = "admin".equalsIgnoreCase(currentUser.getRole());

    List<Student> studentList = (List<Student>) request.getAttribute("studentList");
    if (studentList == null) studentList = new ArrayList<>();

    Map<Integer, Integer> uploadedCountMap =
        (Map<Integer, Integer>) request.getAttribute("uploadedCountMap");
    if (uploadedCountMap == null) uploadedCountMap = new HashMap<>();

    Integer totalRequirements = (Integer) request.getAttribute("totalRequirements");
    if (totalRequirements == null) totalRequirements = 0;

    Integer selectedStudentId = (Integer) session.getAttribute("selectedStudentId");

    // ===== CATEGORY DATA =====
    List<StudentCategory> categoryList = (List<StudentCategory>) request.getAttribute("categoryList");
    Map<Integer, String> categoryMap = new HashMap<>();
    if (categoryList != null) {
        for (StudentCategory c : categoryList) {
            categoryMap.put(c.getCategoryId(), c.getCategoryName());
        }
    }

    // ===== PAGINATION =====
    Integer currentPage = (Integer) request.getAttribute("currentPage");
    if (currentPage == null) currentPage = 1;

    Integer totalPages = (Integer) request.getAttribute("totalPages");
    if (totalPages == null) totalPages = 1;

    Integer pageSize = (Integer) request.getAttribute("pageSize");
    if (pageSize == null) pageSize = 10;

    Integer startRow = (Integer) request.getAttribute("startRow");
    if (startRow == null) startRow = 0;

    Integer endRow = (Integer) request.getAttribute("endRow");
    if (endRow == null) endRow = 0;

    Integer totalStudents = (Integer) request.getAttribute("totalStudents");
    if (totalStudents == null) totalStudents = 0;

    // ===== SEARCH / FILTER PARAMS =====
    String searchParam = request.getParameter("search") != null ? request.getParameter("search") : "";
    String catParam    = request.getParameter("categoryId") != null ? request.getParameter("categoryId") : "";

    // Safe escaped versions for HTML output
    String searchParamEsc = searchParam.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
    String catParamEsc    = catParam.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");

    // Extra params string to preserve search/filter across pagination
    String extraParams = "";
    if (!searchParam.isEmpty()) extraParams += "&search=" + java.net.URLEncoder.encode(searchParam, "UTF-8");
    if (!catParam.isEmpty())    extraParams += "&categoryId=" + java.net.URLEncoder.encode(catParam, "UTF-8");

    boolean showClear = !searchParam.trim().isEmpty() || !catParam.trim().isEmpty();

    // ===== MESSAGES =====
    String success = request.getParameter("success");
    String error   = request.getParameter("error");

    String _theme =
        (String) session.getAttribute("theme");
    if (_theme == null) _theme = "normal";
%>

<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet"
      href="<%= request.getContextPath()
      %>/css/themes.css">
<link rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Students | VANDAM</title>

<style>
* { margin:0; padding:0; box-sizing:border-box; }

body { font-family:'Segoe UI', Arial, sans-serif; background:#f4f4f4; }
.container { display:flex; min-height:100vh; }

/* ── SIDEBAR ── */
.sidebar { width:240px; background:#6d0f0f; color:#fff; display:flex; flex-direction:column; }
.sidebar h2 { margin:0; padding:20px; background:#5a0c0c; text-align:center; font-size:18px; }
.sidebar a {
    display:block;
    padding:14px 20px;
    color:#fff;
    text-decoration:none;
    transition:background 0.2s;
}
.sidebar a:hover  { background:#8b1a1a; }
.sidebar a.active { background:#8b1a1a; font-weight:600; }
.sidebar .logout  { margin-top:auto; background:#4a0a0a; }

/* ── MAIN ── */
.main { flex:1; padding:25px; overflow-x:auto; }

.header {
    background:#fff;
    padding:16px 20px;
    border-radius:8px;
    display:flex;
    justify-content:space-between;
    align-items:center;
    margin-bottom:20px;
    box-shadow:0 2px 4px rgba(0,0,0,0.1);
}
.header h2 { margin:0; }
.header-info { text-align:right; font-size:13px; }

/* ── BUTTONS ── */
.btn {
    padding:8px 14px;
    border:none;
    border-radius:4px;
    cursor:pointer;
    font-size:13px;
    text-decoration:none;
    transition:background 0.2s;
    display:inline-block;
}
.btn-primary   { background:#6d0f0f; color:#fff; }
.btn-primary:hover:not(:disabled)   { background:#5a0c0c; }
.btn-secondary { background:#6c757d; color:#fff; }
.btn-secondary:hover:not(:disabled) { background:#5a6268; }
.btn-success   { background:#2e7d32; color:#fff; }
.btn-success:hover:not(:disabled)   { background:#1b5e20; }
.btn-danger    { background:#c62828; color:#fff; }
.btn-danger:hover:not(:disabled)    { background:#b71c1c; }
.btn:disabled  { opacity:0.6; cursor:not-allowed; }

/* ── TABLE ── */
table {
    width:100%;
    background:#fff;
    border-collapse:collapse;
    border-radius:8px;
    overflow:hidden;
    box-shadow:0 2px 4px rgba(0,0,0,0.1);
}
th, td { padding:12px; border-bottom:1px solid #ddd; text-align:left; font-size:13px; }
th { background:#f5f5f5; font-weight:600; }
tr:hover { background:#f9f9f9; }

/* ── CONTROLS ── */
.controls {
    display:flex;
    justify-content:space-between;
    align-items:center;
    margin-bottom:12px;
    flex-wrap:wrap;
    gap:12px;
}

.page-size-form {
    display:flex;
    align-items:center;
    gap:8px;
    font-size:13px;
}
.page-size-form select {
    padding:6px 10px;
    border:1px solid #ccc;
    border-radius:4px;
    cursor:pointer;
}

/* ── SEARCH BAR ── */
.search-bar {
    display:flex;
    gap:8px;
    flex-wrap:wrap;
    margin-bottom:16px;
    align-items:center;
}
.search-bar input,
.search-bar select {
    padding:8px 10px;
    border:1px solid #ccc;
    border-radius:4px;
    font-size:13px;
}
.search-bar input { min-width:200px; }

/* ── PAGINATION ── */
.pagination {
    display:flex;
    gap:6px;
    margin-top:16px;
    align-items:center;
    flex-wrap:wrap;
}
.pagination-info {
    font-size:12px;
    color:#666;
    margin-left:auto;
}

/* ── MESSAGES ── */
.msg-success {
    background:#eaffea;
    border:1px solid #8bd88b;
    padding:12px;
    border-radius:6px;
    margin-bottom:16px;
    color:#2d5016;
}
.msg-error {
    background:#ffecec;
    border:1px solid #e9a3a3;
    padding:12px;
    border-radius:6px;
    margin-bottom:16px;
    color:#7d2c2c;
}

/* ── MODAL BASE ── */
.modal {
    display:none;
    position:fixed;
    inset:0;
    background:rgba(0,0,0,0.55);
    justify-content:center;
    align-items:center;
    z-index:9999;
    padding:14px;
    overflow-y:auto;
}
.modal.show { display:flex; }
.modal-content {
    background:#fff;
    width:min(520px, 95vw);
    border-radius:8px;
    overflow:hidden;
    box-shadow:0 12px 40px rgba(0,0,0,0.35);
    margin:auto;
}
.modal-header {
    background:#6d0f0f;
    color:#fff;
    padding:14px 16px;
    display:flex;
    justify-content:space-between;
    align-items:center;
    font-weight:600;
}
.modal-body { padding:16px; }

/* ── VIEW MODAL ── */
.view-section { margin-bottom:16px; }
.view-section-title {
    font-weight:600;
    color:#6d0f0f;
    border-bottom:2px solid #f0f0f0;
    padding-bottom:8px;
    margin-bottom:12px;
    font-size:13px;
    text-transform:uppercase;
}
.view-field { display:flex; margin-bottom:10px; font-size:13px; }
.view-label { font-weight:600; width:120px; color:#333; flex-shrink:0; }
.view-value { flex:1; color:#555; word-break:break-word; }
.view-value.empty { color:#999; font-style:italic; }

/* ── FORM MODALS ── */
.modal-body label {
    display:block;
    font-weight:600;
    margin-top:12px;
    margin-bottom:4px;
    font-size:13px;
}
.modal-body input,
.modal-body select,
.modal-body textarea {
    width:100%;
    padding:9px;
    border:1px solid #ccc;
    border-radius:4px;
    font-size:13px;
    font-family:inherit;
}
.modal-body textarea { resize:vertical; min-height:80px; }
.modal-body input:focus,
.modal-body select:focus,
.modal-body textarea:focus {
    outline:none;
    border-color:#6d0f0f;
    box-shadow:0 0 0 2px rgba(109,15,15,0.1);
}

.modal-actions {
    display:flex;
    justify-content:flex-end;
    gap:10px;
    margin-top:16px;
    flex-wrap:wrap;
}

.view-modal-actions {
    display:flex;
    justify-content:space-between;
    gap:10px;
    margin-top:16px;
    flex-wrap:wrap;
}
.view-modal-actions .left-actions { display:flex; gap:10px; flex-wrap:wrap; }

.close-x {
    background:transparent;
    border:none;
    color:#fff;
    font-size:20px;
    cursor:pointer;
    width:24px;
    height:24px;
    display:flex;
    align-items:center;
    justify-content:center;
}

/* ── ACTION BUTTONS IN TABLE ── */
.action-buttons { display:flex; gap:6px; flex-wrap:wrap; }
.action-buttons .btn { padding:5px 9px; font-size:12px; }

/* ── STATUS / PROGRESS ── */
.selected-row { background-color:#ffe0e0 !important; font-weight:bold; }

.badge { padding:4px 10px; border-radius:12px; font-size:12px; white-space:nowrap; }
.complete { background:#4caf50; color:#fff; }
.missing  { background:#f44336; color:#fff; }

.progress-bar  { width:90px; background:#eee; border-radius:5px; overflow:hidden; }
.progress-fill {
    background:#6d0f0f;
    color:#fff;
    text-align:center;
    border-radius:5px;
    font-size:11px;
    min-width:18px;
    padding:2px 0;
    white-space:nowrap;
}

/* ── BATCH TOOLBAR ── */
.batch-toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 16px;
    background: #fff8e1;
    border: 1px solid #f9a825;
    border-radius: 6px;
    margin-bottom: 12px;
    font-size: 13px;
    font-weight: bold;
    color: #555;
}

.btn-batch-print {
    padding: 7px 16px;
    background: #6d0f0f;
    color: #fff;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 13px;
    font-weight: bold;
}

.btn-batch-print:hover {
    background: #5a0c0c;
}

.btn-clear {
    padding: 7px 12px;
    background: #eee;
    color: #555;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 13px;
}

/* ── CHECKBOX COLUMN ── */
input[type="checkbox"] {
    cursor: pointer;
    width: 16px;
    height: 16px;
    accent-color: #6d0f0f;
}
/* ── BATCH CATEGORY BUTTON ── */
.btn-batch-category {
    padding: 7px 16px;
    background: #1565c0;
    color: #fff;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 13px;
    font-weight: bold;
    transition: background 0.2s;
}

.btn-batch-category:hover {
    background: #0d47a1;
}
</style>
</head>

<body class="theme-<%= _theme %>">
<div class="container">

<!-- ═══════════════ SIDEBAR ═══════════════ -->
<jsp:include page="/includes/sidebar.jsp" />

<!-- ═══════════════ MAIN ═══════════════ -->
<div class="main">

    <!-- HEADER -->
    <div class="header-info">
        Logged in as <b><%= currentUser.getUsername() %></b><br>
        Role:
        <b>
            <%= currentUser.getRole() != null
                ? currentUser.getRole().substring(0, 1).toUpperCase()
                + currentUser.getRole().substring(1).toLowerCase()
                : "Unknown" %>
        </b>
    </div>


    <!-- SUCCESS / ERROR MESSAGES -->
    <% if ("created".equals(success)) { %>
        <div class="msg-success">✅ Student added successfully.</div>
    <% } else if ("updated".equals(success)) { %>
        <div class="msg-success">✅ Student updated successfully.</div>
    <% } else if ("deleted".equals(success)) { %>
        <div class="msg-success">✅ Student deleted successfully.</div>
    <% } %>
    <% if (error != null && !error.trim().isEmpty()) { %>
        <div class="msg-error">❌ <%= error.replace("<", "&lt;") %></div>
    <% } %>

    <!-- CONTROLS ROW -->
    <div class="controls">
        <button class="btn btn-primary" type="button" onclick="openModal('addModal')">+ Add Student</button>

        <!-- PAGE SIZE -->
        <form method="get" action="<%=request.getContextPath()%>/students" class="page-size-form">
            <input type="hidden" name="search"     value="<%= searchParamEsc %>">
            <input type="hidden" name="categoryId" value="<%= catParamEsc %>">
            <label for="pageSizeSelect">Show</label>
            <select id="pageSizeSelect" name="pageSize" onchange="this.form.submit()">
                <option value="10"  <%=pageSize==10 ?"selected":""%>>10</option>
                <option value="25"  <%=pageSize==25 ?"selected":""%>>25</option>
                <option value="50"  <%=pageSize==50 ?"selected":""%>>50</option>
                <option value="100" <%=pageSize==100?"selected":""%>>100</option>
            </select>
            <span>entries</span>
        </form>
    </div>

    <!-- SEARCH & FILTER -->
    <form method="get" action="<%=request.getContextPath()%>/students" class="search-bar">
        <input type="hidden" name="pageSize" value="<%= pageSize %>">

        <input type="text" name="search"
               value="<%= searchParamEsc %>"
               placeholder="Search name or email">

        <select name="categoryId">
            <option value="">All Categories</option>
            <% if (categoryList != null) {
                for (StudentCategory c : categoryList) {
                    boolean selected = catParam.equals(String.valueOf(c.getCategoryId()));
            %>
                <option value="<%= c.getCategoryId() %>" <%= selected ? "selected" : "" %>>
                    <%= c.getCategoryName() %>
                </option>
            <% }} %>
        </select>

        <button class="btn btn-primary" type="submit">🔍 Search</button>

        <% if (showClear) { %>
            <a href="<%=request.getContextPath()%>/students?page=1&pageSize=<%= pageSize %>"
               class="btn btn-secondary">✖ Clear</a>
        <% } %>
    </form>

    <%-- ✅ BATCH PRINT and UPDATE TOOLBAR --%>
<div class="batch-toolbar" id="batchToolbar"
     style="display:none;">
    <span id="selectedCount">0</span>
    students selected

    <%-- ✅ EXISTING --%>
    <button onclick="batchPrint()"
            class="btn-batch-print">
        🖨 Print Selected
    </button>

    <%-- ✅ NEW — Batch Category Update --%>
    <button onclick="openCategoryModal()"
            class="btn-batch-category">
        📁 Update Category
    </button>

    <button onclick="clearSelection()"
            class="btn-clear">
        ✕ Clear
    </button>
</div>

    <!-- TABLE -->
<table>
    <thead>
        <tr>
            <%-- ✅ ALREADY EXISTS — keep as is --%>
            <th style="width:40px; text-align:center;">
                <input type="checkbox"
                    id="selectAll"
                    onchange="toggleAll(this)"
                    title="Select All">
            </th>
            <th>#</th>
            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Category</th>
            <th>Phone</th>
            <th>Status</th>
            <th>Progress</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
    <% if (studentList.isEmpty()) { %>
        <tr>
            <%-- ✅ CHANGE 1 — colspan 9 → 10 --%>
            <td colspan="10"
                style="text-align:center;
                       color:#999; padding:24px;">
                No students found.
            </td>
        </tr>
    <% } else {
        int rowNum = (startRow > 0) ? startRow : 1;
        for (Student s : studentList) {
            int uploaded = uploadedCountMap
                .getOrDefault(s.getStudentId(), 0);
            int safeUploaded = (totalRequirements > 0)
                ? Math.min(uploaded, totalRequirements)
                : 0;
            boolean isComplete = (totalRequirements > 0)
                && (safeUploaded >= totalRequirements);
            int percent = (totalRequirements > 0)
                ? Math.min(100,
                    safeUploaded * 100
                    / totalRequirements)
                : 0;

            String safeId      =
                String.valueOf(s.getStudentId());
            String safeName    =
                s.getStudentName() != null
                ? s.getStudentName()
                    .replace("\"", "&quot;")
                    .replace("<", "&lt;")
                : "";
            String safeEmail   =
                s.getEmail() != null
                ? s.getEmail()
                    .replace("\"", "&quot;")
                    .replace("<", "&lt;")
                : "";
            String safePhone   =
                s.getPhone() != null
                ? s.getPhone()
                    .replace("\"", "&quot;")
                    .replace("<", "&lt;")
                : "";
            String safeBirth   =
                s.getBirthDate() != null
                ? s.getBirthDate().toString()
                    .replace("\"", "&quot;")
                : "";
            String safeRemarks =
                s.getRemarks() != null
                ? s.getRemarks()
                    .replace("\"", "&quot;")
                    .replace("<", "&lt;")
                : "";
            String safeCatId   =
                String.valueOf(s.getCategoryId());
            String safeCatName =
                categoryMap.getOrDefault(
                    s.getCategoryId(), "-")
                    .replace("\"", "&quot;")
                    .replace("<", "&lt;");
            boolean isSelected =
                (selectedStudentId != null
                && selectedStudentId.equals(
                    s.getStudentId()));
    %>
        <tr class="<%= isSelected
                ? "selected-row" : "" %>"
            data-id="<%= safeId %>"
            data-name="<%= safeName %>"
            data-email="<%= safeEmail %>"
            data-phone="<%= safePhone %>"
            data-category="<%= safeCatId %>"
            data-category-name="<%= safeCatName %>"
            data-birthdate="<%= safeBirth %>"
            data-remarks="<%= safeRemarks %>">

            <%-- ✅ CHANGE 2 — NEW checkbox cell --%>
            <td style="text-align:center;">
                <input type="checkbox"
                       class="student-check"
                       value="<%= s.getStudentId() %>"
                       onchange="updateToolbar()">
            </td>

            <%-- ✅ ALL EXISTING CELLS — UNCHANGED --%>
            <td><%= rowNum++ %></td>
            <td><%= s.getStudentId() %></td>
            <td><%= safeName %></td>
            <td><%= safeEmail %></td>
            <td><%= safeCatName %></td>
            <td><%= !safePhone.isEmpty()
                ? safePhone : "-" %></td>

            <!-- STATUS — UNCHANGED -->
            <td>
                <% if (isComplete) { %>
                    <span class="badge complete">
                        Complete
                    </span>
                <% } else { %>
                    <span class="badge missing">
                        Missing
                    </span>
                <% } %>
            </td>

            <!-- PROGRESS — UNCHANGED -->
            <td>
                <div class="progress-bar">
                    <div class="progress-fill"
                         style="width:<%= percent %>%;">
                        <%= percent %>%
                    </div>
                </div>
            </td>

            <!-- ACTIONS — UNCHANGED -->
            <td>
                <div class="action-buttons">
                    <button class="btn btn-secondary"
                            type="button"
                            onclick="openView(this)">
                        👁 View
                    </button>
                    <button class="btn btn-primary"
                            type="button"
                            onclick="openEdit(this)">
                        ✏️ Edit
                    </button>
                    <a class="btn btn-success"
                       href="<%= request
                           .getContextPath()
                           %>/uploads?studentId=<%=
                           s.getStudentId() %>">
                        📂 Docs
                    </a>
                    <a href="<%= request
                           .getContextPath()
                           %>/student-print?studentId=<%=
                           s.getStudentId() %>"
                       target="_blank"
                       class="btn btn-secondary">
                        🖨 Print
                    </a>
                </div>
            </td>
        </tr>
    <%  }
       } %>
    </tbody>
</table>

    <!-- PAGINATION -->
    <div class="pagination">
        <% if (currentPage > 1) { %>
            <a class="btn btn-secondary"
               href="<%=request.getContextPath()%>/students?page=1&pageSize=<%=pageSize%><%=extraParams%>">First</a>
            <a class="btn btn-secondary"
               href="<%=request.getContextPath()%>/students?page=<%=currentPage-1%>&pageSize=<%=pageSize%><%=extraParams%>">← Prev</a>
        <% } %>

        <% for (int i = 1; i <= totalPages; i++) {
               if (i == currentPage) { %>
                   <button class="btn btn-primary" disabled><%= i %></button>
        <%     } else if (i >= currentPage - 2 && i <= currentPage + 2) { %>
                   <a class="btn btn-secondary"
                      href="<%=request.getContextPath()%>/students?page=<%=i%>&pageSize=<%=pageSize%><%=extraParams%>"><%= i %></a>
        <%     }
           } %>

        <% if (currentPage < totalPages) { %>
            <a class="btn btn-secondary"
               href="<%=request.getContextPath()%>/students?page=<%=currentPage+1%>&pageSize=<%=pageSize%><%=extraParams%>">Next →</a>
            <a class="btn btn-secondary"
               href="<%=request.getContextPath()%>/students?page=<%=totalPages%>&pageSize=<%=pageSize%><%=extraParams%>">Last</a>
        <% } %>

        <div class="pagination-info">
            Page <%= currentPage %> of <%= totalPages %> &nbsp;|&nbsp;
            Showing <%= startRow %>–<%= endRow %> of <%= totalStudents %> students
        </div>
    </div>

</div><!-- end .main -->
</div><!-- end .container -->


<!-- ═══════════════════════════════════════════
     VIEW MODAL
═══════════════════════════════════════════ -->
<div id="viewModal" class="modal" onclick="if(event.target===this) closeModal('viewModal')">
    <div class="modal-content" onclick="event.stopPropagation()">
        <div class="modal-header">
            <b>Student Details</b>
            <button class="close-x" type="button" onclick="closeModal('viewModal')">×</button>
        </div>
        <div class="modal-body">

            <!-- PERSONAL INFO -->
            <div class="view-section">
                <div class="view-section-title">Personal Information</div>
                <div class="view-field">
                    <div class="view-label">ID:</div>
                    <div class="view-value" id="view_id">-</div>
                </div>
                <div class="view-field">
                    <div class="view-label">Name:</div>
                    <div class="view-value" id="view_name">-</div>
                </div>
                <div class="view-field">
                    <div class="view-label">Birth Date:</div>
                    <div class="view-value" id="view_birthdate">-</div>
                </div>
                <div class="view-field">
                    <div class="view-label">Email:</div>
                    <div class="view-value" id="view_email">-</div>
                </div>
                <div class="view-field">
                    <div class="view-label">Phone:</div>
                    <div class="view-value" id="view_phone">-</div>
                </div>
            </div>

            <!-- ACADEMIC INFO -->
            <div class="view-section">
                <div class="view-section-title">Academic Information</div>
                <div class="view-field">
                    <div class="view-label">Category:</div>
                    <div class="view-value" id="view_category">-</div>
                </div>
            </div>

            <!-- REMARKS -->
            <div class="view-section">
                <div class="view-section-title">Remarks</div>
                <div class="view-value" id="view_remarks"
                     style="padding:10px; background:#f9f9f9; border-radius:4px;
                            border-left:4px solid #6d0f0f; min-height:40px;">
                </div>
            </div>

            <!-- ACTIONS -->
            <div class="view-modal-actions">
                <div class="left-actions">
                    <button class="btn btn-primary"  type="button" onclick="switchToEdit()">✏️ Edit</button>
                    <button class="btn btn-danger"   type="button" onclick="switchToDelete()">🗑️ Delete</button>
                </div>
                <button class="btn btn-secondary" type="button" onclick="closeModal('viewModal')">Close</button>
            </div>
        </div>
    </div>
</div>


<!-- ═══════════════════════════════════════════
     ADD MODAL
═══════════════════════════════════════════ -->
<div id="addModal" class="modal" onclick="if(event.target===this) closeModal('addModal')">
    <div class="modal-content" onclick="event.stopPropagation()">
        <div class="modal-header">
            <b>Add Student</b>
            <button class="close-x" type="button" onclick="closeModal('addModal')">×</button>
        </div>
        <form method="post" action="<%=request.getContextPath()%>/students">
            <div class="modal-body">
                <input type="hidden" name="action" value="save">

                <label for="add_name">Name *</label>
                <input id="add_name" name="studentName" required placeholder="Full name">

                <label for="add_birthdate">Birth Date *</label>
                <input id="add_birthdate" name="birthDate" type="date" required>

                <label for="add_email">Email *</label>
                <input id="add_email" name="email" type="email" required placeholder="email@example.com">

                <label for="add_phone">Phone</label>
                <input id="add_phone" name="phone" type="tel" placeholder="e.g. 09XX-XXX-XXXX">

                <label for="add_category">Category *</label>
                <select id="add_category" name="categoryId" required>
                    <option value="">-- Select Category --</option>
                    <% if (categoryList != null) for (StudentCategory c : categoryList) { %>
                        <option value="<%= c.getCategoryId() %>"><%= c.getCategoryName() %></option>
                    <% } %>
                </select>

                <label for="add_remarks">Remarks</label>
                <textarea id="add_remarks" name="remarks" placeholder="Optional notes..."></textarea>

                <div class="modal-actions">
                    <button class="btn btn-secondary" type="button" onclick="closeModal('addModal')">Cancel</button>
                    <button class="btn btn-primary"   type="submit">💾 Save</button>
                </div>
            </div>
        </form>
    </div>
</div>


<!-- ═══════════════════════════════════════════
     EDIT MODAL
═══════════════════════════════════════════ -->
<div id="editModal" class="modal" onclick="if(event.target===this) closeModal('editModal')">
    <div class="modal-content" onclick="event.stopPropagation()">
        <div class="modal-header">
            <b>Edit Student</b>
            <button class="close-x" type="button" onclick="closeModal('editModal')">×</button>
        </div>
        <form method="post" action="<%=request.getContextPath()%>/students">
            <div class="modal-body">
                <input type="hidden" name="action"    value="update">
                <input type="hidden" name="studentId" id="edit_id">

                <label for="edit_name">Name *</label>
                <input id="edit_name" name="studentName" required>

                <label for="edit_birthdate">Birth Date</label>
                <input id="edit_birthdate" name="birthDate" type="date">

                <label for="edit_email">Email *</label>
                <input id="edit_email" name="email" type="email" required>

                <label for="edit_phone">Phone</label>
                <input id="edit_phone" name="phone" type="tel">

                <label for="edit_category">Category *</label>
                <select id="edit_category" name="categoryId" required>
                    <option value="">-- Select Category --</option>
                    <% if (categoryList != null) for (StudentCategory c : categoryList) { %>
                        <option value="<%= c.getCategoryId() %>"><%= c.getCategoryName() %></option>
                    <% } %>
                </select>

                <label for="edit_remarks">Remarks</label>
                <textarea id="edit_remarks" name="remarks" placeholder="Optional notes..."></textarea>

                <div class="modal-actions">
                    <button class="btn btn-secondary" type="button" onclick="closeModal('editModal')">Cancel</button>
                    <button class="btn btn-primary"   type="submit">✔️ Update</button>
                </div>
            </div>
        </form>
    </div>
</div>


<!-- ═══════════════════════════════════════════
     DELETE MODAL
═══════════════════════════════════════════ -->
<div id="deleteModal" class="modal" onclick="if(event.target===this) closeModal('deleteModal')">
    <div class="modal-content" onclick="event.stopPropagation()">
        <div class="modal-header">
            <b>Delete Student</b>
            <button class="close-x" type="button" onclick="closeModal('deleteModal')">×</button>
        </div>
        <div class="modal-body">
            <p style="margin-bottom:12px;">
                Are you sure you want to delete <b id="delete_name"></b>?
            </p>
            <p style="font-size:12px; color:#d32f2f; margin-bottom:16px;">
                ⚠️ <strong>WARNING:</strong> This action cannot be undone.
                All related documents will also be removed.
            </p>
            <div class="modal-actions">
                <button class="btn btn-secondary" type="button" onclick="closeModal('deleteModal')">Cancel</button>
                <a class="btn btn-danger" id="deleteLink" href="#">🗑️ Delete Permanently</a>
            </div>
        </div>
    </div>
</div>


<!-- ═══════════════════════════════════════════
     JAVASCRIPT
═══════════════════════════════════════════ -->
<script>
// ── Global student snapshot ──
let currentStudent = {};

// ── Modal helpers ──
function openModal(id)  { document.getElementById(id).classList.add('show');    }
function closeModal(id) { document.getElementById(id).classList.remove('show'); }

// Close all modals on Escape
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal.show').forEach(m => m.classList.remove('show'));
    }
});

// ── Read data-* from a table row ──
function readRow(tr) {
    return {
        id           : tr.dataset.id           || '',
        name         : tr.dataset.name         || '',
        email        : tr.dataset.email        || '',
        phone        : tr.dataset.phone        || '',
        category     : tr.dataset.category     || '',
        categoryName : tr.dataset.categoryName || '-',
        birthdate    : tr.dataset.birthdate    || '',
        remarks      : tr.dataset.remarks      || ''
    };
}

// ── OPEN VIEW MODAL ──
function openView(btn) {
    currentStudent = readRow(btn.closest('tr'));

    document.getElementById('view_id').textContent        = currentStudent.id;
    document.getElementById('view_name').textContent      = currentStudent.name;
    document.getElementById('view_email').textContent     = currentStudent.email;
    document.getElementById('view_phone').textContent     = currentStudent.phone  || '-';
    document.getElementById('view_birthdate').textContent = currentStudent.birthdate || 'Not specified';
    document.getElementById('view_category').textContent  = currentStudent.categoryName;

    const remarksEl = document.getElementById('view_remarks');
    if (currentStudent.remarks && currentStudent.remarks.trim() !== '') {
        remarksEl.textContent = currentStudent.remarks;
        remarksEl.classList.remove('empty');
    } else {
        remarksEl.textContent = 'No remarks added.';
        remarksEl.classList.add('empty');
    }

    openModal('viewModal');
}

// ── OPEN EDIT MODAL DIRECTLY ──
function openEdit(btn) {
    currentStudent = readRow(btn.closest('tr'));
    populateEditModal();
    openModal('editModal');
}

// ── SWITCH: VIEW → EDIT ──
function switchToEdit() {
    closeModal('viewModal');
    populateEditModal();
    openModal('editModal');
}

function populateEditModal() {
    document.getElementById('edit_id').value        = currentStudent.id;
    document.getElementById('edit_name').value      = currentStudent.name;
    document.getElementById('edit_email').value     = currentStudent.email;
    document.getElementById('edit_phone').value     = currentStudent.phone;
    document.getElementById('edit_category').value  = currentStudent.category;
    document.getElementById('edit_birthdate').value = currentStudent.birthdate;
    document.getElementById('edit_remarks').value   = currentStudent.remarks;
}

// ── SWITCH: VIEW → DELETE ──
function switchToDelete() {
    closeModal('viewModal');
    document.getElementById('delete_name').textContent = currentStudent.name;
    document.getElementById('deleteLink').href =
        '<%=request.getContextPath()%>/students?action=delete&studentId=' + currentStudent.id;
    openModal('deleteModal');
}
</script>

<script>
// ✅ Toggle all checkboxes
function toggleAll(master) {
    var boxes = document.querySelectorAll(
        '.student-check');
    boxes.forEach(function(box) {
        box.checked = master.checked;
    });
    updateToolbar();
}

// ✅ Update toolbar count
function updateToolbar() {
    var checked = document.querySelectorAll(
        '.student-check:checked');
    var toolbar =
        document.getElementById('batchToolbar');
    var countEl =
        document.getElementById('selectedCount');

    if (checked.length > 0) {
        toolbar.style.display = 'flex';
        countEl.textContent = checked.length;
    } else {
        toolbar.style.display = 'none';
    }
}
// ✅ Open category modal
function openCategoryModal() {
    var checked = document.querySelectorAll(
        '.student-check:checked');

    if (checked.length === 0) {
        alert('Please select at least one student.');
        return;
    }

    // Update count in modal
    document.getElementById('modalCount')
        .textContent = checked.length;

    // Show modal
    var modal =
        document.getElementById('categoryModal');
    modal.style.display = 'flex';
}

// ✅ Close modal
function closeCategoryModal() {
    document.getElementById('categoryModal')
        .style.display = 'none';
    document.getElementById('modalCategorySelect')
        .value = '';
}

// ✅ Apply batch category update
function applyBatchCategory() {
    var categoryId = document
        .getElementById('modalCategorySelect')
        .value;

    if (!categoryId) {
        alert('Please select a category.');
        return;
    }

    // Collect selected IDs
    var checked = document.querySelectorAll(
        '.student-check:checked');
    var ids = [];
    checked.forEach(function(b) {
        ids.push(b.value);
    });

    if (ids.length === 0) {
        alert('No students selected.');
        return;
    }

    // ✅ POST to BatchCategoryServlet
    var form = document.createElement('form');
    form.method = 'POST';
    form.action = '<%= request.getContextPath()
        %>/batch-category';

    // IDs
    var idsInput =
        document.createElement('input');
    idsInput.type  = 'hidden';
    idsInput.name  = 'ids';
    idsInput.value = ids.join(',');
    form.appendChild(idsInput);

    // Category
    var catInput =
        document.createElement('input');
    catInput.type  = 'hidden';
    catInput.name  = 'categoryId';
    catInput.value = categoryId;
    form.appendChild(catInput);

    document.body.appendChild(form);
    form.submit();
}

// ✅ Close modal on background click
document.getElementById('categoryModal')
    .addEventListener('click', function(e) {
        if (e.target === this) {
            closeCategoryModal();
        }
    });

// ✅ Clear all selections
function clearSelection() {
    document.querySelectorAll(
        '.student-check')
        .forEach(function(b) {
            b.checked = false;
        });
    document.getElementById('selectAll')
        .checked = false;
    updateToolbar();
}
// ✅ Open batch print
function batchPrint() {
    var checked = document.querySelectorAll(
        '.student-check:checked');
    var ids = [];
    checked.forEach(function(b) {
        ids.push(b.value);
    });
    if (ids.length === 0) {
        alert('Please select at least one student.');
        return;
    }
    // ✅ ctx now resolves correctly
    var url = '<%= request.getContextPath() %>/batch-print?ids=' + ids.join(',');
    window.open(url, '_blank');
}
</script>

<%-- ✅ BATCH CATEGORY MODAL --%>
<div id="categoryModal" style="
    display:none;
    position:fixed;
    top:0; left:0; right:0; bottom:0;
    background:rgba(0,0,0,0.5);
    z-index:9999;
    justify-content:center;
    align-items:center;">

    <div style="
        background:#fff;
        border-radius:8px;
        padding:30px;
        width:400px;
        box-shadow:0 8px 32px rgba(0,0,0,0.3);">

        <%-- HEADER --%>
        <h3 style="
            color:#6d0f0f;
            margin-bottom:6px;
            font-size:18px;">
            📁 Update Category
        </h3>
        <p style="
            color:#777;
            font-size:13px;
            margin-bottom:20px;">
            Selected students:
            <b id="modalCount">0</b>
        </p>

        <%-- CATEGORY SELECT --%>
        <label style="
            font-size:13px;
            font-weight:bold;
            color:#333;
            display:block;
            margin-bottom:8px;">
            New Category:
        </label>

        <select id="modalCategorySelect" style="
            width:100%;
            padding:10px;
            border:1px solid #ccc;
            border-radius:4px;
            font-size:13px;
            margin-bottom:20px;">
            <option value="">
                -- Select Category --
            </option>
            <%-- ✅ Loop your categories --%>
            <% for (StudentCategory cat :
                    categoryList) { %>
            <option value="<%= cat.getCategoryId() %>">
                <%= cat.getCategoryName() %>
            </option>
            <% } %>
        </select>

        <%-- BUTTONS --%>
        <div style="
            display:flex;
            justify-content:flex-end;
            gap:10px;">
            <button onclick="closeCategoryModal()"
                    style="
                padding:10px 20px;
                background:#eee;
                color:#555;
                border:none;
                border-radius:4px;
                cursor:pointer;
                font-size:13px;">
                Cancel
            </button>
            <button onclick="applyBatchCategory()"
                    style="
                padding:10px 20px;
                background:#6d0f0f;
                color:#fff;
                border:none;
                border-radius:4px;
                cursor:pointer;
                font-size:13px;
                font-weight:bold;">
                ✅ Apply to Selected
            </button>
        </div>
    </div>
</div>
</body>
</html>