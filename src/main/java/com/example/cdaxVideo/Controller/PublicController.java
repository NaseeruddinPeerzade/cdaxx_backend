    package com.example.cdaxVideo.Controller;
    import com.example.cdaxVideo.Service.CourseService;
    import com.example.cdaxVideo.Entity.Assessment;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/public")
public class PublicController {
            @Autowired
        private CourseService courseService;
    
        @GetMapping("/modules/{moduleId}/assessments")
        public ResponseEntity<List<Assessment>> getAssessmentsByModule(@PathVariable Long moduleId) {
            return ResponseEntity.ok(courseService.getAssessmentsByModuleId(moduleId));
        }
}