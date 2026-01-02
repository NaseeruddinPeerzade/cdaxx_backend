package com.example.cdaxVideo.Controller;

import com.example.cdaxVideo.DTO.VideoCompletionRequestDTO;
import com.example.cdaxVideo.DTO.VideoProgressDTO;
import com.example.cdaxVideo.Entity.UserVideoProgress;
import com.example.cdaxVideo.Service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*")
public class VideoController {
    
    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);
    
    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * POST /api/videos/{videoId}/complete
     * Marks a video as completed
     */
    @PostMapping("/{videoId}/complete")
    public ResponseEntity<Map<String, Object>> markVideoAsCompleted(
            @PathVariable Long videoId,
            @RequestParam Long userId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long moduleId) {
        
        try {
            logger.info("Video completion request - Video: {}, User: {}, Course: {}, Module: {}", 
                       videoId, userId, courseId, moduleId);
            
            VideoCompletionRequestDTO request = new VideoCompletionRequestDTO();
            request.setVideoId(videoId);
            request.setUserId(userId);
            request.setCourseId(courseId);
            request.setModuleId(moduleId);
            
            UserVideoProgress progress = videoService.markVideoAsCompleted(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Video marked as completed successfully");
            response.put("videoId", videoId);
            response.put("userId", userId);
            response.put("completed", true);
            response.put("unlocked", true);
            response.put("completedOn", progress.getCompletedOn());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error marking video as completed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Unexpected error marking video as completed: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal server error");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * POST /api/videos/{videoId}/progress
     * Updates video progress (watch time, position, etc.)
     */
    @PostMapping("/{videoId}/progress")
    public ResponseEntity<Map<String, Object>> updateVideoProgress(
            @PathVariable Long videoId,
            @RequestBody VideoProgressDTO progressDTO) {
        
        try {
            logger.debug("Updating video progress: {}", progressDTO);
            
            // Ensure the videoId in path matches the DTO
            progressDTO.setVideoId(videoId);
            
            UserVideoProgress progress = videoService.updateVideoProgress(progressDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Video progress updated successfully");
            response.put("videoId", videoId);
            response.put("userId", progressDTO.getUserId());
            response.put("watchedSeconds", progress.getWatchedSeconds());
            response.put("lastPositionSeconds", progress.getLastPositionSeconds());
            response.put("forwardJumpsCount", progress.getForwardJumpsCount());
            response.put("completed", progress.isCompleted());
            response.put("unlocked", progress.isUnlocked());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error updating video progress: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Unexpected error updating video progress: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal server error");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/videos/{videoId}/progress
     * Gets video progress for a user
     */
    @GetMapping("/{videoId}/progress")
    public ResponseEntity<Map<String, Object>> getVideoProgress(
            @PathVariable Long videoId,
            @RequestParam Long userId) {
        
        try {
            logger.debug("Getting video progress - Video: {}, User: {}", videoId, userId);
            
            VideoProgressDTO progress = videoService.getVideoProgress(videoId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("progress", progress);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error getting video progress: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Unexpected error getting video progress: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal server error");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * POST /api/videos/{videoId}/unlock
     * Unlocks a video for a user
     */
    @PostMapping("/{videoId}/unlock")
    public ResponseEntity<Map<String, Object>> unlockVideo(
            @PathVariable Long videoId,
            @RequestParam Long userId) {
        
        try {
            logger.info("Unlocking video - Video: {}, User: {}", videoId, userId);
            
            UserVideoProgress progress = videoService.unlockVideo(videoId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Video unlocked successfully");
            response.put("videoId", videoId);
            response.put("userId", userId);
            response.put("unlocked", true);
            response.put("unlockedOn", progress.getUnlockedOn());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error unlocking video: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * POST /api/videos/{videoId}/manual-complete
     * Manually marks a video as completed (admin override)
     */
    @PostMapping("/{videoId}/manual-complete")
    public ResponseEntity<Map<String, Object>> manuallyCompleteVideo(
            @PathVariable Long videoId,
            @RequestParam Long userId) {
        
        try {
            logger.info("Manual video completion - Video: {}, User: {}", videoId, userId);
            
            UserVideoProgress progress = videoService.manuallyCompleteVideo(videoId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Video manually marked as completed");
            response.put("videoId", videoId);
            response.put("userId", userId);
            response.put("completed", true);
            response.put("manuallyCompleted", true);
            response.put("completedOn", progress.getCompletedOn());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error in manual video completion: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "VideoService");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}