package com.example.cdaxVideo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    
    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, 
                         JwtRequestFilter jwtRequestFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtRequestFilter = jwtRequestFilter;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(handling -> handling.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // ✅ FIXED: Only permitAll for login/register, profile endpoints need auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/jwt/login").permitAll()
                .requestMatchers("/api/auth/jwt/register").permitAll()
                .requestMatchers("/api/auth/jwt/validate").permitAll()
                .requestMatchers("/api/auth/jwt/refresh").permitAll()
                .requestMatchers("/api/auth/forgot-password").permitAll()
                .requestMatchers("/api/auth/reset-password").permitAll()
                .requestMatchers("/api/auth/verify-email").permitAll()
                .requestMatchers("/api/auth/firstName").permitAll()
                .requestMatchers("/api/auth/getUserByEmail").permitAll()
                
                // Public course endpoints
                .requestMatchers("/api/courses/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/courses").permitAll() // Allow browsing courses without auth
                .requestMatchers(HttpMethod.GET, "/api/courses/{id}").permitAll() // Allow viewing course details
                
                // Public video endpoints
                .requestMatchers("/api/videos/public/**").permitAll()
                
                // ✅ ADDED: Legacy endpoints without JWT (for backward compatibility)
                .requestMatchers("/api/dashboard/public").permitAll()
                
                // Video progress and completion - REQUIRE AUTH
                .requestMatchers(HttpMethod.POST, "/api/videos/*/progress").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/videos/*/complete").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/courses/public").permitAll()
                
                // Assessment endpoints - REQUIRE AUTH
                .requestMatchers("/api/course/assessment/**").authenticated()
                .requestMatchers("/api/assessments/**").authenticated()
                .requestMatchers("/api/modules/*/assessments").authenticated()
                .requestMatchers("/api/courses/*/assessments").authenticated()
                
                // Favorites and cart - REQUIRE AUTH
                .requestMatchers("/api/favorites/**").authenticated()
                .requestMatchers("/api/cart/**").authenticated()
                
                // Courses - Mixed
                .requestMatchers("/api/courses/subscribed/**").authenticated()
                .requestMatchers("/api/courses/user/**").authenticated()
                .requestMatchers("/api/courses/{id}/enroll").authenticated()
                
                // User profile - REQUIRE AUTH
                .requestMatchers("/api/auth/profile/**").authenticated()
                .requestMatchers("/api/auth/jwt/me").authenticated()
                .requestMatchers("/api/auth/change-password").authenticated()
                
                // User data - REQUIRE AUTH
                .requestMatchers("/api/users/**").authenticated()
                
                // Swagger/OpenAPI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Actuator
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                // All other requests require authentication
                .anyRequest().authenticated()
            );
        
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Use allowedOriginPatterns with wildcards for development
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://192.168.*:*",
            "http://10.0.2.2:*",
            "https://cdax-app.onrender.com",
            "https://cdax-app.vercel.app"
        ));

        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Disposition"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}