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
import java.util.HashSet;
import java.util.Set;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    // Cache of public endpoints to avoid repeated string operations
    private final Set<String> publicEndpoints = new HashSet<>();
    
    public JwtRequestFilter() {
        // Initialize public endpoints cache
        initializePublicEndpoints();
    }
    
    private void initializePublicEndpoints() {
        // Static public endpoints
        publicEndpoints.add("/api/dashboard/public");
        publicEndpoints.add("/swagger-ui.html");
        publicEndpoints.add("/actuator/health");
        publicEndpoints.add("/actuator/info");
        
        // Auth endpoints
        publicEndpoints.add("/api/auth/login");
        publicEndpoints.add("/api/auth/register");
        publicEndpoints.add("/api/auth/jwt/login");
        publicEndpoints.add("/api/auth/jwt/register");
        publicEndpoints.add("/api/auth/jwt/validate");
        publicEndpoints.add("/api/auth/jwt/refresh");
        publicEndpoints.add("/api/auth/forgot-password");
        publicEndpoints.add("/api/auth/reset-password");
        publicEndpoints.add("/api/auth/verify-email");
        publicEndpoints.add("/api/auth/firstName");
        publicEndpoints.add("/api/auth/getUserByEmail");
        publicEndpoints.add("/api/auth/logout");
        
        // Course endpoints
        publicEndpoints.add("/api/courses");
        publicEndpoints.add("/api/courses/tags/popular");
        publicEndpoints.add("/api/courses/search/suggestions");
        publicEndpoints.add("/api/courses/advanced-search");
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();
        
        System.out.println("🔍 JWT Filter checking: " + method + " " + path);
        
        // ✅ Quick check: If it's a known public endpoint, skip immediately
        if (publicEndpoints.contains(path.split("\\?")[0])) {
            System.out.println("✅ Known public endpoint, skipping filter");
            return true;
        }
        
        // ✅ Use the full logic
        if (isPublicEndpoint(path, method)) {
            System.out.println("✅ Skipping JWT filter for public endpoint");
            return true;
        }
        
        System.out.println("🔐 Applying JWT filter (requires authentication)");
        return false;
    }
    
private boolean isPublicEndpoint(String path, String method) {
    // Always skip OPTIONS (CORS preflight)
    if ("OPTIONS".equalsIgnoreCase(method)) {
        return true;
    }
    
    // Remove query parameters for clean matching
    String cleanPath = path.split("\\?")[0];
    
    System.out.println("   Clean path for matching: " + cleanPath);
    
    // List ALL public endpoints from SecurityConfig
    boolean isPublic = false;
    
    // Public resources (startsWith checks)
    if (cleanPath.startsWith("/api/public/") ||
        cleanPath.startsWith("/api/debug/") ||
        cleanPath.startsWith("/uploads/") ||
        cleanPath.startsWith("/swagger-ui/") ||
        cleanPath.startsWith("/v3/api-docs/") ||
        cleanPath.startsWith("/webjars/") ||
        cleanPath.startsWith("/swagger-resources/") ||
        cleanPath.startsWith("/api/videos/public/") ||
        cleanPath.startsWith("/api/test/")) {
        
        isPublic = true;
    }
    
    // ⚠️ CRITICAL FIX: Exclude profile endpoints from being treated as public
    if (cleanPath.startsWith("/api/auth/profile/")) {
        System.out.println("   ⚠️ Profile endpoint detected - NOT public, requires authentication");
        return false; // Profile endpoints are NOT public!
    }
    
    // Specific AUTH endpoints that are public
    if (cleanPath.startsWith("/api/auth/")) {
        // Check if it's one of our known public auth endpoints
        isPublic = publicEndpoints.contains(cleanPath);
        System.out.println("   Is auth endpoint public: " + isPublic);
    }
    
    // For GET requests specifically
    if ("GET".equalsIgnoreCase(method)) {
        // ✅ FIXED: Check exact matches first
        if (cleanPath.equals("/api/courses") ||                    // Exact match
            cleanPath.equals("/api/courses/tags/popular") ||
            cleanPath.equals("/api/courses/search/suggestions") ||
            cleanPath.equals("/api/courses/advanced-search") ||
            
            // ✅ FIXED: Check path patterns
            cleanPath.startsWith("/api/courses/public") ||
            cleanPath.matches("/api/courses/\\d+") ||              // /api/courses/{id}
            cleanPath.startsWith("/api/courses/tag/") ||           // /api/courses/tag/{tagName}
            
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
    
    // ✅ CRITICAL DEBUG: Print why a path might not be recognized as public
    if (!isPublic && "GET".equalsIgnoreCase(method)) {
        System.out.println("   ⚠️ Path not recognized as public. Details:");
        System.out.println("      cleanPath: " + cleanPath);
        System.out.println("      equals /api/courses: " + cleanPath.equals("/api/courses"));
        System.out.println("      matches courses pattern: " + cleanPath.matches("/api/courses/\\d+"));
        System.out.println("      startsWith courses/public: " + cleanPath.startsWith("/api/courses/public"));
    }
    
    System.out.println("   Final isPublic: " + isPublic);
    return isPublic;
}
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        System.out.println("🔐 JWT Filter processing path: " + requestPath + 
                         " with query: " + request.getQueryString());
        
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
            // Don't print all headers in production for security
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
            // Note: Spring Security will handle the actual 401 response
        }
        
        chain.doFilter(request, response);
    }
}