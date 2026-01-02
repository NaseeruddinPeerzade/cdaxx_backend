package com.example.cdaxVideo.Repository;

import com.example.cdaxVideo.Entity.UserVideoProgress;
import com.example.cdaxVideo.Entity.User;
import com.example.cdaxVideo.Entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserVideoProgressRepository extends JpaRepository<UserVideoProgress, Long> {
    Optional<UserVideoProgress> findByUserAndVideo(User user, Video video);
    
    // Add this method
    @Query("SELECT COUNT(uvp) FROM UserVideoProgress uvp " +
           "JOIN uvp.video v " +
           "JOIN v.module m " +
           "JOIN m.course c " +
           "WHERE uvp.user.id = :userId AND c.id = :courseId AND uvp.completed = true")
    Long countCompletedVideosByUserAndCourse(@Param("userId") Long userId, 
                                             @Param("courseId") Long courseId);

    Optional<UserVideoProgress> findByUserIdAndVideoId(Long userId, Long videoId);
    boolean existsByUserIdAndVideoIdAndCompletedTrue(Long userId, Long videoId);
    
    // You might also want these
    List<UserVideoProgress> findByUserId(Long userId);
    List<UserVideoProgress> findByUserIdAndCompletedTrue(Long userId);
    List<UserVideoProgress> findByVideoId(Long videoId);
}