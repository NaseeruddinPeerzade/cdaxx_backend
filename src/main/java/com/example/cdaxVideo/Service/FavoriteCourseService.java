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
    
    public FavoriteDTO addToFavorites(Long userId, Long courseId) {
        // Check if already favorited
        if (favoriteRepository.existsByUserIdAndCourseId(userId, courseId)) {
            // Return null to indicate it's already in favorites
            return null;
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        FavoriteCourse favorite = new FavoriteCourse();
        favorite.setUser(user);
        favorite.setCourse(course);
        favorite.setCreatedAt(LocalDateTime.now());
        
        FavoriteCourse saved = favoriteRepository.save(favorite);
        return mapToDTO(saved);
    }
    
    public void removeFromFavorites(Long userId, Long courseId) {
        if (!favoriteRepository.existsByUserIdAndCourseId(userId, courseId)) {
            // If not in favorites, just return (idempotent operation)
            return;
        }
        favoriteRepository.deleteByUserIdAndCourseId(userId, courseId);
    }
    
    public boolean isCourseFavorite(Long userId, Long courseId) {
        return favoriteRepository.existsByUserIdAndCourseId(userId, courseId);
    }
    
    public List<FavoriteDTO> getUserFavorites(Long userId) {
        List<FavoriteCourse> favorites = favoriteRepository.findByUserId(userId);
        return favorites.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    private FavoriteDTO mapToDTO(FavoriteCourse favorite) {
        FavoriteDTO dto = new FavoriteDTO();
        dto.setId(favorite.getId());
        dto.setCourseId(favorite.getCourse().getId());
        dto.setCourseTitle(favorite.getCourse().getTitle());
        dto.setCourseThumbnail(favorite.getCourse().getThumbnailUrl());
        dto.setCoursePrice(favorite.getCourse().getPrice());
        dto.setAddedAt(favorite.getCreatedAt());
        return dto;
    }
}