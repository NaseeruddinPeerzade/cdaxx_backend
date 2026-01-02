package com.example.cdaxVideo.DTO;

import lombok.Data;
import java.time.LocalDate;
import java.util.Map;

@Data
public class StreakDTO {
    private Long id;
    private Long userId;
    private Long courseId;
    private String courseTitle;
    private LocalDate streakDate;
    private Integer watchedSeconds;
    private Integer totalAvailableSeconds;
    private Double progressPercentage;
    private Integer completedVideosCount;
    private Integer totalVideosCount;
    private Boolean isActiveDay;
    private Map<String, Object> videoDetails;
    private String colorCode;
}