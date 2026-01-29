package com.example.cdaxVideo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnore
    private Course course;
    
    @Column(name = "certificate_number", unique = true, nullable = false)
    private String certificateNumber;
    
    @Column(name = "user_name", nullable = false)
    private String userName;
    
    @Column(name = "course_name", nullable = false)
    private String courseName;
    
    @Column(name = "completion_date", nullable = false)
    private LocalDateTime completionDate;
    
    @Column(name = "issued_date", nullable = false)
    @Builder.Default
    private LocalDateTime issuedDate = LocalDateTime.now();
    
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;
    
    @Column(name = "certificate_url")
    private String certificateUrl;
    
    @Column(name = "verification_code", unique = true, nullable = false)
    private String verificationCode;
    
    @Column(name = "certificate_data", columnDefinition = "TEXT")
    private String certificateData;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        
        // Only set if not already set by builder
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        
        if (this.issuedDate == null) {
            this.issuedDate = LocalDateTime.now();
        }
        
        if (this.verificationCode == null) {
            this.verificationCode = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 16)
                .toUpperCase();
        }
        
        if (this.certificateNumber == null) {
            this.certificateNumber = generateCertificateNumber();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    private String generateCertificateNumber() {
        String year = String.format("%04d", LocalDateTime.now().getYear());
        String month = String.format("%02d", LocalDateTime.now().getMonthValue());
        String day = String.format("%02d", LocalDateTime.now().getDayOfMonth());
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        return "CERT-" + year + month + day + "-" + random;
    }
}