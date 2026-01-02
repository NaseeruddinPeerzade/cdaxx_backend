package com.example.cdaxVideo.Entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user_module_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "module_id"}))
public class UserModuleProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Module module;

    private boolean unlocked = false;
    private boolean assessmentPassed = false;
    
    // 🔥 CRITICAL FIX: Add these missing fields
    private boolean completed = false;
    private Date completedOn;
    
    private Date unlockedOn;
    private Date assessmentPassedOn;

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { 
        this.unlocked = unlocked;
        if (unlocked && this.unlockedOn == null) {
            this.unlockedOn = new Date();
        }
    }

    public boolean isAssessmentPassed() { return assessmentPassed; }
    public void setAssessmentPassed(boolean assessmentPassed) { 
        this.assessmentPassed = assessmentPassed;
        if (assessmentPassed && this.assessmentPassedOn == null) {
            this.assessmentPassedOn = new Date();
        }
    }
    
    // 🔥 CRITICAL FIX: Add these getters/setters
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { 
        this.completed = completed;
        if (completed && this.completedOn == null) {
            this.completedOn = new Date();
        }
    }
    
    public Date getCompletedOn() { return completedOn; }
    public void setCompletedOn(Date completedOn) { this.completedOn = completedOn; }

    public Date getUnlockedOn() { return unlockedOn; }
    public void setUnlockedOn(Date unlockedOn) { this.unlockedOn = unlockedOn; }

    public Date getAssessmentPassedOn() { return assessmentPassedOn; }
    public void setAssessmentPassedOn(Date assessmentPassedOn) { this.assessmentPassedOn = assessmentPassedOn; }
    
    // Helper method to mark module as completed (when assessment is passed)
    public void markAsCompleted() {
        this.completed = true;
        this.completedOn = new Date();
        this.assessmentPassed = true; // Module is considered passed when completed
        if (this.assessmentPassedOn == null) {
            this.assessmentPassedOn = new Date();
        }
    }
}