package com.example.cdaxVideo.DTO.Certificate;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareCertificateRequestDTO {
    
    private String platform; // "linkedin", "twitter", "facebook"
    private LocalDateTime sharedAt;
}