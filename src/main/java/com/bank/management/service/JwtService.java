package com.bank.management.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * JWT (JSON Web Token) Service for authentication
 * 
 * This service handles JWT token operations including:
 * - Token generation with user information and expiration
 * - Token validation and expiration checking
 * - Username extraction from tokens
 * - Secure token signing with HMAC SHA-256
 * 
 * Tokens are valid for 7 days and contain the username/user ID in the subject claim.
 * Used by authentication services to create and validate JWT tokens for API access.
 */
@Service
public class JwtService {
    
    private static final String SECRET_KEY = "mySecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256";
    private static final long JWT_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days in milliseconds
    
    /**
     * Get the signing key for JWT token creation and validation
     * Uses HMAC SHA-256 algorithm with the secret key
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
    
    /**
     * Generate a JWT token for a user
     * Contains username, issued date, expiration date, and signature
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // Username/User ID stored in subject claim
                .setIssuedAt(new Date()) // Token creation timestamp
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION)) // 7 days validity
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Secure signature
                .compact();
    }
    
    /**
     * Extract username from JWT token
     * Returns the subject claim which contains the user identifier
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }
    
    /**
     * Validate JWT token by checking expiration and signature
     * Returns false if token is expired or malformed
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false; // Invalid signature or malformed token
        }
    }
    
    /**
     * Check if JWT token has expired
     */
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
    
    /**
     * Parse JWT token and extract all claims
     * Verifies signature and returns token body
     */
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}