package com.example.cdaxVideo.Config;

import com.example.cdaxVideo.Service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();
        
        System.out.println("🔍 Checking if should filter: " + method + " " + path);
        
        // ✅ Skip ALL public endpoints (not just OPTIONS)
        if (isPublicEndpoint(path, method)) {
            System.out.println("✅ Skipping JWT filter for public endpoint");
            return true;
        }
        
        return false;
    }
    
    private boolean isPublicEndpoint(String path, String method) {
        // Always skip OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        
        // Remove query parameters for clean matching
        String cleanPath = path.split("\\?")[0];
        
        // Debug: Print what we're checking
        System.out.println("   Clean path for matching: " + cleanPath);
        
        // List ALL public endpoints from SecurityConfig
        boolean isPublic = 
            // Authentication endpoints (all methods)
            cleanPath.startsWith("/api/auth/") ||
            
            // Public resources
            cleanPath.startsWith("/api/public/") ||
            cleanPath.startsWith("/api/debug/") ||
            cleanPath.startsWith("/uploads/") ||
            
            // Swagger/OpenAPI
            cleanPath.startsWith("/swagger-ui/") ||
            cleanPath.startsWith("/v3/api-docs/") ||
            cleanPath.equals("/swagger-ui.html") ||
            cleanPath.startsWith("/webjars/") ||
            cleanPath.startsWith("/swagger-resources/") ||
            
            // Actuator
            cleanPath.equals("/actuator/health") ||
            cleanPath.equals("/actuator/info") ||
            
            // Legacy public endpoints
            cleanPath.equals("/api/dashboard/public") ||
            cleanPath.startsWith("/api/videos/public/") ||
            cleanPath.startsWith("/api/test/");
        
        // For GET requests specifically
        if ("GET".equalsIgnoreCase(method)) {
            isPublic = isPublic ||
                // Public courses
                cleanPath.startsWith("/api/courses/public") ||
                cleanPath.equals("/api/courses") ||
                cleanPath.matches("/api/courses/\\d+") ||
                
                // ✅ FIXED: Module endpoints - CRITICAL FIX
                cleanPath.matches("/api/modules/\\d+") || // /api/modules/1
                cleanPath.matches("/api/modules/course/\\d+") || // /api/modules/course/1
                
                // ✅ FIXED: Videos and assessments under modules
                cleanPath.matches("/api/modules/\\d+/videos") || // /api/modules/1/videos
                cleanPath.matches("/api/modules/\\d+/assessments") || // /api/modules/1/assessments
                
                // Assessment endpoints
                cleanPath.startsWith("/api/course/assessment/") ||
                cleanPath.startsWith("/api/assessments/");
        }
        
        // For POST requests to auth endpoints
        if (("POST".equalsIgnoreCase(method) || "GET".equalsIgnoreCase(method)) && 
            cleanPath.startsWith("/api/auth/")) {
            isPublic = true;
        }
        
        System.out.println("   Is public endpoint: " + isPublic);
        return isPublic;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        System.out.println("🔐 JWT Filter processing: " + requestPath);
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwtToken = null;
        
        // Extract JWT token from Authorization header
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                System.out.println("Extracted Username: " + username);
            } catch (Exception e) {
                System.out.println("Error extracting username: " + e.getMessage());
            }
        }
        
        // Validate token and set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    System.out.println("✅ Token validated for: " + username);
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    
                    authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                System.out.println("Error setting authentication: " + e.getMessage());
            }
        }
        
        chain.doFilter(request, response);
    }
}