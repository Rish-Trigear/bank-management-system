package com.bank.customer.service;

import com.bank.customer.dto.LoginResponse;
import com.bank.customer.model.Customer;
import com.bank.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    public Customer createCustomer(Customer customer) {
        if (customer.getSsnId() == null || customer.getSsnId().trim().isEmpty()) {
            customer.setSsnId(generateUniqueSsnId());
        } else if (customerRepository.existsBySsnId(customer.getSsnId())) {
            throw new RuntimeException("Customer with SSN ID " + customer.getSsnId() + " already exists");
        }
        
        if (customer.getAccountNumber() == null || customer.getAccountNumber().trim().isEmpty()) {
            customer.setAccountNumber(generateUniqueAccountNumber());
        }
        
        customer.setPasswordHash(passwordEncoder.encode(customer.getPassword()));
        return customerRepository.save(customer);
    }
    
    private String generateUniqueSsnId() {
        Random random = new Random();
        String ssnId;
        do {
            int randomNumber = 1000000 + random.nextInt(9000000);
            ssnId = String.valueOf(randomNumber);
        } while (customerRepository.existsBySsnId(ssnId));
        return ssnId;
    }
    
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "ACC" + System.currentTimeMillis() + (int)(Math.random() * 10000);
        } while (customerRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
    
    public LoginResponse login(String ssnId, String password) {
        Customer customer = customerRepository.findBySsnId(ssnId)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (!customer.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }
        
        if (!passwordEncoder.matches(password, customer.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        String token = jwtService.generateToken(ssnId);
        String fullName = customer.getFirstName() + " " + (customer.getLastName() != null ? customer.getLastName() : "");
        return new LoginResponse(token, "Login successful", 
            customer.getRole().toString(), ssnId, fullName.trim());
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
        
        customer.setFirstName(customerDetails.getFirstName());
        customer.setLastName(customerDetails.getLastName());
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
    
    public long getCustomerCount() {
        return customerRepository.count();
    }
    
    public Customer activateCustomer(String ssnId) {
        Customer customer = getCustomerBySsn(ssnId);
        customer.setActive(true);
        return customerRepository.save(customer);
    }
    
    public Customer deactivateCustomer(String ssnId) {
        Customer customer = getCustomerBySsn(ssnId);
        customer.setActive(false);
        return customerRepository.save(customer);
    }
}