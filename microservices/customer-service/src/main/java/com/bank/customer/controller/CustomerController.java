package com.bank.customer.controller;

import com.bank.customer.dto.LoginRequest;
import com.bank.customer.dto.LoginResponse;
import com.bank.customer.model.Customer;
import com.bank.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    @PostMapping("/register")
    public ResponseEntity<Customer> registerCustomer(@RequestBody Customer customer) {
        Customer createdCustomer = customerService.createCustomer(customer);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginCustomer(@RequestBody LoginRequest request) {
        LoginResponse response = customerService.login(request.getSsnId(), request.getPassword());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }
    
    @GetMapping("/{ssnId}")
    public ResponseEntity<Customer> getCustomerBySsn(@PathVariable String ssnId) {
        Customer customer = customerService.getCustomerBySsn(ssnId);
        return ResponseEntity.ok(customer);
    }
    
    @PutMapping("/{ssnId}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable String ssnId, @RequestBody Customer customer) {
        Customer updatedCustomer = customerService.updateCustomer(ssnId, customer);
        return ResponseEntity.ok(updatedCustomer);
    }
    
    @DeleteMapping("/{ssnId}")
    public ResponseEntity<String> deleteCustomer(@PathVariable String ssnId) {
        customerService.deleteCustomer(ssnId);
        return ResponseEntity.ok("Customer deleted successfully");
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getCustomerCount() {
        Long count = customerService.getCustomerCount();
        return ResponseEntity.ok(count);
    }
    
    @PutMapping("/{ssnId}/activate")
    public ResponseEntity<Customer> activateCustomer(@PathVariable String ssnId) {
        Customer customer = customerService.activateCustomer(ssnId);
        return ResponseEntity.ok(customer);
    }
    
    @PutMapping("/{ssnId}/deactivate")
    public ResponseEntity<Customer> deactivateCustomer(@PathVariable String ssnId) {
        Customer customer = customerService.deactivateCustomer(ssnId);
        return ResponseEntity.ok(customer);
    }
}