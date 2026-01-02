package com.example.cdaxVideo.Repository;

import com.example.cdaxVideo.Entity.User;
import com.example.cdaxVideo.Entity.Assessment;
import com.example.cdaxVideo.Entity.UserAssessmentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAssessmentProgressRepository extends JpaRepository<UserAssessmentProgress, Long> {
    
    Optional<UserAssessmentProgress> findByUserAndAssessment(User user, Assessment assessment);
    
    boolean existsByUserAndAssessment(User user, Assessment assessment);
    
    List<UserAssessmentProgress> findByUserId(Long userId);
    
    List<UserAssessmentProgress> findByUserIdAndPassed(Long userId, boolean passed);
    
    List<UserAssessmentProgress> findByAssessmentId(Long assessmentId);
    
    @Query("SELECT uap FROM UserAssessmentProgress uap WHERE uap.user.id = :userId AND uap.assessment.module.id = :moduleId")
    List<UserAssessmentProgress> findByUserIdAndModuleId(@Param("userId") Long userId, @Param("moduleId") Long moduleId);
    
    @Query("SELECT uap FROM UserAssessmentProgress uap WHERE uap.user.id = :userId AND uap.assessment.module.course.id = :courseId")
    List<UserAssessmentProgress> findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(uap) FROM UserAssessmentProgress uap WHERE uap.user.id = :userId AND uap.passed = true")
    Long countPassedAssessmentsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT uap FROM UserAssessmentProgress uap WHERE uap.user.id = :userId AND uap.assessment.id = :assessmentId AND uap.passed = true")
    Optional<UserAssessmentProgress> findPassedAssessment(@Param("userId") Long userId, @Param("assessmentId") Long assessmentId);
}