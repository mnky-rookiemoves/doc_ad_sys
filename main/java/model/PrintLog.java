package model;

import java.sql.Timestamp;

public class PrintLog {

    private String    refNo;
    private String    documentType;
    private String    printedByName;
    private Timestamp printedAt;
    private String    campusCode;
    private String    studentName;
    private String    email;

    // ✅ Getters and Setters
    public String getRefNo() { return refNo; }
    public void setRefNo(String v) { refNo = v; }

    public String getDocumentType() {
        return documentType; }
    public void setDocumentType(String v) {
        documentType = v; }

    public String getPrintedByName() {
        return printedByName; }
    public void setPrintedByName(String v) {
        printedByName = v; }

    public Timestamp getPrintedAt() {
        return printedAt; }
    public void setPrintedAt(Timestamp v) {
        printedAt = v; }

    public String getCampusCode() {
        return campusCode; }
    public void setCampusCode(String v) {
        campusCode = v; }

    public String getStudentName() {
        return studentName; }
    public void setStudentName(String v) {
        studentName = v; }

    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
}