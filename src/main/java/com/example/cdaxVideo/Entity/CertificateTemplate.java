package com.example.cdaxVideo.Entity;

import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificate_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "template_html", columnDefinition = "text", nullable = false)
    private String templateHtml;
    
    @Column(name = "template_css", columnDefinition = "text")
    private String templateCss;
    
    @Column(name = "background_image_url")
    private String backgroundImageUrl;
    
    @Column(name = "signature_image_url")
    private String signatureImageUrl;
    
    @Column(name = "default_fields", columnDefinition = "jsonb")
    private String defaultFields;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}