package com.example.cdaxVideo.DTO.Certificate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CertificateDataDTO {
    
    private Double grade;
    private Integer totalModules;
    private Integer completedModules;
    private String completionDuration;
    private String instructorName;
    private Map<String, Object> additionalData;
}