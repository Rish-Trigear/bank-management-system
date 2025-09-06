package com.bank.management.dto;

/**
 * Data Transfer Object for dashboard response data
 * 
 * This DTO contains summary statistics for the bank management dashboard:
 * - totalCustomers: Count of registered customers in the system
 * - totalEmployees: Count of bank employees
 * - totalTransactions: Count of all financial transactions
 * - totalLoans: Count of loan applications
 * 
 * Used by DashboardController to provide quick overview metrics
 * for employee/admin dashboard displays.
 */
public class DashboardResponse {
    private long totalCustomers; // Number of registered customers
    private long totalEmployees; // Number of bank employees
    private long totalTransactions; // Number of financial transactions
    private long totalLoans; // Number of loan applications
    
    public DashboardResponse(long totalCustomers, long totalEmployees, long totalTransactions, long totalLoans) {
        this.totalCustomers = totalCustomers;
        this.totalEmployees = totalEmployees;
        this.totalTransactions = totalTransactions;
        this.totalLoans = totalLoans;
    }
    
    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
    
    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }
    
    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
    
    public long getTotalLoans() { return totalLoans; }
    public void setTotalLoans(long totalLoans) { this.totalLoans = totalLoans; }
}