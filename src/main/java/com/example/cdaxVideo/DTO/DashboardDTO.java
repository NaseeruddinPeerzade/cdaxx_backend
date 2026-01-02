package com.example.cdaxVideo.DTO;
import java.util.List;

public class DashboardDTO {

    private Long totalUsers;
    private Long newUsers;
    private Long totalCourses;
    private List<CourseResponseDTO> courses; // dynamic courses list

    public DashboardDTO() {}

    public DashboardDTO(Long totalUsers, Long newUsers, Long totalCourses, List<CourseResponseDTO> courses) {
        this.totalUsers = totalUsers;
        this.newUsers = newUsers;
        this.totalCourses = totalCourses;
        this.courses = courses;
    }

    // Getters and Setters
    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getNewUsers() {
        return newUsers;
    }

    public void setNewUsers(Long newUsers) {
        this.newUsers = newUsers;
    }

    public Long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(Long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public List<CourseResponseDTO> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseResponseDTO> courses) {
        this.courses = courses;
    }
}
