package com.bank.management.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for the Bank Management System
 * 
 * This class configures Spring Security to:
 * - Use JWT tokens for authentication (stateless)
 * - Allow public access to login/register endpoints
 * - Protect all other endpoints
 * - Enable H2 console and Swagger UI for development
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * Password encoder bean for hashing passwords
     * Uses BCrypt algorithm for secure password storage
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Main security filter chain configuration
     * Sets up authentication rules and JWT filter
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API usage
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS
            .headers(headers -> headers.frameOptions().sameOrigin()) // Allow H2 console in frames
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No sessions, use JWT
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication needed
                .requestMatchers(HttpMethod.POST, "/api/customers/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/customers/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/employees/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/employees/login").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // Database console
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll() // API docs
                .anyRequest().authenticated() // All other endpoints need authentication
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT filter
            
        return http.build();
    }
    
    /**
     * CORS configuration to allow requests from Angular frontend
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}