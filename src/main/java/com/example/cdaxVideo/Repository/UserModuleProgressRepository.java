package com.example.cdaxVideo.Repository;

import com.example.cdaxVideo.Entity.UserModuleProgress;
import com.example.cdaxVideo.Entity.User;
import com.example.cdaxVideo.Entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserModuleProgressRepository extends JpaRepository<UserModuleProgress, Long> {
    Optional<UserModuleProgress> findByUserAndModule(User user, Module module);
    boolean existsByUserAndModule(User user, Module module);
    
    // ADD THESE: Count completed modules
    @Query("SELECT COUNT(DISTINCT ump.module.id) FROM UserModuleProgress ump " +
           "WHERE ump.user.id = :userId AND ump.completed = true")
    Long countCompletedModulesByUser(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(DISTINCT ump.module.id) FROM UserModuleProgress ump " +
           "JOIN Module m ON ump.module.id = m.id " +
           "WHERE ump.user.id = :userId AND m.course.id = :courseId AND ump.completed = true")
    Long countCompletedModulesByUserAndCourse(@Param("userId") Long userId, 
                                              @Param("courseId") Long courseId);
}