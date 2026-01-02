package com.example.cdaxVideo.DTO;

public class VideoCompletionRequestDTO {
    private Long videoId;
    private Long userId;
    private Long courseId;
    private Long moduleId;

    // Default constructor
    public VideoCompletionRequestDTO() {}

    // Parameterized constructor
    public VideoCompletionRequestDTO(Long videoId, Long userId, Long courseId, Long moduleId) {
        this.videoId = videoId;
        this.userId = userId;
        this.courseId = courseId;
        this.moduleId = moduleId;
    }

    // Getters and Setters
    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public String toString() {
        return "VideoCompletionRequest{" +
                "videoId=" + videoId +
                ", userId=" + userId +
                ", courseId=" + courseId +
                ", moduleId=" + moduleId +
                '}';
    }
}