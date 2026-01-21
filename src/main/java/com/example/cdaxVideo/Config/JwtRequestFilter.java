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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        
        System.out.println("\n🔍 JWT Filter - shouldNotFilter:");
        System.out.println("   Path: " + path);
        System.out.println("   Method: " + method);
        
        // ✅ Skip OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("   ✅ SKIP: OPTIONS request");
            return true;
        }
        
        // ✅ Skip all auth endpoints
        if (path.startsWith("/api/auth/")) {
            System.out.println("   ✅ SKIP: Auth endpoint");
            return true;
        }
        
        // ✅ Skip public uploads
        if (path.startsWith("/uploads/")) {
            System.out.println("   ✅ SKIP: Public uploads");
            return true;
        }
        
        // ✅ Skip Swagger
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            System.out.println("   ✅ SKIP: Swagger");
            return true;
        }
        
        // ✅ Skip debug endpoints
        if (path.startsWith("/api/debug/")) {
            System.out.println("   ✅ SKIP: Debug endpoint");
            return true;
        }
        
        // 🔥 CRITICAL: Skip ALL GET requests to these patterns
        if ("GET".equalsIgnoreCase(method)) {
            // Module assessments: /api/modules/{id}/assessments
            if (path.matches("/api/modules/\\d+/assessments")) {
                System.out.println("   ✅ SKIP: GET module assessments (PUBLIC)");
                return true;
            }
            
            // Assessment questions: /api/assessments/{id}/questions
            if (path.matches("/api/assessments/\\d+/questions")) {
                System.out.println("   ✅ SKIP: GET assessment questions (PUBLIC)");
                return true;
            }
            
            // Single course: /api/courses/{id}
            if (path.matches("/api/courses/\\d+")) {
                System.out.println("   ✅ SKIP: GET single course (PUBLIC)");
                return true;
            }
            
            // Course list: /api/courses
            if (path.equals("/api/courses")) {
                System.out.println("   ✅ SKIP: GET courses list (PUBLIC)");
                return true;
            }
            
            // Any modules endpoint
            if (path.startsWith("/api/modules/")) {
                System.out.println("   ✅ SKIP: GET modules (PUBLIC)");
                return true;
            }
            
            // Any videos endpoint
            if (path.startsWith("/api/videos/")) {
                System.out.println("   ✅ SKIP: GET videos (PUBLIC)");
                return true;
            }
            
            // Any assessments endpoint (GET only)
            if (path.startsWith("/api/assessments/")) {
                System.out.println("   ✅ SKIP: GET assessments (PUBLIC)");
                return true;
            }
            
            // Course assessment endpoints
            if (path.startsWith("/api/course/assessment/")) {
                System.out.println("   ✅ SKIP: GET course assessment (PUBLIC)");
                return true;
            }
            
            // Questions endpoint
            if (path.startsWith("/api/questions/")) {
                System.out.println("   ✅ SKIP: GET questions (PUBLIC)");
                return true;
            }
        }
        
        System.out.println("   ➡️ WILL FILTER: Requires JWT validation");
        return false;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain)
            throws ServletException, IOException {
        
        String requestPath = request.getServletPath();
        String method = request.getMethod();
        
        System.out.println("\n=== JWT FILTER - VALIDATING ===");
        System.out.println("Path: " + requestPath);
        System.out.println("Method: " + method);
        
        final String requestTokenHeader = request.getHeader("Authorization");
        System.out.println("Auth Header: " + (requestTokenHeader != null ? "Present" : "Missing"));
        
        String username = null;
        String jwtToken = null;
        
        // Extract JWT token from Authorization header
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                System.out.println("Username: " + username);
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Invalid JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("⚠️ JWT Token expired");
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
        
        // Validate token and set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    System.out.println("✅ Token valid - Setting authentication");
                    
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
                } else {
                    System.out.println("❌ Token validation failed");
                }
            } catch (UsernameNotFoundException e) {
                System.out.println("❌ User not found: " + username);
            }
        }
        
        System.out.println("=== JWT FILTER END ===\n");
        chain.doFilter(request, response);
    }
}