package com.example.cdaxVideo.Entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user_assessment_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "assessment_id"}))
public class UserAssessmentProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;
    
    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(nullable = false)
    private boolean passed = false;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date passedOn;
    
    @Column(nullable = false)
    private Boolean unlocked = false;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date unlockedOn;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date submittedOn;
    
    // NEW FIELDS for storing assessment scores
    private Integer obtainedMarks;
    
    private Integer totalMarks;
    
    private Double percentage;
    
    // Constructors
    public UserAssessmentProgress() {}
    
    public UserAssessmentProgress(User user, Assessment assessment) {
        this.user = user;
        this.assessment = assessment;
        this.attempts = 0;
        this.passed = false;
        this.unlocked = false;
    }
    
    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { 
        this.passed = passed;
        if (passed && this.passedOn == null) {
            this.passedOn = new Date();
        }
    }

    public Date getPassedOn() { return passedOn; }
    public void setPassedOn(Date passedOn) { this.passedOn = passedOn; }
    
    public Integer getAttempts() {
        return attempts == null ? 0 : attempts;
    }
    
    public void setAttempts(Integer attempts) {
        this.attempts = (attempts == null ? 0 : attempts);
    }
    
    public Boolean getUnlocked() {
        return unlocked == null ? false : unlocked;
    }
    
    public void setUnlocked(Boolean unlocked) {
        this.unlocked = (unlocked == null ? false : unlocked);
        if (unlocked && this.unlockedOn == null) {
            this.unlockedOn = new Date();
        }
    }
    
    // New getters and setters
    public Date getUnlockedOn() { return unlockedOn; }
    public void setUnlockedOn(Date unlockedOn) { this.unlockedOn = unlockedOn; }
    
    public Date getSubmittedOn() { return submittedOn; }
    public void setSubmittedOn(Date submittedOn) { this.submittedOn = submittedOn; }
    
    public Integer getObtainedMarks() { return obtainedMarks; }
    public void setObtainedMarks(Integer obtainedMarks) { this.obtainedMarks = obtainedMarks; }
    
    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { 
        this.percentage = percentage;
        // Auto-set passed status based on percentage (70% threshold)
        if (percentage != null && percentage >= 70.0 && !this.passed) {
            this.passed = true;
            if (this.passedOn == null) {
                this.passedOn = new Date();
            }
        }
    }
    
    // Helper method to calculate and set results
    public void setResults(Integer obtainedMarks, Integer totalMarks) {
        this.obtainedMarks = obtainedMarks;
        this.totalMarks = totalMarks;
        this.submittedOn = new Date();
        
        if (totalMarks != null && totalMarks > 0) {
            this.percentage = (obtainedMarks.doubleValue() / totalMarks.doubleValue()) * 100.0;
            
            // Auto-set passed if percentage >= 70%
            if (this.percentage >= 70.0) {
                this.passed = true;
                if (this.passedOn == null) {
                    this.passedOn = new Date();
                }
            } else {
                this.passed = false;
            }
        }
        
        // Increment attempts
        this.attempts = (this.attempts == null ? 1 : this.attempts + 1);
    }
    
    // Helper method to check if assessment is available (unlocked and not passed)
    public boolean isAvailableForAttempt() {
        return Boolean.TRUE.equals(this.unlocked) && !this.passed;
    }
    
    // Helper method to check if user can retake (failed but unlocked)
    public boolean canRetake() {
        return Boolean.TRUE.equals(this.unlocked) && !this.passed;
    }
    
    // Helper method to get formatted percentage
    public String getFormattedPercentage() {
        if (percentage == null) return "N/A";
        return String.format("%.1f%%", percentage);
    }
    
    @Override
    public String toString() {
        return "UserAssessmentProgress{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", assessmentId=" + (assessment != null ? assessment.getId() : null) +
                ", attempts=" + attempts +
                ", passed=" + passed +
                ", unlocked=" + unlocked +
                ", obtainedMarks=" + obtainedMarks +
                ", totalMarks=" + totalMarks +
                ", percentage=" + percentage +
                '}';
    }
}