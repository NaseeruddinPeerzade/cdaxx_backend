package com.example.cdaxVideo.DTO;

import java.time.LocalDateTime;

public class CartItemDTO {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String thumbnailUrl;
    private Double price;
    private Integer duration; // in minutes
    private LocalDateTime addedAt;
    
    // Constructors
    public CartItemDTO() {}
    
    public CartItemDTO(Long id, Long courseId, String courseTitle, String thumbnailUrl, 
                      Double price, Integer duration, LocalDateTime addedAt) {
        this.id = id;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
        this.duration = duration;
        this.addedAt = addedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    public String getCourseTitle() {
        return courseTitle;
    }
    
    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public LocalDateTime getAddedAt() {
        return addedAt;
    }
    
    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}