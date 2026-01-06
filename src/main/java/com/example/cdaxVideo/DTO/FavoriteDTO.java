// FavoriteDTO.java
package com.example.cdaxVideo.DTO;

import java.time.LocalDateTime;

public class FavoriteDTO {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseThumbnail;
    private Double coursePrice;
    private LocalDateTime addedAt;
    
    // Constructors
    public FavoriteDTO() {}
    
    public FavoriteDTO(Long id, Long courseId, String courseTitle, String courseThumbnail, Double coursePrice, LocalDateTime addedAt) {
        this.id = id;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.courseThumbnail = courseThumbnail;
        this.coursePrice = coursePrice;
        this.addedAt = addedAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    
    public String getCourseThumbnail() { return courseThumbnail; }
    public void setCourseThumbnail(String courseThumbnail) { this.courseThumbnail = courseThumbnail; }
    
    public Double getCoursePrice() { return coursePrice; }
    public void setCoursePrice(Double coursePrice) { this.coursePrice = coursePrice; }
    
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}