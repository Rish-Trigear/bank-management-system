package com.bank.management.controller;

import com.bank.management.dto.LoginRequest;
import com.bank.management.dto.LoginResponse;
import com.bank.management.model.Employee;
import com.bank.management.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Employee operations
 * 
 * This controller provides HTTP endpoints for employee management:
 * - POST /api/employees/register - Create new employee account
 * - POST /api/employees/login - Employee authentication
 * - GET /api/employees - Retrieve all employees
 * - GET /api/employees/{employeeId} - Get specific employee
 * - PUT /api/employees/{employeeId} - Update employee details
 * - DELETE /api/employees/{employeeId} - Delete employee account
 * 
 * Employees use email for login instead of SSN ID.
 * All endpoints (except register/login) require authentication.
 */
@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "Employee CRUD operations and authentication")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new employee or manager")
    public ResponseEntity<Employee> registerEmployee(@RequestBody Employee employee) {
        Employee createdEmployee = employeeService.createEmployee(employee);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Employee/Manager login")
    public ResponseEntity<LoginResponse> loginEmployee(@RequestBody LoginRequest request) {
        // For employees, we use email as the login identifier
        LoginResponse response = employeeService.login(request.getSsnId(), request.getPassword());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/{employeeId}")
    @Operation(summary = "Get employee by Employee ID")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String employeeId) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(employee);
    }
    
    @PutMapping("/{employeeId}")
    @Operation(summary = "Update employee")
    public ResponseEntity<Employee> updateEmployee(@PathVariable String employeeId, @RequestBody Employee employee) {
        Employee updatedEmployee = employeeService.updateEmployee(employeeId, employee);
        return ResponseEntity.ok(updatedEmployee);
    }
    
    @DeleteMapping("/{employeeId}")
    @Operation(summary = "Delete employee")
    public ResponseEntity<String> deleteEmployee(@PathVariable String employeeId) {
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok("Employee deleted successfully");
    }
}