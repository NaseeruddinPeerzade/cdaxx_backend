package com.example.cdaxVideo.Entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "userProgress"})
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int durationSec;

    // FIX: Add @JsonIgnoreProperties to prevent circular reference
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "userProgress"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // FIX: Add @JsonIgnoreProperties
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "userProgress"})
    private List<Video> videos = new ArrayList<>();
    
    @Transient
    @JsonProperty("isLocked")
    private boolean isLocked = true;

    @Transient
    @JsonProperty("assessmentLocked")
    private boolean assessmentLocked = true;

    // Constructors
    public Module() {}

    public Module(String title, int durationSec) {
        this.title = title;
        this.durationSec = durationSec;
    }

    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    // ========== CRITICAL FIX: Videos Collection Methods ==========
    
    // GETTER - Ensure collection is never null
    public List<Video> getVideos() { 
        if (this.videos == null) {
            this.videos = new ArrayList<>();
        }
        return videos; 
    }
    
    // OLD WRONG WAY - Causes orphan removal error:
    // public void setVideos(List<Video> videos) { this.videos = videos; }
    
    // NEW CORRECT WAY: Clear and add all elements
    public void setVideos(List<Video> videos) {
        // First, clear the existing collection properly
        if (this.videos != null) {
            this.videos.clear(); // This properly handles orphan removal
        } else {
            this.videos = new ArrayList<>();
        }
        
        // Then add all new videos
        if (videos != null) {
            for (Video video : videos) {
                addVideo(video); // Use helper method to maintain bidirectional relationship
            }
        }
    }
    
    // Helper method to add video (maintains bidirectional relationship)
    public void addVideo(Video video) {
        if (this.videos == null) {
            this.videos = new ArrayList<>();
        }
        if (!this.videos.contains(video)) {
            this.videos.add(video);
            video.setModule(this); // Set the back reference
        }
    }
    
    // Helper method to remove video
    public void removeVideo(Video video) {
        if (this.videos != null && this.videos.contains(video)) {
            this.videos.remove(video);
            video.setModule(null); // Remove the back reference
        }
    }
    
    // Alternative: If you don't want to modify the collection, remove orphanRemoval
    // OPTION 2: Change from orphanRemoval = true to just cascade
    // @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    // @OrderBy("displayOrder ASC")
    // @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "userProgress"})
    // private List<Video> videos = new ArrayList<>();
    
    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { this.isLocked = locked; }

    public boolean isAssessmentLocked() { return assessmentLocked; }
    public void setAssessmentLocked(boolean assessmentLocked) { this.assessmentLocked = assessmentLocked; }
    
    // ========== Equals and HashCode (Important for collections) ==========
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Module)) return false;
        return id != null && id.equals(((Module) o).getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}