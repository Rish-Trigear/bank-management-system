package com.bank.management.service;

import com.bank.management.model.Customer;
import com.bank.management.model.Loan;
import com.bank.management.repository.CustomerRepository;
import com.bank.management.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * Service layer for Loan business logic
 * 
 * This service handles all loan-related operations including:
 * - Loan application creation with customer association
 * - Auto-generation of unique loan IDs (LOAN + 8 digits)
 * - CRUD operations for loan management
 * - Customer loan history retrieval
 * - Loan processing and updates by employees
 * 
 * Integrates with LoanRepository and CustomerRepository for data access.
 * Ensures proper customer validation before loan creation.
 */
@Service
public class LoanService {
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * Create a new loan application for a customer
     * Validates customer exists, generates loan ID, and associates with customer
     */
    public Loan createLoan(Loan loan, String customerSsnId) {
        Customer customer = customerRepository.findBySsnId(customerSsnId)
                .orElseThrow(() -> new RuntimeException("Customer not found with SSN: " + customerSsnId));
        
        loan.setCustomer(customer);
        
        // Generate loan ID if not provided
        if (loan.getLoanId() == null || loan.getLoanId().isEmpty()) {
            loan.setLoanId(generateLoanId());
        }
        
        return loanRepository.save(loan);
    }
    
    public Loan getLoanById(String loanId) {
        return loanRepository.findByLoanId(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));
    }
    
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }
    
    public List<Loan> getLoansByCustomer(String customerSsnId) {
        return loanRepository.findAllByCustomer_SsnId(customerSsnId);
    }
    
    public Loan updateLoan(String loanId, Loan loanDetails) {
        Loan loan = getLoanById(loanId);
        
        loan.setOccupation(loanDetails.getOccupation());
        loan.setEmployerName(loanDetails.getEmployerName());
        loan.setEmployerAddress(loanDetails.getEmployerAddress());
        loan.setEmail(loanDetails.getEmail());
        loan.setAddress(loanDetails.getAddress());
        loan.setMaritalStatus(loanDetails.getMaritalStatus());
        loan.setContactNumber(loanDetails.getContactNumber());
        loan.setLoanAmount(loanDetails.getLoanAmount());
        loan.setDurationMonths(loanDetails.getDurationMonths());
        
        return loanRepository.save(loan);
    }
    
    public void deleteLoan(String loanId) {
        Loan loan = getLoanById(loanId);
        loanRepository.delete(loan);
    }
    
    /**
     * Generate a unique loan ID with format "LOAN" + 8 digits
     * Ensures uniqueness by checking against existing loan IDs
     */
    private String generateLoanId() {
        Random random = new Random();
        String loanId;
        do {
            loanId = "LOAN" + String.format("%08d", random.nextInt(100000000));
        } while (loanRepository.findByLoanId(loanId).isPresent());
        
        return loanId;
    }
}