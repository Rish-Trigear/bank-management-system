# Microservice Architecture Plan

## Overview
Converting the Bank Management System monolith into 4 independent microservices + 1 API Gateway, maintaining exact functionality.

## 🚀 **Implementation Progress**

- ✅ **Customer Service** - COMPLETED ✅
  - Location: `microservices/customer-service/`
  - Port: 8081 | Database: `customer_db`
  - Status: Fully functional and tested
  
- ✅ **Employee Service** - COMPLETED ✅
  - Location: `microservices/employee-service/`
  - Port: 8082 | Database: `employee_db`
  - Status: Fully functional and tested
  
- ✅ **Transaction Service** - COMPLETED ✅
  - Location: `microservices/transaction-service/`
  - Port: 8083 | Database: `transaction_db`
  - Status: Fully functional with inter-service communication
  
- ⏳ **Loan Service** - NEXT
  - Location: `microservices/loan-service/` (planned)
  - Port: 8084 | Database: `loan_db`
  
- ⏳ **API Gateway** - PENDING
  - Location: `microservices/api-gateway/` (planned)
  - Port: 8080 | Routes to all services

## Service Architecture

```
┌─────────────────┐
│   API Gateway   │ ← Routes requests, JWT validation (Port: 8080)
└─────────────────┘
         │
    ┌────┼────────────────┬─────────────────┐
    │    │                │                 │
┌───▼────┐   ┌───▼─────┐   ┌───▼──────┐   ┌────▼────┐
│Customer│   │Employee │   │Transaction│   │  Loan   │
│Service │   │Service  │   │ Service   │   │ Service │
│:8081   │   │:8082    │   │  :8083    │   │ :8084   │
└────────┘   └─────────┘   └───────────┘   └─────────┘
```

## Service Breakdown

### 1. Customer Service (Port: 8081)
**Package:** `com.bank.customer`
**Database:** H2 (customer_db)

**Responsibilities:**
- Customer registration & authentication
- Customer profile CRUD operations
- JWT token generation for customers

**Components to Extract:**
- `model/Customer.java`
- `controller/CustomerController.java`
- `service/CustomerService.java`
- `repository/CustomerRepository.java`
- `dto/LoginRequest.java`, `dto/LoginResponse.java`
- JWT and Security configuration

**API Endpoints:**
```
POST /customers/register
POST /customers/login
GET /customers/{ssnId}
PUT /customers/{ssnId}
DELETE /customers/{ssnId}
GET /customers (for admin)
GET /customers/count (for dashboard)
```

**Database Schema:**
```sql
customers (
    id BIGINT PRIMARY KEY,
    ssn_id VARCHAR(255) UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    address TEXT,
    contact_number VARCHAR(15),
    aadhar_number VARCHAR(12),
    pan_number VARCHAR(10),
    account_number VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    marital_status VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'CUSTOMER'
);
```

---

### 2. Employee Service (Port: 8082)
**Package:** `com.bank.employee`
**Database:** H2 (employee_db)

**Responsibilities:**
- Employee management
- Employee authentication
- JWT token generation for employees

**Components to Extract:**
- `model/Employee.java`
- `controller/EmployeeController.java`
- `service/EmployeeService.java`
- `repository/EmployeeRepository.java`
- JWT and Security configuration

**API Endpoints:**
```
POST /employees/register
POST /employees/login
GET /employees/{employeeId}
PUT /employees/{employeeId}
DELETE /employees/{employeeId}
GET /employees
GET /employees/count
```

**Database Schema:**
```sql
employees (
    id BIGINT PRIMARY KEY,
    employee_id VARCHAR(255) UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    address TEXT,
    contact_number VARCHAR(15),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20)
);
```

---

### 3. Transaction Service (Port: 8083)
**Package:** `com.bank.transaction`
**Database:** H2 (transaction_db)

**Responsibilities:**
- Transaction processing
- Transaction history management
- Customer validation via REST calls

**Components to Extract:**
- `model/Transaction.java` (modified - remove Customer relationship)
- `controller/TransactionController.java`
- `service/TransactionService.java`
- `repository/TransactionRepository.java`

**Key Changes:**
- Replace `Customer customer` field with `String customerSsnId`
- Add REST client to validate customer existence
- Remove JPA relationship annotations

**API Endpoints:**
```
POST /transactions/customer/{customerSsnId}
GET /transactions/{transactionId}
GET /transactions/customer/{customerSsnId}
PUT /transactions/{transactionId}
DELETE /transactions/{transactionId}
GET /transactions
GET /transactions/count
```

**Database Schema:**
```sql
transactions (
    id BIGINT PRIMARY KEY,
    transaction_id VARCHAR(255) UNIQUE,
    customer_ssn_id VARCHAR(255) NOT NULL,
    account_id VARCHAR(255),
    transaction_date TIMESTAMP,
    mode_of_transaction VARCHAR(50),
    amount DECIMAL(15,2),
    type VARCHAR(10)
);
```

---

### 4. Loan Service (Port: 8084)
**Package:** `com.bank.loan`
**Database:** H2 (loan_db)

**Responsibilities:**
- Loan application processing
- Loan status management
- Customer validation via REST calls

**Components to Extract:**
- `model/Loan.java` (modified - remove Customer relationship)
- `controller/LoanController.java`
- `service/LoanService.java`
- `repository/LoanRepository.java`

**Key Changes:**
- Replace `Customer customer` field with `String customerSsnId`
- Add REST client to validate customer existence

**API Endpoints:**
```
POST /loans/customer/{customerSsnId}
GET /loans/{loanId}
GET /loans/customer/{customerSsnId}
PUT /loans/{loanId}
DELETE /loans/{loanId}
GET /loans
GET /loans/count
```

**Database Schema:**
```sql
loans (
    id BIGINT PRIMARY KEY,
    loan_id VARCHAR(255) UNIQUE,
    customer_ssn_id VARCHAR(255) NOT NULL,
    occupation VARCHAR(255),
    employer_name VARCHAR(255),
    employer_address TEXT,
    email VARCHAR(255),
    address TEXT,
    marital_status VARCHAR(20),
    contact_number VARCHAR(15),
    loan_amount DECIMAL(15,2),
    duration_months INTEGER
);
```

---

### 5. API Gateway (Port: 8080)
**Package:** `com.bank.gateway`

**Responsibilities:**
- Route requests to appropriate services
- JWT token validation
- CORS configuration
- Request/Response forwarding

**Configuration Example:**
```properties
# Service URLs
customer.service.url=http://localhost:8081
employee.service.url=http://localhost:8082
transaction.service.url=http://localhost:8083
loan.service.url=http://localhost:8084
```

**Routing Logic:**
- `/api/customers/**` → Customer Service
- `/api/employees/**` → Employee Service  
- `/api/transactions/**` → Transaction Service
- `/api/loans/**` → Loan Service

---

## Inter-Service Communication

### 🔑 **CRITICAL Configuration Requirements**

**1. Shared JWT Secret (MANDATORY)**
All services MUST use the same JWT secret for cross-service authentication:

```yaml
# Required in ALL service application.yml/properties
jwt:
  secret: bank-microservices-shared-jwt-secret-2024
  expiration: 86400000
```

**2. Authenticated Service-to-Service Calls**
Services making REST calls to other services MUST include JWT tokens:

```java
@Service
public class CustomerValidationService {
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private JwtService jwtService;
    
    public boolean customerExists(String ssnId) {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<CustomerDto> response = restTemplate.exchange(
                customerServiceUrl + "/customers/" + ssnId,
                HttpMethod.GET, entity, CustomerDto.class);
            
            return response.getBody() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String serviceToken = jwtService.generateToken(auth.getName());
            headers.set("Authorization", "Bearer " + serviceToken);
        }
        return headers;
    }
}
```

### REST Template Configuration
```java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### Service Discovery Configuration
```yaml
# Service URLs for inter-service communication
services:
  customer:
    url: http://localhost:8081
  employee:
    url: http://localhost:8082
  transaction:
    url: http://localhost:8083
  loan:
    url: http://localhost:8084
```

### Cross-Service Authentication Flow
1. **Customer/Employee** authenticates with their respective service
2. **JWT token** is generated with shared secret
3. **Token is valid** across all services (customer-service, employee-service, transaction-service, loan-service)
4. **Inter-service calls** use current user's context to generate service tokens
5. **All services** can validate tokens from any other service

---

## Implementation Steps

### Phase 1: Customer Service
1. Create new Spring Boot project `customer-service`
2. Copy Customer domain components
3. Configure H2 database (port 8081)
4. Add JWT security configuration
5. Test customer registration/login independently

### Phase 2: Employee Service
1. Create new Spring Boot project `employee-service`
2. Copy Employee domain components
3. Configure H2 database (port 8082)
4. Add JWT security configuration
5. Test employee functionality independently

### Phase 3: Transaction Service
1. Create new Spring Boot project `transaction-service`
2. Copy and modify Transaction components
3. Remove Customer entity relationship
4. Add customer validation via REST
5. Configure H2 database (port 8083)

### Phase 4: Loan Service
1. Create new Spring Boot project `loan-service`
2. Copy and modify Loan components
3. Remove Customer entity relationship
4. Add customer validation via REST
5. Configure H2 database (port 8084)

### Phase 5: API Gateway
1. Create Spring Boot project with routing
2. Configure service URLs
3. Add JWT validation
4. Test end-to-end functionality

---

## Configuration Files

### Service Application Properties Template
```properties
server.port=808X
spring.application.name=service-name
spring.datasource.url=jdbc:h2:mem:service_db
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=update

# JWT Configuration - MUST BE SAME ACROSS ALL SERVICES
jwt.secret=bank-microservices-shared-jwt-secret-2024
jwt.expiration=86400000

# Other service URLs
customer.service.url=http://localhost:8081
employee.service.url=http://localhost:8082
transaction.service.url=http://localhost:8083
loan.service.url=http://localhost:8084
```

### Maven Dependencies (Common)
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
</dependencies>
```

---

## Benefits

1. **Independent Development** - Teams can work on different services
2. **Independent Deployment** - Deploy services separately  
3. **Scalability** - Scale high-usage services independently
4. **Technology Flexibility** - Use different tech stacks per service
5. **Fault Isolation** - Service failures don't affect others

## Trade-offs

1. **Network Calls** - Inter-service communication adds latency
2. **Data Consistency** - No ACID transactions across services
3. **Testing Complexity** - Need multiple services for integration tests
4. **Operational Overhead** - More services to monitor and manage

---

## 🧪 **Implementation Results & Testing**

### Current Working Services (as of latest implementation)

**Customer Service** ✅
- **Port**: 8081 
- **Database**: H2 (`customer_db`)
- **Test Users**: 
  - `1001001` / `password123` (John Doe)
  - `1001002` / `password123` (Jane Smith)
- **Working Endpoints**: All CRUD operations, login/register

**Employee Service** ✅  
- **Port**: 8082
- **Database**: H2 (`employee_db`) 
- **Test Users**:
  - `john.smith@bank.com` / `password123` (Employee)
  - `sarah.johnson@bank.com` / `password123` (Manager)
- **Working Endpoints**: All CRUD operations, login/register

**Transaction Service** ✅
- **Port**: 8083
- **Database**: H2 (`transaction_db`)
- **Inter-Service**: Successfully validates customers via Customer Service
- **Cross-Auth**: Both customer and employee tokens work
- **Test Results**:
  - ✅ Customer can view transactions
  - ✅ Employee can create transactions for customers
  - ✅ Customer validation working across services
  - ✅ JWT authentication working across all services

### Key Implementation Discoveries

1. **Shared JWT Secret is CRITICAL**
   - All services must use identical JWT secret: `bank-microservices-shared-jwt-secret-2024`
   - Without this, cross-service authentication fails

2. **Inter-Service Authentication Required**
   - Services making REST calls to other services need JWT tokens
   - Cannot use unauthenticated calls between secured services
   - Solution: Pass through current user's authentication context

3. **Service-to-Service Call Pattern**
```java
// WRONG - Will get 403 Forbidden
restTemplate.getForObject(url, CustomerDto.class);

// CORRECT - Include JWT token
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Bearer " + jwtService.generateToken(currentUser));
HttpEntity<String> entity = new HttpEntity<>(headers);
restTemplate.exchange(url, HttpMethod.GET, entity, CustomerDto.class);
```

4. **Employee-Customer Workflow**
   - Employees can perform transactions on behalf of customers
   - Employee JWT token is accepted by transaction service
   - Customer validation works through inter-service calls

### Running All Services

```bash
# Start all three services
cd microservices/customer-service && ./mvnw spring-boot:run &     # Port 8081
cd microservices/employee-service && ./mvnw spring-boot:run &     # Port 8082  
cd microservices/transaction-service && ./mvnw spring-boot:run &  # Port 8083

# Test customer login and transaction access
curl -X POST http://localhost:8081/customers/login \
  -H "Content-Type: application/json" \
  -d '{"ssnId": "1001001", "password": "password123"}'

# Use returned token for transaction service
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8083/transactions/count
```

---

This architecture maintains exact functionality while splitting into manageable, independent services. **Critical lesson**: Inter-service authentication requires shared JWT secrets and proper token passing between services.