package com.bank.transaction.dto;

public class CustomerDto {
    private String ssnId;
    private String name;
    private String email;
    private String accountNumber;
    
    public CustomerDto() {}
    
    public String getSsnId() {
        return ssnId;
    }
    
    public void setSsnId(String ssnId) {
        this.ssnId = ssnId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}