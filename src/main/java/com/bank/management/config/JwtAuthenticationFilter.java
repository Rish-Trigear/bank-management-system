package com.bank.management.config;

import com.bank.management.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT Authentication Filter for Spring Security
 * 
 * This filter intercepts all HTTP requests to validate JWT tokens:
 * - Extracts JWT token from Authorization header (Bearer token)
 * - Validates token using JwtService
 * - Sets Spring Security authentication context if token is valid
 * - Allows request to continue through the filter chain
 * 
 * Runs once per request and integrates with Spring Security's authentication system.
 * Registered in SecurityConfig to process requests before other authentication filters.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    /**
     * Process each HTTP request to validate JWT authentication
     * 
     * Steps:
     * 1. Extract Authorization header and check for Bearer token
     * 2. Parse JWT token and extract username
     * 3. Validate token and create Spring Security authentication
     * 4. Continue filter chain processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Check if request has Authorization header with Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token (remove "Bearer " prefix)
        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);

        // Validate token and set authentication if valid
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtService.isTokenValid(jwt)) {
                UserDetails userDetails = User.builder()
                        .username(username)
                        .password("") // No password needed for JWT auth
                        .authorities(new ArrayList<>()) // Basic authorities
                        .build();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}