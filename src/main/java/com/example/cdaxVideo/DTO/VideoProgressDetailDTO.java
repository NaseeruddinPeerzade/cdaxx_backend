package com.example.cdaxVideo.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class VideoProgressDetailDTO {
    private Long videoId;
    private String videoTitle;
    private Integer watchedSeconds;
    private Integer videoDuration;
    private Double videoProgress;
    private Boolean isCompleted;
    private LocalDate watchedDate;
}