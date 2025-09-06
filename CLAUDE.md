# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Bank Management System built using Spring Boot 3.5.5 with Java 17. It provides REST APIs for managing customers, employees, loans, and transactions in a banking environment.

## Technology Stack

- **Framework**: Spring Boot 3.5.5
- **Java Version**: 17
- **Database**: H2 (in-memory)
- **Security**: Spring Security
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven

## Development Commands

### Build and Run
```bash
# Run the application
./mvnw spring-boot:run

# Build the project
./mvnw clean compile

# Run tests
./mvnw test

# Package as JAR
./mvnw clean package
```

### Database Access
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:bmsdb`
  - Username: `sa`
  - Password: `password`

### API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html (when running)

## Architecture

The application follows a layered Spring Boot architecture:

```
src/main/java/com/bank/management/
├── config/          # Configuration classes (Security, Swagger, DataSeeder)
├── controller/      # REST controllers
├── dto/             # Data Transfer Objects organized by domain
│   ├── customer/
│   ├── employee/
│   ├── loan/
│   └── transaction/
├── exception/       # Custom exceptions and error handling
├── mapper/          # Entity-DTO mapping logic
├── model/           # JPA entities (Customer, Employee, Loan, Transaction)
├── repository/      # Spring Data JPA repositories
└── service/         # Business logic
    └── impl/        # Service implementations
```

## Key Models

- **Customer**: Bank customers with personal details and account information
- **Employee**: Bank employees with authentication capabilities
- **Loan**: Customer loan records
- **Transaction**: Financial transactions between accounts

## Configuration Notes

- Application runs on port 8080
- Database is H2 in-memory (data resets on restart)
- SQL logging is enabled in DEBUG mode
- Jackson is configured for ISO date format
- JPA uses `hibernate.ddl-auto: update`

## Security

The application uses Spring Security with custom authentication. Employee login is handled through the `/api/employees/login` endpoint.

## Testing

Run tests with: `./mvnw test`
Test files are located in `src/test/java/com/bank/management/`