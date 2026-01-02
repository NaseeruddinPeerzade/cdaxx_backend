package com.example.cdaxVideo.Repository;

import com.example.cdaxVideo.Entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByCourseId(Long courseId);
    
    // ADD THIS: Count modules in a course
    @Query("SELECT COUNT(m) FROM Module m WHERE m.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);
}