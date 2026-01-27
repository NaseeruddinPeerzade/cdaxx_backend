package com.example.cdaxVideo.Repository;

import com.example.cdaxVideo.Entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    
    // ========== EXISTING METHODS ==========
    
    // Basic method
    List<Video> findByModuleId(Long moduleId);
    
    // Count videos in course
    @Query("SELECT COUNT(v) FROM Video v " +
           "JOIN v.module m " +
           "WHERE m.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);
    
    // ========== OPTIMIZED METHODS WITH JOIN FETCH ==========
    
    // ✅ 1. Get videos with module (FIXED - no userVideoProgresses)
    @Query("SELECT DISTINCT v FROM Video v " +
           "LEFT JOIN FETCH v.module m " +
           "WHERE v.module.id = :moduleId " +
           "ORDER BY v.displayOrder ASC")
    List<Video> findByModuleIdWithModule(@Param("moduleId") Long moduleId);
    
    // ✅ 2. Get video with module and course
    @Query("SELECT DISTINCT v FROM Video v " +
           "LEFT JOIN FETCH v.module m " +
           "LEFT JOIN FETCH m.course c " +
           "WHERE v.id = :videoId")
    Optional<Video> findByIdWithModuleAndCourse(@Param("videoId") Long videoId);
    
    // ✅ 3. Get videos for multiple modules
    @Query("SELECT DISTINCT v FROM Video v " +
           "LEFT JOIN FETCH v.module m " +
           "WHERE m.id IN :moduleIds " +
           "ORDER BY m.id, v.displayOrder ASC")
    List<Video> findByModuleIdsWithModule(@Param("moduleIds") List<Long> moduleIds);
    
    // ✅ 4. Get video with user progress (if you need it)
    @Query("SELECT v FROM Video v " +
           "WHERE v.id = :videoId")
    Optional<Video> findByIdBasic(@Param("videoId") Long videoId);
    
    // ✅ 5. Get first unlocked video in module
    @Query("SELECT v FROM Video v " +
           "WHERE v.module.id = :moduleId " +
           "ORDER BY v.displayOrder ASC " +
           "LIMIT 1")
    Optional<Video> findFirstByModuleId(@Param("moduleId") Long moduleId);
    
    // ✅ 6. Get next video in sequence
    @Query("SELECT v FROM Video v " +
           "WHERE v.module.id = :moduleId " +
           "AND v.displayOrder > :currentOrder " +
           "ORDER BY v.displayOrder ASC " +
           "LIMIT 1")
    Optional<Video> findNextVideo(@Param("moduleId") Long moduleId, 
                                  @Param("currentOrder") Integer currentOrder);
    
    // ✅ 7. Check if video exists in module
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END " +
           "FROM Video v WHERE v.id = :videoId AND v.module.id = :moduleId")
    boolean existsByIdAndModuleId(@Param("videoId") Long videoId, 
                                  @Param("moduleId") Long moduleId);
}