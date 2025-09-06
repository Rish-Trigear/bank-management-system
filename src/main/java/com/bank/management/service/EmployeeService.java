package com.bank.management.service;

import com.bank.management.dto.LoginResponse;
import com.bank.management.model.Employee;
import com.bank.management.model.Role;
import com.bank.management.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * Service layer for Employee business logic
 * 
 * This service handles all employee-related operations including:
 * - Employee registration with email validation and password encryption
 * - Employee authentication using email and JWT token generation
 * - CRUD operations for employee management
 * - Auto-generation of unique employee IDs
 * - Email uniqueness validation to prevent duplicates
 * 
 * Integrates with EmployeeRepository for data access and JwtService for authentication.
 * Employees use email for login instead of SSN ID like customers.
 */
@Service
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * Create a new employee account
     * Validates email uniqueness, generates employee ID, and encrypts password
     */
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new RuntimeException("Employee with email " + employee.getEmail() + " already exists");
        }
        
        // Generate employee ID if not provided
        if (employee.getEmployeeId() == null || employee.getEmployeeId().isEmpty()) {
            employee.setEmployeeId(generateEmployeeId());
        }
        
        employee.setPasswordHash(passwordEncoder.encode(employee.getPassword()));
        return employeeRepository.save(employee);
    }
    
    /**
     * Authenticate employee using email and password
     * Validates credentials and returns login response with JWT token
     */
    public LoginResponse login(String email, String password) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (!passwordEncoder.matches(password, employee.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        String token = jwtService.generateToken(employee.getEmployeeId());
        String fullName = employee.getFirstName() + " " + (employee.getLastName() != null ? employee.getLastName() : "");
        return new LoginResponse(token, "Login successful", 
            employee.getRole().toString(), employee.getEmployeeId(), fullName.trim());
    }
    
    public Employee getEmployeeById(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));
    }
    
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    public Employee updateEmployee(String employeeId, Employee employeeDetails) {
        Employee employee = getEmployeeById(employeeId);
        
        employee.setFirstName(employeeDetails.getFirstName());
        employee.setLastName(employeeDetails.getLastName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setAddress(employeeDetails.getAddress());
        employee.setContactNumber(employeeDetails.getContactNumber());
        employee.setRole(employeeDetails.getRole());
        
        if (employeeDetails.getPassword() != null && !employeeDetails.getPassword().isEmpty()) {
            employee.setPasswordHash(passwordEncoder.encode(employeeDetails.getPassword()));
        }
        
        return employeeRepository.save(employee);
    }
    
    public void deleteEmployee(String employeeId) {
        Employee employee = getEmployeeById(employeeId);
        employeeRepository.delete(employee);
    }
    
    /**
     * Generate a unique 7-digit employee ID
     * Ensures uniqueness by checking against existing employee IDs
     */
    private String generateEmployeeId() {
        Random random = new Random();
        String employeeId;
        do {
            employeeId = String.format("%07d", random.nextInt(10000000));
        } while (employeeRepository.findByEmployeeId(employeeId).isPresent());
        
        return employeeId;
    }
}