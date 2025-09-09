package com.bank.loan.service;

import com.bank.loan.model.Loan;
import com.bank.loan.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoanService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private CustomerValidationService customerValidationService;
    
    public Loan createLoan(String customerSsnId, Loan loan) {
        logger.info("Creating loan for customer: {}", customerSsnId);
        
        if (!customerValidationService.customerExists(customerSsnId)) {
            logger.error("Customer not found: {}", customerSsnId);
            throw new RuntimeException("Customer not found with SSN ID: " + customerSsnId);
        }
        
        String loanId;
        do {
            loanId = "LOAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (loanRepository.existsByLoanId(loanId));
        
        loan.setLoanId(loanId);
        loan.setCustomerSsnId(customerSsnId);
        
        Loan savedLoan = loanRepository.save(loan);
        logger.info("Loan created successfully: {}", savedLoan.getLoanId());
        
        return savedLoan;
    }
    
    public Optional<Loan> getLoanById(String loanId) {
        logger.info("Fetching loan: {}", loanId);
        return loanRepository.findByLoanId(loanId);
    }
    
    public List<Loan> getLoansByCustomer(String customerSsnId) {
        logger.info("Fetching loans for customer: {}", customerSsnId);
        
        if (!customerValidationService.customerExists(customerSsnId)) {
            logger.error("Customer not found: {}", customerSsnId);
            throw new RuntimeException("Customer not found with SSN ID: " + customerSsnId);
        }
        
        return loanRepository.findByCustomerSsnId(customerSsnId);
    }
    
    public Loan updateLoan(String loanId, Loan updatedLoan) {
        logger.info("Updating loan: {}", loanId);
        
        Optional<Loan> existingLoanOpt = loanRepository.findByLoanId(loanId);
        if (existingLoanOpt.isEmpty()) {
            logger.error("Loan not found: {}", loanId);
            throw new RuntimeException("Loan not found with ID: " + loanId);
        }
        
        Loan existingLoan = existingLoanOpt.get();
        
        if (updatedLoan.getOccupation() != null) {
            existingLoan.setOccupation(updatedLoan.getOccupation());
        }
        if (updatedLoan.getEmployerName() != null) {
            existingLoan.setEmployerName(updatedLoan.getEmployerName());
        }
        if (updatedLoan.getEmployerAddress() != null) {
            existingLoan.setEmployerAddress(updatedLoan.getEmployerAddress());
        }
        if (updatedLoan.getEmail() != null) {
            existingLoan.setEmail(updatedLoan.getEmail());
        }
        if (updatedLoan.getAddress() != null) {
            existingLoan.setAddress(updatedLoan.getAddress());
        }
        if (updatedLoan.getMaritalStatus() != null) {
            existingLoan.setMaritalStatus(updatedLoan.getMaritalStatus());
        }
        if (updatedLoan.getContactNumber() != null) {
            existingLoan.setContactNumber(updatedLoan.getContactNumber());
        }
        if (updatedLoan.getLoanAmount() != null) {
            existingLoan.setLoanAmount(updatedLoan.getLoanAmount());
        }
        if (updatedLoan.getDurationMonths() != null) {
            existingLoan.setDurationMonths(updatedLoan.getDurationMonths());
        }
        
        Loan savedLoan = loanRepository.save(existingLoan);
        logger.info("Loan updated successfully: {}", savedLoan.getLoanId());
        
        return savedLoan;
    }
    
    public void deleteLoan(String loanId) {
        logger.info("Deleting loan: {}", loanId);
        
        Optional<Loan> loanOpt = loanRepository.findByLoanId(loanId);
        if (loanOpt.isEmpty()) {
            logger.error("Loan not found: {}", loanId);
            throw new RuntimeException("Loan not found with ID: " + loanId);
        }
        
        loanRepository.delete(loanOpt.get());
        logger.info("Loan deleted successfully: {}", loanId);
    }
    
    public List<Loan> getAllLoans() {
        logger.info("Fetching all loans");
        return loanRepository.findAll();
    }
    
    public long getLoanCount() {
        logger.info("Fetching loan count");
        return loanRepository.countAllLoans();
    }
}