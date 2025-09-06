package com.bank.management.repository;

import com.bank.management.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Loan entity
 * 
 * This repository provides CRUD operations for loan applications.
 * Custom methods include:
 * - Finding loans by unique loan ID (for individual loan lookup)
 * - Retrieving all loans for a specific customer (by customer's SSN ID)
 * 
 * Supports loan management operations and customer loan history queries.
 * Used by employees to process loan applications and by customers to view their loans.
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    /**
     * Find a loan application by its unique loan ID
     * Used for loan verification, processing, and details lookup
     */
    Optional<Loan> findByLoanId(String loanId);
    
    /**
     * Find all loan applications for a specific customer by their SSN ID
     * Used for generating customer loan history and portfolio view
     */
    List<Loan> findAllByCustomer_SsnId(String ssnId);
}