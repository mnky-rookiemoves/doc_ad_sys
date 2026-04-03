package model;

import java.util.List;

public class Student {
    private Integer studentId;
    private Integer userId;
    private String studentName;
    private String birthDate;
    private String email;
    private Integer categoryId;
    private String categoryName;
    private int totalDocuments;
    private int incompleteDocuments;
    private int completionPercentage;
    private List<StudentRequirement> requirements;  // Phase 3
    private String remarks;

    public Student() {}

    public Integer getStudentId() { 
        return studentId; 
    }
    
    public void setStudentId(Integer studentId) { 
        this.studentId = studentId; 
    }

    public Integer getUserId() { 
        return userId; 
    }
    
    public void setUserId(Integer userId) { 
        this.userId = userId; 
    }

    public String getStudentName() { 
        return studentName; 
    }
    
    public void setStudentName(String studentName) { 
        this.studentName = studentName; 
    }

    public String getBirthDate() { 
        return birthDate; 
    }
    
    public void setBirthDate(String birthDate) { 
        this.birthDate = birthDate; 
    }

    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }

    public Integer getCategoryId() { 
        return categoryId; 
    }
    
    public void setCategoryId(Integer categoryId) { 
        this.categoryId = categoryId; 
    }

    public String getCategoryName() { 
        return categoryName; 
    }
    
    public void setCategoryName(String categoryName) { 
        this.categoryName = categoryName; 
    }

    public int getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(int totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public int getIncompleteDocuments() {
        return incompleteDocuments;
    }

    public void setIncompleteDocuments(int incompleteDocuments) {
        this.incompleteDocuments = incompleteDocuments;
    }

    public int getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(int completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    // Phase 3: Requirements/Documents
    public List<StudentRequirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<StudentRequirement> requirements) {
        this.requirements = requirements;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    // Helper method to get document status summary
    public String getDocumentStatusSummary() {
        if (requirements == null || requirements.isEmpty()) {
            return "No documents tracked";
        }
        int approved = 0;
        for (StudentRequirement req : requirements) {
            if (req.getStatus() != null && req.getStatus().equalsIgnoreCase("approved")) {
                approved++;
            }
        }
        return approved + " of " + requirements.size() + " approved";
    }

    // Helper method for completion badge
    public String getCompletionBadgeClass() {
        if (completionPercentage >= 100) {
            return "status-complete";
        } else if (completionPercentage >= 50) {
            return "status-in-progress";
        } else {
            return "status-incomplete";
        }
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", userId=" + userId +
                ", studentName='" + studentName + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", email='" + email + '\'' +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", totalDocuments=" + totalDocuments +
                ", incompleteDocuments=" + incompleteDocuments +
                ", completionPercentage=" + completionPercentage +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}