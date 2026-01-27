package com.example.cdaxVideo.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.cdaxVideo.Entity.Course;
import com.example.cdaxVideo.Entity.FavoriteCourse;
import com.example.cdaxVideo.Entity.User;
import com.example.cdaxVideo.Repository.CourseRepository;
import com.example.cdaxVideo.Repository.FavoriteCourseRepository;
import com.example.cdaxVideo.Repository.UserRepository;
import com.example.cdaxVideo.DTO.FavoriteDTO;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class FavoriteCourseService {
    private final FavoriteCourseRepository favoriteRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    public FavoriteCourseService(FavoriteCourseRepository favoriteRepository,
                                CourseRepository courseRepository,
                                UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }
    
    // ✅ FIXED: Use optimized method
    public FavoriteDTO addToFavorites(Long userId, Long courseId) {
        // Check if already favorited
        if (favoriteRepository.existsByUserIdAndCourseId(userId, courseId)) {
            // Return existing favorite using optimized method
            return favoriteRepository.findByUserIdAndCourseIdWithDetails(userId, courseId)
                .map(this::mapToDTO)
                .orElse(null);
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        FavoriteCourse favorite = new FavoriteCourse();
        favorite.setUser(user);
        favorite.setCourse(course);
        favorite.setCreatedAt(LocalDateTime.now());
        
        favoriteRepository.save(favorite);
        
        // Return using optimized fetch
        return favoriteRepository.findByUserIdAndCourseIdWithDetails(userId, courseId)
            .map(this::mapToDTO)
            .orElseThrow(() -> new RuntimeException("Failed to retrieve favorite after saving"));
    }
    
    public void removeFromFavorites(Long userId, Long courseId) {
        if (!favoriteRepository.existsByUserIdAndCourseId(userId, courseId)) {
            return; // Idempotent operation
        }
        favoriteRepository.deleteByUserIdAndCourseId(userId, courseId);
    }
    
    public boolean isCourseFavorite(Long userId, Long courseId) {
        return favoriteRepository.existsByUserIdAndCourseId(userId, courseId);
    }
    
    // ✅ FIXED: Use optimized method with JOIN FETCH
    public List<FavoriteDTO> getUserFavorites(Long userId) {
        // Use the optimized method that fetches course eagerly
        return favoriteRepository.findByUserIdWithCourse(userId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    // ✅ FIXED: Safe DTO mapping with null checks
    private FavoriteDTO mapToDTO(FavoriteCourse favorite) {
        FavoriteDTO dto = new FavoriteDTO();
        dto.setId(favorite.getId());
        
        // Course is guaranteed to be loaded because we used JOIN FETCH
        if (favorite.getCourse() != null) {
            Course course = favorite.getCourse();
            dto.setCourseId(course.getId());
            dto.setCourseTitle(course.getTitle());
            dto.setCourseThumbnail(course.getThumbnailUrl());
            
            // Handle nullable price
            Double price = course.getPrice();
            dto.setCoursePrice(price != null ? price : 0.0);
            
            // Add more course details if needed
            dto.setCourseDescription(course.getDescription());
            dto.setCourseInstructor(course.getInstructor());
            dto.setCourseRating(course.getRating() != null ? course.getRating() : 0.0);
            
            // Check if course has discount
            if (course.getDiscountPrice() != null && price != null && price > 0) {
                dto.setHasDiscount(true);
                dto.setOriginalPrice(price);
                dto.setDiscountedPrice(course.getDiscountPrice());
                dto.setDiscountPercentage(((price - course.getDiscountPrice()) / price) * 100);
            } else {
                dto.setHasDiscount(false);
                dto.setOriginalPrice(price != null ? price : 0.0);
                dto.setDiscountedPrice(price != null ? price : 0.0);
                dto.setDiscountPercentage(0.0);
            }
        } else {
            // Fallback values (shouldn't happen with JOIN FETCH)
            dto.setCoursePrice(0.0);
            dto.setHasDiscount(false);
            dto.setOriginalPrice(0.0);
            dto.setDiscountedPrice(0.0);
            dto.setDiscountPercentage(0.0);
        }
        
        dto.setAddedAt(favorite.getCreatedAt());
        return dto;
    }
    
    // ✅ Additional useful methods
    
    public int getFavoriteCount(Long userId) {
        return (int) favoriteRepository.countByUserId(userId);
    }
    
    public boolean userHasFavorites(Long userId) {
        return favoriteRepository.userHasFavorites(userId);
    }
    
    // ✅ Bulk operations
    
    public void clearUserFavorites(Long userId) {
        List<FavoriteDTO> favorites = getUserFavorites(userId);
        favorites.forEach(fav -> {
            removeFromFavorites(userId, fav.getCourseId());
        });
    }
    
    public List<FavoriteDTO> toggleFavorite(Long userId, Long courseId) {
        if (isCourseFavorite(userId, courseId)) {
            removeFromFavorites(userId, courseId);
        } else {
            addToFavorites(userId, courseId);
        }
        return getUserFavorites(userId);
    }
}