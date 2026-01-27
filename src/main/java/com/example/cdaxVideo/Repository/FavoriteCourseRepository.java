package com.example.cdaxVideo.Repository;

import com.example.cdaxVideo.Entity.FavoriteCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteCourseRepository extends JpaRepository<FavoriteCourse, Long> {
    
    // ========== OPTIMIZED METHODS WITH JOIN FETCH ==========
    
    // ✅ 1. Get favorites with course details (CRITICAL FIX)
    @Query("SELECT DISTINCT f FROM FavoriteCourse f " +
           "JOIN FETCH f.course c " +  // Fetch course eagerly to prevent LazyInitializationException
           "WHERE f.user.id = :userId " +
           "ORDER BY f.createdAt DESC")
    List<FavoriteCourse> findByUserIdWithCourse(@Param("userId") Long userId);
    
    // ✅ 2. Get favorite with full details
    @Query("SELECT f FROM FavoriteCourse f " +
           "JOIN FETCH f.course c " +
           "JOIN FETCH f.user u " +
           "WHERE f.user.id = :userId AND f.course.id = :courseId")
    Optional<FavoriteCourse> findByUserIdAndCourseIdWithDetails(
        @Param("userId") Long userId, 
        @Param("courseId") Long courseId
    );
    
    // ✅ 3. Existing basic methods (keep for backward compatibility)
    List<FavoriteCourse> findByUserId(Long userId);
    
    Optional<FavoriteCourse> findByUserIdAndCourseId(Long userId, Long courseId);
    
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    
    void deleteByUserIdAndCourseId(Long userId, Long courseId);
    
    long countByUserId(Long userId);
    
    // ✅ 4. Check if user has any favorites
    @Query("SELECT COUNT(f) > 0 FROM FavoriteCourse f WHERE f.user.id = :userId")
    boolean userHasFavorites(@Param("userId") Long userId);
}