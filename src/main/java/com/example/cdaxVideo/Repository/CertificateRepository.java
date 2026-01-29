// Repository/CertificateRepository.java
package com.example.cdaxVideo.Repository;

import com.example.cdaxVideo.Entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, String> {
    
    @Query("SELECT c FROM Certificate c WHERE c.user.id = :userId AND c.isActive = true ORDER BY c.issuedDate DESC")
    List<Certificate> findByUserIdAndIsActiveTrue(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Certificate c WHERE c.user.id = :userId ORDER BY c.issuedDate DESC")
    List<Certificate> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Certificate c WHERE c.user.id = :userId AND c.course.id = :courseId")
    Optional<Certificate> findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
    
    Optional<Certificate> findByVerificationCode(String verificationCode);
    
    @Query("SELECT COUNT(c) > 0 FROM Certificate c WHERE c.user.id = :userId AND c.course.id = :courseId")
    boolean existsByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}