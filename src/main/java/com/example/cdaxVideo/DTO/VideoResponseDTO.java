package com.example.cdaxVideo.DTO;

import com.example.cdaxVideo.Entity.Video;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoResponseDTO {
    private Long id;
    private String title;
    private String videoUrl;
    private String youtubeId;
    private Integer duration;
    private Integer displayOrder;
    
    @JsonProperty("isPreview")
    private Boolean isPreview;
    
    @JsonProperty("isLocked")
    private Boolean isLocked;
    
    @JsonProperty("isCompleted")
    private Boolean isCompleted;
    
    // Constructors
    public VideoResponseDTO() {}
    
    public VideoResponseDTO(Video video) {
        this.id = video.getId();
        this.title = video.getTitle();
        this.videoUrl = video.getVideoUrl();
        this.youtubeId = video.getYoutubeId();
        this.duration = video.getDuration();
        this.displayOrder = video.getDisplayOrder();
        this.isPreview = video.getIsPreview();
        this.isLocked = video.isLocked();
        this.isCompleted = video.isCompleted();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    
    public String getYoutubeId() { return youtubeId; }
    public void setYoutubeId(String youtubeId) { this.youtubeId = youtubeId; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    
    public Boolean getIsPreview() { return isPreview; }
    public void setIsPreview(Boolean isPreview) { this.isPreview = isPreview; }
    
    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }
    
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
}