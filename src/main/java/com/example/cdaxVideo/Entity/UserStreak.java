package com.example.cdaxVideo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_streaks", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id", "streak_date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStreak {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "streak_date", nullable = false)
    private LocalDate streakDate;
    
    @Column(name = "watched_seconds", nullable = false)
    private Integer watchedSeconds = 0;
    
    @Column(name = "total_available_seconds", nullable = false)
    private Integer totalAvailableSeconds = 0;
    
    @Column(name = "progress_percentage", nullable = false)
    private Double progressPercentage = 0.0;
    
    @Column(name = "completed_videos_count", nullable = false)
    private Integer completedVideosCount = 0;
    
    @Column(name = "total_videos_count", nullable = false)
    private Integer totalVideosCount = 0;
    
    @Column(name = "is_active_day", nullable = false)
    private Boolean isActiveDay = false;
    
    @Column(name = "video_details", columnDefinition = "TEXT")
    private String videoDetails;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper method
    public void addWatchedSeconds(Integer seconds) {
        if (seconds != null && seconds > 0) {
            this.watchedSeconds = (this.watchedSeconds == null ? 0 : this.watchedSeconds) + seconds;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Calculate percentage
        if (totalAvailableSeconds != null && totalAvailableSeconds > 0) {
            progressPercentage = (watchedSeconds.doubleValue() / totalAvailableSeconds) * 100.0;
        }
        
        // Determine if active day
        isActiveDay = watchedSeconds != null && watchedSeconds > 0;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        
        // Recalculate percentage
        if (totalAvailableSeconds != null && totalAvailableSeconds > 0) {
            progressPercentage = (watchedSeconds.doubleValue() / totalAvailableSeconds) * 100.0;
        }
        
        // Re-determine active day
        isActiveDay = watchedSeconds != null && watchedSeconds > 0;
    }
}