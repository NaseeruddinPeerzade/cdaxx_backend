package com.example.cdaxVideo.Repository;

import com.example.cdaxVideo.Entity.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {
    
    Optional<UserStreak> findByUserIdAndCourseIdAndStreakDate(Long userId, Long courseId, LocalDate date);
    
    List<UserStreak> findByUserIdAndCourseIdAndStreakDateBetween(
        Long userId, Long courseId, LocalDate startDate, LocalDate endDate);
    
    List<UserStreak> findByUserIdAndStreakDateBetween(
        Long userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT us FROM UserStreak us WHERE us.user.id = :userId AND us.course.id = :courseId " +
           "AND us.streakDate >= :startDate ORDER BY us.streakDate ASC")
    List<UserStreak> findStreakForUserAndCourse(
        @Param("userId") Long userId, 
        @Param("courseId") Long courseId,
        @Param("startDate") LocalDate startDate);
    
    @Query("SELECT COUNT(DISTINCT us.streakDate) FROM UserStreak us " +
           "WHERE us.user.id = :userId AND us.streakDate BETWEEN :startDate AND :endDate " +
           "AND us.isActiveDay = true")
    Integer countActiveDaysInPeriod(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
}