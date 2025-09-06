package com.bank.management.repository;

import com.bank.management.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for Employee entity
 * 
 * This repository extends JpaRepository providing CRUD operations for employees.
 * Custom methods include:
 * - Finding employees by their unique employee ID (for profile lookup)
 * - Finding employees by email (used for login/authentication)
 * - Checking if email already exists (for registration validation)
 * 
 * Employees use email for login instead of SSN ID like customers.
 * Spring Data JPA automatically implements these methods at runtime.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    /**
     * Find an employee by their unique employee ID
     * Used for employee profile lookup and management operations
     */
    Optional<Employee> findByEmployeeId(String employeeId);
    
    /**
     * Find an employee by their email address
     * Used for employee authentication during login
     */
    Optional<Employee> findByEmail(String email);
    
    /**
     * Check if an employee with given email already exists
     * Used during registration to prevent duplicate email addresses
     */
    boolean existsByEmail(String email);
}