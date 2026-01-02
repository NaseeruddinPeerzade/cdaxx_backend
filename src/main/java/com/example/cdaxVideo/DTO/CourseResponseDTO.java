package com.example.cdaxVideo.DTO;

import com.example.cdaxVideo.Entity.Course;
import com.example.cdaxVideo.Entity.Module;
import com.example.cdaxVideo.Entity.Video;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String shortDescription;
    private String instructor;
    private String instructorId;
    private String thumbnailUrl;
    private String bannerImage;
    private Double price;
    private Double discountPrice;
    private Double rating;
    private Integer totalRatings;
    private Integer enrolledStudents;
    private Integer totalDuration;
    private String level;
    private String category;
    private String subCategory;
    private List<String> tags = new ArrayList<>();
    private Boolean isPublished;
    private Boolean isFeatured;
    private Boolean isPopular;
    private Boolean isSubscribed;
    private Boolean isPurchased;
    
    // Progress fields
    private Double progressPercent;
    private Integer totalVideos;
    private Integer completedVideos;
    private Integer totalDurationSec;
    private String formattedDuration;
    private Integer totalModules;
    private Integer unlockedModules;
    private Boolean isCompleted;
    private Integer completedModules;
    
    private List<ModuleResponseDTO> modules = new ArrayList<>();
    
    // Default constructor
    public CourseResponseDTO() {}
    
    // Constructor from Course entity
    public CourseResponseDTO(Course course) {
        this.id = course.getId();
        this.title = course.getTitle();
        this.description = course.getDescription();
        this.shortDescription = course.getShortDescription();
        this.instructor = course.getInstructor();
        this.instructorId = course.getInstructorId();
        this.thumbnailUrl = course.getThumbnailUrl();
        this.bannerImage = course.getBannerImage();
        this.price = course.getPrice();
        this.discountPrice = course.getDiscountPrice();
        this.rating = course.getRating();
        this.totalRatings = course.getTotalRatings();
        this.enrolledStudents = course.getEnrolledStudents();
        this.totalDuration = course.getTotalDuration();
        this.level = course.getLevel();
        this.category = course.getCategory();
        this.subCategory = course.getSubCategory();
        this.tags = course.getTags() != null ? course.getTags() : new ArrayList<>();
        this.isPublished = course.getIsPublished();
        this.isFeatured = course.getIsFeatured();
        this.isPopular = course.getIsPopular();
        this.formattedDuration = course.getFormattedDuration();
        this.isPurchased = course.isPurchased();
        this.isSubscribed = course.isPurchased(); // Assuming purchased = subscribed
        
        // Calculate derived fields
        calculateDerivedFields(course);
        
        // Map modules if they exist
        if (course.getModules() != null && !course.getModules().isEmpty()) {
            this.modules = course.getModules().stream()
                .map(ModuleResponseDTO::new)
                .collect(Collectors.toList());
        }
    }
    
    private void calculateDerivedFields(Course course) {
        int totalVideosCount = 0;
        int completedVideosCount = 0;
        int totalModulesCount = 0;
        int unlockedModulesCount = 0;
        int completedModulesCount = 0;
        
        if (course.getModules() != null) {
            totalModulesCount = course.getModules().size();
            
            for (Module module : course.getModules()) {
                // Check if module is unlocked
                if (!module.isLocked()) {
                    unlockedModulesCount++;
                }
                
                // Initialize module completion check
                boolean moduleCompleted = false;
                
                // Count videos
                if (module.getVideos() != null && !module.getVideos().isEmpty()) {
                    totalVideosCount += module.getVideos().size();
                    
                    // Check if module is completed (all videos completed)
                    moduleCompleted = true;
                    
                    for (Video video : module.getVideos()) {
                        if (video.isCompleted()) {
                            completedVideosCount++;
                        } else {
                            moduleCompleted = false;
                        }
                    }
                    
                    // Module is only completed if it's unlocked AND all videos are completed
                    if (!module.isLocked() && moduleCompleted) {
                        completedModulesCount++;
                    }
                }
            }
        }
        
        // Set all calculated fields
        this.totalVideos = totalVideosCount;
        this.completedVideos = completedVideosCount;
        this.totalModules = totalModulesCount;
        this.unlockedModules = unlockedModulesCount;
        this.completedModules = completedModulesCount;
        this.totalDurationSec = course.getTotalDuration() != null ? course.getTotalDuration() * 60 : 0;
        
        // Calculate progress percentage
        if (totalVideosCount > 0) {
            double progress = (double) completedVideosCount / totalVideosCount;
            this.progressPercent = progress * 100;
        } else {
            this.progressPercent = 0.0;
        }
        
        this.isCompleted = completedVideosCount == totalVideosCount && totalVideosCount > 0;
    }
    
    // Getters and setters for all fields...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    
    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }
    
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    
    public String getBannerImage() { return bannerImage; }
    public void setBannerImage(String bannerImage) { this.bannerImage = bannerImage; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Double getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(Double discountPrice) { this.discountPrice = discountPrice; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    
    public Integer getEnrolledStudents() { return enrolledStudents; }
    public void setEnrolledStudents(Integer enrolledStudents) { this.enrolledStudents = enrolledStudents; }
    
    public Integer getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Integer totalDuration) { this.totalDuration = totalDuration; }
    
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    @JsonProperty("isPublished")
    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }
    
    @JsonProperty("isFeatured")
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    
    @JsonProperty("isPopular")
    public Boolean getIsPopular() { return isPopular; }
    public void setIsPopular(Boolean isPopular) { this.isPopular = isPopular; }
    
    @JsonProperty("isSubscribed")
    public Boolean getIsSubscribed() { return isSubscribed; }
    public void setIsSubscribed(Boolean isSubscribed) { this.isSubscribed = isSubscribed; }
    
    @JsonProperty("isPurchased")
    public Boolean getIsPurchased() { return isPurchased; }
    public void setIsPurchased(Boolean isPurchased) { this.isPurchased = isPurchased; }
    
    @JsonProperty("progressPercent")
    public Double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Double progressPercent) { this.progressPercent = progressPercent; }
    
    public Integer getTotalVideos() { return totalVideos; }
    public void setTotalVideos(Integer totalVideos) { this.totalVideos = totalVideos; }
    
    public Integer getCompletedVideos() { return completedVideos; }
    public void setCompletedVideos(Integer completedVideos) { this.completedVideos = completedVideos; }
    
    public Integer getTotalDurationSec() { return totalDurationSec; }
    public void setTotalDurationSec(Integer totalDurationSec) { this.totalDurationSec = totalDurationSec; }
    
    public String getFormattedDuration() { return formattedDuration; }
    public void setFormattedDuration(String formattedDuration) { this.formattedDuration = formattedDuration; }
    
    public Integer getTotalModules() { return totalModules; }
    public void setTotalModules(Integer totalModules) { this.totalModules = totalModules; }
    
    public Integer getUnlockedModules() { return unlockedModules; }
    public void setUnlockedModules(Integer unlockedModules) { this.unlockedModules = unlockedModules; }
    
    @JsonProperty("isCompleted")
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    
    public Integer getCompletedModules() { return completedModules; }
    public void setCompletedModules(Integer completedModules) { this.completedModules = completedModules; }
    
    public List<ModuleResponseDTO> getModules() { return modules; }
    public void setModules(List<ModuleResponseDTO> modules) { this.modules = modules; }
}