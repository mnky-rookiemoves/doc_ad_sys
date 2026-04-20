package model;

import java.sql.Date;
import java.sql.Timestamp;

public class StudentRequirement {

    private int studentRequirementId;
    private String studentId;
    private int requirementId;

    private String fileName;
    private String filePath;
    private String mimeType;
    private long fileSize;

    private String status;
    private Date uploadDate;
    private Timestamp uploadedAt;

    private String requirementName;

    // ===== GETTERS & SETTERS =====

    public int getStudentRequirementId() { return studentRequirementId; }
    public void setStudentRequirementId(int studentRequirementId) { this.studentRequirementId = studentRequirementId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public int getRequirementId() { return requirementId; }
    public void setRequirementId(int requirementId) { this.requirementId = requirementId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getUploadDate() { return uploadDate; }
    public void setUploadDate(Date uploadDate) { this.uploadDate = uploadDate; }

    public Timestamp getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Timestamp uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getRequirementName() { return requirementName; }
    public void setRequirementName(String requirementName) { this.requirementName = requirementName; }
}