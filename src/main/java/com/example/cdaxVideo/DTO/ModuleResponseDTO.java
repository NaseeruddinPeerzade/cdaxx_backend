package com.example.cdaxVideo.DTO;

import com.example.cdaxVideo.Entity.Module;
import com.example.cdaxVideo.Entity.Video;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleResponseDTO {
    private Long id;
    private String title;
    private Integer durationSec;
    
    @JsonProperty("isLocked")
    private Boolean isLocked;
    
    @JsonProperty("assessmentLocked")
    private Boolean assessmentLocked;
    
    private List<VideoResponseDTO> videos;
    private Integer videoCount;
    
    // NEW FIELDS for dashboard stats
    @JsonProperty("isCompleted")
    private Boolean isCompleted;        // NEW: Whether module is fully completed
    
    private Integer completedVideos;    // NEW: Count of completed videos in this module
    
    private Integer totalVideos;        // NEW: Total videos in this module (same as videoCount but explicit)
    
    private Integer progressPercent;    // NEW: Progress percentage (0-100)
    
    // Constructors
    public ModuleResponseDTO() {}
    
    public ModuleResponseDTO(Module module) {
        this.id = module.getId();
        this.title = module.getTitle();
        this.durationSec = module.getDurationSec();
        this.isLocked = module.isLocked();
        this.assessmentLocked = module.isAssessmentLocked();
        
        // Initialize new fields with default values
        this.isCompleted = false;
        this.completedVideos = 0;
        this.progressPercent = 0;
        
        if (module.getVideos() != null && !module.getVideos().isEmpty()) {
            this.videos = module.getVideos().stream()
                .sorted(Comparator.comparing(Video::getDisplayOrder))
                .map(VideoResponseDTO::new)
                .collect(Collectors.toList());
            this.videoCount = this.videos.size();
            this.totalVideos = this.videoCount; // Set totalVideos same as videoCount
        } else {
            this.videoCount = 0;
            this.totalVideos = 0;
        }
    }
    
    // Getters and Setters for existing fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
    
    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }
    
    public Boolean getAssessmentLocked() { return assessmentLocked; }
    public void setAssessmentLocked(Boolean assessmentLocked) { this.assessmentLocked = assessmentLocked; }
    
    public List<VideoResponseDTO> getVideos() { return videos; }
    public void setVideos(List<VideoResponseDTO> videos) { 
        this.videos = videos;
        this.videoCount = videos != null ? videos.size() : 0;
        this.totalVideos = this.videoCount; // Update totalVideos
    }
    
    public Integer getVideoCount() { return videoCount; }
    public void setVideoCount(Integer videoCount) { 
        this.videoCount = videoCount;
        this.totalVideos = videoCount; // Update totalVideos
    }
    
    // NEW GETTERS AND SETTERS
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    
    public Integer getCompletedVideos() { return completedVideos; }
    public void setCompletedVideos(Integer completedVideos) { 
        this.completedVideos = completedVideos;
        // Auto-calculate progress percentage when completedVideos is set
        if (this.totalVideos != null && this.totalVideos > 0) {
            this.progressPercent = (completedVideos * 100) / this.totalVideos;
            // Auto-calculate isCompleted
            this.isCompleted = completedVideos.equals(this.totalVideos);
        } else {
            this.progressPercent = 0;
            this.isCompleted = false;
        }
    }
    
    public Integer getTotalVideos() { return totalVideos; }
    public void setTotalVideos(Integer totalVideos) { 
        this.totalVideos = totalVideos;
        // Auto-calculate progress percentage when totalVideos is set
        if (totalVideos != null && totalVideos > 0 && this.completedVideos != null) {
            this.progressPercent = (this.completedVideos * 100) / totalVideos;
            this.isCompleted = this.completedVideos.equals(totalVideos);
        }
    }
    
    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { 
        this.progressPercent = progressPercent;
        // Auto-calculate isCompleted when progress is 100%
        if (progressPercent != null && progressPercent >= 100) {
            this.isCompleted = true;
            if (this.totalVideos != null) {
                this.completedVideos = this.totalVideos;
            }
        }
    }
    
    // Helper method to calculate stats from videos
    public void calculateStatsFromVideos() {
        if (this.videos != null) {
            this.totalVideos = this.videos.size();
            
            // Count completed videos
            long completedCount = this.videos.stream()
                .filter(video -> Boolean.TRUE.equals(video.getIsCompleted()))
                .count();
            this.completedVideos = (int) completedCount;
            
            // Calculate progress percentage
            if (this.totalVideos > 0) {
                this.progressPercent = (this.completedVideos * 100) / this.totalVideos;
            } else {
                this.progressPercent = 0;
            }
            
            // Determine if module is completed
            this.isCompleted = this.totalVideos > 0 && 
                              this.completedVideos.equals(this.totalVideos) && 
                              Boolean.FALSE.equals(this.isLocked);
        } else {
            this.totalVideos = 0;
            this.completedVideos = 0;
            this.progressPercent = 0;
            this.isCompleted = false;
        }
    }
}