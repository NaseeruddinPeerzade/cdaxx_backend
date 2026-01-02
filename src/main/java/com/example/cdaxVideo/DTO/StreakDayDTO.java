package com.example.cdaxVideo.DTO;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class StreakDayDTO {
    private LocalDate date;
    private Integer watchedSeconds;
    private Integer totalAvailableSeconds;
    private Double progressPercentage;
    private Boolean isActiveDay;
    private String colorCode;
    private List<VideoProgressDetailDTO> videoDetails;
}