package com.example.cdaxVideo.Service;

import com.example.cdaxVideo.DTO.VideoCompletionRequestDTO;
import com.example.cdaxVideo.DTO.VideoProgressDTO;
import com.example.cdaxVideo.Entity.User;
import com.example.cdaxVideo.Entity.Video;
import com.example.cdaxVideo.Entity.UserVideoProgress;
import com.example.cdaxVideo.Repository.UserRepository;
import com.example.cdaxVideo.Repository.VideoRepository;
import com.example.cdaxVideo.Repository.UserVideoProgressRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class VideoService {
    
    private static final Logger logger = LoggerFactory.getLogger(VideoService.class);
    
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final UserVideoProgressRepository progressRepository;
    private final StreakService streakService;
    private final CourseService courseService; // ✅ ADDED: Inject CourseService

    public VideoService(VideoRepository videoRepository,
                       UserRepository userRepository,
                       UserVideoProgressRepository progressRepository,
                       StreakService streakService,
                       @Lazy CourseService courseService) { // ✅ ADDED: CourseService parameter
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.streakService = streakService;
        this.courseService = courseService; // ✅ INITIALIZE
    }

    /**
     * Marks a video as completed for a user
     * Uses CourseService.completeVideoAndUnlockNext() for the established unlocking flow
     */
@Transactional
public UserVideoProgress markVideoAsCompleted(VideoCompletionRequestDTO request) {
    logger.info("Marking video as completed: {}", request);
    
    validateRequest(request);
    
    Video video = videoRepository.findById(request.getVideoId())
            .orElseThrow(() -> new RuntimeException("Video not found with ID: " + request.getVideoId()));
    
    User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
    
    // ✅ ADD THIS: Mark user as NOT new after completing video
    if (user.getIsNewUser() != null && user.getIsNewUser() == 1) {
        user.setIsNewUser(0); // Set to 0 (false) - user is no longer new
        userRepository.save(user);
        logger.info("✅ User {} marked as NOT new after completing video {}", user.getId(), video.getId());
    }
    
    UserVideoProgress progress = progressRepository
            .findByUserIdAndVideoId(user.getId(), video.getId())
            .orElseGet(() -> createNewProgressRecord(user, video));
    
    // Mark as completed
    progress.setCompleted(true);
    progress.setUnlocked(true); // Video should be unlocked when completed
    
    // Update watch time if needed
    if (progress.getWatchedSeconds() == null || 
        progress.getWatchedSeconds() < (int)(video.getDuration() * 0.95)) {
        progress.setWatchedSeconds((int)(video.getDuration() * 0.95));
    }
    
    // Save progress first
    UserVideoProgress savedProgress = progressRepository.save(progress);
    
    // Update streak
    updateStreakForVideoCompletion(request, video, savedProgress);
    
    // ✅ USE EXISTING UNLOCKING FLOW FROM CourseService
    // This will handle: unlocking next video, unlocking assessment, unlocking next module
    try {
        boolean success = courseService.completeVideoAndUnlockNext(
            request.getUserId(), 
            request.getCourseId(), 
            request.getModuleId(), 
            request.getVideoId()
        );
        
        if (success) {
            logger.info("✅ CourseService successfully processed video completion and unlocking");
        } else {
            logger.warn("⚠️ CourseService.completeVideoAndUnlockNext returned false");
        }
    } catch (Exception e) {
        logger.error("❌ Error calling CourseService.completeVideoAndUnlockNext: {}", e.getMessage(), e);
        // Don't throw - video completion should still be recorded
    }
    
    logger.info("Video {} marked as completed for user {}", video.getId(), user.getId());
    
    return savedProgress;
}
    /**
     * ✅ NEW METHOD: Update streak when video is completed
     */
private void updateStreakForVideoCompletion(VideoCompletionRequestDTO request, 
                                           Video video, 
                                           UserVideoProgress progress) {
    try {
        if (request.getCourseId() != null) {
            // Get video duration or watched seconds
            int watchedSeconds = progress.getWatchedSeconds() != null ? 
                               progress.getWatchedSeconds() : video.getDuration();
            
            logger.info("📞 ===== CALLING STREAK SERVICE =====");
            logger.info("   ├─ User ID: {}", request.getUserId());
            logger.info("   ├─ Course ID: {}", request.getCourseId());
            logger.info("   ├─ Video ID: {}", request.getVideoId());
            logger.info("   ├─ Video Title: {}", video.getTitle());
            logger.info("   ├─ Video Duration: {}s", video.getDuration());
            logger.info("   ├─ Watched Seconds: {}s", watchedSeconds);
            logger.info("   ├─ Is Completed: {}", true);
            logger.info("   └─ Current Time: {}", new Date());
            
            // Call streak service
            streakService.updateStreakForVideoWatch(
                request.getUserId(),
                request.getCourseId(),
                request.getVideoId(),
                watchedSeconds,
                true // isCompleted
            );
            
            logger.info("✅ StreakService called successfully");
            
            // Log after call to verify
            logger.info("📊 After streak update - checking database...");
            
        } else {
            logger.warn("⚠️ Cannot update streak: Course ID is null for video {} user {}", 
                request.getVideoId(), request.getUserId());
        }
    } catch (Exception e) {
        logger.error("❌ Failed to update streak for video completion: {}", e.getMessage(), e);
    }
}

    /**
     * Updates video progress (watch time, position, forward jumps)
     */
    @Transactional
    public UserVideoProgress updateVideoProgress(VideoProgressDTO progressDTO) {
        logger.debug("Updating video progress: {}", progressDTO);
        
        // Validate
        if (progressDTO.getVideoId() == null || progressDTO.getUserId() == null) {
            throw new RuntimeException("Video ID and User ID are required");
        }
        
        // Find video
        Video video = videoRepository.findById(progressDTO.getVideoId())
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + progressDTO.getVideoId()));
        
        // Find user
        User user = userRepository.findById(progressDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + progressDTO.getUserId()));
        
        // Find or create progress record
        UserVideoProgress progress = progressRepository
                .findByUserIdAndVideoId(user.getId(), video.getId())
                .orElseGet(() -> createNewProgressRecord(user, video));
        
        // Update progress fields
        if (progressDTO.getWatchedSeconds() != null) {
            progress.setWatchedSeconds(progressDTO.getWatchedSeconds());
        }
        
        if (progressDTO.getLastPositionSeconds() != null) {
            progress.setLastPositionSeconds(progressDTO.getLastPositionSeconds());
        }
        
        if (progressDTO.getForwardJumpsCount() != null) {
            progress.setForwardJumpsCount(progressDTO.getForwardJumpsCount());
        }
        
        // Check if should be marked as completed
        checkAndMarkCompletion(progress, video);
        
        // ✅ Update streak for progress (optional - for significant watch time)
        updateStreakForVideoProgress(progressDTO, video);
        
        return progressRepository.save(progress);
    }

    /**
     * ✅ NEW METHOD: Update streak for video progress updates
     */
    private void updateStreakForVideoProgress(VideoProgressDTO progressDTO, Video video) {
        try {
            // Update streak when there's significant watch time
            if (progressDTO.getWatchedSeconds() != null && progressDTO.getWatchedSeconds() > 60) {
                // Note: Need courseId for streak update - this would come from video->module->course
                logger.debug("Video progress updated: {} seconds watched", progressDTO.getWatchedSeconds());
                // You could add course lookup here if needed for streak updates
            }
        } catch (Exception e) {
            logger.error("Failed to update streak for video progress: {}", e.getMessage());
        }
    }

    /**
     * Gets video progress for a user
     */
    public VideoProgressDTO getVideoProgress(Long videoId, Long userId) {
        Optional<UserVideoProgress> progressOpt = 
            progressRepository.findByUserIdAndVideoId(userId, videoId);
        
        VideoProgressDTO dto = new VideoProgressDTO();
        dto.setVideoId(videoId);
        dto.setUserId(userId);
        
        if (progressOpt.isPresent()) {
            UserVideoProgress progress = progressOpt.get();
            dto.setWatchedSeconds(progress.getWatchedSeconds());
            dto.setLastPositionSeconds(progress.getLastPositionSeconds());
            dto.setForwardJumpsCount(progress.getForwardJumpsCount());
            dto.setCompleted(progress.isCompleted());
            dto.setUnlocked(progress.isUnlocked());
            // Note: completedOn is not included in VideoProgressDTO
        }
        
        return dto;
    }

    /**
     * Manually marks a video as completed (admin/instructor override)
     */
    @Transactional
    public UserVideoProgress manuallyCompleteVideo(Long videoId, Long userId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserVideoProgress progress = progressRepository
                .findByUserIdAndVideoId(userId, videoId)
                .orElseGet(() -> createNewProgressRecord(user, video));
        
        progress.setCompleted(true);
        progress.setUnlocked(true);
        progress.setManuallyCompleted(true);
        progress.setWatchedSeconds(video.getDuration());
        
        // ✅ Also call CourseService to handle unlocking flow
        try {
            // Get module and course info
            Long moduleId = video.getModule().getId();
            Long courseId = video.getModule().getCourse().getId();
            courseService.completeVideoAndUnlockNext(userId, courseId, moduleId, videoId);
        } catch (Exception e) {
            logger.error("Error calling CourseService for manual completion: {}", e.getMessage());
        }
        
        return progressRepository.save(progress);
    }

    /**
     * Unlocks a video for a user (when prerequisites are met)
     * Note: This should typically be called from CourseService
     */
    @Transactional
    public UserVideoProgress unlockVideo(Long videoId, Long userId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserVideoProgress progress = progressRepository
                .findByUserIdAndVideoId(userId, videoId)
                .orElseGet(() -> createNewProgressRecord(user, video));
        
        progress.setUnlocked(true);
        
        return progressRepository.save(progress);
    }

    /**
     * Helper method to validate completion request
     */
    private void validateRequest(VideoCompletionRequestDTO request) {
        if (request.getVideoId() == null) {
            throw new RuntimeException("Video ID is required");
        }
        if (request.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }
        if (request.getCourseId() == null) {
            logger.warn("Course ID is missing in completion request");
        }
        if (request.getModuleId() == null) {
            logger.warn("Module ID is missing in completion request");
        }
    }

    /**
     * Helper method to create new progress record
     */
    private UserVideoProgress createNewProgressRecord(User user, Video video) {
        UserVideoProgress progress = new UserVideoProgress();
        progress.setUser(user);
        progress.setVideo(video);
        progress.setUnlocked(false);
        progress.setCompleted(false);
        progress.setWatchedSeconds(0);
        progress.setLastPositionSeconds(0);
        progress.setForwardJumpsCount(0);
        return progress;
    }

    /**
     * Checks if video should be marked as completed based on watch time
     */
    private void checkAndMarkCompletion(UserVideoProgress progress, Video video) {
        // Only check if not already completed
        if (!progress.isCompleted() && !Boolean.TRUE.equals(progress.getManuallyCompleted())) {
            // Check if watched enough (95%) and not skipped too much (<10 forward jumps)
            if (progress.getWatchedSeconds() != null && 
                progress.getWatchedSeconds() >= (int)(video.getDuration() * 0.95) &&
                (progress.getForwardJumpsCount() == null || progress.getForwardJumpsCount() < 10)) {
                
                progress.setCompleted(true);
                progress.setUnlocked(true);
                logger.info("Auto-marking video {} as completed for user {} (watched: {}s, duration: {}s)", 
                    video.getId(), progress.getUser().getId(), 
                    progress.getWatchedSeconds(), video.getDuration());
                
                // ✅ If auto-completing, also trigger the CourseService unlocking flow
                try {
                    Long userId = progress.getUser().getId();
                    Long videoId = video.getId();
                    Long moduleId = video.getModule().getId();
                    Long courseId = video.getModule().getCourse().getId();
                    
                    courseService.completeVideoAndUnlockNext(userId, courseId, moduleId, videoId);
                } catch (Exception e) {
                    logger.error("Error calling CourseService for auto-completion: {}", e.getMessage());
                }
            }
        }
    }
    
    /**
     * ✅ SIMPLIFIED HELPER: Unlock just this video (without triggering chain)
     * For use by CourseService when it controls the unlocking flow
     */
    @Transactional
    public boolean unlockSingleVideo(Long userId, Long videoId) {
        try {
            Video video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new RuntimeException("Video not found"));
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            UserVideoProgress progress = progressRepository
                    .findByUserIdAndVideoId(userId, videoId)
                    .orElseGet(() -> createNewProgressRecord(user, video));
            
            if (!progress.isUnlocked()) {
                progress.setUnlocked(true);
                progressRepository.save(progress);
                logger.info("Unlocked video {} for user {}", videoId, userId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error unlocking video {} for user {}: {}", videoId, userId, e.getMessage());
            return false;
        }
    }
}