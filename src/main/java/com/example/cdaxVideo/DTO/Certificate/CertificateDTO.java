// DTO/CertificateDTO.java
package com.example.cdaxVideo.DTO.Certificate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CertificateDTO {
    
    private String id;
    
    @NotNull
    private Long userId;
    
    @NotNull
    private Long courseId;
    
    @NotBlank
    private String certificateNumber;
    
    @NotBlank
    private String userName;
    
    @NotBlank
    private String courseName;
    
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionDate;
    
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issuedDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationDate;
    
    private String certificateUrl;
    
    @NotBlank
    private String verificationCode;
    
    private CertificateDataDTO certificateData;
    
    private boolean isActive;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}