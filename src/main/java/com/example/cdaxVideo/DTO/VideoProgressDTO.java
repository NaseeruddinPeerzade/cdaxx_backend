package com.example.cdaxVideo.DTO;

public class VideoProgressDTO {
    private Long videoId;
    private Long userId;
    private Integer watchedSeconds;
    private Integer lastPositionSeconds;
    private Integer forwardJumpsCount;
    private boolean completed;
    private boolean unlocked;

    // Default constructor
    public VideoProgressDTO() {}

    // Parameterized constructor
    public VideoProgressDTO(Long videoId, Long userId, Integer watchedSeconds, 
                           Integer lastPositionSeconds, Integer forwardJumpsCount) {
        this.videoId = videoId;
        this.userId = userId;
        this.watchedSeconds = watchedSeconds;
        this.lastPositionSeconds = lastPositionSeconds;
        this.forwardJumpsCount = forwardJumpsCount;
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

    public Integer getWatchedSeconds() {
        return watchedSeconds;
    }

    public void setWatchedSeconds(Integer watchedSeconds) {
        this.watchedSeconds = watchedSeconds;
    }

    public Integer getLastPositionSeconds() {
        return lastPositionSeconds;
    }

    public void setLastPositionSeconds(Integer lastPositionSeconds) {
        this.lastPositionSeconds = lastPositionSeconds;
    }

    public Integer getForwardJumpsCount() {
        return forwardJumpsCount;
    }

    public void setForwardJumpsCount(Integer forwardJumpsCount) {
        this.forwardJumpsCount = forwardJumpsCount;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    @Override
    public String toString() {
        return "VideoProgressDTO{" +
                "videoId=" + videoId +
                ", userId=" + userId +
                ", watchedSeconds=" + watchedSeconds +
                ", lastPositionSeconds=" + lastPositionSeconds +
                ", forwardJumpsCount=" + forwardJumpsCount +
                ", completed=" + completed +
                ", unlocked=" + unlocked +
                '}';
    }
}