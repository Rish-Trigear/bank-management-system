# Bank Management System Microservices

This folder contains all the microservices extracted from the original monolithic Bank Management System.

## Services Structure

```
microservices/
├── customer-service/     # Customer management (Port: 8081)
├── employee-service/     # Employee management (Port: 8082)
├── transaction-service/  # Transaction management (Port: 8083)
├── loan-service/         # Loan management (Port: 8084)
└── api-gateway/          # API Gateway & routing (Port: 8080)
```

## Running Services

Each service can be run independently:

```bash
# Customer Service
cd customer-service
./mvnw spring-boot:run

# Employee Service
cd employee-service
./mvnw spring-boot:run

# Transaction Service
cd transaction-service
./mvnw spring-boot:run

# Loan Service
cd loan-service
./mvnw spring-boot:run

# API Gateway
cd api-gateway
./mvnw spring-boot:run
```

## Service Status

- ✅ **Customer Service** - Complete (Port: 8081)
- ⏳ **Employee Service** - In progress (Port: 8082)
- ⏳ **Transaction Service** - Pending (Port: 8083)
- ⏳ **Loan Service** - Pending (Port: 8084)
- ⏳ **API Gateway** - Pending (Port: 8080)

## Service Dependencies

- Customer Service: Independent
- Employee Service: Independent
- Transaction Service: Depends on Customer Service (for validation)
- Loan Service: Depends on Customer Service (for validation)
- API Gateway: Routes to all services

## Databases

Each service has its own H2 database:
- Customer Service: `customer_db`
- Employee Service: `employee_db`
- Transaction Service: `transaction_db`
- Loan Service: `loan_db`

## API Documentation

Each service exposes its endpoints:
- Customer Service: http://localhost:8081/h2-console
- Employee Service: http://localhost:8082/h2-console
- Transaction Service: http://localhost:8083/h2-console
- Loan Service: http://localhost:8084/h2-console
- API Gateway: http://localhost:8080 (aggregates all services)