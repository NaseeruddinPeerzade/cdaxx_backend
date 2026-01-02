package com.example.cdaxVideo.DTO;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class StreakSummaryDTO {
    private Long courseId;
    private String courseTitle;
    private Integer currentStreakDays;
    private Integer longestStreakDays;
    private Double overallProgress;
    private LocalDate lastActiveDate;
    private List<StreakDayDTO> last30Days;
}