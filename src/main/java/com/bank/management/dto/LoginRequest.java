package com.bank.management.dto;

/**
 * Data Transfer Object for login requests
 * 
 * This DTO captures user credentials for authentication:
 * - ssnId: User's Social Security Number (identifier)
 * - password: Plain text password (encrypted during transmission)
 * 
 * Used by both customer and employee login endpoints.
 */
public class LoginRequest {
    private String ssnId; // User identifier
    private String password; // Plain text password for authentication
    
    public LoginRequest() {}
    
    public String getSsnId() { return ssnId; }
    public void setSsnId(String ssnId) { this.ssnId = ssnId; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}