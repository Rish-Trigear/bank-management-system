# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Run
- `./mvnw clean compile` - Compile the application
- `./mvnw spring-boot:run` - Start the development server (runs on port 8080)
- `./mvnw clean package` - Build JAR package
- `java -jar target/bank-management-system-0.0.1-SNAPSHOT.jar` - Run the packaged JAR

### Testing
- `./mvnw test` - Run all unit tests
- `./mvnw test -Dtest=ClassName` - Run specific test class
- `./mvnw test -Dtest=ClassName#methodName` - Run specific test method

### Database Access
- H2 Console: http://localhost:8080/h2-console (when app is running)
  - JDBC URL: `jdbc:h2:mem:bmsdb`
  - Username: `sa`
  - Password: `password`

### API Documentation
- Swagger UI: http://localhost:8080/swagger-ui/index.html (when app is running)
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Project Architecture

### Core Stack
- **Framework**: Spring Boot 3.5.5 with Java 17
- **Database**: H2 in-memory database (development)
- **Security**: JWT-based authentication with Spring Security
- **Documentation**: OpenAPI 3 with Swagger UI
- **Build Tool**: Maven with wrapper

### Package Structure
```
com.bank.management/
├── config/          # Configuration classes (Security, JWT, CORS, Swagger)
├── controller/      # REST API endpoints
├── dto/            # Data Transfer Objects (LoginRequest, LoginResponse, etc.)
├── exception/      # Global exception handling
├── model/          # JPA entities (Customer, Employee, Transaction, Loan)
├── repository/     # Data access layer interfaces
└── service/        # Business logic layer
```

### Authentication Flow
- JWT tokens are required for most endpoints
- Public endpoints: `/api/customers/register`, `/api/customers/login`, `/api/employees/login`
- Customers authenticate with `ssnId` + password
- Employees authenticate with `email` + password
- CORS configured for Angular frontend at `http://localhost:4200`

### Key Entities and Relationships
- **Customer**: Primary entity with ssnId as unique identifier, has transactions and loans
- **Employee**: Staff members with employeeId, can be EMPLOYEE or MANAGER role
- **Transaction**: CREDIT/DEBIT operations linked to customers via ssnId
- **Loan**: Loan applications associated with customers via ssnId

### Database Configuration
- Uses H2 in-memory database that resets on restart
- JPA auto-creates tables with `hibernate.ddl-auto: update`
- DataSeeder class populates initial data on startup
- SQL logging enabled in development for debugging

### API Structure
All APIs are prefixed with `/api` and follow RESTful conventions:
- `/api/customers/*` - Customer management (registration, profile, CRUD)
- `/api/employees/*` - Employee management (requires authentication)
- `/api/transactions/*` - Transaction operations (customer-specific endpoints)
- `/api/loans/*` - Loan application management
- `/api/dashboard/summary` - System statistics for admin dashboard

### Security Features
- BCrypt password encoding
- JWT token-based stateless authentication
- Role-based access control (CUSTOMER, EMPLOYEE, MANAGER)
- CORS enabled for frontend integration
- H2 console and Swagger UI accessible in development

### Data Seeding
The DataSeeder component automatically creates sample data on application startup, including test customers, employees, and transactions for development purposes.