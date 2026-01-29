package com.example.cdaxVideo.Controller;

import com.example.cdaxVideo.DTO.Certificate.*;
import com.example.cdaxVideo.Service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class CertificateController {
    
    private final CertificateService certificateService;
    
    // Get user certificates
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserCertificates(
            @PathVariable @NotNull Long userId, // Add validation
            Authentication authentication) {
        
        // Validate user access
        if (!hasAccess(authentication, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "message", "Access denied"));
        }
        
        try {
            List<CertificateDTO> certificates = certificateService.getUserCertificates(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", certificates
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // Get certificate by ID
    @GetMapping("/{certificateId}")
    public ResponseEntity<?> getCertificateById(
            @PathVariable String certificateId,
            Authentication authentication) {
        
        try {
            CertificateDTO certificate = certificateService.getCertificateById(certificateId);
            
            // Check if user has access to this certificate
            if (!hasAccess(authentication, certificate.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", certificate
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // Check course completion
    @GetMapping("/course/{courseId}/completion-status")
    public ResponseEntity<?> checkCourseCompletion(
            @PathVariable Long courseId,
            @RequestParam Long userId,
            Authentication authentication) {
        
        if (!hasAccess(authentication, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "message", "Access denied"));
        }
        
        try {
            CertificateDTO certificate = certificateService.checkCourseCompletion(userId, courseId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", certificate
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // Generate certificate
    @PostMapping("/generate")
    public ResponseEntity<?> generateCertificate(
            @Valid @RequestBody GenerateCertificateRequestDTO request,
            Authentication authentication) {
        
        if (!hasAccess(authentication, request.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "message", "Access denied"));
        }
        
        try {
            CertificateDTO certificate = certificateService.generateCertificate(request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Certificate generated successfully",
                "data", certificate
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // Auto-generate certificate when course is completed
@PostMapping("/auto-generate/{courseId}")
public ResponseEntity<?> autoGenerateCertificate(
        @PathVariable @NotNull Long courseId, // Add validation
        Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        
        try {
            CertificateDTO certificate = certificateService.autoGenerateCertificate(userId, courseId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Certificate generated successfully",
                "data", certificate
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // Verify certificate (public endpoint)
    @GetMapping("/verify/{verificationCode}")
    public ResponseEntity<?> verifyCertificate(
            @PathVariable String verificationCode) {
        
        try {
            CertificateVerificationResponseDTO response = 
                certificateService.verifyCertificate(verificationCode);
            
            return ResponseEntity.ok(Map.of(
                "success", response.isValid(),
                "message", response.getMessage(),
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // Share certificate
    @PostMapping("/{certificateId}/share")
    public ResponseEntity<?> shareCertificate(
            @PathVariable String certificateId,
            Authentication authentication) {
        
        try {
            CertificateDTO certificate = certificateService.getCertificateById(certificateId);
            
            if (!hasAccess(authentication, certificate.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
            certificateService.shareCertificate(certificateId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Certificate shared successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // Download certificate PDF
    @GetMapping("/{certificateId}/download-pdf")
    public ResponseEntity<?> downloadCertificatePdf(
            @PathVariable String certificateId,
            Authentication authentication) {
        
        try {
            CertificateDTO certificate = certificateService.getCertificateById(certificateId);
            
            if (!hasAccess(authentication, certificate.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Access denied"));
            }
            
            // Here you would generate or fetch the PDF file
            // This is a placeholder - implement your PDF generation logic
            byte[] pdfBytes = generatePdfBytes(certificate);
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", 
                    "attachment; filename=\"certificate-" + certificate.getCertificateNumber() + ".pdf\"")
                .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // Get certificate analytics
    @GetMapping("/user/{userId}/analytics")
    public ResponseEntity<?> getCertificateAnalytics(
            @PathVariable Long userId,
            Authentication authentication) {
        
        if (!hasAccess(authentication, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "message", "Access denied"));
        }
        
        try {
            Map<String, Object> analytics = certificateService.getUserCertificateAnalytics(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", analytics
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // Helper methods
    private boolean hasAccess(Authentication authentication, Long targetUserId) {
        if (authentication == null) return false;
        
        String currentUserEmail = authentication.getName();
        // In a real implementation, you would get the user ID from the authentication
        // For now, we'll assume the user ID is passed as a Long
        // You might need to implement a UserDetailsService that includes user ID
        
        return true; // Simplified for now - implement proper access control
    }
    
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        // This is a placeholder - you need to implement based on your UserDetails
        // For example, if your UserDetails has getUserId() method:
        // return ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        
        // For now, return a default or throw an exception
        throw new RuntimeException("User ID extraction not implemented");
    }
    
    private byte[] generatePdfBytes(CertificateDTO certificate) {
        // Implement PDF generation logic here
        // You can use libraries like iText, Apache PDFBox, or generate HTML and convert to PDF
        // For now, return empty bytes
        return new byte[0];
    }
}