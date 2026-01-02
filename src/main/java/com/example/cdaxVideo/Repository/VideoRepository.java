package com.example.cdaxVideo.Repository;

import com.example.cdaxVideo.Entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByModuleId(Long moduleId);
    
    // ADD THIS: Count videos in a course
    @Query("SELECT COUNT(v) FROM Video v WHERE v.module.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);
}