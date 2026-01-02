package com.example.cdaxVideo.Service;

import com.example.cdaxVideo.DTO.*;
import com.example.cdaxVideo.Entity.*;
import com.example.cdaxVideo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StreakService {
    
    private final UserStreakRepository userStreakRepository;
    private final UserVideoProgressRepository userVideoProgressRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    
    private static final int STREAK_CYCLE_DAYS = 30;
    
    /**
     * Update streak for a user when they watch a video
     */
    @Transactional
    public void updateStreakForVideoWatch(Long userId, Long courseId, Long videoId, 
                                          Integer watchedSeconds, boolean isCompleted) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        Video video = videoRepository.findById(videoId)
            .orElseThrow(() -> new RuntimeException("Video not found"));
        
        LocalDate today = LocalDate.now();
        
        // Get or create streak for today
        UserStreak streak = userStreakRepository
            .findByUserIdAndCourseIdAndStreakDate(userId, courseId, today)
            .orElseGet(() -> {
                UserStreak newStreak = new UserStreak();
                newStreak.setUser(user);
                newStreak.setCourse(course);
                newStreak.setStreakDate(today);
                return newStreak;
            });
        
        // Update watched seconds
        streak.addWatchedSeconds(watchedSeconds);
        
        // Update total available seconds (all videos in course) if not set
        if (streak.getTotalAvailableSeconds() == 0) {
            int totalDuration = course.getModules().stream()
                .flatMap(module -> module.getVideos().stream())
                .mapToInt(Video::getDuration)
                .sum();
            streak.setTotalAvailableSeconds(totalDuration);
        }
        
        // Update video counts
        if (isCompleted) {
            streak.setCompletedVideosCount(streak.getCompletedVideosCount() + 1);
        }
        
        // Update total videos count
        streak.setTotalVideosCount(course.getModules().stream()
            .mapToInt(module -> module.getVideos().size())
            .sum());
        
        userStreakRepository.save(streak);
    }
    
    /**
     * Get 30-day streak for a specific course
     */
    public StreakSummaryDTO getCourseStreak(Long userId, Long courseId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(STREAK_CYCLE_DAYS - 1);
        
        List<UserStreak> streaks = userStreakRepository
            .findByUserIdAndCourseIdAndStreakDateBetween(userId, courseId, startDate, endDate);
        
        return buildStreakSummary(userId, courseId, streaks, startDate, endDate);
    }
    
    /**
     * Get streak for all courses (profile page)
     */
    public Map<String, Object> getUserStreakOverview(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(STREAK_CYCLE_DAYS - 1);
        
        List<UserStreak> allStreaks = userStreakRepository
            .findByUserIdAndStreakDateBetween(userId, startDate, endDate);
        
        // Group by course
        Map<Course, List<UserStreak>> streaksByCourse = allStreaks.stream()
            .collect(Collectors.groupingBy(UserStreak::getCourse));
        
        List<StreakSummaryDTO> courseSummaries = new ArrayList<>();
        
        for (Map.Entry<Course, List<UserStreak>> entry : streaksByCourse.entrySet()) {
            Course course = entry.getKey();
            List<UserStreak> courseStreaks = entry.getValue();
            
            StreakSummaryDTO summary = buildStreakSummary(
                userId, course.getId(), courseStreaks, startDate, endDate);
            courseSummaries.add(summary);
        }
        
        // Calculate overall stats
        int totalActiveDays = (int) allStreaks.stream()
            .filter(streak -> streak.getIsActiveDay())
            .count();
        
        Map<String, Object> response = new HashMap<>();
        response.put("courseSummaries", courseSummaries);
        response.put("totalActiveDays", totalActiveDays);
        response.put("currentCycleStart", startDate.toString());
        response.put("currentCycleEnd", endDate.toString());
        response.put("cycleDurationDays", STREAK_CYCLE_DAYS);
        
        return response;
    }
    
    /**
     * Get detailed day information
     */
    public StreakDayDTO getDayDetails(Long userId, Long courseId, LocalDate date) {
        Optional<UserStreak> streakOpt = userStreakRepository
            .findByUserIdAndCourseIdAndStreakDate(userId, courseId, date);
        
        if (streakOpt.isEmpty()) {
            return createEmptyDayDTO(date);
        }
        
        return convertToDayDTO(streakOpt.get());
    }
    
    /**
     * Build 30-day calendar with empty days filled in
     */
    private StreakSummaryDTO buildStreakSummary(Long userId, Long courseId, 
                                                List<UserStreak> streaks, 
                                                LocalDate startDate, LocalDate endDate) {
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Create map for quick lookup
        Map<LocalDate, UserStreak> streakMap = streaks.stream()
            .collect(Collectors.toMap(UserStreak::getStreakDate, s -> s));
        
        // Build 30-day calendar
        List<StreakDayDTO> dayCalendar = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            UserStreak streak = streakMap.get(currentDate);
            if (streak != null) {
                dayCalendar.add(convertToDayDTO(streak));
            } else {
                dayCalendar.add(createEmptyDayDTO(currentDate));
            }
            currentDate = currentDate.plusDays(1);
        }
        
        // Calculate streak stats
        int currentStreak = calculateCurrentStreak(dayCalendar);
        int longestStreak = calculateLongestStreak(dayCalendar);
        
        double overallProgress = streaks.stream()
            .mapToDouble(UserStreak::getProgressPercentage)
            .average()
            .orElse(0.0);
        
        StreakSummaryDTO summary = new StreakSummaryDTO();
        summary.setCourseId(courseId);
        summary.setCourseTitle(course.getTitle());
        summary.setCurrentStreakDays(currentStreak);
        summary.setLongestStreakDays(longestStreak);
        summary.setOverallProgress(overallProgress);
        summary.setLastActiveDate(getLastActiveDate(streaks));
        summary.setLast30Days(dayCalendar);
        
        return summary;
    }
    
    private StreakDayDTO convertToDayDTO(UserStreak streak) {
        StreakDayDTO dto = new StreakDayDTO();
        dto.setDate(streak.getStreakDate());
        dto.setWatchedSeconds(streak.getWatchedSeconds());
        dto.setTotalAvailableSeconds(streak.getTotalAvailableSeconds());
        dto.setProgressPercentage(streak.getProgressPercentage());
        dto.setIsActiveDay(streak.getIsActiveDay());
        dto.setColorCode(getColorCode(streak.getProgressPercentage()));
        return dto;
    }
    
    private StreakDayDTO createEmptyDayDTO(LocalDate date) {
        StreakDayDTO dto = new StreakDayDTO();
        dto.setDate(date);
        dto.setWatchedSeconds(0);
        dto.setTotalAvailableSeconds(0);
        dto.setProgressPercentage(0.0);
        dto.setIsActiveDay(false);
        dto.setColorCode(getColorCode(0.0));
        return dto;
    }
    
    /**
     * Color coding based on progress percentage
     */
    private String getColorCode(Double percentage) {
        if (percentage == null || percentage == 0) return "#E5E7EB"; // Gray
        if (percentage < 25) return "#FEF3C7"; // Light yellow
        if (percentage < 50) return "#FDE68A"; // Yellow
        if (percentage < 75) return "#FBBF24"; // Orange
        if (percentage < 100) return "#F59E0B"; // Dark orange
        return "#10B981"; // Green for 100%
    }
    
    private int calculateCurrentStreak(List<StreakDayDTO> days) {
        int streak = 0;
        // Count backwards from today
        for (int i = days.size() - 1; i >= 0; i--) {
            if (days.get(i).getIsActiveDay()) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
    
    private int calculateLongestStreak(List<StreakDayDTO> days) {
        int maxStreak = 0;
        int currentStreak = 0;
        
        for (StreakDayDTO day : days) {
            if (day.getIsActiveDay()) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }
        
        return maxStreak;
    }
    
    private LocalDate getLastActiveDate(List<UserStreak> streaks) {
        return streaks.stream()
            .filter(UserStreak::getIsActiveDay)
            .map(UserStreak::getStreakDate)
            .max(LocalDate::compareTo)
            .orElse(null);
    }
}