package com.example.cdaxVideo.Controller;

import com.example.cdaxVideo.DTO.CourseResponseDTO;
import com.example.cdaxVideo.Service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private CourseService courseService;

    /**
     * Fetch dashboard courses for a user
     * - New user: all courses
     * - Existing user: enrolled courses
     */
    @GetMapping("/user-courses")
    public ResponseEntity<Map<String, Object>> getDashboardCourses(@RequestParam Long userId) {
        List<CourseResponseDTO> courses = courseService.getDashboardCourses(userId);
        Map<String,Object> resp = new HashMap<>();
        resp.put("userId", userId);
        resp.put("courses", courses);
        return ResponseEntity.ok(resp);
    }

    /**
     * Fetch available courses for Explore More section
     * Excludes courses already purchased
     */
    @GetMapping("/courses/available")
    public ResponseEntity<List<CourseResponseDTO>> getAvailableCourses(@RequestParam Long userId) {
        List<CourseResponseDTO> available = courseService.getAvailableCoursesForUser(userId);
        return ResponseEntity.ok(available);
    }

    /**
     * Dashboard stats for cards: Courses, In Progress, Videos, Progress %
     */

}
