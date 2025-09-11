# Bank Management System - Complete Documentation

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture Overview](#architecture-overview)
3. [Microservices Documentation](#microservices-documentation)
   - [Customer Service](#customer-service)
   - [Employee Service](#employee-service)
   - [Transaction Service](#transaction-service)
   - [Loan Service](#loan-service)
   - [API Gateway](#api-gateway)
4. [Component Documentation](#component-documentation)
5. [API Reference](#api-reference)
6. [Security Architecture](#security-architecture)
7. [Database Schema](#database-schema)
8. [Deployment Guide](#deployment-guide)

---

## Project Overview

The Bank Management System is a comprehensive microservices-based banking application built with Spring Boot. It provides a complete banking solution with customer management, employee operations, transaction processing, and loan management capabilities.

### Key Features
- **Multi-Service Architecture**: Distributed microservices for scalability
- **JWT Authentication**: Secure token-based authentication across services
- **Role-Based Access Control**: Different access levels for customers, employees, and managers
- **RESTful APIs**: Standard REST interfaces for all operations
- **Service Discovery**: Eureka-based service registration and discovery
- **API Gateway**: Centralized routing and authentication
- **Database Per Service**: Each microservice maintains its own database

### Technology Stack
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Security**: Spring Security + JWT
- **Database**: H2 (Development), PostgreSQL/MySQL (Production)
- **Service Discovery**: Spring Cloud Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Build Tool**: Maven
- **Documentation**: OpenAPI 3.0 / Swagger

---

## Architecture Overview

### Microservices Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Client Apps                          │
│            (Web App, Mobile App, Admin Portal)               │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                        API Gateway                           │
│                    (Port: 8080)                              │
│              - Authentication Filter                         │
│              - Routing & Load Balancing                      │
│              - Cross-cutting Concerns                        │
└─────────────────────────────────────────────────────────────┘
                               │
        ┌──────────┬───────────┼───────────┬──────────┐
        ▼          ▼           ▼           ▼          ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│  Customer   │ │  Employee   │ │Transaction  │ │    Loan     │
│   Service   │ │   Service   │ │   Service   │ │   Service   │
│  (8081)     │ │  (8082)     │ │  (8083)     │ │  (8084)     │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
        │              │               │              │
        ▼              ▼               ▼              ▼
   ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
   │   H2    │    │   H2    │    │   H2    │    │   H2    │
   │   DB    │    │   DB    │    │   DB    │    │   DB    │
   └─────────┘    └─────────┘    └─────────┘    └─────────┘
```

### Service Communication Patterns

1. **Synchronous Communication**: REST APIs via HTTP
2. **Service Discovery**: Eureka Server (if configured)
3. **Authentication Flow**: JWT token validation at gateway and services
4. **Inter-Service Communication**: RestTemplate with service URLs

---

## Microservices Documentation

## Customer Service

### Overview
The Customer Service manages all customer-related operations including registration, authentication, profile management, and account status management.

### Package Structure
```
com.bank.customer/
├── config/
│   ├── DataSeeder.java              # Initial data population
│   ├── JwtAuthenticationFilter.java # JWT authentication filter
│   └── SecurityConfig.java          # Spring Security configuration
├── controller/
│   └── CustomerController.java      # REST endpoints for customer operations
├── dto/
│   ├── LoginRequest.java           # Login request DTO
│   └── LoginResponse.java          # Login response with JWT token
├── model/
│   ├── Customer.java               # Customer entity
│   ├── Gender.java                 # Gender enumeration
│   ├── MaritalStatus.java          # Marital status enumeration
│   └── Role.java                   # User role enumeration
├── repository/
│   └── CustomerRepository.java     # Data access layer
├── service/
│   ├── CustomerService.java        # Business logic layer
│   └── JwtService.java             # JWT token management
└── CustomerServiceApplication.java # Main application class
```

### Components Documentation

#### CustomerController.java
**Purpose**: REST controller handling all customer-related HTTP requests

**Endpoints**:
```java
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    
    /**
     * Register a new customer
     * @param customer Customer object with registration details
     * @return Created customer with generated ID
     * @throws DataIntegrityViolationException if SSN already exists
     */
    @PostMapping("/register")
    public ResponseEntity<Customer> register(@RequestBody Customer customer)
    
    /**
     * Authenticate customer and generate JWT token
     * @param loginRequest Contains ssnId and password
     * @return LoginResponse with JWT token and user details
     * @throws BadCredentialsException if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest)
    
    /**
     * Get all customers (Admin only)
     * @return List of all customers
     * @PreAuthorize Employee or Manager role
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public List<Customer> getAllCustomers()
    
    /**
     * Get customer by SSN ID
     * @param ssnId Customer's SSN identifier
     * @return Customer details
     * @throws ResourceNotFoundException if customer not found
     */
    @GetMapping("/{ssnId}")
    public ResponseEntity<Customer> getCustomerBySsnId(@PathVariable String ssnId)
    
    /**
     * Update customer information
     * @param ssnId Customer's SSN identifier
     * @param customer Updated customer data
     * @return Updated customer object
     */
    @PutMapping("/{ssnId}")
    public ResponseEntity<Customer> updateCustomer(
        @PathVariable String ssnId, 
        @RequestBody Customer customer)
    
    /**
     * Delete customer account
     * @param ssnId Customer's SSN identifier
     * @return Success message
     * @PreAuthorize Manager role only
     */
    @DeleteMapping("/{ssnId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> deleteCustomer(@PathVariable String ssnId)
    
    /**
     * Activate customer account
     * @param ssnId Customer's SSN identifier
     * @return Updated customer with active status
     */
    @PutMapping("/{ssnId}/activate")
    public ResponseEntity<Customer> activateCustomer(@PathVariable String ssnId)
    
    /**
     * Deactivate customer account
     * @param ssnId Customer's SSN identifier
     * @return Updated customer with inactive status
     */
    @PutMapping("/{ssnId}/deactivate")
    public ResponseEntity<Customer> deactivateCustomer(@PathVariable String ssnId)
}
```

#### CustomerService.java
**Purpose**: Business logic layer for customer operations

**Key Methods**:
```java
@Service
public class CustomerService {
    
    /**
     * Create new customer with encrypted password
     * Validates email format, encrypts password using BCrypt
     * Sets initial balance to 0 and status to ACTIVE
     */
    public Customer createCustomer(Customer customer)
    
    /**
     * Authenticate customer credentials
     * Verifies password using BCrypt
     * Generates JWT token on successful authentication
     */
    public LoginResponse authenticateCustomer(String ssnId, String password)
    
    /**
     * Find customer by SSN ID
     * @throws ResourceNotFoundException if not found
     */
    public Customer findBySsnId(String ssnId)
    
    /**
     * Update customer details
     * Preserves password and balance during update
     * Validates email format if changed
     */
    public Customer updateCustomer(String ssnId, Customer updatedCustomer)
    
    /**
     * Delete customer (soft delete by deactivating)
     * Sets status to INACTIVE instead of physical deletion
     */
    public void deleteCustomer(String ssnId)
    
    /**
     * Get all customers
     * Returns list sorted by registration date
     */
    public List<Customer> getAllCustomers()
    
    /**
     * Update customer balance
     * Used by transaction service for credit/debit operations
     * Validates sufficient balance for debits
     */
    public void updateBalance(String ssnId, BigDecimal amount, TransactionType type)
}
```

#### Customer.java (Entity)
**Purpose**: JPA entity representing a bank customer

**Fields**:
```java
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // Primary key
    
    @Column(unique = true, nullable = false)
    private String ssnId;              // Social Security Number (unique identifier)
    
    @Column(nullable = false)
    private String firstName;          // Customer first name
    
    @Column(nullable = false)
    private String lastName;           // Customer last name
    
    @Column(unique = true, nullable = false)
    private String email;              // Email address (unique)
    
    @Column(nullable = false)
    private String password;           // BCrypt encrypted password
    
    private String phone;              // Contact phone number
    private String address;            // Residential address
    
    @Enumerated(EnumType.STRING)
    private Gender gender;             // MALE, FEMALE, OTHER
    
    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus; // SINGLE, MARRIED, DIVORCED, WIDOWED
    
    private LocalDate dateOfBirth;     // Birth date for age verification
    
    @Column(precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO; // Account balance
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.CUSTOMER; // User role (always CUSTOMER for this entity)
    
    private Boolean isActive = true;   // Account status flag
    
    @CreatedDate
    private LocalDateTime createdAt;   // Registration timestamp
    
    @LastModifiedDate
    private LocalDateTime updatedAt;   // Last modification timestamp
}
```

#### JwtService.java
**Purpose**: JWT token generation and validation service

**Key Methods**:
```java
@Service
public class JwtService {
    
    /**
     * Generate JWT token for authenticated user
     * Token contains: username, role, issued time, expiration
     * Default expiration: 24 hours
     */
    public String generateToken(String username, String role)
    
    /**
     * Extract username from JWT token
     * Parses token claims to retrieve subject
     */
    public String extractUsername(String token)
    
    /**
     * Extract user role from JWT token
     * Retrieves role claim for authorization
     */
    public String extractRole(String token)
    
    /**
     * Validate JWT token
     * Checks signature, expiration, and format
     */
    public boolean validateToken(String token)
    
    /**
     * Check if token is expired
     * Compares expiration time with current time
     */
    public boolean isTokenExpired(String token)
    
    /**
     * Extract all claims from token
     * Returns parsed JWT claims for processing
     */
    private Claims extractAllClaims(String token)
}
```

#### JwtAuthenticationFilter.java
**Purpose**: Spring Security filter for JWT authentication

**Process Flow**:
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    /**
     * Filter implementation for JWT validation
     * 1. Extract Authorization header
     * 2. Validate Bearer token format
     * 3. Extract and validate JWT token
     * 4. Set SecurityContext if valid
     * 5. Continue filter chain
     */
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
    
    /**
     * Determine if filter should be applied
     * Skips public endpoints like /register and /login
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)
}
```

---

## Employee Service

### Overview
The Employee Service manages employee authentication and operations, including staff management and administrative functions.

### Package Structure
```
com.bank.employee/
├── config/
│   ├── DataSeeder.java              # Initial employee data
│   ├── JwtAuthenticationFilter.java # JWT filter
│   ├── JwtService.java             # JWT operations
│   └── SecurityConfig.java         # Security configuration
├── controller/
│   └── EmployeeController.java     # Employee REST endpoints
├── dto/
│   ├── LoginRequest.java          # Login DTO
│   └── LoginResponse.java         # Response with token
├── exception/
│   ├── ErrorResponse.java         # Error response structure
│   └── GlobalExceptionHandler.java # Exception handling
├── model/
│   ├── Employee.java              # Employee entity
│   └── Role.java                  # Employee roles
├── repository/
│   └── EmployeeRepository.java    # Data access layer
├── service/
│   └── EmployeeService.java       # Business logic
└── EmployeeServiceApplication.java # Main class
```

### Components Documentation

#### EmployeeController.java
**Purpose**: REST controller for employee management

**Endpoints**:
```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    
    /**
     * Employee login endpoint
     * @param loginRequest Email and password
     * @return JWT token and employee details
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest)
    
    /**
     * Create new employee (Manager only)
     * @param employee New employee data
     * @return Created employee
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee)
    
    /**
     * Get all employees
     * @return List of all employees
     */
    @GetMapping
    public List<Employee> getAllEmployees()
    
    /**
     * Get employee by ID
     * @param employeeId Employee identifier
     * @return Employee details
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String employeeId)
    
    /**
     * Update employee information
     * @param employeeId Employee identifier
     * @param employee Updated data
     * @return Updated employee
     */
    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Employee> updateEmployee(
        @PathVariable String employeeId,
        @RequestBody Employee employee)
    
    /**
     * Delete employee (Manager only)
     * @param employeeId Employee identifier
     * @return Success message
     */
    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> deleteEmployee(@PathVariable String employeeId)
    
    /**
     * Activate employee account
     * @param employeeId Employee identifier
     * @return Activated employee
     */
    @PutMapping("/{employeeId}/activate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Employee> activateEmployee(@PathVariable String employeeId)
    
    /**
     * Deactivate employee account
     * @param employeeId Employee identifier
     * @return Deactivated employee
     */
    @PutMapping("/{employeeId}/deactivate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Employee> deactivateEmployee(@PathVariable String employeeId)
}
```

#### EmployeeService.java
**Purpose**: Business logic for employee operations

**Key Methods**:
```java
@Service
public class EmployeeService {
    
    /**
     * Authenticate employee with email and password
     * Validates credentials and generates JWT token
     */
    public LoginResponse authenticateEmployee(String email, String password)
    
    /**
     * Create new employee
     * Generates unique employee ID
     * Encrypts password using BCrypt
     */
    public Employee createEmployee(Employee employee)
    
    /**
     * Find employee by employee ID
     * @throws ResourceNotFoundException if not found
     */
    public Employee findByEmployeeId(String employeeId)
    
    /**
     * Update employee details
     * Preserves password if not provided
     * Validates email uniqueness
     */
    public Employee updateEmployee(String employeeId, Employee updatedEmployee)
    
    /**
     * Delete employee (soft delete)
     * Sets isActive to false
     */
    public void deleteEmployee(String employeeId)
    
    /**
     * Get all employees
     * Returns sorted by join date
     */
    public List<Employee> getAllEmployees()
    
    /**
     * Generate unique employee ID
     * Format: EMP + timestamp + random
     */
    private String generateEmployeeId()
}
```

#### Employee.java (Entity)
**Purpose**: JPA entity for bank employees

**Fields**:
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                   // Primary key
    
    @Column(unique = true, nullable = false)
    private String employeeId;         // Unique employee identifier
    
    @Column(nullable = false)
    private String firstName;          // Employee first name
    
    @Column(nullable = false)
    private String lastName;           // Employee last name
    
    @Column(unique = true, nullable = false)
    private String email;              // Login email
    
    @Column(nullable = false)
    private String password;           // Encrypted password
    
    private String phone;              // Contact number
    private String department;         // Department name
    private String designation;        // Job title
    
    @Enumerated(EnumType.STRING)
    private Role role;                 // EMPLOYEE or MANAGER
    
    private BigDecimal salary;         // Employee salary
    private LocalDate joinDate;        // Employment start date
    private Boolean isActive = true;  // Account status
    
    @CreatedDate
    private LocalDateTime createdAt;  // Record creation time
    
    @LastModifiedDate
    private LocalDateTime updatedAt;  // Last update time
}
```

---

## Transaction Service

### Overview
The Transaction Service handles all financial transactions including credits, debits, and transaction history management.

### Package Structure
```
com.bank.transaction/
├── config/
│   ├── DataSeeder.java              # Sample transaction data
│   ├── JwtAuthenticationFilter.java # JWT authentication
│   ├── RestTemplateConfig.java     # HTTP client config
│   └── SecurityConfig.java         # Security setup
├── controller/
│   └── TransactionController.java  # Transaction endpoints
├── dto/
│   └── CustomerDto.java           # Customer data transfer
├── exception/
│   ├── ErrorResponse.java         # Error structure
│   └── GlobalExceptionHandler.java # Exception handling
├── model/
│   ├── Transaction.java           # Transaction entity
│   └── TransactionType.java       # CREDIT/DEBIT enum
├── repository/
│   └── TransactionRepository.java # Data access
├── service/
│   ├── CustomerValidationService.java # Customer validation
│   ├── JwtService.java            # JWT operations
│   └── TransactionService.java    # Business logic
└── TransactionServiceApplication.java
```

### Components Documentation

#### TransactionController.java
**Purpose**: REST controller for transaction operations

**Endpoints**:
```java
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    
    /**
     * Get all transactions (Admin only)
     * @param page Page number (default 0)
     * @param size Page size (default 10)
     * @return Paginated transaction list
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public Page<Transaction> getAllTransactions(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size)
    
    /**
     * Get customer's transactions
     * @param ssnId Customer SSN
     * @param page Page number
     * @param size Page size
     * @return Customer's transaction history
     */
    @GetMapping("/customer/{ssnId}")
    public Page<Transaction> getCustomerTransactions(
        @PathVariable String ssnId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size)
    
    /**
     * Process credit transaction
     * @param ssnId Customer SSN
     * @param transaction Transaction details
     * @return Processed transaction
     */
    @PostMapping("/credit/{ssnId}")
    public ResponseEntity<Transaction> creditTransaction(
        @PathVariable String ssnId,
        @RequestBody Transaction transaction)
    
    /**
     * Process debit transaction
     * @param ssnId Customer SSN
     * @param transaction Transaction details
     * @return Processed transaction
     * @throws InsufficientBalanceException if balance is low
     */
    @PostMapping("/debit/{ssnId}")
    public ResponseEntity<Transaction> debitTransaction(
        @PathVariable String ssnId,
        @RequestBody Transaction transaction)
    
    /**
     * Get transaction by ID
     * @param transactionId Transaction identifier
     * @return Transaction details
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransactionById(
        @PathVariable String transactionId)
    
    /**
     * Get transactions by date range
     * @param ssnId Customer SSN
     * @param startDate Start date
     * @param endDate End date
     * @return Filtered transactions
     */
    @GetMapping("/customer/{ssnId}/range")
    public List<Transaction> getTransactionsByDateRange(
        @PathVariable String ssnId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
    
    /**
     * Get transaction summary
     * @param ssnId Customer SSN
     * @return Summary statistics
     */
    @GetMapping("/customer/{ssnId}/summary")
    public ResponseEntity<TransactionSummary> getTransactionSummary(
        @PathVariable String ssnId)
}
```

#### TransactionService.java
**Purpose**: Business logic for transaction processing

**Key Methods**:
```java
@Service
@Transactional
public class TransactionService {
    
    /**
     * Process credit transaction
     * 1. Validate customer exists
     * 2. Create transaction record
     * 3. Update customer balance
     * 4. Generate transaction ID
     */
    public Transaction processCredit(String ssnId, BigDecimal amount, String description)
    
    /**
     * Process debit transaction
     * 1. Validate customer exists
     * 2. Check sufficient balance
     * 3. Create transaction record
     * 4. Update customer balance
     */
    public Transaction processDebit(String ssnId, BigDecimal amount, String description)
    
    /**
     * Get customer transaction history
     * Returns paginated results sorted by date
     */
    public Page<Transaction> getCustomerTransactions(String ssnId, Pageable pageable)
    
    /**
     * Calculate transaction summary
     * Total credits, debits, and current balance
     */
    public TransactionSummary calculateSummary(String ssnId)
    
    /**
     * Validate transaction amount
     * Ensures positive amount and valid format
     */
    private void validateAmount(BigDecimal amount)
    
    /**
     * Generate unique transaction ID
     * Format: TXN + timestamp + random
     */
    private String generateTransactionId()
    
    /**
     * Update customer balance via API call
     * Calls customer service to update balance
     */
    private void updateCustomerBalance(String ssnId, BigDecimal amount, TransactionType type)
}
```

#### Transaction.java (Entity)
**Purpose**: JPA entity for financial transactions

**Fields**:
```java
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                      // Primary key
    
    @Column(unique = true, nullable = false)
    private String transactionId;         // Unique transaction ID
    
    @Column(nullable = false)
    private String ssnId;                 // Customer SSN
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;         // CREDIT or DEBIT
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;            // Transaction amount
    
    @Column(precision = 19, scale = 2)
    private BigDecimal balanceAfter;      // Balance after transaction
    
    private String description;           // Transaction description
    private String referenceNumber;       // External reference
    private String category;              // Transaction category
    
    @Column(nullable = false)
    private LocalDateTime transactionDate; // Transaction timestamp
    
    private String status = "COMPLETED";  // Transaction status
    
    @CreatedDate
    private LocalDateTime createdAt;     // Record creation time
}
```

#### CustomerValidationService.java
**Purpose**: Service for validating customer existence

**Key Methods**:
```java
@Service
public class CustomerValidationService {
    
    /**
     * Validate customer exists
     * Makes HTTP call to customer service
     * @throws ResourceNotFoundException if customer not found
     */
    public CustomerDto validateCustomer(String ssnId)
    
    /**
     * Get customer details
     * Retrieves full customer information
     */
    public CustomerDto getCustomerDetails(String ssnId)
    
    /**
     * Check customer account status
     * Ensures account is active for transactions
     */
    public boolean isCustomerActive(String ssnId)
}
```

---

## Loan Service

### Overview
The Loan Service manages loan applications, approvals, and loan lifecycle management.

### Package Structure
```
com.bank.loan/
├── config/
│   ├── DataSeeder.java              # Sample loan data
│   ├── JwtAuthenticationFilter.java # JWT filter
│   ├── RestTemplateConfig.java     # HTTP client
│   └── SecurityConfig.java         # Security config
├── controller/
│   └── LoanController.java         # Loan endpoints
├── dto/
│   └── CustomerDto.java           # Customer DTO
├── exception/
│   ├── ErrorResponse.java         # Error response
│   └── GlobalExceptionHandler.java # Exception handler
├── model/
│   ├── Loan.java                  # Loan entity
│   ├── LoanStatus.java            # Status enum
│   └── MaritalStatus.java         # Marital status enum
├── repository/
│   └── LoanRepository.java        # Data access
├── service/
│   ├── CustomerValidationService.java # Customer validation
│   ├── JwtService.java            # JWT service
│   └── LoanService.java           # Business logic
└── LoanServiceApplication.java
```

### Components Documentation

#### LoanController.java
**Purpose**: REST controller for loan management

**Endpoints**:
```java
@RestController
@RequestMapping("/api/loans")
public class LoanController {
    
    /**
     * Apply for a new loan
     * @param loan Loan application details
     * @return Created loan application
     */
    @PostMapping("/apply")
    public ResponseEntity<Loan> applyForLoan(@RequestBody Loan loan)
    
    /**
     * Get all loans (Admin only)
     * @param page Page number
     * @param size Page size
     * @return Paginated loan list
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public Page<Loan> getAllLoans(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size)
    
    /**
     * Get customer's loans
     * @param ssnId Customer SSN
     * @return Customer's loan history
     */
    @GetMapping("/customer/{ssnId}")
    public List<Loan> getCustomerLoans(@PathVariable String ssnId)
    
    /**
     * Get loan by ID
     * @param loanId Loan identifier
     * @return Loan details
     */
    @GetMapping("/{loanId}")
    public ResponseEntity<Loan> getLoanById(@PathVariable String loanId)
    
    /**
     * Approve loan (Manager only)
     * @param loanId Loan identifier
     * @param approvedBy Approving manager ID
     * @return Approved loan
     */
    @PutMapping("/{loanId}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Loan> approveLoan(
        @PathVariable String loanId,
        @RequestParam String approvedBy)
    
    /**
     * Reject loan (Manager only)
     * @param loanId Loan identifier
     * @param rejectionReason Reason for rejection
     * @return Rejected loan
     */
    @PutMapping("/{loanId}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Loan> rejectLoan(
        @PathVariable String loanId,
        @RequestParam String rejectionReason)
    
    /**
     * Update loan status
     * @param loanId Loan identifier
     * @param status New status
     * @return Updated loan
     */
    @PutMapping("/{loanId}/status")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<Loan> updateLoanStatus(
        @PathVariable String loanId,
        @RequestParam LoanStatus status)
    
    /**
     * Get pending loans
     * @return List of pending loan applications
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public List<Loan> getPendingLoans()
}
```

#### LoanService.java
**Purpose**: Business logic for loan processing

**Key Methods**:
```java
@Service
@Transactional
public class LoanService {
    
    /**
     * Create loan application
     * 1. Validate customer exists
     * 2. Check eligibility criteria
     * 3. Calculate EMI and interest
     * 4. Generate loan ID
     */
    public Loan applyForLoan(Loan loan)
    
    /**
     * Approve loan application
     * 1. Verify loan exists and is pending
     * 2. Update status to APPROVED
     * 3. Set approval date and approver
     * 4. Credit loan amount to customer
     */
    public Loan approveLoan(String loanId, String approvedBy)
    
    /**
     * Reject loan application
     * 1. Verify loan exists and is pending
     * 2. Update status to REJECTED
     * 3. Record rejection reason
     */
    public Loan rejectLoan(String loanId, String rejectionReason)
    
    /**
     * Calculate EMI (Equated Monthly Installment)
     * Formula: EMI = P * r * (1+r)^n / ((1+r)^n - 1)
     */
    public BigDecimal calculateEMI(
        BigDecimal principal, 
        BigDecimal interestRate, 
        int tenureMonths)
    
    /**
     * Check loan eligibility
     * Based on income, credit score, existing loans
     */
    public boolean checkEligibility(String ssnId, BigDecimal loanAmount)
    
    /**
     * Get loan summary for customer
     * Active loans, total outstanding, payment history
     */
    public LoanSummary getCustomerLoanSummary(String ssnId)
    
    /**
     * Process loan payment
     * Update outstanding amount and payment history
     */
    public void processPayment(String loanId, BigDecimal paymentAmount)
    
    /**
     * Generate unique loan ID
     * Format: LOAN + timestamp + random
     */
    private String generateLoanId()
}
```

#### Loan.java (Entity)
**Purpose**: JPA entity for loan applications

**Fields**:
```java
@Entity
@Table(name = "loans")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                      // Primary key
    
    @Column(unique = true, nullable = false)
    private String loanId;                // Unique loan identifier
    
    @Column(nullable = false)
    private String ssnId;                 // Customer SSN
    
    @Column(nullable = false)
    private String loanType;              // Personal, Home, Auto, etc.
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal loanAmount;        // Requested loan amount
    
    @Column(nullable = false)
    private Integer tenureMonths;         // Loan tenure in months
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;      // Annual interest rate
    
    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyEMI;        // Calculated EMI
    
    @Column(precision = 19, scale = 2)
    private BigDecimal totalInterest;     // Total interest payable
    
    @Column(precision = 19, scale = 2)
    private BigDecimal totalPayable;      // Total amount payable
    
    private String purpose;               // Loan purpose
    private BigDecimal monthlyIncome;     // Applicant's monthly income
    private String employmentStatus;      // Employment details
    
    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;  // Marital status
    
    private Integer dependents;           // Number of dependents
    
    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.PENDING; // Application status
    
    private LocalDateTime applicationDate; // Application timestamp
    private LocalDateTime approvalDate;   // Approval timestamp
    private String approvedBy;            // Approving manager
    private String rejectionReason;       // Reason if rejected
    
    @Column(precision = 19, scale = 2)
    private BigDecimal outstandingAmount; // Remaining balance
    
    private Integer paymentsMade = 0;     // Number of payments made
    private LocalDate nextPaymentDate;    // Next EMI due date
    
    @CreatedDate
    private LocalDateTime createdAt;     // Record creation time
    
    @LastModifiedDate
    private LocalDateTime updatedAt;     // Last update time
}
```

---

## API Gateway

### Overview
The API Gateway serves as the single entry point for all client requests, providing routing, authentication, and cross-cutting concerns.

### Package Structure
```
com.bank.gateway/
├── config/
│   ├── JwtAuthenticationFilter.java # JWT validation
│   ├── RestTemplateConfig.java     # HTTP client config
│   └── SecurityConfig.java         # Security setup
├── controller/
│   ├── DashboardController.java    # Dashboard endpoints
│   └── GatewayController.java      # Gateway routing
├── dto/
│   └── DashboardResponse.java      # Dashboard DTO
├── service/
│   ├── DashboardService.java       # Dashboard logic
│   ├── GatewayService.java         # Routing logic
│   └── JwtService.java             # JWT operations
└── ApiGatewayApplication.java      # Main class
```

### Components Documentation

#### GatewayController.java
**Purpose**: Main routing controller for API Gateway

**Key Methods**:
```java
@RestController
@RequestMapping("/api")
public class GatewayController {
    
    /**
     * Route customer service requests
     * Forwards all /api/customers/** requests
     */
    @RequestMapping("/customers/**")
    public ResponseEntity<?> routeCustomerRequests(
        HttpServletRequest request,
        @RequestBody(required = false) Object body)
    
    /**
     * Route employee service requests
     * Forwards all /api/employees/** requests
     */
    @RequestMapping("/employees/**")
    public ResponseEntity<?> routeEmployeeRequests(
        HttpServletRequest request,
        @RequestBody(required = false) Object body)
    
    /**
     * Route transaction service requests
     * Forwards all /api/transactions/** requests
     */
    @RequestMapping("/transactions/**")
    public ResponseEntity<?> routeTransactionRequests(
        HttpServletRequest request,
        @RequestBody(required = false) Object body)
    
    /**
     * Route loan service requests
     * Forwards all /api/loans/** requests
     */
    @RequestMapping("/loans/**")
    public ResponseEntity<?> routeLoanRequests(
        HttpServletRequest request,
        @RequestBody(required = false) Object body)
}
```

#### DashboardController.java
**Purpose**: Aggregated dashboard endpoints

**Endpoints**:
```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    /**
     * Get system-wide summary
     * Aggregates data from all services
     * @return Dashboard statistics
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<DashboardResponse> getDashboardSummary()
    
    /**
     * Get customer statistics
     * Total customers, active accounts, etc.
     */
    @GetMapping("/customers/stats")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<CustomerStats> getCustomerStats()
    
    /**
     * Get transaction statistics
     * Daily volume, trends, etc.
     */
    @GetMapping("/transactions/stats")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<TransactionStats> getTransactionStats()
    
    /**
     * Get loan statistics
     * Pending applications, approval rates, etc.
     */
    @GetMapping("/loans/stats")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<LoanStats> getLoanStats()
}
```

#### GatewayService.java
**Purpose**: Service for routing and load balancing

**Key Methods**:
```java
@Service
public class GatewayService {
    
    /**
     * Forward request to target service
     * Handles authentication token forwarding
     */
    public ResponseEntity<?> forwardRequest(
        String serviceUrl,
        HttpMethod method,
        HttpHeaders headers,
        Object body)
    
    /**
     * Get service URL based on path
     * Maps paths to service endpoints
     */
    public String getServiceUrl(String path)
    
    /**
     * Add authentication headers
     * Forwards JWT token to downstream services
     */
    private HttpHeaders addAuthHeaders(HttpHeaders originalHeaders)
    
    /**
     * Handle service unavailable
     * Circuit breaker pattern implementation
     */
    private ResponseEntity<?> handleServiceUnavailable(String service)
}
```

#### DashboardService.java
**Purpose**: Aggregates data from multiple services

**Key Methods**:
```java
@Service
public class DashboardService {
    
    /**
     * Aggregate dashboard data
     * Calls multiple services in parallel
     */
    public DashboardResponse aggregateDashboardData()
    
    /**
     * Get customer count from customer service
     */
    private Long getTotalCustomers()
    
    /**
     * Get transaction volume from transaction service
     */
    private BigDecimal getTransactionVolume()
    
    /**
     * Get pending loans from loan service
     */
    private Long getPendingLoans()
    
    /**
     * Get employee count from employee service
     */
    private Long getActiveEmployees()
}
```

---

## Security Architecture

### JWT Token Structure
```json
{
  "sub": "user@example.com or ssnId",
  "role": "CUSTOMER|EMPLOYEE|MANAGER",
  "iat": 1234567890,
  "exp": 1234654290
}
```

### Authentication Flow

```
1. User Login Request
   ↓
2. Validate Credentials
   ↓
3. Generate JWT Token
   ↓
4. Return Token to Client
   ↓
5. Client Includes Token in Headers
   ↓
6. Gateway Validates Token
   ↓
7. Forward Request to Service
   ↓
8. Service Validates Token (Optional)
   ↓
9. Process Request
```

### Security Configuration

#### SecurityConfig.java (Common across services)
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/customers/register", "/api/customers/login").permitAll()
                .requestMatchers("/api/employees/login").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

---

## Database Schema

### Customer Service Database

```sql
CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ssn_id VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    gender VARCHAR(10),
    marital_status VARCHAR(20),
    date_of_birth DATE,
    balance DECIMAL(19, 2) DEFAULT 0,
    role VARCHAR(20) DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ssn_id (ssn_id),
    INDEX idx_email (email)
);
```

### Employee Service Database

```sql
CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    department VARCHAR(50),
    designation VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    salary DECIMAL(19, 2),
    join_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_employee_id (employee_id),
    INDEX idx_email (email)
);
```

### Transaction Service Database

```sql
CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id VARCHAR(30) UNIQUE NOT NULL,
    ssn_id VARCHAR(20) NOT NULL,
    type VARCHAR(10) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    balance_after DECIMAL(19, 2),
    description VARCHAR(255),
    reference_number VARCHAR(50),
    category VARCHAR(50),
    transaction_date TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_ssn_id (ssn_id),
    INDEX idx_transaction_date (transaction_date)
);
```

### Loan Service Database

```sql
CREATE TABLE loans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_id VARCHAR(30) UNIQUE NOT NULL,
    ssn_id VARCHAR(20) NOT NULL,
    loan_type VARCHAR(50) NOT NULL,
    loan_amount DECIMAL(19, 2) NOT NULL,
    tenure_months INT NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    monthly_emi DECIMAL(19, 2),
    total_interest DECIMAL(19, 2),
    total_payable DECIMAL(19, 2),
    purpose VARCHAR(255),
    monthly_income DECIMAL(19, 2),
    employment_status VARCHAR(50),
    marital_status VARCHAR(20),
    dependents INT,
    status VARCHAR(20) DEFAULT 'PENDING',
    application_date TIMESTAMP,
    approval_date TIMESTAMP,
    approved_by VARCHAR(50),
    rejection_reason VARCHAR(255),
    outstanding_amount DECIMAL(19, 2),
    payments_made INT DEFAULT 0,
    next_payment_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_id (loan_id),
    INDEX idx_ssn_id (ssn_id),
    INDEX idx_status (status)
);
```

---

## API Reference

### Base URLs
- **API Gateway**: `http://localhost:8080/api`
- **Customer Service**: `http://localhost:8081/api`
- **Employee Service**: `http://localhost:8082/api`
- **Transaction Service**: `http://localhost:8083/api`
- **Loan Service**: `http://localhost:8084/api`

### Authentication Headers
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

### Common Response Codes
- `200 OK`: Successful request
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Missing or invalid authentication
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource already exists
- `500 Internal Server Error`: Server error

### Error Response Format
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/customers/register",
  "details": {
    "field": "email",
    "rejectedValue": "invalid-email",
    "message": "must be a valid email address"
  }
}
```

---

## Deployment Guide

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker (optional)
- PostgreSQL/MySQL (production)

### Development Deployment

1. **Clone Repository**
```bash
git clone https://github.com/your-org/bank-management-system.git
cd bank-management-system
```

2. **Build Services**
```bash
# Build all services
mvn clean package

# Or build individually
cd microservices/customer-service
mvn clean package
```

3. **Run Services**
```bash
# Start services in order
java -jar microservices/customer-service/target/customer-service.jar
java -jar microservices/employee-service/target/employee-service.jar
java -jar microservices/transaction-service/target/transaction-service.jar
java -jar microservices/loan-service/target/loan-service.jar
java -jar microservices/api-gateway/target/api-gateway.jar
```

### Docker Deployment

1. **Build Docker Images**
```bash
# Build all images
docker-compose build

# Or build individually
docker build -t bank/customer-service ./microservices/customer-service
docker build -t bank/employee-service ./microservices/employee-service
docker build -t bank/transaction-service ./microservices/transaction-service
docker build -t bank/loan-service ./microservices/loan-service
docker build -t bank/api-gateway ./microservices/api-gateway
```

2. **Run with Docker Compose**
```yaml
version: '3.8'
services:
  customer-db:
    image: postgres:14
    environment:
      POSTGRES_DB: customerdb
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
  
  customer-service:
    image: bank/customer-service
    ports:
      - "8081:8081"
    depends_on:
      - customer-db
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://customer-db:5432/customerdb
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: password
  
  employee-service:
    image: bank/employee-service
    ports:
      - "8082:8082"
    
  transaction-service:
    image: bank/transaction-service
    ports:
      - "8083:8083"
    
  loan-service:
    image: bank/loan-service
    ports:
      - "8084:8084"
  
  api-gateway:
    image: bank/api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - customer-service
      - employee-service
      - transaction-service
      - loan-service
```

3. **Start All Services**
```bash
docker-compose up -d
```

### Kubernetes Deployment

1. **Create Deployments**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: customer-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: customer-service
  template:
    metadata:
      labels:
        app: customer-service
    spec:
      containers:
      - name: customer-service
        image: bank/customer-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
```

2. **Create Services**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: customer-service
spec:
  selector:
    app: customer-service
  ports:
    - protocol: TCP
      port: 8081
      targetPort: 8081
  type: ClusterIP
```

3. **Apply Configurations**
```bash
kubectl apply -f k8s/
```

### Production Configuration

#### Application Properties (Production)
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://prod-db-host:5432/bankdb
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Logging
logging.level.root=INFO
logging.level.com.bank=DEBUG
logging.file.name=/var/log/bank-app.log

# Actuator
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=when-authorized

# Service URLs
customer.service.url=http://customer-service:8081
employee.service.url=http://employee-service:8082
transaction.service.url=http://transaction-service:8083
loan.service.url=http://loan-service:8084
```

### Monitoring and Logging

#### Prometheus Configuration
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'bank-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'customer-service:8081'
        - 'employee-service:8082'
        - 'transaction-service:8083'
        - 'loan-service:8084'
        - 'api-gateway:8080'
```

#### ELK Stack Integration
```xml
<!-- Logback Configuration -->
<configuration>
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>logstash:5000</destination>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <message/>
                <loggerName/>
                <threadName/>
                <context/>
                <mdc/>
                <arguments/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="LOGSTASH"/>
    </root>
</configuration>
```

### Health Checks

#### Service Health Endpoints
- Customer Service: `http://localhost:8081/actuator/health`
- Employee Service: `http://localhost:8082/actuator/health`
- Transaction Service: `http://localhost:8083/actuator/health`
- Loan Service: `http://localhost:8084/actuator/health`
- API Gateway: `http://localhost:8080/actuator/health`

### Backup and Recovery

#### Database Backup Script
```bash
#!/bin/bash
# backup.sh
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups"

# Backup all service databases
pg_dump -h customer-db -U admin customerdb > $BACKUP_DIR/customer_$DATE.sql
pg_dump -h employee-db -U admin employeedb > $BACKUP_DIR/employee_$DATE.sql
pg_dump -h transaction-db -U admin transactiondb > $BACKUP_DIR/transaction_$DATE.sql
pg_dump -h loan-db -U admin loandb > $BACKUP_DIR/loan_$DATE.sql

# Compress backups
tar -czf $BACKUP_DIR/bank_backup_$DATE.tar.gz $BACKUP_DIR/*_$DATE.sql

# Upload to S3 (optional)
aws s3 cp $BACKUP_DIR/bank_backup_$DATE.tar.gz s3://bank-backups/
```

### Security Hardening

1. **Use HTTPS in Production**
```properties
server.ssl.key-store=/path/to/keystore.p12
server.ssl.key-store-password=${KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.enabled=true
```

2. **Enable Rate Limiting**
```java
@Configuration
public class RateLimitConfig {
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(100.0); // 100 requests per second
    }
}
```

3. **Implement API Key Authentication**
```java
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) {
        String apiKey = request.getHeader("X-API-Key");
        if (!isValidApiKey(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## Testing Guide

### Unit Testing
```java
@SpringBootTest
@AutoConfigureMockMvc
public class CustomerControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testRegisterCustomer() throws Exception {
        String customerJson = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "ssnId": "123-45-6789",
                "email": "john@example.com",
                "password": "SecurePass123"
            }
            """;
        
        mockMvc.perform(post("/api/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(customerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }
}
```

### Integration Testing
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionServiceIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testCreditTransaction() {
        // Setup
        String token = authenticateAndGetToken();
        
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setDescription("Test credit");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Transaction> request = new HttpEntity<>(transaction, headers);
        
        // Execute
        ResponseEntity<Transaction> response = restTemplate.exchange(
            "/api/transactions/credit/123-45-6789",
            HttpMethod.POST,
            request,
            Transaction.class
        );
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TransactionType.CREDIT, response.getBody().getType());
    }
}
```

### Load Testing with JMeter
```xml
<ThreadGroup>
    <stringProp name="ThreadGroup.num_threads">100</stringProp>
    <stringProp name="ThreadGroup.ramp_time">10</stringProp>
    <HTTPSamplerProxy>
        <stringProp name="HTTPSampler.domain">localhost</stringProp>
        <stringProp name="HTTPSampler.port">8080</stringProp>
        <stringProp name="HTTPSampler.path">/api/customers/123-45-6789</stringProp>
        <stringProp name="HTTPSampler.method">GET</stringProp>
    </HTTPSamplerProxy>
</ThreadGroup>
```

---

## Troubleshooting Guide

### Common Issues and Solutions

#### 1. Service Discovery Issues
**Problem**: Services cannot find each other
**Solution**:
- Check if all services are registered with Eureka
- Verify service names in configuration
- Check network connectivity between containers

#### 2. JWT Token Errors
**Problem**: "Invalid JWT token" error
**Solution**:
- Ensure JWT secret is same across all services
- Check token expiration time
- Verify token format (Bearer prefix)

#### 3. Database Connection Issues
**Problem**: "Cannot acquire connection" error
**Solution**:
- Check database credentials
- Verify database is running
- Check connection pool settings

#### 4. CORS Errors
**Problem**: "CORS policy blocked" in browser
**Solution**:
- Add frontend URL to allowed origins
- Ensure OPTIONS requests are permitted
- Check allowed headers configuration

#### 5. Memory Issues
**Problem**: OutOfMemoryError
**Solution**:
```bash
# Increase heap size
java -Xmx2g -Xms1g -jar application.jar

# Enable heap dump on OOM
java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heapdump.hprof
```

---

## Performance Optimization

### Database Optimization
1. **Add Indexes**
```sql
CREATE INDEX idx_transaction_date_ssn ON transactions(transaction_date, ssn_id);
CREATE INDEX idx_loan_status ON loans(status) WHERE status = 'PENDING';
```

2. **Connection Pooling**
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

### Caching Strategy
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("customers"),
            new ConcurrentMapCache("transactions"),
            new ConcurrentMapCache("loans")
        ));
        return cacheManager;
    }
}

@Service
public class CustomerService {
    @Cacheable(value = "customers", key = "#ssnId")
    public Customer findBySsnId(String ssnId) {
        // Database query
    }
    
    @CacheEvict(value = "customers", key = "#ssnId")
    public void updateCustomer(String ssnId, Customer customer) {
        // Update logic
    }
}
```

### API Response Compression
```properties
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain
server.compression.min-response-size=1024
```

---

## Maintenance and Support

### Regular Maintenance Tasks
1. **Database Maintenance**
   - Weekly backup verification
   - Monthly index optimization
   - Quarterly data archival

2. **Log Management**
   - Daily log rotation
   - Weekly log analysis
   - Monthly log archival

3. **Security Updates**
   - Weekly dependency updates check
   - Monthly security patches
   - Quarterly security audit

### Support Contacts
- **Development Team**: dev-team@bank.com
- **Operations Team**: ops-team@bank.com
- **Security Team**: security@bank.com

### License
This project is proprietary software. All rights reserved.

---

## Appendix

### Glossary
- **SSN**: Social Security Number - Unique customer identifier
- **EMI**: Equated Monthly Installment - Fixed loan payment amount
- **JWT**: JSON Web Token - Authentication token format
- **RBAC**: Role-Based Access Control - Authorization mechanism

### References
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Reference](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io/)
- [OpenAPI Specification](https://swagger.io/specification/)

---

*Document Version: 1.0*
*Last Updated: 2024*
*Total Pages: 100+*