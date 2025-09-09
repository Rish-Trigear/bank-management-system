package com.bank.gateway.dto;

public class DashboardResponse {
    private long totalCustomers;
    private long totalEmployees;
    private long totalLoanRequests;
    private double totalBankBalance;
    
    public DashboardResponse() {}
    
    public DashboardResponse(long totalCustomers, long totalEmployees, 
                           long totalLoanRequests, double totalBankBalance) {
        this.totalCustomers = totalCustomers;
        this.totalEmployees = totalEmployees;
        this.totalLoanRequests = totalLoanRequests;
        this.totalBankBalance = totalBankBalance;
    }
    
    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
    
    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }
    
    public long getTotalLoanRequests() { return totalLoanRequests; }
    public void setTotalLoanRequests(long totalLoanRequests) { this.totalLoanRequests = totalLoanRequests; }
    
    public double getTotalBankBalance() { return totalBankBalance; }
    public void setTotalBankBalance(double totalBankBalance) { this.totalBankBalance = totalBankBalance; }
}