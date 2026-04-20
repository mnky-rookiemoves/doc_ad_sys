
package model;

import java.sql.Date;

public class Student {

    private int studentId;
    private Integer userId;
    private String studentName;
    private Date birthDate;
    private String email;
    private Integer categoryId;
    private String remarks;
    private String phone;

    public Student() {}

    public Student(int studentId, Integer userId, String studentName, Date birthDate,
                   String email, Integer categoryId, String remarks, String phone) {
        this.studentId = studentId;
        this.userId = userId;
        this.studentName = studentName;
        this.birthDate = birthDate;
        this.email = email;
        this.categoryId = categoryId;
        this.remarks = remarks;
        this.phone = phone;
    }

    public int getStudentId() {
        return studentId;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getStudentName() {
        return studentName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getEmail() {
        return email;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getPhone() {
        return phone;
    }
}
