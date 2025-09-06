package com.bank.management.controller;

import com.bank.management.dto.LoginRequest;
import com.bank.management.dto.LoginResponse;
import com.bank.management.model.Customer;
import com.bank.management.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Customer operations
 * 
 * This controller provides HTTP endpoints for customer management:
 * - POST /api/customers/register - Create new customer account
 * - POST /api/customers/login - Customer authentication
 * - GET /api/customers - Retrieve all customers
 * - GET /api/customers/{ssnId} - Get specific customer
 * - PUT /api/customers/{ssnId} - Update customer details
 * - DELETE /api/customers/{ssnId} - Delete customer account
 * 
 * All endpoints (except register/login) require authentication.
 */
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer Management", description = "Simple customer CRUD operations")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new customer")
    public ResponseEntity<Customer> registerCustomer(@RequestBody Customer customer) {
        Customer createdCustomer = customerService.createCustomer(customer);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Customer login")
    public ResponseEntity<LoginResponse> loginCustomer(@RequestBody LoginRequest request) {
        LoginResponse response = customerService.login(request.getSsnId(), request.getPassword());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }
    
    @GetMapping("/{ssnId}")
    @Operation(summary = "Get customer by SSN ID")
    public ResponseEntity<Customer> getCustomerBySsn(@PathVariable String ssnId) {
        Customer customer = customerService.getCustomerBySsn(ssnId);
        return ResponseEntity.ok(customer);
    }
    
    @PutMapping("/{ssnId}")
    @Operation(summary = "Update customer")
    public ResponseEntity<Customer> updateCustomer(@PathVariable String ssnId, @RequestBody Customer customer) {
        Customer updatedCustomer = customerService.updateCustomer(ssnId, customer);
        return ResponseEntity.ok(updatedCustomer);
    }
    
    @DeleteMapping("/{ssnId}")
    @Operation(summary = "Delete customer")
    public ResponseEntity<String> deleteCustomer(@PathVariable String ssnId) {
        customerService.deleteCustomer(ssnId);
        return ResponseEntity.ok("Customer deleted successfully");
    }
}