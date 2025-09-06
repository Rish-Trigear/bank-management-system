package com.bank.management.controller;

import com.bank.management.dto.DashboardResponse;
import com.bank.management.service.CustomerService;
import com.bank.management.service.EmployeeService;
import com.bank.management.service.TransactionService;
import com.bank.management.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Dashboard operations
 * 
 * This controller provides summary statistics for the bank management dashboard:
 * - GET /api/dashboard/summary - Returns count of customers, employees, transactions, and loans
 * 
 * Designed for employee/admin use to get quick overview of system data.
 * Aggregates data from multiple services to provide comprehensive dashboard metrics.
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Dashboard summary data for employees")
public class DashboardController {
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private LoanService loanService;
    
    /**
     * Get dashboard summary statistics
     * 
     * Aggregates counts from all major entities:
     * - Total customers registered
     * - Total employees in system
     * - Total transactions processed
     * - Total loans issued
     * 
     * Returns consolidated metrics for dashboard display
     */
    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary data")
    public ResponseEntity<DashboardResponse> getDashboardSummary() {
        long totalCustomers = customerService.getAllCustomers().size();
        long totalEmployees = employeeService.getAllEmployees().size();
        long totalTransactions = transactionService.getAllTransactions().size();
        long totalLoans = loanService.getAllLoans().size();
        
        DashboardResponse response = new DashboardResponse(
            totalCustomers, totalEmployees, totalTransactions, totalLoans);
        
        return ResponseEntity.ok(response);
    }
}