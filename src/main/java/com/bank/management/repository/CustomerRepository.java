package com.bank.management.repository;

import com.bank.management.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for Customer entity
 * 
 * This repository extends JpaRepository providing CRUD operations for customers.
 * Custom methods include:
 * - Finding customers by SSN ID (used for login/authentication)
 * - Checking if SSN ID already exists (for registration validation)
 * 
 * Spring Data JPA automatically implements these methods at runtime.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    /**
     * Find a customer by their unique SSN ID
     * Used for customer authentication and profile lookup
     */
    Optional<Customer> findBySsnId(String ssnId);
    
    /**
     * Check if a customer with given SSN ID already exists
     * Used during registration to prevent duplicate SSN IDs
     */
    boolean existsBySsnId(String ssnId);
    
    /**
     * Check if a customer with given account number already exists
     * Used during registration to prevent duplicate account numbers
     */
    boolean existsByAccountNumber(String accountNumber);
}