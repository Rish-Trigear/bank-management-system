package com.bank.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Loan entity representing customer loan applications
 * 
 * This entity stores loan application information including:
 * - Personal details (address, contact, marital status)
 * - Employment information (occupation, employer details)
 * - Loan specifics (amount, duration in months)
 * - Customer association (many-to-one relationship)
 * - Validation constraints (email format, positive amounts, phone pattern)
 * 
 * Each loan has a unique loan ID and is linked to a customer.
 * Uses BigDecimal for precise financial calculations.
 */
@Entity
@Table(name = "loans")
public class Loan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary key, auto-generated
    
    @Column(unique = true)
    private String loanId; // Unique loan application identifier
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer; // Associated customer who applied for loan
    
    private String occupation; // Applicant's job/profession
    
    private String employerName; // Name of employer company
    
    private String employerAddress; // Employer's business address
    
    @Email
    private String email; // Contact email for loan communication
    
    private String address; // Applicant's residential address
    
    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus; // Marital status for loan assessment
    
    @Pattern(regexp = "^[0-9]{10}$")
    private String contactNumber; // Phone number (10 digits validation)
    
    @Positive
    @Column(precision = 15, scale = 2)
    private BigDecimal loanAmount; // Requested loan amount (must be positive)
    
    @Positive
    private Integer durationMonths; // Loan term in months (must be positive)
    
    public Loan() {}
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getLoanId() {
        return loanId;
    }
    
    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public String getOccupation() {
        return occupation;
    }
    
    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
    
    public String getEmployerName() {
        return employerName;
    }
    
    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }
    
    public String getEmployerAddress() {
        return employerAddress;
    }
    
    public void setEmployerAddress(String employerAddress) {
        this.employerAddress = employerAddress;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }
    
    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public BigDecimal getLoanAmount() {
        return loanAmount;
    }
    
    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }
    
    public Integer getDurationMonths() {
        return durationMonths;
    }
    
    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }
}