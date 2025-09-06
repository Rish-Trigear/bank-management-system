package com.bank.management.service;

import com.bank.management.dto.LoginResponse;
import com.bank.management.model.Customer;
import com.bank.management.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for Customer business logic
 * 
 * This service handles all customer-related operations including:
 * - Customer registration with password encryption
 * - Customer authentication and JWT token generation
 * - CRUD operations for customer management
 * - SSN ID validation to prevent duplicates
 * 
 * Integrates with CustomerRepository for data access and JwtService for authentication.
 */
@Service
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * Create a new customer account
     * Generates SSN ID if not provided, validates uniqueness and encrypts password before saving
     */
    public Customer createCustomer(Customer customer) {
        // Generate SSN ID if not provided
        if (customer.getSsnId() == null || customer.getSsnId().trim().isEmpty()) {
            customer.setSsnId(generateUniqueSsnId());
        } else if (customerRepository.existsBySsnId(customer.getSsnId())) {
            throw new RuntimeException("Customer with SSN ID " + customer.getSsnId() + " already exists");
        }
        
        // Generate account number if not provided
        if (customer.getAccountNumber() == null || customer.getAccountNumber().trim().isEmpty()) {
            customer.setAccountNumber(generateUniqueAccountNumber());
        }
        
        customer.setPasswordHash(passwordEncoder.encode(customer.getPassword()));
        return customerRepository.save(customer);
    }
    
    /**
     * Generate a unique SSN ID
     */
    private String generateUniqueSsnId() {
        String ssnId;
        do {
            ssnId = "SSN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (customerRepository.existsBySsnId(ssnId));
        return ssnId;
    }
    
    /**
     * Generate a unique account number
     */
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "ACC" + System.currentTimeMillis() + (int)(Math.random() * 10000);
        } while (customerRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
    
    /**
     * Authenticate customer and generate JWT token
     * Validates credentials and returns login response with token
     */
    public LoginResponse login(String ssnId, String password) {
        Customer customer = customerRepository.findBySsnId(ssnId)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (!passwordEncoder.matches(password, customer.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        String token = jwtService.generateToken(ssnId);
        return new LoginResponse(token, "Login successful", 
            customer.getRole().toString(), ssnId, customer.getName());
    }
    
    public Customer getCustomerBySsn(String ssnId) {
        return customerRepository.findBySsnId(ssnId)
                .orElseThrow(() -> new RuntimeException("Customer not found with SSN: " + ssnId));
    }
    
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    public Customer updateCustomer(String ssnId, Customer customerDetails) {
        Customer customer = getCustomerBySsn(ssnId);
        
        customer.setName(customerDetails.getName());
        customer.setEmail(customerDetails.getEmail());
        customer.setAddress(customerDetails.getAddress());
        customer.setContactNumber(customerDetails.getContactNumber());
        customer.setAadharNumber(customerDetails.getAadharNumber());
        customer.setPanNumber(customerDetails.getPanNumber());
        customer.setAccountNumber(customerDetails.getAccountNumber());
        customer.setDateOfBirth(customerDetails.getDateOfBirth());
        customer.setGender(customerDetails.getGender());
        customer.setMaritalStatus(customerDetails.getMaritalStatus());
        
        if (customerDetails.getPassword() != null && !customerDetails.getPassword().isEmpty()) {
            customer.setPasswordHash(passwordEncoder.encode(customerDetails.getPassword()));
        }
        
        return customerRepository.save(customer);
    }
    
    public void deleteCustomer(String ssnId) {
        Customer customer = getCustomerBySsn(ssnId);
        customerRepository.delete(customer);
    }
}