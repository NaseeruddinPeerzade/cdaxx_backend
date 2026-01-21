// ============================================
// FILE 2: JwtRequestFilter.java (OPTIMIZED)
// ============================================

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
import java.util.Collections;

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
        
        // Log every request for debugging
        System.out.println("\n🎯 JWT Filter - shouldNotFilter()");
        System.out.println("📍 Path: " + path);
        System.out.println("📍 Method: " + method);
        
        // Skip OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("✅ SKIP: OPTIONS (CORS Preflight)");
            return true;
        }
        
        // Skip auth endpoints
        if (path.startsWith("/api/auth/")) {
            System.out.println("✅ SKIP: Auth endpoint");
            return true;
        }
        
        // Skip uploads
        if (path.startsWith("/uploads/")) {
            System.out.println("✅ SKIP: Public uploads");
            return true;
        }
        
        // Skip Swagger
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            System.out.println("✅ SKIP: Swagger");
            return true;
        }
        
        // Skip debug
        if (path.startsWith("/api/debug/")) {
            System.out.println("✅ SKIP: Debug endpoint");
            return true;
        }
        
        // Skip actuator
        if (path.startsWith("/actuator/")) {
            System.out.println("✅ SKIP: Actuator endpoint");
            return true;
        }
        
        // 🔥 ONLY SKIP THESE SPECIFIC PUBLIC ENDPOINTS (GET only)
        if ("GET".equalsIgnoreCase(method)) {
            // Course list
            if (path.equals("/api/courses")) {
                System.out.println("✅ SKIP: GET /api/courses (PUBLIC)");
                return true;
            }
            
            // Single course
            if (path.matches("^/api/courses/\\d+$")) {
                System.out.println("✅ SKIP: GET /api/courses/{id} (PUBLIC)");
                return true;
            }
            
            // Module details (NOT assessments!)
            if (path.matches("^/api/modules/\\d+$")) {
                System.out.println("✅ SKIP: GET /api/modules/{id} (PUBLIC)");
                return true;
            }
            
            // Video details (NOT progress!)
            if (path.matches("^/api/videos/\\d+$")) {
                System.out.println("✅ SKIP: GET /api/videos/{id} (PUBLIC)");
                return true;
            }
        }
        
        // 🔒 Everything else requires JWT validation
        System.out.println("🔒 VALIDATE: JWT required for this endpoint");
        return false;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain)
            throws ServletException, IOException {
        
        // Add debug headers to response
        response.addHeader("X-JWT-Filter", "processed");
        response.addHeader("X-Request-Path", request.getServletPath());
        
        String path = request.getServletPath();
        System.out.println("\n🔐 JWT FILTER - VALIDATING TOKEN");
        System.out.println("📍 Path: " + path);
        System.out.println("📍 Method: " + request.getMethod());
        
        // Log headers for debugging
        System.out.println("📋 Request Headers:");
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            String value = headerName.equals("Authorization") 
                ? request.getHeader(headerName).substring(0, Math.min(20, request.getHeader(headerName).length())) + "..." 
                : request.getHeader(headerName);
            System.out.println("   - " + headerName + ": " + value);
        });
        
        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;
        
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            System.out.println("🔑 Token found (length: " + jwtToken.length() + ")");
            
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                System.out.println("👤 Username extracted: " + username);
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Invalid JWT Token format");
            } catch (ExpiredJwtException e) {
                System.out.println("⚠️ JWT Token expired");
            } catch (Exception e) {
                System.out.println("❌ JWT parsing error: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ No Authorization header or doesn't start with 'Bearer '");
            if (requestTokenHeader != null) {
                System.out.println("   Header value: " + requestTokenHeader.substring(0, Math.min(20, requestTokenHeader.length())) + "...");
            }
        }
        
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    System.out.println("✅ Token valid - Setting authentication");
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                        );
                    
                    authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("✅ Authentication set in SecurityContext");
                } else {
                    System.out.println("❌ Token validation failed");
                }
            } catch (UsernameNotFoundException e) {
                System.out.println("❌ User not found: " + username);
            } catch (Exception e) {
                System.out.println("❌ Error loading user: " + e.getMessage());
            }
        } else {
            if (username == null) {
                System.out.println("❌ No username extracted from token");
            } else {
                System.out.println("ℹ️ Authentication already exists in context");
            }
        }
        
        System.out.println("➡️ Continuing filter chain...");
        chain.doFilter(request, response);
    }
}