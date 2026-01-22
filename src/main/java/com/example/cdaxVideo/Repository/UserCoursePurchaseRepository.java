package com.example.cdaxVideo.Repository;

import com.example.cdaxVideo.Entity.UserCoursePurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserCoursePurchaseRepository extends JpaRepository<UserCoursePurchase, Long> {
    
    // ✅ ADD THESE METHODS:
    
    // Count purchases by user ID
    @Query("SELECT COUNT(u) FROM UserCoursePurchase u WHERE u.user.id = :userId")
    Integer countByUserId(@Param("userId") Long userId);
    
    // Check if user has any purchases
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM UserCoursePurchase u WHERE u.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
    
    // Existing methods...
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM UserCoursePurchase u " +
           "WHERE u.user.id = :userId AND u.course.id = :courseId")
    boolean existsByUserIdAndCourseId(@Param("userId") Long userId, 
                                      @Param("courseId") Long courseId);
    
    List<UserCoursePurchase> findByUserId(Long userId);
    
    @Query("SELECT u FROM UserCoursePurchase u WHERE u.user.id = :userId")
    List<UserCoursePurchase> findByUserIdWithQuery(@Param("userId") Long userId);
}