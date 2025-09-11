# Customer Service - Detailed Component Documentation

## Table of Contents
1. [CustomerController.java](#customercontrollerjava)
2. [CustomerService.java](#customerservicejava)
3. [Customer.java (Entity)](#customerjava-entity)
4. [CustomerRepository.java](#customerrepositoryjava)
5. [JwtService.java](#jwtservicejava)
6. [JwtAuthenticationFilter.java](#jwtauthenticationfilterjava)
7. [SecurityConfig.java](#securityconfigjava)
8. [DataSeeder.java](#dataseederjava)
9. [DTOs](#dtos)
10. [Enums](#enums)

---

## CustomerController.java

**File Location**: `com/bank/customer/controller/CustomerController.java`

**Purpose**: REST controller that exposes HTTP endpoints for customer-related operations.

### Class Definition
```java
@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:4200")
public class CustomerController
```

### Dependencies
- `CustomerService`: Business logic layer
- `JwtService`: JWT token generation
- `PasswordEncoder`: Password encryption

### Detailed Method Documentation

#### 1. registerCustomer()
```java
@PostMapping("/register")
public ResponseEntity<Customer> registerCustomer(@RequestBody Customer customer)
```
**Purpose**: Register a new customer account
**Process Flow**:
1. Receives customer registration data
2. Validates required fields (firstName, lastName, ssnId, email, password)
3. Checks if SSN or email already exists
4. Encrypts password using BCrypt
5. Sets initial balance to 0.00
6. Sets account status to ACTIVE
7. Assigns CUSTOMER role
8. Saves to database
9. Returns created customer (without password)

**Validation Rules**:
- SSN must be unique
- Email must be unique and valid format
- Password minimum 8 characters
- All required fields must be present

**Error Handling**:
- `409 Conflict`: If SSN or email already exists
- `400 Bad Request`: If validation fails

#### 2. loginCustomer()
```java
@PostMapping("/login")
public ResponseEntity<LoginResponse> loginCustomer(@RequestBody LoginRequest loginRequest)
```
**Purpose**: Authenticate customer and generate JWT token
**Process Flow**:
1. Extract ssnId and password from request
2. Retrieve customer by ssnId
3. Verify password using BCrypt comparison
4. Check if account is active
5. Generate JWT token with 24-hour expiration
6. Return token with customer details

**Response Structure**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "username": "123-45-6789",
  "role": "CUSTOMER",
  "customerId": 1,
  "firstName": "John",
  "lastName": "Doe"
}
```

**Error Handling**:
- `401 Unauthorized`: Invalid credentials
- `403 Forbidden`: Account deactivated

#### 3. getAllCustomers()
```java
@GetMapping
@PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
public ResponseEntity<List<Customer>> getAllCustomers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(required = false) String sortBy,
    @RequestParam(required = false) String filterBy
)
```
**Purpose**: Retrieve all customers with pagination and filtering
**Authorization**: Employee or Manager role required
**Features**:
- Pagination support
- Sorting by any field
- Filtering by status, balance range, registration date
- Password field excluded from response

**Query Parameters**:
- `page`: Page number (0-indexed)
- `size`: Items per page
- `sortBy`: Field to sort by (e.g., "lastName", "balance")
- `filterBy`: Filter criteria (e.g., "active", "balance>1000")

#### 4. getCustomerBySsnId()
```java
@GetMapping("/{ssnId}")
@PreAuthorize("hasRole('CUSTOMER') and #ssnId == authentication.principal or hasAnyRole('EMPLOYEE', 'MANAGER')")
public ResponseEntity<Customer> getCustomerBySsnId(@PathVariable String ssnId)
```
**Purpose**: Retrieve specific customer details
**Authorization**: 
- Customer can only access their own data
- Employees and Managers can access any customer

**Process Flow**:
1. Validate SSN format
2. Query database for customer
3. Check authorization
4. Return customer data (password excluded)

**Error Handling**:
- `404 Not Found`: Customer doesn't exist
- `403 Forbidden`: Unauthorized access attempt

#### 5. updateCustomer()
```java
@PutMapping("/{ssnId}")
@PreAuthorize("hasRole('CUSTOMER') and #ssnId == authentication.principal or hasRole('MANAGER')")
public ResponseEntity<Customer> updateCustomer(
    @PathVariable String ssnId,
    @RequestBody Customer customerDetails
)
```
**Purpose**: Update customer information
**Updatable Fields**:
- Personal info: firstName, lastName, phone, address
- Contact: email (must be unique)
- Demographics: gender, maritalStatus, dateOfBirth
- Password (requires current password verification)

**Non-Updatable Fields**:
- ssnId (immutable identifier)
- balance (only via transactions)
- role (system-assigned)
- createdAt (audit field)

**Process Flow**:
1. Retrieve existing customer
2. Validate authorization
3. Check email uniqueness if changed
4. Apply updates (null fields ignored)
5. Update timestamp
6. Save and return updated customer

#### 6. deleteCustomer()
```java
@DeleteMapping("/{ssnId}")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<Map<String, String>> deleteCustomer(@PathVariable String ssnId)
```
**Purpose**: Soft delete customer account
**Authorization**: Manager only
**Process**:
1. Set isActive to false
2. Preserve all data for audit
3. Customer cannot login after deletion
4. Can be reactivated if needed

**Response**:
```json
{
  "message": "Customer account deactivated successfully",
  "ssnId": "123-45-6789",
  "timestamp": "2024-01-01T10:00:00"
}
```

#### 7. activateCustomer()
```java
@PutMapping("/{ssnId}/activate")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<Customer> activateCustomer(@PathVariable String ssnId)
```
**Purpose**: Reactivate a deactivated customer account
**Use Cases**:
- Account recovery
- Reinstatement after review
- Correcting accidental deactivation

#### 8. deactivateCustomer()
```java
@PutMapping("/{ssnId}/deactivate")
@PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
public ResponseEntity<Customer> deactivateCustomer(@PathVariable String ssnId)
```
**Purpose**: Temporarily disable customer account
**Effects**:
- Customer cannot login
- Transactions blocked
- Data preserved
- Can be reactivated

#### 9. getCustomerBalance()
```java
@GetMapping("/{ssnId}/balance")
@PreAuthorize("hasRole('CUSTOMER') and #ssnId == authentication.principal or hasAnyRole('EMPLOYEE', 'MANAGER')")
public ResponseEntity<Map<String, Object>> getCustomerBalance(@PathVariable String ssnId)
```
**Purpose**: Get current account balance
**Response**:
```json
{
  "ssnId": "123-45-6789",
  "balance": 1500.50,
  "currency": "USD",
  "lastUpdated": "2024-01-01T10:00:00"
}
```

#### 10. updateCustomerBalance() [Internal]
```java
@PutMapping("/{ssnId}/balance")
@PreAuthorize("hasRole('SYSTEM')")
public ResponseEntity<Customer> updateCustomerBalance(
    @PathVariable String ssnId,
    @RequestParam BigDecimal amount,
    @RequestParam TransactionType type
)
```
**Purpose**: Internal endpoint for transaction service
**Authorization**: System-only (inter-service communication)
**Operations**:
- CREDIT: Add to balance
- DEBIT: Subtract from balance (with validation)

---

## CustomerService.java

**File Location**: `com/bank/customer/service/CustomerService.java`

**Purpose**: Business logic layer implementing customer operations

### Class Definition
```java
@Service
@Transactional
public class CustomerService
```

### Dependencies
```java
private final CustomerRepository customerRepository;
private final PasswordEncoder passwordEncoder;
private final JwtService jwtService;
private final ApplicationEventPublisher eventPublisher;
```

### Detailed Method Documentation

#### 1. createCustomer()
```java
public Customer createCustomer(Customer customer)
```
**Implementation Details**:
```java
public Customer createCustomer(Customer customer) {
    // 1. Validation
    validateCustomerData(customer);
    
    // 2. Check uniqueness
    if (customerRepository.existsBySsnId(customer.getSsnId())) {
        throw new DataIntegrityViolationException("SSN already exists");
    }
    if (customerRepository.existsByEmail(customer.getEmail())) {
        throw new DataIntegrityViolationException("Email already exists");
    }
    
    // 3. Process data
    customer.setPassword(passwordEncoder.encode(customer.getPassword()));
    customer.setBalance(BigDecimal.ZERO);
    customer.setRole(Role.CUSTOMER);
    customer.setIsActive(true);
    customer.setCreatedAt(LocalDateTime.now());
    
    // 4. Save
    Customer savedCustomer = customerRepository.save(customer);
    
    // 5. Publish event
    eventPublisher.publishEvent(new CustomerCreatedEvent(savedCustomer));
    
    // 6. Return without password
    savedCustomer.setPassword(null);
    return savedCustomer;
}
```

#### 2. authenticateCustomer()
```java
public LoginResponse authenticateCustomer(String ssnId, String password)
```
**Security Implementation**:
```java
public LoginResponse authenticateCustomer(String ssnId, String password) {
    // 1. Find customer
    Customer customer = customerRepository.findBySsnId(ssnId)
        .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    
    // 2. Verify password
    if (!passwordEncoder.matches(password, customer.getPassword())) {
        // Log failed attempt
        logFailedLoginAttempt(ssnId);
        throw new BadCredentialsException("Invalid credentials");
    }
    
    // 3. Check account status
    if (!customer.getIsActive()) {
        throw new AccountLockedException("Account is deactivated");
    }
    
    // 4. Generate token
    String token = jwtService.generateToken(ssnId, customer.getRole().name());
    
    // 5. Update last login
    customer.setLastLoginAt(LocalDateTime.now());
    customerRepository.save(customer);
    
    // 6. Build response
    return LoginResponse.builder()
        .token(token)
        .type("Bearer")
        .username(ssnId)
        .role(customer.getRole().name())
        .customerId(customer.getId())
        .firstName(customer.getFirstName())
        .lastName(customer.getLastName())
        .build();
}
```

#### 3. findBySsnId()
```java
public Customer findBySsnId(String ssnId)
```
**Caching Implementation**:
```java
@Cacheable(value = "customers", key = "#ssnId")
public Customer findBySsnId(String ssnId) {
    return customerRepository.findBySsnId(ssnId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Customer not found with SSN: " + ssnId
        ));
}
```

#### 4. updateCustomer()
```java
public Customer updateCustomer(String ssnId, Customer customerDetails)
```
**Update Logic**:
```java
@CacheEvict(value = "customers", key = "#ssnId")
public Customer updateCustomer(String ssnId, Customer customerDetails) {
    Customer existingCustomer = findBySsnId(ssnId);
    
    // Update fields if provided (null-safe)
    Optional.ofNullable(customerDetails.getFirstName())
        .ifPresent(existingCustomer::setFirstName);
    Optional.ofNullable(customerDetails.getLastName())
        .ifPresent(existingCustomer::setLastName);
    
    // Email change validation
    if (customerDetails.getEmail() != null && 
        !customerDetails.getEmail().equals(existingCustomer.getEmail())) {
        if (customerRepository.existsByEmail(customerDetails.getEmail())) {
            throw new DataIntegrityViolationException("Email already in use");
        }
        existingCustomer.setEmail(customerDetails.getEmail());
    }
    
    // Update other fields
    Optional.ofNullable(customerDetails.getPhone())
        .ifPresent(existingCustomer::setPhone);
    Optional.ofNullable(customerDetails.getAddress())
        .ifPresent(existingCustomer::setAddress);
    Optional.ofNullable(customerDetails.getGender())
        .ifPresent(existingCustomer::setGender);
    Optional.ofNullable(customerDetails.getMaritalStatus())
        .ifPresent(existingCustomer::setMaritalStatus);
    Optional.ofNullable(customerDetails.getDateOfBirth())
        .ifPresent(existingCustomer::setDateOfBirth);
    
    existingCustomer.setUpdatedAt(LocalDateTime.now());
    
    return customerRepository.save(existingCustomer);
}
```

#### 5. updateBalance()
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public void updateBalance(String ssnId, BigDecimal amount, TransactionType type)
```
**Thread-Safe Implementation**:
```java
public void updateBalance(String ssnId, BigDecimal amount, TransactionType type) {
    // Use pessimistic locking to prevent concurrent updates
    Customer customer = customerRepository.findBySsnIdWithLock(ssnId)
        .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    
    BigDecimal currentBalance = customer.getBalance();
    BigDecimal newBalance;
    
    if (type == TransactionType.CREDIT) {
        newBalance = currentBalance.add(amount);
    } else { // DEBIT
        if (currentBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                String.format("Insufficient balance. Current: %.2f, Required: %.2f",
                    currentBalance, amount)
            );
        }
        newBalance = currentBalance.subtract(amount);
    }
    
    customer.setBalance(newBalance);
    customer.setLastTransactionAt(LocalDateTime.now());
    customerRepository.save(customer);
    
    // Publish balance update event
    eventPublisher.publishEvent(new BalanceUpdatedEvent(ssnId, newBalance, type));
}
```

#### 6. validateCustomerData()
```java
private void validateCustomerData(Customer customer)
```
**Validation Rules**:
```java
private void validateCustomerData(Customer customer) {
    List<String> errors = new ArrayList<>();
    
    // Required field validation
    if (StringUtils.isBlank(customer.getFirstName())) {
        errors.add("First name is required");
    }
    if (StringUtils.isBlank(customer.getLastName())) {
        errors.add("Last name is required");
    }
    if (StringUtils.isBlank(customer.getSsnId())) {
        errors.add("SSN is required");
    }
    if (StringUtils.isBlank(customer.getEmail())) {
        errors.add("Email is required");
    }
    if (StringUtils.isBlank(customer.getPassword())) {
        errors.add("Password is required");
    }
    
    // Format validation
    if (!isValidSSN(customer.getSsnId())) {
        errors.add("Invalid SSN format");
    }
    if (!isValidEmail(customer.getEmail())) {
        errors.add("Invalid email format");
    }
    if (!isValidPassword(customer.getPassword())) {
        errors.add("Password must be at least 8 characters with 1 uppercase, 1 lowercase, 1 digit");
    }
    
    // Age validation (must be 18+)
    if (customer.getDateOfBirth() != null) {
        int age = Period.between(customer.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 18) {
            errors.add("Customer must be at least 18 years old");
        }
    }
    
    if (!errors.isEmpty()) {
        throw new ValidationException(String.join(", ", errors));
    }
}
```

---

## Customer.java (Entity)

**File Location**: `com/bank/customer/model/Customer.java`

**Purpose**: JPA entity representing a bank customer

### Class Definition
```java
@Entity
@Table(name = "customers")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer implements Serializable
```

### Field Documentation

#### Identity Fields
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
// Primary key, auto-generated, used for internal references

@Column(unique = true, nullable = false, length = 20)
@Index(name = "idx_ssn_id")
private String ssnId;
// Social Security Number - primary business identifier
// Format: XXX-XX-XXXX
// Used for customer identification in all transactions
```

#### Personal Information
```java
@Column(nullable = false, length = 50)
private String firstName;
// Customer's legal first name

@Column(nullable = false, length = 50)
private String lastName;
// Customer's legal last name

@Column(unique = true, nullable = false, length = 100)
@Email
@Index(name = "idx_email")
private String email;
// Primary contact email, used for login
// Must be unique across all customers

@Column(nullable = false)
@JsonIgnore
private String password;
// BCrypt encrypted password
// Never returned in API responses

@Column(length = 20)
@Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
private String phone;
// International phone format

@Column(length = 255)
private String address;
// Full residential address

@Enumerated(EnumType.STRING)
@Column(length = 10)
private Gender gender;
// MALE, FEMALE, OTHER

@Enumerated(EnumType.STRING)
@Column(length = 20)
private MaritalStatus maritalStatus;
// SINGLE, MARRIED, DIVORCED, WIDOWED

@Temporal(TemporalType.DATE)
private LocalDate dateOfBirth;
// Used for age verification and compliance
```

#### Account Information
```java
@Column(precision = 19, scale = 2, nullable = false)
@ColumnDefault("0.00")
private BigDecimal balance = BigDecimal.ZERO;
// Current account balance
// Precision: 19 digits total, 2 decimal places
// Updated only through transaction service

@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private Role role = Role.CUSTOMER;
// User role for authorization
// Always CUSTOMER for this entity

@Column(nullable = false)
@ColumnDefault("true")
private Boolean isActive = true;
// Account status flag
// false = account deactivated/suspended
```

#### Audit Fields
```java
@CreatedDate
@Column(nullable = false, updatable = false)
private LocalDateTime createdAt;
// Account creation timestamp
// Automatically set on insert

@LastModifiedDate
private LocalDateTime updatedAt;
// Last modification timestamp
// Automatically updated on any change

@Column
private LocalDateTime lastLoginAt;
// Track last successful login

@Column
private LocalDateTime lastTransactionAt;
// Track last transaction activity
```

### Validation Annotations
```java
@PrePersist
public void prePersist() {
    if (balance == null) balance = BigDecimal.ZERO;
    if (role == null) role = Role.CUSTOMER;
    if (isActive == null) isActive = true;
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}

@PreUpdate
public void preUpdate() {
    updatedAt = LocalDateTime.now();
}
```

---

## CustomerRepository.java

**File Location**: `com/bank/customer/repository/CustomerRepository.java`

**Purpose**: Data access layer for customer operations

### Interface Definition
```java
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>
```

### Method Documentation

#### 1. Query Methods
```java
Optional<Customer> findBySsnId(String ssnId);
// Find customer by SSN
// Returns Optional to handle not found case

Optional<Customer> findByEmail(String email);
// Find customer by email
// Used for login and uniqueness check

boolean existsBySsnId(String ssnId);
// Check if SSN exists
// More efficient than findBySsnId for existence check

boolean existsByEmail(String email);
// Check if email exists
// Used during registration validation

List<Customer> findByIsActive(Boolean isActive);
// Get customers by status
// Used for reporting and admin functions

@Query("SELECT c FROM Customer c WHERE c.ssnId = :ssnId AND c.isActive = true")
Optional<Customer> findActiveBySsnId(@Param("ssnId") String ssnId);
// Find only active customers
// Used for transaction validation
```

#### 2. Custom Queries
```java
@Query("SELECT c FROM Customer c WHERE c.balance > :amount")
List<Customer> findCustomersWithBalanceGreaterThan(@Param("amount") BigDecimal amount);
// Find high-value customers
// Used for marketing and analysis

@Query("SELECT c FROM Customer c WHERE c.createdAt BETWEEN :startDate AND :endDate")
List<Customer> findCustomersRegisteredBetween(
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate
);
// Get customers by registration date range
// Used for reporting

@Query(value = "SELECT * FROM customers WHERE ssn_id = :ssnId FOR UPDATE", nativeQuery = true)
Optional<Customer> findBySsnIdWithLock(@Param("ssnId") String ssnId);
// Pessimistic locking for balance updates
// Prevents concurrent modifications
```

#### 3. Aggregate Queries
```java
@Query("SELECT COUNT(c) FROM Customer c WHERE c.isActive = true")
Long countActiveCustomers();
// Get active customer count
// Used for dashboard

@Query("SELECT SUM(c.balance) FROM Customer c WHERE c.isActive = true")
BigDecimal getTotalBalance();
// Calculate total deposits
// Used for financial reporting

@Query("SELECT AVG(c.balance) FROM Customer c WHERE c.isActive = true")
BigDecimal getAverageBalance();
// Calculate average customer balance
// Used for analytics
```

#### 4. Pagination and Sorting
```java
Page<Customer> findByIsActive(Boolean isActive, Pageable pageable);
// Paginated customer list
// Supports sorting and pagination

@Query("SELECT c FROM Customer c WHERE " +
       "(:firstName IS NULL OR c.firstName LIKE %:firstName%) AND " +
       "(:lastName IS NULL OR c.lastName LIKE %:lastName%) AND " +
       "(:email IS NULL OR c.email LIKE %:email%)")
Page<Customer> searchCustomers(
    @Param("firstName") String firstName,
    @Param("lastName") String lastName,
    @Param("email") String email,
    Pageable pageable
);
// Advanced search with multiple criteria
// Supports partial matching
```

#### 5. Update Operations
```java
@Modifying
@Query("UPDATE Customer c SET c.isActive = :status WHERE c.ssnId = :ssnId")
int updateCustomerStatus(@Param("ssnId") String ssnId, @Param("status") Boolean status);
// Bulk status update
// Returns number of affected rows

@Modifying
@Query("UPDATE Customer c SET c.balance = c.balance + :amount WHERE c.ssnId = :ssnId")
int creditBalance(@Param("ssnId") String ssnId, @Param("amount") BigDecimal amount);
// Direct balance credit
// Used by transaction service

@Modifying
@Query("UPDATE Customer c SET c.balance = c.balance - :amount WHERE c.ssnId = :ssnId AND c.balance >= :amount")
int debitBalance(@Param("ssnId") String ssnId, @Param("amount") BigDecimal amount);
// Direct balance debit with validation
// Returns 0 if insufficient balance
```

---

## JwtService.java

**File Location**: `com/bank/customer/service/JwtService.java`

**Purpose**: JWT token generation, validation, and management

### Class Definition
```java
@Service
@Component
public class JwtService
```

### Configuration
```java
@Value("${jwt.secret}")
private String jwtSecret; // Secret key from properties

@Value("${jwt.expiration:86400000}") // Default 24 hours
private long jwtExpiration;

@Value("${jwt.refresh.expiration:604800000}") // Default 7 days
private long refreshTokenExpiration;
```

### Method Documentation

#### 1. generateToken()
```java
public String generateToken(String username, String role)
```
**Implementation**:
```java
public String generateToken(String username, String role) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);
    claims.put("timestamp", System.currentTimeMillis());
    claims.put("type", "access");
    
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
}
```

#### 2. generateRefreshToken()
```java
public String generateRefreshToken(String username)
```
**Purpose**: Generate long-lived refresh token
```java
public String generateRefreshToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("type", "refresh");
    
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
}
```

#### 3. validateToken()
```java
public boolean validateToken(String token)
```
**Validation Process**:
```java
public boolean validateToken(String token) {
    try {
        Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
        return !isTokenExpired(token);
    } catch (SignatureException e) {
        log.error("Invalid JWT signature: {}", e.getMessage());
    } catch (MalformedJwtException e) {
        log.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
        log.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
        log.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
        log.error("JWT claims string is empty: {}", e.getMessage());
    }
    return false;
}
```

#### 4. extractUsername()
```java
public String extractUsername(String token)
```
**Purpose**: Extract subject (username/ssnId) from token
```java
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
}
```

#### 5. extractRole()
```java
public String extractRole(String token)
```
**Purpose**: Extract user role from token claims
```java
public String extractRole(String token) {
    Claims claims = extractAllClaims(token);
    return (String) claims.get("role");
}
```

#### 6. extractExpiration()
```java
public Date extractExpiration(String token)
```
**Purpose**: Get token expiration time
```java
public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
}
```

#### 7. isTokenExpired()
```java
public boolean isTokenExpired(String token)
```
**Check**: Verify if token has expired
```java
public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
}
```

#### 8. refreshToken()
```java
public String refreshToken(String refreshToken)
```
**Purpose**: Generate new access token from refresh token
```java
public String refreshToken(String refreshToken) {
    if (!validateToken(refreshToken)) {
        throw new InvalidTokenException("Invalid refresh token");
    }
    
    Claims claims = extractAllClaims(refreshToken);
    if (!"refresh".equals(claims.get("type"))) {
        throw new InvalidTokenException("Not a refresh token");
    }
    
    String username = claims.getSubject();
    // Fetch current role from database
    Customer customer = customerRepository.findBySsnId(username)
        .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    
    return generateToken(username, customer.getRole().name());
}
```

---

## JwtAuthenticationFilter.java

**File Location**: `com/bank/customer/config/JwtAuthenticationFilter.java`

**Purpose**: Spring Security filter for JWT authentication

### Class Definition
```java
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter
```

### Dependencies
```java
@Autowired
private JwtService jwtService;

@Autowired
private UserDetailsService userDetailsService;
```

### Method Documentation

#### 1. doFilterInternal()
```java
@Override
protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
) throws ServletException, IOException
```
**Filter Logic**:
```java
protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
) throws ServletException, IOException {
    
    // 1. Extract Authorization header
    final String authorizationHeader = request.getHeader("Authorization");
    
    String username = null;
    String jwt = null;
    
    // 2. Check Bearer token format
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        jwt = authorizationHeader.substring(7);
        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            log.error("Error extracting username from JWT", e);
        }
    }
    
    // 3. Validate token and set authentication
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        
        if (jwtService.validateToken(jwt)) {
            String role = jwtService.extractRole(jwt);
            
            // Create authentication token
            List<SimpleGrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_" + role)
            );
            
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(
                    username, null, authorities
                );
            
            authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );
            
            // Set authentication in context
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            log.debug("JWT authentication successful for user: {}", username);
        } else {
            log.warn("Invalid JWT token for user: {}", username);
        }
    }
    
    // 4. Continue filter chain
    filterChain.doFilter(request, response);
}
```

#### 2. shouldNotFilter()
```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request)
```
**Purpose**: Skip filter for public endpoints
```java
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    
    // Public endpoints that don't require authentication
    List<String> publicPaths = Arrays.asList(
        "/api/customers/register",
        "/api/customers/login",
        "/h2-console",
        "/swagger-ui",
        "/v3/api-docs",
        "/actuator/health"
    );
    
    return publicPaths.stream()
        .anyMatch(path::startsWith);
}
```

---

## SecurityConfig.java

**File Location**: `com/bank/customer/config/SecurityConfig.java`

**Purpose**: Spring Security configuration

### Class Definition
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Slf4j
public class SecurityConfig
```

### Bean Configurations

#### 1. SecurityFilterChain
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
```
**Configuration**:
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        // Disable CSRF for stateless API
        .csrf(csrf -> csrf.disable())
        
        // Configure CORS
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        
        // Session management
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        
        // Authorization rules
        .authorizeHttpRequests(auth -> auth
            // Public endpoints
            .requestMatchers(
                "/api/customers/register",
                "/api/customers/login",
                "/h2-console/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/actuator/health"
            ).permitAll()
            
            // Customer endpoints
            .requestMatchers("/api/customers/{ssnId}")
                .hasAnyRole("CUSTOMER", "EMPLOYEE", "MANAGER")
            
            // Admin endpoints
            .requestMatchers(HttpMethod.GET, "/api/customers")
                .hasAnyRole("EMPLOYEE", "MANAGER")
            
            .requestMatchers(HttpMethod.DELETE, "/api/customers/**")
                .hasRole("MANAGER")
            
            // All other endpoints require authentication
            .anyRequest().authenticated()
        )
        
        // Exception handling
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(customAccessDeniedHandler)
        )
        
        // Add JWT filter
        .addFilterBefore(
            jwtAuthenticationFilter, 
            UsernamePasswordAuthenticationFilter.class
        )
        
        // H2 Console configuration
        .headers(headers -> headers
            .frameOptions(frame -> frame.sameOrigin())
        )
        
        .build();
}
```

#### 2. PasswordEncoder
```java
@Bean
public PasswordEncoder passwordEncoder()
```
**Configuration**:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // Strength 12 for better security
}
```

#### 3. CORS Configuration
```java
@Bean
public CorsConfigurationSource corsConfigurationSource()
```
**Setup**:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Allowed origins
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:4200",  // Angular dev
        "http://localhost:3000",  // React dev
        "https://bank-app.com"    // Production
    ));
    
    // Allowed methods
    configuration.setAllowedMethods(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    ));
    
    // Allowed headers
    configuration.setAllowedHeaders(Arrays.asList(
        "Authorization",
        "Content-Type",
        "X-Requested-With",
        "Accept",
        "Origin",
        "Access-Control-Request-Method",
        "Access-Control-Request-Headers"
    ));
    
    // Exposed headers
    configuration.setExposedHeaders(Arrays.asList(
        "Access-Control-Allow-Origin",
        "Access-Control-Allow-Credentials"
    ));
    
    // Allow credentials
    configuration.setAllowCredentials(true);
    
    // Max age
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    
    return source;
}
```

#### 4. Authentication Entry Point
```java
@Bean
public AuthenticationEntryPoint jwtAuthenticationEntryPoint()
```
**Implementation**:
```java
@Bean
public AuthenticationEntryPoint jwtAuthenticationEntryPoint() {
    return (request, response, authException) -> {
        log.error("Unauthorized access attempt: {}", authException.getMessage());
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", authException.getMessage());
        errorDetails.put("path", request.getRequestURI());
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.writeValue(response.getOutputStream(), errorDetails);
    };
}
```

---

## DataSeeder.java

**File Location**: `com/bank/customer/config/DataSeeder.java`

**Purpose**: Initialize database with sample data for development

### Class Definition
```java
@Component
@Profile("!production")
@Slf4j
public class DataSeeder implements CommandLineRunner
```

### Implementation
```java
@Override
public void run(String... args) throws Exception {
    if (customerRepository.count() == 0) {
        log.info("Seeding customer data...");
        
        List<Customer> customers = Arrays.asList(
            Customer.builder()
                .ssnId("111-11-1111")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password(passwordEncoder.encode("Password123"))
                .phone("+1-555-0101")
                .address("123 Main St, New York, NY 10001")
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .balance(new BigDecimal("5000.00"))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build(),
                
            Customer.builder()
                .ssnId("222-22-2222")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .password(passwordEncoder.encode("Password123"))
                .phone("+1-555-0102")
                .address("456 Oak Ave, Los Angeles, CA 90001")
                .gender(Gender.FEMALE)
                .maritalStatus(MaritalStatus.SINGLE)
                .dateOfBirth(LocalDate.of(1990, 8, 20))
                .balance(new BigDecimal("10000.00"))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build(),
                
            Customer.builder()
                .ssnId("333-33-3333")
                .firstName("Robert")
                .lastName("Johnson")
                .email("robert.johnson@example.com")
                .password(passwordEncoder.encode("Password123"))
                .phone("+1-555-0103")
                .address("789 Pine Rd, Chicago, IL 60601")
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.DIVORCED)
                .dateOfBirth(LocalDate.of(1975, 3, 10))
                .balance(new BigDecimal("15000.00"))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build(),
                
            Customer.builder()
                .ssnId("444-44-4444")
                .firstName("Maria")
                .lastName("Garcia")
                .email("maria.garcia@example.com")
                .password(passwordEncoder.encode("Password123"))
                .phone("+1-555-0104")
                .address("321 Elm St, Houston, TX 77001")
                .gender(Gender.FEMALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .dateOfBirth(LocalDate.of(1988, 11, 25))
                .balance(new BigDecimal("7500.00"))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build(),
                
            Customer.builder()
                .ssnId("555-55-5555")
                .firstName("David")
                .lastName("Wilson")
                .email("david.wilson@example.com")
                .password(passwordEncoder.encode("Password123"))
                .phone("+1-555-0105")
                .address("654 Maple Dr, Phoenix, AZ 85001")
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.SINGLE)
                .dateOfBirth(LocalDate.of(1995, 7, 5))
                .balance(new BigDecimal("2500.00"))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build()
        );
        
        customerRepository.saveAll(customers);
        log.info("Seeded {} customers", customers.size());
    }
}
```

---

## DTOs

### LoginRequest.java
**File Location**: `com/bank/customer/dto/LoginRequest.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    
    @NotBlank(message = "SSN ID is required")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{4}$", 
             message = "SSN must be in format XXX-XX-XXXX")
    private String ssnId;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
```

### LoginResponse.java
**File Location**: `com/bank/customer/dto/LoginResponse.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    @JsonProperty("access_token")
    private String token;
    
    @JsonProperty("token_type")
    private String type = "Bearer";
    
    @JsonProperty("expires_in")
    private long expiresIn = 86400; // 24 hours in seconds
    
    private String username;
    private String role;
    private Long customerId;
    private String firstName;
    private String lastName;
    
    @JsonProperty("issued_at")
    private LocalDateTime issuedAt = LocalDateTime.now();
}
```

---

## Enums

### Gender.java
**File Location**: `com/bank/customer/model/Gender.java`

```java
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");
    
    private final String displayName;
    
    Gender(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

### MaritalStatus.java
**File Location**: `com/bank/customer/model/MaritalStatus.java`

```java
public enum MaritalStatus {
    SINGLE("Single"),
    MARRIED("Married"),
    DIVORCED("Divorced"),
    WIDOWED("Widowed"),
    SEPARATED("Separated");
    
    private final String displayName;
    
    MaritalStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

### Role.java
**File Location**: `com/bank/customer/model/Role.java`

```java
public enum Role {
    CUSTOMER("Customer"),
    EMPLOYEE("Employee"),
    MANAGER("Manager"),
    ADMIN("Admin");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean hasAuthority(String authority) {
        return this.name().equals(authority);
    }
}
```

---

## Exception Classes

### ResourceNotFoundException.java
```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

### ValidationException.java
```java
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {
    private Map<String, String> errors;
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(Map<String, String> errors) {
        super("Validation failed");
        this.errors = errors;
    }
}
```

### InsufficientBalanceException.java
```java
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
```

---

## Testing Examples

### CustomerControllerTest.java
```java
@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testRegisterCustomer() throws Exception {
        String customerJson = """
            {
                "firstName": "Test",
                "lastName": "User",
                "ssnId": "999-99-9999",
                "email": "test@example.com",
                "password": "TestPass123"
            }
            """;
        
        mockMvc.perform(post("/api/customers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(customerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
    
    @Test
    void testLoginCustomer() throws Exception {
        String loginJson = """
            {
                "ssnId": "111-11-1111",
                "password": "Password123"
            }
            """;
        
        mockMvc.perform(post("/api/customers/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }
}
```

---

This completes the detailed documentation for the Customer Service. Each component is thoroughly documented with its purpose, implementation details, and usage examples.