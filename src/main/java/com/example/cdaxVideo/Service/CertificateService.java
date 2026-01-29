// package com.example.cdaxVideo.Service;

// import com.example.cdaxVideo.DTO.Certificate.*;
// import com.example.cdaxVideo.Entity.*;
// import com.example.cdaxVideo.Repository.*;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.*;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// @Slf4j
// @SuppressWarnings("unused")
// public class CertificateService {
    
//     private final CertificateRepository certificateRepository;
//     private final UserRepository userRepository;
//     private final CourseRepository courseRepository;
//     private final UserCoursePurchaseRepository userCoursePurchaseRepository;
//     private final UserVideoProgressRepository userVideoProgressRepository;
//     private final ObjectMapper objectMapper;
    
//     // Get user certificates
//     public List<CertificateDTO> getUserCertificates(Long userId) {
//         List<Certificate> certificates = certificateRepository
//             .findByUserIdAndIsActiveTrue(userId);
        
//         return certificates.stream()
//             .map(this::convertToDTO)
//             .collect(Collectors.toList());
//     }
    
//     // Get certificate by ID
//     public CertificateDTO getCertificateById(String certificateId) {
//         Certificate certificate = certificateRepository.findById(certificateId)
//             .orElseThrow(() -> new RuntimeException("Certificate not found"));
        
//         return convertToDTO(certificate);
//     }
    
//     // Check if user has completed a course
//     public CertificateDTO checkCourseCompletion(Long userId, Long courseId) {
//         Optional<Certificate> certificate = certificateRepository
//             .findByUserIdAndCourseId(userId, courseId);
        
//         return certificate.map(this::convertToDTO).orElse(null);
//     }
    
//     // Generate certificate
//     @Transactional
//     public CertificateDTO generateCertificate(GenerateCertificateRequestDTO request) {
//         // Check if certificate already exists
//         if (certificateRepository.existsByUserIdAndCourseId(request.getUserId(), request.getCourseId())) {
//             throw new RuntimeException("Certificate already exists for this course");
//         }
        
//         // Validate user and course
//         User user = userRepository.findById(request.getUserId())
//             .orElseThrow(() -> new RuntimeException("User not found"));
        
//         Course course = courseRepository.findById(request.getCourseId())
//             .orElseThrow(() -> new RuntimeException("Course not found"));
        
//         // Check if user has purchased the course
//         boolean hasPurchased = userCoursePurchaseRepository
//             .existsByUserIdAndCourseId(request.getUserId(), request.getCourseId());
        
//         if (!hasPurchased) {
//             throw new RuntimeException("User hasn't purchased this course");
//         }
        
//         // Create certificate data JSON
//         CertificateDataDTO certificateData = CertificateDataDTO.builder()
//             .grade(request.getGrade())
//             .totalModules(request.getTotalModules())
//             .completedModules(request.getCompletedModules())
//             .instructorName("Course Instructor")
//             .additionalData(Map.of("generatedAt", LocalDateTime.now().toString()))
//             .build();
        
//         // Calculate completion duration (simplified)
//         String completionDuration = "1 week";
        
//         // Generate certificate URL
//         String certificateUrl = "https://yourdomain.com/certificates/" + UUID.randomUUID() + ".pdf";
        
//         // Create certificate entity
//         Certificate certificate = Certificate.builder()
//             .user(user)
//             .course(course)
//             .userName(user.getFullName())
//             .courseName(course.getTitle())
//             .completionDate(request.getCompletionDate())
//             .certificateData(convertToJson(certificateData))
//             .certificateUrl(certificateUrl)
//             .isActive(true)
//             .build();
        
//         Certificate savedCertificate = certificateRepository.save(certificate);
        
//         log.info("Certificate generated: {} for user: {} course: {}", 
//             savedCertificate.getCertificateNumber(), user.getId(), course.getId());
        
//         return convertToDTO(savedCertificate);
//     }
    
//     // Automatically generate certificate when course is completed
//     @Transactional
//     public CertificateDTO autoGenerateCertificate(Long userId, Long courseId) {
//         Course course = courseRepository.findById(courseId)
//             .orElseThrow(() -> new RuntimeException("Course not found"));
        
//         // Check if auto-generation is enabled
//         Boolean autoGenerate = course.getAutoGenerateCertificate();
//         if (autoGenerate == null || !autoGenerate) {
//             throw new RuntimeException("Auto certificate generation is disabled for this course");
//         }
        
//         // Calculate grade (simplified - implement your own logic)
//         Double grade = 85.0;
        
//         // Check minimum grade requirement
//         Double requiresMinimumGrade = course.getRequiresMinimumGrade();
//         if (requiresMinimumGrade != null && grade < requiresMinimumGrade) {
//             throw new RuntimeException("Minimum grade not met for certificate");
//         }
        
//         // Count modules (simplified)
//         int totalModules = course.getModules() != null ? course.getModules().size() : 0;
//         int completedModules = totalModules; // Assuming all modules are completed
        
//         // Create request
//         GenerateCertificateRequestDTO request = GenerateCertificateRequestDTO.builder()
//             .userId(userId)
//             .courseId(courseId)
//             .grade(grade)
//             .totalModules(totalModules)
//             .completedModules(completedModules)
//             .completionDate(LocalDateTime.now())
//             .build();
        
//         return generateCertificate(request);
//     }
    
//     // Verify certificate
//     public CertificateVerificationResponseDTO verifyCertificate(String verificationCode) {
//         Optional<Certificate> certificateOpt = certificateRepository
//             .findByVerificationCode(verificationCode.toUpperCase());
        
//         if (certificateOpt.isEmpty()) {
//             return CertificateVerificationResponseDTO.builder()
//                 .isValid(false)
//                 .message("Certificate not found or invalid verification code")
//                 .verifiedAt(LocalDateTime.now())
//                 .build();
//         }
        
//         Certificate certificate = certificateOpt.get();
        
//         if (!certificate.isActive()) {
//             return CertificateVerificationResponseDTO.builder()
//                 .isValid(false)
//                 .message("Certificate has been revoked")
//                 .verifiedAt(LocalDateTime.now())
//                 .build();
//         }
        
//         if (certificate.getExpirationDate() != null && 
//             certificate.getExpirationDate().isBefore(LocalDateTime.now())) {
//             return CertificateVerificationResponseDTO.builder()
//                 .isValid(false)
//                 .message("Certificate has expired")
//                 .verifiedAt(LocalDateTime.now())
//                 .build();
//         }
        
//         return CertificateVerificationResponseDTO.builder()
//             .isValid(true)
//             .certificate(convertToDTO(certificate))
//             .message("Certificate is valid")
//             .verifiedAt(LocalDateTime.now())
//             .build();
//     }
    
//     // Share certificate
//     public void shareCertificate(String certificateId) {
//         Certificate certificate = certificateRepository.findById(certificateId)
//             .orElseThrow(() -> new RuntimeException("Certificate not found"));
        
//         // Log sharing activity
//         log.info("Certificate {} shared at {}", 
//             certificate.getCertificateNumber(), 
//             LocalDateTime.now());
//     }
    
//     // Get certificate analytics
//     public Map<String, Object> getUserCertificateAnalytics(Long userId) {
//         long totalCertificates = certificateRepository.countByUserId(userId);
//         List<Certificate> certificates = certificateRepository.findByUserId(userId);
        
//         Map<String, Object> analytics = new HashMap<>();
//         analytics.put("totalCertificates", totalCertificates);
//         analytics.put("recentCertificates", certificates.stream()
//             .limit(5)
//             .map(this::convertToDTO)
//             .collect(Collectors.toList()));
        
//         // Calculate average grade
//         double averageGrade = certificates.stream()
//             .mapToDouble(c -> {
//                 try {
//                     CertificateDataDTO data = objectMapper.readValue(
//                         c.getCertificateData(), CertificateDataDTO.class);
//                     return data.getGrade() != null ? data.getGrade() : 0.0;
//                 } catch (Exception e) {
//                     return 0.0;
//                 }
//             })
//             .average()
//             .orElse(0.0);
        
//         analytics.put("averageGrade", Math.round(averageGrade * 10.0) / 10.0);
        
//         return analytics;
//     }
    
//     // Helper methods
//     private CertificateDTO convertToDTO(Certificate certificate) {
//         CertificateDataDTO certificateData = null;
        
//         try {
//             certificateData = objectMapper.readValue(
//                 certificate.getCertificateData(), CertificateDataDTO.class);
//         } catch (Exception e) {
//             log.error("Error parsing certificate data", e);
//             certificateData = CertificateDataDTO.builder().build();
//         }
        
//         return CertificateDTO.builder()
//             .id(certificate.getId())
//             .userId(certificate.getUser().getId())
//             .courseId(certificate.getCourse().getId())
//             .certificateNumber(certificate.getCertificateNumber())
//             .userName(certificate.getUserName())
//             .courseName(certificate.getCourseName())
//             .completionDate(certificate.getCompletionDate())
//             .issuedDate(certificate.getIssuedDate())
//             .expirationDate(certificate.getExpirationDate())
//             .certificateUrl(certificate.getCertificateUrl())
//             .verificationCode(certificate.getVerificationCode())
//             .certificateData(certificateData)
//             .isActive(certificate.isActive())
//             .createdAt(certificate.getCreatedAt())
//             .build();
//     }
    
//     private String convertToJson(CertificateDataDTO data) {
//         try {
//             return objectMapper.writeValueAsString(data);
//         } catch (Exception e) {
//             log.error("Error converting certificate data to JSON", e);
//             return "{}";
//         }
//     }
// }