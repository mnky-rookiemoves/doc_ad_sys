package model;

public class RequirementType {

    private int requirementId;
    private String requirementName;
    private String description;

    // Default constructor
    public RequirementType() {}
    
    @Override
    public String toString() {
        return requirementName + " (" + description + ")";
    }

    // Full constructor
    public RequirementType(int requirementId, String requirementName, String description) {
        this.requirementId = requirementId;
        this.requirementName = requirementName;
        this.description = description;
    }

    // GETTERS
    public int getRequirementId() {
        return requirementId;
    }

    public String getRequirementName() {
        return requirementName;
    }

    public String getDescription() {
        return description;
    }

    // SETTERS
    public void setRequirementId(int requirementId) {
        this.requirementId = requirementId;
    }

    public void setRequirementName(String requirementName) {
        this.requirementName = requirementName;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}