package com.bank.customer.dto;

public class LoginResponse {
    private String token;
    private String message;
    private String role;
    private String userId;
    private String name;
    
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