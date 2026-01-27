package com.example.cdaxVideo.Repository;

import com.example.cdaxVideo.Entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    // ========== OPTIMIZED METHODS WITH JOIN FETCH ==========
    
    // ✅ 1. Get modules with videos for a course (CRITICAL for course details)
    @Query("SELECT DISTINCT m FROM Module m " +
           "LEFT JOIN FETCH m.videos v " +  // Fetch videos eagerly
           "WHERE m.course.id = :courseId " +
           "ORDER BY m.id ASC, v.displayOrder ASC")
    List<Module> findByCourseIdWithVideos(@Param("courseId") Long courseId);
    
    // ✅ 2. Get single module with videos and course
    @Query("SELECT DISTINCT m FROM Module m " +
           "LEFT JOIN FETCH m.videos v " +
           "LEFT JOIN FETCH m.course c " +
           "WHERE m.id = :moduleId " +
           "ORDER BY v.displayOrder ASC")
    Optional<Module> findByIdWithVideosAndCourse(@Param("moduleId") Long moduleId);
    
    // ✅ 3. Get module with just videos (for module details page)
    @Query("SELECT DISTINCT m FROM Module m " +
           "LEFT JOIN FETCH m.videos v " +
           "WHERE m.id = :moduleId " +
           "ORDER BY v.displayOrder ASC")
    Optional<Module> findByIdWithVideos(@Param("moduleId") Long moduleId);
    
    // ✅ 4. Get module with course only (lightweight)
    @Query("SELECT DISTINCT m FROM Module m " +
           "LEFT JOIN FETCH m.course c " +
           "WHERE m.id = :moduleId")
    Optional<Module> findByIdWithCourse(@Param("moduleId") Long moduleId);
    
    // ✅ 5. Get modules for multiple courses (for dashboard)
    @Query("SELECT DISTINCT m FROM Module m " +
           "LEFT JOIN FETCH m.videos v " +
           "WHERE m.course.id IN :courseIds " +
           "ORDER BY m.course.id, m.id ASC, v.displayOrder ASC")
    List<Module> findByCourseIdsWithVideos(@Param("courseIds") List<Long> courseIds);
    
    // ========== EXISTING METHODS (Keep for backward compatibility) ==========
    
    // Basic method (without JOIN FETCH - use with caution)
    @Query("SELECT m FROM Module m WHERE m.course.id = :courseId ORDER BY m.id ASC")
    List<Module> findByCourseId(@Param("courseId") Long courseId);
    
    // Count method
    @Query("SELECT COUNT(m) FROM Module m WHERE m.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);
    
    // ✅ 6. Check if module exists in course
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
           "FROM Module m WHERE m.id = :moduleId AND m.course.id = :courseId")
    boolean existsByIdAndCourseId(@Param("moduleId") Long moduleId, 
                                  @Param("courseId") Long courseId);
    
    // ✅ 7. Get next module in sequence
    @Query("SELECT m FROM Module m " +
           "WHERE m.course.id = :courseId " +
           "AND m.id > :currentModuleId " +
           "ORDER BY m.id ASC " +
           "LIMIT 1")
    Optional<Module> findNextModule(@Param("courseId") Long courseId, 
                                    @Param("currentModuleId") Long currentModuleId);
    
    // ✅ 8. Get previous module in sequence
    @Query("SELECT m FROM Module m " +
           "WHERE m.course.id = :courseId " +
           "AND m.id < :currentModuleId " +
           "ORDER BY m.id DESC " +
           "LIMIT 1")
    Optional<Module> findPreviousModule(@Param("courseId") Long courseId, 
                                        @Param("currentModuleId") Long currentModuleId);
}