package com.example.cdaxVideo.Config;

import com.example.cdaxVideo.Service.CustomUserDetailsService;
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
    boolean isPublic = false; // Start with false
    
    // Public resources
    if (cleanPath.startsWith("/api/public/") ||
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
        cleanPath.startsWith("/api/test/")) {
        
        isPublic = true;
    }
    
    // ⚠️ CRITICAL FIX: Exclude profile endpoints from being treated as public
    if (cleanPath.startsWith("/api/auth/profile/")) {
        System.out.println("   ⚠️ Profile endpoint detected - NOT public, requires authentication");
        return false; // Profile endpoints are NOT public!
    }
    
    // Specific AUTH endpoints that are public (excluding profile)
    if (cleanPath.startsWith("/api/auth/")) {
        // Only these specific auth endpoints are public
        isPublic = 
            // Login/Register endpoints
            cleanPath.equals("/api/auth/login") ||
            cleanPath.equals("/api/auth/register") ||
            cleanPath.equals("/api/auth/jwt/login") ||
            cleanPath.equals("/api/auth/jwt/register") ||
            cleanPath.equals("/api/auth/jwt/validate") ||
            cleanPath.equals("/api/auth/jwt/refresh") ||
            cleanPath.equals("/api/auth/forgot-password") ||
            cleanPath.equals("/api/auth/reset-password") ||
            cleanPath.equals("/api/auth/verify-email") ||
            cleanPath.equals("/api/auth/firstName") ||
            cleanPath.equals("/api/auth/getUserByEmail") ||
            
            // Logout (handled separately)
            cleanPath.equals("/api/auth/logout");
        
        System.out.println("   Is auth endpoint public: " + isPublic);
    }
    
    // For GET requests specifically - ADD THESE LINES:
    if ("GET".equalsIgnoreCase(method)) {
        // Course endpoints (already in SecurityConfig as public)
        if (cleanPath.startsWith("/api/courses/public") ||
            cleanPath.equals("/api/courses") ||
            cleanPath.matches("/api/courses/\\d+") ||  // /api/courses/{id}
            
            // ✅ ADD THESE CRITICAL LINES - Tag endpoints
            cleanPath.startsWith("/api/courses/tag/") ||           // /api/courses/tag/{tagName}
            cleanPath.equals("/api/courses/tags/popular") ||      // /api/courses/tags/popular
            cleanPath.equals("/api/courses/search/suggestions") || // /api/courses/search/suggestions
            cleanPath.equals("/api/courses/advanced-search") ||    // /api/courses/advanced-search
            
            // Module endpoints
            cleanPath.matches("/api/modules/\\d+") ||              // /api/modules/{id}
            cleanPath.matches("/api/modules/course/\\d+") ||       // /api/modules/course/{courseId}
            
            // Videos and assessments under modules
            cleanPath.matches("/api/modules/\\d+/videos") ||       // /api/modules/{moduleId}/videos
            cleanPath.matches("/api/modules/\\d+/assessments") ||  // /api/modules/{moduleId}/assessments
            
            // Assessment endpoints
            cleanPath.startsWith("/api/course/assessment/") ||
            cleanPath.startsWith("/api/assessments/")) {
            
            isPublic = true;
        }
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
        } else {
            System.out.println("⚠️ No Authorization header or not Bearer token");
            System.out.println("⚠️ Available headers:");
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                if (!headerName.equalsIgnoreCase("authorization")) {
                    System.out.println("   - " + headerName + ": " + request.getHeader(headerName));
                }
            });
            if (requestTokenHeader != null) {
                System.out.println("⚠️ Authorization header exists but doesn't start with Bearer:");
                System.out.println("⚠️ '" + requestTokenHeader + "'");
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
                } else {
                    System.out.println("❌ Token validation failed for: " + username);
                }
            } catch (Exception e) {
                System.out.println("Error setting authentication: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (!isPublicEndpoint(requestPath, request.getMethod())) {
            // If this is not a public endpoint and no username was extracted
            System.out.println("❌ No valid token found for protected endpoint");
        }
        
        chain.doFilter(request, response);
    }
}