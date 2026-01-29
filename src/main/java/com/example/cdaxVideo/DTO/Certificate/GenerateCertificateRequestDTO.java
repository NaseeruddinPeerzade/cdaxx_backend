// DTO/GenerateCertificateRequestDTO.java
package com.example.cdaxVideo.DTO.Certificate;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateCertificateRequestDTO {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotNull(message = "Grade is required")
    private Double grade;
    
    @NotNull(message = "Total modules is required")
    private Integer totalModules;
    
    @NotNull(message = "Completed modules is required")
    private Integer completedModules;
    
    private LocalDateTime completionDate;
}