package model;

public class StudentRequirement {
    
    private int studentRequirementId;
    private int studentId;
    private int requirementId;
    private String fileName;
    private String filePath;
    private String mimeType;
    private long fileSize;
    private int uploadedBy;
    private String status;  // pending, submitted, approved, rejected
    private String uploadDate;
    private String uploadedAt;
    private String requirementName;
    private String description;

    // Constructors
    public StudentRequirement() {
    }

    public StudentRequirement(int studentId, int requirementId) {
        this.studentId = studentId;
        this.requirementId = requirementId;
    }

    // Getters and Setters
    public int getStudentRequirementId() {
        return studentRequirementId;
    }

    public void setStudentRequirementId(int studentRequirementId) {
        this.studentRequirementId = studentRequirementId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(int requirementId) {
        this.requirementId = requirementId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(int uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getRequirementName() {
        return requirementName;
    }

    public void setRequirementName(String requirementName) {
        this.requirementName = requirementName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Helper method to get status badge color
    public String getStatusColor() {
        if (status == null || status.equalsIgnoreCase("not_submitted") || status.isEmpty()) {
            return "#f44336"; // Red
        } else if (status.equalsIgnoreCase("pending")) {
            return "#ff9800"; // Orange
        } else if (status.equalsIgnoreCase("submitted")) {
            return "#2196f3"; // Blue
        } else if (status.equalsIgnoreCase("approved")) {
            return "#4caf50"; // Green
        } else if (status.equalsIgnoreCase("rejected")) {
            return "#e91e63"; // Pink
        }
        return "#999"; // Gray
    }

    // Helper method to get status display text
    public String getStatusDisplay() {
        if (status == null || status.isEmpty()) {
            return "Not Submitted";
        }
        return status.substring(0, 1).toUpperCase() + status.substring(1);
    }
}