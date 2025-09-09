# Bank Management System - Microservices Architecture

A modern, scalable bank management system built with Spring Boot microservices architecture, featuring customer management, employee operations, transactions, and loan processing.

## Architecture Overview

This system follows a microservices architecture pattern with the following services:

- **API Gateway** - Central entry point for all client requests (Port 8080)
- **Customer Service** - Manages customer accounts and authentication (Port 8081)
- **Employee Service** - Handles employee operations and management (Port 8082)
- **Transaction Service** - Processes all banking transactions (Port 8083)
- **Loan Service** - Manages loan applications and processing (Port 8084)
- **Discovery Service** - Service registry using Netflix Eureka (Port 8761)

## Tech Stack

- **Backend Framework**: Spring Boot 3.5.5
- **Language**: Java 17
- **Database**: H2 (In-memory for development)
- **Service Discovery**: Netflix Eureka
- **Security**: JWT-based authentication with Spring Security
- **API Documentation**: OpenAPI 3.0 with Swagger UI
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/yourusername/bank-management-system.git
cd bank-management-system
```

### Running the Microservices

Services should be started in the following order:

1. **Start Discovery Service** (Eureka Server)
```bash
cd microservices/discovery-service
./mvnw spring-boot:run
```
Access Eureka Dashboard: http://localhost:8761

2. **Start Customer Service**
```bash
cd microservices/customer-service
./mvnw spring-boot:run
```

3. **Start Employee Service**
```bash
cd microservices/employee-service
./mvnw spring-boot:run
```

4. **Start Transaction Service**
```bash
cd microservices/transaction-service
./mvnw spring-boot:run
```

5. **Start Loan Service**
```bash
cd microservices/loan-service
./mvnw spring-boot:run
```

6. **Start API Gateway** (Last)
```bash
cd microservices/api-gateway
./mvnw spring-boot:run
```

## API Documentation

Once the services are running, access the Swagger UI for API documentation:

- **API Gateway**: http://localhost:8080/swagger-ui/index.html

### Key API Endpoints

#### Customer Operations
- `POST /api/customers/register` - Register new customer
- `POST /api/customers/login` - Customer login
- `GET /api/customers/{ssnId}` - Get customer details
- `PUT /api/customers/{ssnId}` - Update customer information
- `DELETE /api/customers/{ssnId}` - Delete customer account

#### Employee Operations
- `POST /api/employees/login` - Employee login
- `GET /api/employees` - List all employees
- `POST /api/employees` - Create new employee
- `PUT /api/employees/{employeeId}` - Update employee
- `DELETE /api/employees/{employeeId}` - Delete employee

#### Transaction Operations
- `POST /api/transactions/deposit` - Deposit money
- `POST /api/transactions/withdraw` - Withdraw money
- `POST /api/transactions/transfer` - Transfer between accounts
- `GET /api/transactions/customer/{ssnId}` - Get customer transactions
- `GET /api/transactions/{transactionId}` - Get transaction details

#### Loan Operations
- `POST /api/loans/apply` - Apply for a loan
- `GET /api/loans/customer/{ssnId}` - Get customer loans
- `PUT /api/loans/{loanId}/approve` - Approve loan
- `PUT /api/loans/{loanId}/reject` - Reject loan

## Default Test Credentials

### Customer Login
- **SSN ID**: 123456789
- **Password**: password123

### Employee Login
- **Email**: john.doe@bank.com
- **Password**: password123

### Manager Login
- **Email**: sarah.admin@bank.com
- **Password**: admin123

## Database Access

Each service has its own H2 in-memory database. Access the H2 console:

- Customer Service: http://localhost:8081/h2-console
- Employee Service: http://localhost:8082/h2-console
- Transaction Service: http://localhost:8083/h2-console
- Loan Service: http://localhost:8084/h2-console

**Database Credentials:**
- JDBC URL: `jdbc:h2:mem:[service]db` (e.g., `jdbc:h2:mem:customerdb`)
- Username: `sa`
- Password: `password`

## Security

The system implements JWT-based authentication:
- JWT tokens are required for most endpoints
- Tokens expire after 24 hours
- Role-based access control (CUSTOMER, EMPLOYEE, MANAGER)

## Project Structure

```
bank-management-system/
├── microservices/
│   ├── api-gateway/          # API Gateway service
│   ├── customer-service/     # Customer management
│   ├── employee-service/     # Employee management
│   ├── transaction-service/  # Transaction processing
│   ├── loan-service/        # Loan management
│   └── discovery-service/    # Eureka service registry
├── MICROSERVICE_ARCHITECTURE.md  # Detailed architecture documentation
├── BANK_MANAGEMENT_API_DOCUMENTATION.md  # API specifications
└── README.md                 # This file
```

## Building for Production

To build all services:

```bash
# Build each service
cd microservices/[service-name]
./mvnw clean package
```

The JAR files will be created in each service's `target/` directory.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Contact

For questions or support, please open an issue in the GitHub repository.