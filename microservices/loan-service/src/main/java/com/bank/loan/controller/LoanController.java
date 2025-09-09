package com.bank.loan.controller;

import com.bank.loan.model.Loan;
import com.bank.loan.service.LoanService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/loans")
@CrossOrigin(origins = "http://localhost:4200")
public class LoanController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);
    
    @Autowired
    private LoanService loanService;
    
    @PostMapping("/customer/{customerSsnId}")
    public ResponseEntity<Loan> createLoan(@PathVariable String customerSsnId, 
                                          @Valid @RequestBody Loan loan) {
        try {
            logger.info("Creating loan for customer: {}", customerSsnId);
            Loan createdLoan = loanService.createLoan(customerSsnId, loan);
            return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            logger.error("Failed to create loan for customer {}: {}", customerSsnId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error creating loan for customer {}: {}", customerSsnId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{loanId}")
    public ResponseEntity<Loan> getLoan(@PathVariable String loanId) {
        try {
            logger.info("Fetching loan: {}", loanId);
            Optional<Loan> loan = loanService.getLoanById(loanId);
            
            if (loan.isPresent()) {
                return new ResponseEntity<>(loan.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error fetching loan {}: {}", loanId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/customer/{customerSsnId}")
    public ResponseEntity<List<Loan>> getLoansByCustomer(@PathVariable String customerSsnId) {
        try {
            logger.info("Fetching loans for customer: {}", customerSsnId);
            List<Loan> loans = loanService.getLoansByCustomer(customerSsnId);
            return new ResponseEntity<>(loans, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Failed to fetch loans for customer {}: {}", customerSsnId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error fetching loans for customer {}: {}", customerSsnId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/{loanId}")
    public ResponseEntity<Loan> updateLoan(@PathVariable String loanId, 
                                          @Valid @RequestBody Loan loan) {
        try {
            logger.info("Updating loan: {}", loanId);
            Loan updatedLoan = loanService.updateLoan(loanId, loan);
            return new ResponseEntity<>(updatedLoan, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Failed to update loan {}: {}", loanId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error updating loan {}: {}", loanId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/{loanId}")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    public ResponseEntity<Map<String, String>> deleteLoan(@PathVariable String loanId) {
        try {
            logger.info("Deleting loan: {}", loanId);
            loanService.deleteLoan(loanId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Loan deleted successfully");
            response.put("loanId", loanId);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Failed to delete loan {}: {}", loanId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Unexpected error deleting loan {}: {}", loanId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    public ResponseEntity<List<Loan>> getAllLoans() {
        try {
            logger.info("Fetching all loans");
            List<Loan> loans = loanService.getAllLoans();
            return new ResponseEntity<>(loans, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching all loans: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/count")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER')")
    public ResponseEntity<Map<String, Long>> getLoanCount() {
        try {
            logger.info("Fetching loan count");
            long count = loanService.getLoanCount();
            
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching loan count: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}