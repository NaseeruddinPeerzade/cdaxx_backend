package com.example.cdaxVideo.Controller;

import com.example.cdaxVideo.Service.FavoriteCourseService;
import com.example.cdaxVideo.DTO.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteCourseController {
    private final FavoriteCourseService favoriteService;
    
    public FavoriteCourseController(FavoriteCourseService favoriteService) {
        this.favoriteService = favoriteService;
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<List<FavoriteDTO>> getUserFavorites(@PathVariable Long userId) {
        List<FavoriteDTO> favorites = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(favorites);
    }
    
    @PostMapping("/{userId}/add/{courseId}")
    public ResponseEntity<?> addToFavorites(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        try {
            FavoriteDTO favorite = favoriteService.addToFavorites(userId, courseId);
            
            if (favorite == null) {
                // Course is already in favorites
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("alreadyFavorited", true);
                response.put("message", "Course is already in your favorites");
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("alreadyFavorited", false);
            response.put("message", "Course added to favorites successfully");
            response.put("favorite", favorite);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "Failed to add course to favorites");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @DeleteMapping("/{userId}/remove/{courseId}")
    public ResponseEntity<?> removeFromFavorites(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        try {
            favoriteService.removeFromFavorites(userId, courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Course removed from favorites");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "Failed to remove course from favorites");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/{userId}/check/{courseId}")
    public ResponseEntity<Map<String, Object>> isFavorite(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        try {
            boolean isFavorite = favoriteService.isCourseFavorite(userId, courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isFavorite", isFavorite);
            response.put("userId", userId);
            response.put("courseId", courseId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "Failed to check favorite status");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}