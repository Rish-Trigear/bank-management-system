# Employee Service - Detailed Component Documentation

## Table of Contents
1. [EmployeeController.java](#employeecontrollerjava)
2. [EmployeeService.java](#employeeservicejava)
3. [Employee.java (Entity)](#employeejava-entity)
4. [EmployeeRepository.java](#employeerepositoryjava)
5. [JwtService.java](#jwtservicejava)
6. [JwtAuthenticationFilter.java](#jwtauthenticationfilterjava)
7. [SecurityConfig.java](#securityconfigjava)
8. [DataSeeder.java](#dataseederjava)
9. [DTOs](#dtos)
10. [Exception Handling](#exception-handling)

---

## EmployeeController.java

**File Location**: `com/bank/employee/controller/EmployeeController.java`

**Purpose**: REST controller managing all employee-related HTTP endpoints including authentication, CRUD operations, and employee management.

### Class Definition
```java
@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"})
@Slf4j
public class EmployeeController
```

### Dependencies
```java
private final EmployeeService employeeService;
private final JwtService jwtService;
private final PasswordEncoder passwordEncoder;
```

### Detailed Method Documentation

#### 1. loginEmployee()
```java
@PostMapping("/login")
public ResponseEntity<LoginResponse> loginEmployee(@Valid @RequestBody LoginRequest loginRequest)
```
**Purpose**: Authenticate employee and generate JWT token
**Process Flow**:
1. Validate email format and password
2. Retrieve employee by email
3. Verify password using BCrypt
4. Check if account is active
5. Generate JWT token with role (EMPLOYEE or MANAGER)
6. Update last login timestamp
7. Return token with employee details

**Request Body**:
```json
{
  "email": "john.doe@bank.com",
  "password": "SecurePass123"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "username": "john.doe@bank.com",
  "role": "EMPLOYEE",
  "employeeId": "EMP001",
  "firstName": "John",
  "lastName": "Doe",
  "department": "Customer Service"
}
```

**Error Scenarios**:
- `401 Unauthorized`: Invalid credentials
- `403 Forbidden`: Account deactivated
- `400 Bad Request`: Missing or invalid fields

#### 2. createEmployee()
```java
@PostMapping
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee)
```
**Purpose**: Create new employee account (Manager only)
**Process Flow**:
1. Validate manager authorization
2. Check email uniqueness
3. Generate unique employee ID
4. Encrypt password
5. Set initial values (joinDate, isActive)
6. Save to database
7. Send welcome email (if configured)

**Validation Rules**:
- Email must be unique
- Password minimum 8 characters with complexity
- Role must be EMPLOYEE or MANAGER
- Salary must be positive
- Department and designation required

**Generated Employee ID Format**: `EMP + YYYYMMDD + 4-digit-sequence`
Example: `EMP202401010001`

#### 3. getAllEmployees()
```java
@GetMapping
@PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
public ResponseEntity<Page<Employee>> getAllEmployees(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "employeeId") String sortBy,
    @RequestParam(defaultValue = "ASC") String sortDirection,
    @RequestParam(required = false) String department,
    @RequestParam(required = false) Boolean isActive
)
```
**Purpose**: Retrieve all employees with pagination and filtering
**Features**:
- Pagination with configurable page size
- Sorting by any field
- Filter by department
- Filter by active status
- Password field excluded from response

**Query Examples**:
```
GET /api/employees?page=0&size=20&sortBy=joinDate&sortDirection=DESC
GET /api/employees?department=IT&isActive=true
```

**Response Structure**:
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 50,
  "totalPages": 5,
  "last": false
}
```

#### 4. getEmployeeById()
```java
@GetMapping("/{employeeId}")
@PreAuthorize("hasRole('EMPLOYEE') and #employeeId == authentication.principal or hasRole('MANAGER')")
public ResponseEntity<Employee> getEmployeeById(@PathVariable String employeeId)
```
**Purpose**: Retrieve specific employee details
**Authorization Logic**:
- Employees can view their own profile
- Managers can view any employee profile

**Path Variable Validation**:
- Must match pattern: `EMP\d{12}`
- Example: `EMP202401010001`

#### 5. updateEmployee()
```java
@PutMapping("/{employeeId}")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<Employee> updateEmployee(
    @PathVariable String employeeId,
    @Valid @RequestBody Employee employeeDetails
)
```
**Purpose**: Update employee information (Manager only)
**Updatable Fields**:
- Personal: firstName, lastName, phone
- Professional: department, designation, salary
- Account: email (must be unique), role
- Status: isActive

**Non-Updatable Fields**:
- employeeId (immutable)
- joinDate (audit field)
- createdAt/updatedAt (system managed)

**Update Process**:
1. Validate manager authorization
2. Retrieve existing employee
3. Check email uniqueness if changed
4. Apply updates (null fields ignored)
5. Log changes for audit
6. Save and return updated employee

#### 6. deleteEmployee()
```java
@DeleteMapping("/{employeeId}")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<Map<String, String>> deleteEmployee(@PathVariable String employeeId)
```
**Purpose**: Soft delete employee account (Manager only)
**Process**:
1. Validate manager authorization
2. Check if employee exists
3. Prevent self-deletion
4. Set isActive to false
5. Revoke all active sessions
6. Log deletion for audit

**Response**:
```json
{
  "message": "Employee deactivated successfully",
  "employeeId": "EMP001",
  "deactivatedAt": "2024-01-01T10:00:00"
}
```

#### 7. activateEmployee()
```java
@PutMapping("/{employeeId}/activate")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<Employee> activateEmployee(@PathVariable String employeeId)
```
**Purpose**: Reactivate deactivated employee account
**Use Cases**:
- Rehiring former employees
- Correcting accidental deactivation
- Temporary suspension lifted

#### 8. deactivateEmployee()
```java
@PutMapping("/{employeeId}/deactivate")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<Employee> deactivateEmployee(@PathVariable String employeeId)
```
**Purpose**: Temporarily disable employee account
**Effects**:
- Cannot login
- All permissions revoked
- Data preserved for audit
- Can be reactivated

#### 9. changePassword()
```java
@PutMapping("/{employeeId}/change-password")
@PreAuthorize("hasRole('EMPLOYEE') and #employeeId == authentication.principal or hasRole('MANAGER')")
public ResponseEntity<Map<String, String>> changePassword(
    @PathVariable String employeeId,
    @Valid @RequestBody ChangePasswordRequest request
)
```
**Purpose**: Change employee password
**Request Body**:
```json
{
  "currentPassword": "OldPass123",
  "newPassword": "NewPass456",
  "confirmPassword": "NewPass456"
}
```

**Validation**:
- Current password must be correct
- New password must meet complexity requirements
- New and confirm passwords must match
- Cannot reuse last 5 passwords

#### 10. getEmployeesByDepartment()
```java
@GetMapping("/department/{department}")
@PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
public ResponseEntity<List<Employee>> getEmployeesByDepartment(@PathVariable String department)
```
**Purpose**: Get all employees in a specific department
**Use Cases**:
- Department rosters
- Team management
- Workload distribution

---

## EmployeeService.java

**File Location**: `com/bank/employee/service/EmployeeService.java`

**Purpose**: Business logic layer for employee operations

### Class Definition
```java
@Service
@Transactional
@Slf4j
public class EmployeeService
```

### Dependencies
```java
private final EmployeeRepository employeeRepository;
private final PasswordEncoder passwordEncoder;
private final JwtService jwtService;
private final ApplicationEventPublisher eventPublisher;
private final EmailService emailService;
```

### Detailed Method Documentation

#### 1. authenticateEmployee()
```java
public LoginResponse authenticateEmployee(String email, String password)
```
**Implementation**:
```java
public LoginResponse authenticateEmployee(String email, String password) {
    log.info("Authentication attempt for email: {}", email);
    
    // 1. Find employee
    Employee employee = employeeRepository.findByEmail(email)
        .orElseThrow(() -> {
            log.warn("Login attempt with non-existent email: {}", email);
            return new BadCredentialsException("Invalid credentials");
        });
    
    // 2. Check account status
    if (!employee.getIsActive()) {
        log.warn("Login attempt on deactivated account: {}", email);
        throw new AccountLockedException("Account is deactivated");
    }
    
    // 3. Verify password
    if (!passwordEncoder.matches(password, employee.getPassword())) {
        log.warn("Failed login attempt for email: {}", email);
        incrementFailedAttempts(employee);
        throw new BadCredentialsException("Invalid credentials");
    }
    
    // 4. Reset failed attempts on successful login
    if (employee.getFailedLoginAttempts() > 0) {
        employee.setFailedLoginAttempts(0);
    }
    
    // 5. Generate JWT token
    String token = jwtService.generateToken(
        employee.getEmployeeId(), 
        employee.getRole().name()
    );
    
    // 6. Update last login
    employee.setLastLoginAt(LocalDateTime.now());
    employeeRepository.save(employee);
    
    // 7. Publish login event
    eventPublisher.publishEvent(new EmployeeLoginEvent(employee));
    
    // 8. Build response
    return LoginResponse.builder()
        .token(token)
        .type("Bearer")
        .username(employee.getEmail())
        .role(employee.getRole().name())
        .employeeId(employee.getEmployeeId())
        .firstName(employee.getFirstName())
        .lastName(employee.getLastName())
        .department(employee.getDepartment())
        .build();
}
```

#### 2. createEmployee()
```java
public Employee createEmployee(Employee employee)
```
**Implementation with ID Generation**:
```java
public Employee createEmployee(Employee employee) {
    log.info("Creating new employee with email: {}", employee.getEmail());
    
    // 1. Validate data
    validateEmployeeData(employee);
    
    // 2. Check email uniqueness
    if (employeeRepository.existsByEmail(employee.getEmail())) {
        throw new DataIntegrityViolationException("Email already exists");
    }
    
    // 3. Generate unique employee ID
    String employeeId = generateEmployeeId();
    employee.setEmployeeId(employeeId);
    
    // 4. Encrypt password
    employee.setPassword(passwordEncoder.encode(employee.getPassword()));
    
    // 5. Set defaults
    employee.setJoinDate(LocalDate.now());
    employee.setIsActive(true);
    employee.setFailedLoginAttempts(0);
    employee.setCreatedAt(LocalDateTime.now());
    
    // 6. Save employee
    Employee savedEmployee = employeeRepository.save(employee);
    
    // 7. Send welcome email
    sendWelcomeEmail(savedEmployee);
    
    // 8. Publish event
    eventPublisher.publishEvent(new EmployeeCreatedEvent(savedEmployee));
    
    // 9. Return without password
    savedEmployee.setPassword(null);
    return savedEmployee;
}

private String generateEmployeeId() {
    String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    Long sequenceNumber = employeeRepository.getNextSequenceNumber();
    return String.format("EMP%s%04d", datePrefix, sequenceNumber);
}
```

#### 3. updateEmployee()
```java
public Employee updateEmployee(String employeeId, Employee employeeDetails)
```
**Update with Audit Logging**:
```java
@Transactional
public Employee updateEmployee(String employeeId, Employee employeeDetails) {
    log.info("Updating employee: {}", employeeId);
    
    Employee existingEmployee = findByEmployeeId(employeeId);
    Map<String, Object> changes = new HashMap<>();
    
    // Track changes for audit
    if (employeeDetails.getFirstName() != null && 
        !employeeDetails.getFirstName().equals(existingEmployee.getFirstName())) {
        changes.put("firstName", new Object[]{
            existingEmployee.getFirstName(), 
            employeeDetails.getFirstName()
        });
        existingEmployee.setFirstName(employeeDetails.getFirstName());
    }
    
    if (employeeDetails.getLastName() != null && 
        !employeeDetails.getLastName().equals(existingEmployee.getLastName())) {
        changes.put("lastName", new Object[]{
            existingEmployee.getLastName(), 
            employeeDetails.getLastName()
        });
        existingEmployee.setLastName(employeeDetails.getLastName());
    }
    
    // Email change with uniqueness check
    if (employeeDetails.getEmail() != null && 
        !employeeDetails.getEmail().equals(existingEmployee.getEmail())) {
        if (employeeRepository.existsByEmail(employeeDetails.getEmail())) {
            throw new DataIntegrityViolationException("Email already in use");
        }
        changes.put("email", new Object[]{
            existingEmployee.getEmail(), 
            employeeDetails.getEmail()
        });
        existingEmployee.setEmail(employeeDetails.getEmail());
    }
    
    // Department change
    if (employeeDetails.getDepartment() != null && 
        !employeeDetails.getDepartment().equals(existingEmployee.getDepartment())) {
        changes.put("department", new Object[]{
            existingEmployee.getDepartment(), 
            employeeDetails.getDepartment()
        });
        existingEmployee.setDepartment(employeeDetails.getDepartment());
    }
    
    // Salary change (requires special permission)
    if (employeeDetails.getSalary() != null && 
        !employeeDetails.getSalary().equals(existingEmployee.getSalary())) {
        changes.put("salary", new Object[]{
            existingEmployee.getSalary(), 
            employeeDetails.getSalary()
        });
        existingEmployee.setSalary(employeeDetails.getSalary());
    }
    
    // Role change (critical operation)
    if (employeeDetails.getRole() != null && 
        !employeeDetails.getRole().equals(existingEmployee.getRole())) {
        changes.put("role", new Object[]{
            existingEmployee.getRole(), 
            employeeDetails.getRole()
        });
        existingEmployee.setRole(employeeDetails.getRole());
        // Revoke existing sessions when role changes
        revokeEmployeeSessions(employeeId);
    }
    
    existingEmployee.setUpdatedAt(LocalDateTime.now());
    Employee updatedEmployee = employeeRepository.save(existingEmployee);
    
    // Log audit trail
    if (!changes.isEmpty()) {
        logAuditTrail(employeeId, "UPDATE", changes);
    }
    
    updatedEmployee.setPassword(null);
    return updatedEmployee;
}
```

#### 4. findByEmployeeId()
```java
public Employee findByEmployeeId(String employeeId)
```
**With Caching**:
```java
@Cacheable(value = "employees", key = "#employeeId")
public Employee findByEmployeeId(String employeeId) {
    return employeeRepository.findByEmployeeId(employeeId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Employee not found with ID: " + employeeId
        ));
}
```

#### 5. deleteEmployee()
```java
public void deleteEmployee(String employeeId)
```
**Soft Delete Implementation**:
```java
@Transactional
@CacheEvict(value = "employees", key = "#employeeId")
public void deleteEmployee(String employeeId) {
    log.info("Deactivating employee: {}", employeeId);
    
    Employee employee = findByEmployeeId(employeeId);
    
    // Prevent deletion of last manager
    if (employee.getRole() == Role.MANAGER) {
        long managerCount = employeeRepository.countByRoleAndIsActive(Role.MANAGER, true);
        if (managerCount <= 1) {
            throw new IllegalStateException("Cannot deactivate the last active manager");
        }
    }
    
    employee.setIsActive(false);
    employee.setDeactivatedAt(LocalDateTime.now());
    employeeRepository.save(employee);
    
    // Revoke all sessions
    revokeEmployeeSessions(employeeId);
    
    // Log audit
    logAuditTrail(employeeId, "DEACTIVATE", null);
    
    // Publish event
    eventPublisher.publishEvent(new EmployeeDeactivatedEvent(employee));
}
```

#### 6. getEmployeesByDepartment()
```java
public List<Employee> getEmployeesByDepartment(String department)
```
**Implementation**:
```java
public List<Employee> getEmployeesByDepartment(String department) {
    List<Employee> employees = employeeRepository.findByDepartmentAndIsActive(department, true);
    
    // Remove sensitive data
    employees.forEach(emp -> emp.setPassword(null));
    
    return employees;
}
```

#### 7. updatePassword()
```java
public void updatePassword(String employeeId, String currentPassword, String newPassword)
```
**Password Update with History**:
```java
@Transactional
public void updatePassword(String employeeId, String currentPassword, String newPassword) {
    Employee employee = findByEmployeeId(employeeId);
    
    // Verify current password
    if (!passwordEncoder.matches(currentPassword, employee.getPassword())) {
        throw new BadCredentialsException("Current password is incorrect");
    }
    
    // Check password history (prevent reuse of last 5 passwords)
    if (isPasswordReused(employeeId, newPassword)) {
        throw new ValidationException("Cannot reuse recent passwords");
    }
    
    // Validate new password strength
    validatePasswordStrength(newPassword);
    
    // Update password
    String encodedPassword = passwordEncoder.encode(newPassword);
    employee.setPassword(encodedPassword);
    employee.setPasswordChangedAt(LocalDateTime.now());
    employeeRepository.save(employee);
    
    // Save to password history
    savePasswordHistory(employeeId, encodedPassword);
    
    // Revoke existing sessions
    revokeEmployeeSessions(employeeId);
    
    // Send notification
    sendPasswordChangeNotification(employee);
    
    log.info("Password updated for employee: {}", employeeId);
}
```

---

## Employee.java (Entity)

**File Location**: `com/bank/employee/model/Employee.java`

**Purpose**: JPA entity representing a bank employee

### Class Definition
```java
@Entity
@Table(name = "employees",
    indexes = {
        @Index(name = "idx_employee_id", columnList = "employeeId"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_department", columnList = "department")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee implements Serializable
```

### Field Documentation

#### Identity Fields
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
// Primary key for database

@Column(unique = true, nullable = false, length = 20)
private String employeeId;
// Unique business identifier
// Format: EMP + Date + Sequence (e.g., EMP202401010001)

@Column(unique = true, nullable = false, length = 100)
@Email(message = "Invalid email format")
private String email;
// Login credential and contact email

@Column(nullable = false)
@JsonIgnore
private String password;
// BCrypt encrypted password
```

#### Personal Information
```java
@Column(nullable = false, length = 50)
@NotBlank(message = "First name is required")
private String firstName;

@Column(nullable = false, length = 50)
@NotBlank(message = "Last name is required")
private String lastName;

@Column(length = 20)
@Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
private String phone;

@Column(length = 255)
private String address;

@Temporal(TemporalType.DATE)
private LocalDate dateOfBirth;

@Enumerated(EnumType.STRING)
private Gender gender;
```

#### Professional Information
```java
@Column(nullable = false, length = 50)
@NotBlank(message = "Department is required")
private String department;
// e.g., "Customer Service", "IT", "Finance", "HR"

@Column(nullable = false, length = 50)
@NotBlank(message = "Designation is required")
private String designation;
// e.g., "Executive", "Senior Executive", "Manager", "Senior Manager"

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private Role role;
// EMPLOYEE or MANAGER

@Column(precision = 19, scale = 2)
@Positive(message = "Salary must be positive")
private BigDecimal salary;

@Column(nullable = false)
private LocalDate joinDate;
// Employment start date

private String reportingTo;
// Employee ID of immediate supervisor

private String emergencyContact;
// Emergency contact details
```

#### Account Status
```java
@Column(nullable = false)
@ColumnDefault("true")
private Boolean isActive = true;
// Account active status

private Integer failedLoginAttempts = 0;
// Track failed login attempts for security

private LocalDateTime lastLoginAt;
// Last successful login timestamp

private LocalDateTime passwordChangedAt;
// Last password change timestamp

private LocalDateTime deactivatedAt;
// Account deactivation timestamp
```

#### Audit Fields
```java
@CreatedDate
@Column(nullable = false, updatable = false)
private LocalDateTime createdAt;

@LastModifiedDate
private LocalDateTime updatedAt;

@CreatedBy
private String createdBy;
// Employee ID who created this record

@LastModifiedBy
private String modifiedBy;
// Employee ID who last modified
```

### Validation Methods
```java
@PrePersist
public void prePersist() {
    if (isActive == null) isActive = true;
    if (failedLoginAttempts == null) failedLoginAttempts = 0;
    if (joinDate == null) joinDate = LocalDate.now();
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}

@PreUpdate
public void preUpdate() {
    updatedAt = LocalDateTime.now();
}
```

---

## EmployeeRepository.java

**File Location**: `com/bank/employee/repository/EmployeeRepository.java`

**Purpose**: Data access layer for employee operations

### Interface Definition
```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee>
```

### Method Documentation

#### 1. Basic Query Methods
```java
Optional<Employee> findByEmployeeId(String employeeId);
// Find employee by unique ID

Optional<Employee> findByEmail(String email);
// Find employee by email for login

boolean existsByEmployeeId(String employeeId);
// Check if employee ID exists

boolean existsByEmail(String email);
// Check if email is already registered

List<Employee> findByIsActive(Boolean isActive);
// Get employees by active status

List<Employee> findByDepartment(String department);
// Get all employees in a department

List<Employee> findByRole(Role role);
// Get employees by role (EMPLOYEE/MANAGER)
```

#### 2. Complex Queries
```java
@Query("SELECT e FROM Employee e WHERE e.department = :department AND e.isActive = true")
List<Employee> findByDepartmentAndIsActive(
    @Param("department") String department, 
    @Param("isActive") Boolean isActive
);

@Query("SELECT e FROM Employee e WHERE e.role = :role AND e.isActive = :isActive")
List<Employee> findByRoleAndIsActive(
    @Param("role") Role role, 
    @Param("isActive") Boolean isActive
);

@Query("SELECT e FROM Employee e WHERE e.joinDate BETWEEN :startDate AND :endDate")
List<Employee> findEmployeesJoinedBetween(
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate
);

@Query("SELECT e FROM Employee e WHERE e.salary BETWEEN :minSalary AND :maxSalary")
List<Employee> findEmployeesBySalaryRange(
    @Param("minSalary") BigDecimal minSalary,
    @Param("maxSalary") BigDecimal maxSalary
);
```

#### 3. Aggregate Queries
```java
@Query("SELECT COUNT(e) FROM Employee e WHERE e.isActive = true")
Long countActiveEmployees();

@Query("SELECT COUNT(e) FROM Employee e WHERE e.role = :role AND e.isActive = true")
Long countByRoleAndIsActive(@Param("role") Role role, @Param("isActive") Boolean isActive);

@Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department = :department")
BigDecimal getAverageSalaryByDepartment(@Param("department") String department);

@Query("SELECT SUM(e.salary) FROM Employee e WHERE e.isActive = true")
BigDecimal getTotalSalaryExpense();

@Query("SELECT e.department, COUNT(e) FROM Employee e WHERE e.isActive = true GROUP BY e.department")
List<Object[]> getEmployeeCountByDepartment();
```

#### 4. Update Operations
```java
@Modifying
@Query("UPDATE Employee e SET e.isActive = :status WHERE e.employeeId = :employeeId")
int updateEmployeeStatus(
    @Param("employeeId") String employeeId, 
    @Param("status") Boolean status
);

@Modifying
@Query("UPDATE Employee e SET e.failedLoginAttempts = e.failedLoginAttempts + 1 WHERE e.email = :email")
int incrementFailedLoginAttempts(@Param("email") String email);

@Modifying
@Query("UPDATE Employee e SET e.failedLoginAttempts = 0 WHERE e.email = :email")
int resetFailedLoginAttempts(@Param("email") String email);

@Modifying
@Query("UPDATE Employee e SET e.lastLoginAt = :timestamp WHERE e.employeeId = :employeeId")
int updateLastLogin(
    @Param("employeeId") String employeeId, 
    @Param("timestamp") LocalDateTime timestamp
);
```

#### 5. Native Queries
```java
@Query(value = "SELECT NEXT VALUE FOR employee_sequence", nativeQuery = true)
Long getNextSequenceNumber();

@Query(value = "SELECT * FROM employees WHERE employee_id = :employeeId FOR UPDATE", 
       nativeQuery = true)
Optional<Employee> findByEmployeeIdWithLock(@Param("employeeId") String employeeId);
```

#### 6. Specification Queries
```java
// Using JpaSpecificationExecutor for dynamic queries
default Page<Employee> findWithFilters(
    String department, 
    Role role, 
    Boolean isActive, 
    Pageable pageable
) {
    Specification<Employee> spec = Specification.where(null);
    
    if (department != null) {
        spec = spec.and((root, query, cb) -> 
            cb.equal(root.get("department"), department));
    }
    
    if (role != null) {
        spec = spec.and((root, query, cb) -> 
            cb.equal(root.get("role"), role));
    }
    
    if (isActive != null) {
        spec = spec.and((root, query, cb) -> 
            cb.equal(root.get("isActive"), isActive));
    }
    
    return findAll(spec, pageable);
}
```

---

## JwtService.java

**File Location**: `com/bank/employee/config/JwtService.java`

**Purpose**: JWT token management for employee authentication

### Implementation Details
```java
@Service
@Component
@Slf4j
public class JwtService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    
    public String generateToken(String employeeId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("employeeId", employeeId);
        claims.put("tokenType", "access");
        claims.put("issuer", "employee-service");
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(employeeId)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    public String extractEmployeeId(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("role");
    }
}
```

---

## JwtAuthenticationFilter.java

**File Location**: `com/bank/employee/config/JwtAuthenticationFilter.java`

### Implementation
```java
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtService jwtService;
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authorizationHeader = request.getHeader("Authorization");
        
        String employeeId = null;
        String jwt = null;
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                employeeId = jwtService.extractEmployeeId(jwt);
            } catch (Exception e) {
                log.error("Error extracting employee ID from JWT", e);
            }
        }
        
        if (employeeId != null && 
            SecurityContextHolder.getContext().getAuthentication() == null) {
            
            if (jwtService.validateToken(jwt)) {
                String role = jwtService.extractRole(jwt);
                
                List<SimpleGrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_" + role)
                );
                
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        employeeId, null, authorities
                    );
                
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/employees/login") || 
               path.startsWith("/h2-console") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }
}
```

---

## SecurityConfig.java

**File Location**: `com/bank/employee/config/SecurityConfig.java`

### Configuration
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/employees/login").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/employees").hasRole("MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasRole("MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("MANAGER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",
            "http://localhost:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

---

## DataSeeder.java

**File Location**: `com/bank/employee/config/DataSeeder.java`

### Implementation
```java
@Component
@Profile("!production")
@Slf4j
public class DataSeeder implements CommandLineRunner {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        if (employeeRepository.count() == 0) {
            log.info("Seeding employee data...");
            
            List<Employee> employees = Arrays.asList(
                Employee.builder()
                    .employeeId("EMP000001")
                    .firstName("Admin")
                    .lastName("Manager")
                    .email("admin@bank.com")
                    .password(passwordEncoder.encode("Admin123"))
                    .phone("+1-555-0001")
                    .department("Administration")
                    .designation("Senior Manager")
                    .role(Role.MANAGER)
                    .salary(new BigDecimal("100000.00"))
                    .joinDate(LocalDate.of(2020, 1, 1))
                    .isActive(true)
                    .build(),
                    
                Employee.builder()
                    .employeeId("EMP000002")
                    .firstName("John")
                    .lastName("Smith")
                    .email("john.smith@bank.com")
                    .password(passwordEncoder.encode("Password123"))
                    .phone("+1-555-0002")
                    .department("Customer Service")
                    .designation("Senior Executive")
                    .role(Role.EMPLOYEE)
                    .salary(new BigDecimal("60000.00"))
                    .joinDate(LocalDate.of(2021, 3, 15))
                    .reportingTo("EMP000001")
                    .isActive(true)
                    .build(),
                    
                Employee.builder()
                    .employeeId("EMP000003")
                    .firstName("Sarah")
                    .lastName("Johnson")
                    .email("sarah.johnson@bank.com")
                    .password(passwordEncoder.encode("Password123"))
                    .phone("+1-555-0003")
                    .department("Finance")
                    .designation("Manager")
                    .role(Role.MANAGER)
                    .salary(new BigDecimal("80000.00"))
                    .joinDate(LocalDate.of(2020, 6, 1))
                    .isActive(true)
                    .build()
            );
            
            employeeRepository.saveAll(employees);
            log.info("Seeded {} employees", employees.size());
        }
    }
}
```

---

## DTOs

### LoginRequest.java
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
```

### LoginResponse.java
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
    private long expiresIn = 86400;
    
    private String username;
    private String role;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String department;
    
    @JsonProperty("issued_at")
    private LocalDateTime issuedAt = LocalDateTime.now();
}
```

### ChangePasswordRequest.java
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    
    @NotBlank(message = "Current password is required")
    private String currentPassword;
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
             message = "Password must contain uppercase, lowercase and digit")
    private String newPassword;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
    
    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
```

---

## Exception Handling

### GlobalExceptionHandler.java
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
        ResourceNotFoundException ex, 
        WebRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Resource Not Found")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
            
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
        BadCredentialsException ex,
        WebRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Authentication Failed")
            .message("Invalid credentials")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
            
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
        DataIntegrityViolationException ex,
        WebRequest request
    ) {
        String message = "Data integrity violation";
        if (ex.getMessage().contains("email")) {
            message = "Email already exists";
        } else if (ex.getMessage().contains("employee_id")) {
            message = "Employee ID already exists";
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Data Conflict")
            .message(message)
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
            
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
        MethodArgumentNotValidException ex,
        WebRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid input parameters")
            .validationErrors(errors)
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
            
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
```

### ErrorResponse.java
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;
}
```

---

This completes the detailed documentation for the Employee Service with all components, methods, and implementations thoroughly documented.