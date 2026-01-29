package com.example.cdaxVideo.DTO;

import java.time.LocalDateTime;

public class FavoriteDTO {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseThumbnail;
    private Double coursePrice; // Current price (after discount if any)
    private LocalDateTime addedAt;
    
    // NEW DISCOUNT FIELDS (required by FavoriteCourseService)
    private boolean hasDiscount;
    private Double originalPrice;
    private Double discountedPrice;
    private Double discountPercentage;
    
    // Optional additional course details
    private String courseDescription;
    private String courseInstructor;
    private Double courseRating;
    
    // Constructors
    public FavoriteDTO() {}
    
    public FavoriteDTO(Long id, Long courseId, String courseTitle, String courseThumbnail, 
                      Double coursePrice, LocalDateTime addedAt) {
        this.id = id;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.courseThumbnail = courseThumbnail;
        this.coursePrice = coursePrice;
        this.addedAt = addedAt;
        this.hasDiscount = false;
        this.originalPrice = coursePrice;
        this.discountedPrice = coursePrice;
        this.discountPercentage = 0.0;
    }
    
    // Getters and Setters for existing fields
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
    
    // NEW: Getters and Setters for discount fields
    public boolean isHasDiscount() { return hasDiscount; }
    public void setHasDiscount(boolean hasDiscount) { this.hasDiscount = hasDiscount; }
    
    public Double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }
    
    public Double getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(Double discountedPrice) { this.discountedPrice = discountedPrice; }
    
    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }
    
    // Getters and Setters for additional fields
    public String getCourseDescription() { return courseDescription; }
    public void setCourseDescription(String courseDescription) { this.courseDescription = courseDescription; }
    
    public String getCourseInstructor() { return courseInstructor; }
    public void setCourseInstructor(String courseInstructor) { this.courseInstructor = courseInstructor; }
    
    public Double getCourseRating() { return courseRating; }
    public void setCourseRating(Double courseRating) { this.courseRating = courseRating; }
    
    // Helper method to calculate discount
    public void calculateDiscount(Double original, Double discounted) {
        if (original != null && discounted != null && original > 0 && discounted < original) {
            this.hasDiscount = true;
            this.originalPrice = original;
            this.discountedPrice = discounted;
            this.coursePrice = discounted; // Set current price to discounted price
            this.discountPercentage = ((original - discounted) / original) * 100;
        } else {
            this.hasDiscount = false;
            this.originalPrice = original;
            this.discountedPrice = original;
            this.coursePrice = original;
            this.discountPercentage = 0.0;
        }
    }
}   