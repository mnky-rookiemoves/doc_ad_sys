package model;

import java.sql.Timestamp;

public class Upload {

    private String studentId;
    private int studentRequirementId;
    private int requirementId;
    private String fileName;
    private byte[] fileContent;
    private String mimeType;
    private long fileSize;
    private String status;
    private Timestamp uploadedAt;
    private int uploadId;

    // ✅ EMPTY CONSTRUCTOR (VERY IMPORTANT)
    public Upload() {}

    // ✅ GETTERS + SETTERS
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public int getStudentRequirementId() { return studentRequirementId; }
    public void setStudentRequirementId(int studentRequirementId) { this.studentRequirementId = studentRequirementId; }
    

    public int getRequirementId() { return requirementId; }
    public void setRequirementId(int requirementId) { this.requirementId = requirementId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public byte[] getFileContent() { return fileContent; }
    public void setFileContent(byte[] fileContent) { this.fileContent = fileContent; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Timestamp uploadedAt) { this.uploadedAt = uploadedAt; }

    public int getUploadId() { return uploadId;}
    public void setUploadId(int uploadId) { this.uploadId = uploadId; }
}