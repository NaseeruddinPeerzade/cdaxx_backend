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
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class VideoService {
    
    private static final Logger logger = LoggerFactory.getLogger(VideoService.class);
    
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final UserVideoProgressRepository progressRepository;

    public VideoService(VideoRepository videoRepository,
                       UserRepository userRepository,
                       UserVideoProgressRepository progressRepository) {
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
    }

    /**
     * Marks a video as completed for a user
     */
    @Transactional
    public UserVideoProgress markVideoAsCompleted(VideoCompletionRequestDTO request) {
        logger.info("Marking video as completed: {}", request);
        
        // Validate request
        validateRequest(request);
        
        // Find video
        Video video = videoRepository.findById(request.getVideoId())
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + request.getVideoId()));
        
        // Find user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        
        // Find or create progress record
        UserVideoProgress progress = progressRepository
                .findByUserIdAndVideoId(user.getId(), video.getId())
                .orElseGet(() -> createNewProgressRecord(user, video));
        
        // Mark as completed
        progress.setCompleted(true);
        progress.setUnlocked(true);
        
        // Update watched seconds to at least 95% of video duration
        if (progress.getWatchedSeconds() == null || 
            progress.getWatchedSeconds() < (int)(video.getDuration() * 0.95)) {
            progress.setWatchedSeconds((int)(video.getDuration() * 0.95));
        }
        
        logger.info("Video {} marked as completed for user {}", video.getId(), user.getId());
        
        return progressRepository.save(progress);
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
        
        return progressRepository.save(progress);
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
        
        return progressRepository.save(progress);
    }

    /**
     * Unlocks a video for a user (when prerequisites are met)
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
            }
        }
    }
}