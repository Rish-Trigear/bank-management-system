package com.bank.management.controller;

import com.bank.management.model.Loan;
import com.bank.management.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Loan operations
 * 
 * This controller provides HTTP endpoints for loan management:
 * - POST /api/loans/customer/{customerSsnId} - Create new loan application
 * - GET /api/loans - Get all loan applications
 * - GET /api/loans/{loanId} - Get specific loan application
 * - GET /api/loans/customer/{customerSsnId} - Get customer's loan applications
 * - PUT /api/loans/{loanId} - Update loan (for employee processing/approval)
 * - DELETE /api/loans/{loanId} - Delete loan application
 * 
 * Handles loan applications with proper customer association and employee processing.
 */
@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loan Management", description = "Loan CRUD operations")
public class LoanController {
    
    @Autowired
    private LoanService loanService;
    
    @PostMapping("/customer/{customerSsnId}")
    @Operation(summary = "Create a new loan application for a customer")
    public ResponseEntity<Loan> createLoan(
            @PathVariable String customerSsnId, 
            @RequestBody Loan loan) {
        Loan createdLoan = loanService.createLoan(loan, customerSsnId);
        return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
    }
    
    @GetMapping
    @Operation(summary = "Get all loan applications")
    public ResponseEntity<List<Loan>> getAllLoans() {
        List<Loan> loans = loanService.getAllLoans();
        return ResponseEntity.ok(loans);
    }
    
    @GetMapping("/{loanId}")
    @Operation(summary = "Get loan by Loan ID")
    public ResponseEntity<Loan> getLoanById(@PathVariable String loanId) {
        Loan loan = loanService.getLoanById(loanId);
        return ResponseEntity.ok(loan);
    }
    
    @GetMapping("/customer/{customerSsnId}")
    @Operation(summary = "Get all loan applications for a specific customer")
    public ResponseEntity<List<Loan>> getLoansByCustomer(@PathVariable String customerSsnId) {
        List<Loan> loans = loanService.getLoansByCustomer(customerSsnId);
        return ResponseEntity.ok(loans);
    }
    
    @PutMapping("/{loanId}")
    @Operation(summary = "Update loan application (for processing by employees)")
    public ResponseEntity<Loan> updateLoan(
            @PathVariable String loanId, 
            @RequestBody Loan loan) {
        Loan updatedLoan = loanService.updateLoan(loanId, loan);
        return ResponseEntity.ok(updatedLoan);
    }
    
    @DeleteMapping("/{loanId}")
    @Operation(summary = "Delete loan application")
    public ResponseEntity<String> deleteLoan(@PathVariable String loanId) {
        loanService.deleteLoan(loanId);
        return ResponseEntity.ok("Loan application deleted successfully");
    }
}