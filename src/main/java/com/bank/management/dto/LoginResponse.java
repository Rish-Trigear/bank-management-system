package com.bank.management.dto;

/**
 * Data Transfer Object for login responses
 * 
 * This DTO contains authentication response data:
 * - token: JWT token for authenticated requests
 * - message: Success/failure message
 * - role: User role (CUSTOMER or EMPLOYEE)
 * - userId: User identifier (SSN ID or Employee ID)
 * - name: User's display name
 * 
 * Returned after successful authentication to provide client with necessary data.
 */
public class LoginResponse {
    private String token; // JWT authentication token
    private String message; // Response message
    private String role; // User role (CUSTOMER/EMPLOYEE)
    private String userId; // ssnId for customer, employeeId for employee
    private String name; // User's full name
    
    public LoginResponse(String token, String message, String role, String userId, String name) {
        this.token = token;
        this.message = message;
        this.role = role;
        this.userId = userId;
        this.name = name;
    }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}