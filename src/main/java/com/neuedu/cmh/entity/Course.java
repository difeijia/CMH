package com.neuedu.cmh.entity;

public class Course {
    private String courseId;
    private String tenantId;
    private String courseName;
    private String courseCover;
    private String courseIntroduction;
    private String courseOrder;
    private String courseVideos;
    private String courseAuthor;

    // Getters and Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCover() {
        return courseCover;
    }

    public void setCourseCover(String courseCover) {
        this.courseCover = courseCover;
    }

    public String getCourseIntroduction() {
        return courseIntroduction;
    }

    public void setCourseIntroduction(String courseIntroduction) {
        this.courseIntroduction = courseIntroduction;
    }

    public String getCourseOrder() {
        return courseOrder;
    }

    public void setCourseOrder(String courseOrder) {
        this.courseOrder = courseOrder;
    }

    public String getCourseVideos() {
        return courseVideos;
    }

    public void setCourseVideos(String courseVideos) {
        this.courseVideos = courseVideos;
    }

    public String getCourseAuthor() {
        return courseAuthor;
    }

    public void setCourseAuthor(String courseAuthor) {
        this.courseAuthor = courseAuthor;
    }
}