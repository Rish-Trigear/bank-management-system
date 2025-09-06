# Bank Management System - API Documentation for Angular Frontend

## Overview
This document provides comprehensive API reference for implementing Angular frontend for the Bank Management System. The backend is built with Spring Boot 3.5.5 and runs on `http://localhost:8080`.

## Base Configuration
```typescript
// environment.ts
export const environment = {
  apiUrl: 'http://localhost:8080/api'
};
```

## Authentication
All endpoints except login/register require authentication. Use JWT token in Authorization header:
```typescript
headers: { 'Authorization': `Bearer ${token}` }
```

---

## 1. CUSTOMER MANAGEMENT APIs

### 1.1 Customer Registration
**Endpoint:** `POST /api/customers/register`
**Purpose:** Register new customer account
**Authentication:** None required

**Request Body:**
```typescript
interface CustomerRegistration {
  ssnId: string;           // Unique identifier
  name: string;            // Required
  email?: string;
  address?: string;
  contactNumber?: string;
  aadharNumber?: string;
  panNumber?: string;
  accountNumber?: string;
  dateOfBirth?: string;    // ISO format: "2024-01-15"
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  maritalStatus?: 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';
  password: string;        // Required for registration
}
```

**Response:** Customer object without password
**Status:** 201 CREATED

**Angular Implementation:**
```typescript
registerCustomer(customer: CustomerRegistration) {
  return this.http.post<Customer>(`${this.apiUrl}/customers/register`, customer);
}
```

### 1.2 Customer Login
**Endpoint:** `POST /api/customers/login`
**Purpose:** Authenticate customer
**Authentication:** None required

**Request Body:**
```typescript
interface LoginRequest {
  ssnId: string;    // Customer's SSN ID
  password: string;
}
```

**Response:**
```typescript
interface LoginResponse {
  token: string;
  message: string;
  role: string;     // "CUSTOMER"
  userId: string;   // Same as ssnId
  name: string;
}
```

**Angular Implementation:**
```typescript
loginCustomer(credentials: LoginRequest) {
  return this.http.post<LoginResponse>(`${this.apiUrl}/customers/login`, credentials);
}
```

### 1.3 Get All Customers
**Endpoint:** `GET /api/customers`
**Purpose:** Retrieve all customers (Admin/Employee use)
**Authentication:** Required

**Response:** Array of Customer objects

**Angular Implementation:**
```typescript
getAllCustomers() {
  return this.http.get<Customer[]>(`${this.apiUrl}/customers`);
}
```

### 1.4 Get Customer by SSN
**Endpoint:** `GET /api/customers/{ssnId}`
**Purpose:** Get specific customer details
**Authentication:** Required

**Angular Implementation:**
```typescript
getCustomerBySsn(ssnId: string) {
  return this.http.get<Customer>(`${this.apiUrl}/customers/${ssnId}`);
}
```

### 1.5 Update Customer
**Endpoint:** `PUT /api/customers/{ssnId}`
**Purpose:** Update customer information
**Authentication:** Required

**Request Body:** Customer object (same as registration)

**Angular Implementation:**
```typescript
updateCustomer(ssnId: string, customer: Customer) {
  return this.http.put<Customer>(`${this.apiUrl}/customers/${ssnId}`, customer);
}
```

### 1.6 Delete Customer
**Endpoint:** `DELETE /api/customers/{ssnId}`
**Purpose:** Delete customer account
**Authentication:** Required

**Response:** Success message string

---

## 2. EMPLOYEE MANAGEMENT APIs

### 2.1 Employee Registration
**Endpoint:** `POST /api/employees/register`
**Purpose:** Register new employee
**Authentication:** Required (Admin only)

**Request Body:**
```typescript
interface EmployeeRegistration {
  employeeId: string;      // Unique identifier
  firstName: string;       // Required
  lastName?: string;
  email: string;          // Required, unique
  address?: string;
  contactNumber?: string;
  password: string;       // Required for registration
  role: string  // EMPLOYEE / MANAGER
}
```

### 2.2 Employee Login
**Endpoint:** `POST /api/employees/login`
**Purpose:** Authenticate employee
**Authentication:** None required

**Request Body:**
```typescript
interface LoginRequest {
  ssnId: string;    // Actually email for employees
  password: string;
}
```

**Note:** For employees, use email in the `ssnId` field for login.

### 2.3 Other Employee Endpoints
- `GET /api/employees` - Get all employees
- `GET /api/employees/{employeeId}` - Get employee by ID
- `PUT /api/employees/{employeeId}` - Update employee
- `DELETE /api/employees/{employeeId}` - Delete employee

---

## 3. TRANSACTION MANAGEMENT APIs

### 3.1 Create Transaction
**Endpoint:** `POST /api/transactions/customer/{customerSsnId}`
**Purpose:** Create new transaction for customer
**Authentication:** Required

**Request Body:**
```typescript
interface TransactionRequest {
  transactionId?: string;         // Auto-generated if not provided
  accountId: string;
  modeOfTransaction: string;      // e.g., "ONLINE", "ATM", "BRANCH"
  amount: number;                 // Must be positive
  type: 'CREDIT' | 'DEBIT';
}
```

**Angular Implementation:**
```typescript
createTransaction(customerSsnId: string, transaction: TransactionRequest) {
  return this.http.post<Transaction>(`${this.apiUrl}/transactions/customer/${customerSsnId}`, transaction);
}
```

### 3.2 Get All Transactions
**Endpoint:** `GET /api/transactions`
**Purpose:** Get all transactions (Admin/Employee view)
**Authentication:** Required

### 3.3 Get Transaction by ID
**Endpoint:** `GET /api/transactions/{transactionId}`
**Purpose:** Get specific transaction
**Authentication:** Required

### 3.4 Get Customer Transactions
**Endpoint:** `GET /api/transactions/customer/{customerSsnId}`
**Purpose:** Get all transactions for specific customer
**Authentication:** Required

**Angular Implementation:**
```typescript
getCustomerTransactions(customerSsnId: string) {
  return this.http.get<Transaction[]>(`${this.apiUrl}/transactions/customer/${customerSsnId}`);
}
```

### 3.5 Update/Delete Transaction
- `PUT /api/transactions/{transactionId}` - Update transaction
- `DELETE /api/transactions/{transactionId}` - Delete transaction

---

## 4. LOAN MANAGEMENT APIs

### 4.1 Create Loan Application
**Endpoint:** `POST /api/loans/customer/{customerSsnId}`
**Purpose:** Submit loan application for customer
**Authentication:** Required

**Request Body:**
```typescript
interface LoanApplication {
  loanId?: string;                    // Auto-generated if not provided
  occupation: string;
  employerName: string;
  employerAddress: string;
  email: string;                      // Must be valid email format
  address: string;
  maritalStatus: 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';
  contactNumber: string;              // Must be 10 digits
  loanAmount: number;                 // Must be positive
  durationMonths: number;             // Must be positive
}
```

**Angular Implementation:**
```typescript
createLoanApplication(customerSsnId: string, loan: LoanApplication) {
  return this.http.post<Loan>(`${this.apiUrl}/loans/customer/${customerSsnId}`, loan);
}
```

### 4.2 Other Loan Endpoints
- `GET /api/loans` - Get all loan applications
- `GET /api/loans/{loanId}` - Get loan by ID
- `GET /api/loans/customer/{customerSsnId}` - Get customer's loan applications
- `PUT /api/loans/{loanId}` - Update loan (employee processing)
- `DELETE /api/loans/{loanId}` - Delete loan application

---

## 5. DASHBOARD API

### 5.1 Get Dashboard Summary
**Endpoint:** `GET /api/dashboard/summary`
**Purpose:** Get system statistics for admin dashboard
**Authentication:** Required (Employee/Admin)

**Response:**
```typescript
interface DashboardResponse {
  totalCustomers: number;
  totalEmployees: number;
  totalTransactions: number;
  totalLoans: number;
}
```

**Angular Implementation:**
```typescript
getDashboardSummary() {
  return this.http.get<DashboardResponse>(`${this.apiUrl}/dashboard/summary`);
}
```

---

## 6. DATA MODELS

### Customer Model
```typescript
interface Customer {
  id?: number;
  ssnId: string;
  name: string;
  email?: string;
  address?: string;
  contactNumber?: string;
  aadharNumber?: string;
  panNumber?: string;
  accountNumber?: string;
  dateOfBirth?: string;           // ISO format
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  maritalStatus?: 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';
  role: 'CUSTOMER';
  // password field excluded from responses
}
```

### Employee Model
```typescript
interface Employee {
  id?: number;
  employeeId: string;
  firstName: string;
  lastName?: string;
  email: string;
  address?: string;
  contactNumber?: string;
  role: 'EMPLOYEE' | 'MANAGER';
  // passwordHash excluded from responses
}
```

### Transaction Model
```typescript
interface Transaction {
  id?: number;
  transactionId: string;
  customer?: Customer;            // Populated in responses
  accountId: string;
  date?: string;                  // ISO datetime format
  modeOfTransaction: string;
  amount: number;                 // BigDecimal in Java
  type: 'CREDIT' | 'DEBIT';
}
```

### Loan Model
```typescript
interface Loan {
  id?: number;
  loanId: string;
  customer?: Customer;            // Populated in responses
  occupation: string;
  employerName: string;
  employerAddress: string;
  email: string;
  address: string;
  maritalStatus: 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';
  contactNumber: string;          // 10 digits
  loanAmount: number;             // BigDecimal in Java
  durationMonths: number;
}
```

---

## 7. ANGULAR IMPLEMENTATION PHASES

### Phase 1: Project Setup & Authentication
1. Create Angular project with routing and HttpClient
2. Set up environment configuration
3. Create authentication service with JWT handling
4. Implement login/logout functionality
5. Create auth guard for protected routes

### Phase 2: Customer Management
1. Create customer registration form with validations
2. Implement customer login
3. Create customer profile view/edit
4. Add customer listing (admin view)

### Phase 3: Transaction Management  
1. Create transaction form for CREDIT/DEBIT
2. Implement transaction history view
3. Add transaction filtering and search
4. Create transaction details view

### Phase 4: Loan Management
1. Create loan application form with validations
2. Implement loan status tracking
3. Add loan approval workflow (employee view)
4. Create loan history and details

### Phase 5: Dashboard & Analytics
1. Implement admin dashboard with statistics
2. Create charts and graphs for data visualization
3. Add reporting features
4. Implement search and filtering across modules

### Phase 6: UI/UX & Polish
1. Add responsive design
2. Implement loading states and error handling
3. Add notifications and alerts
4. Create print/export functionality

---

## 8. VALIDATION RULES

### Customer Registration
- ssnId: Required, unique
- name: Required
- email: Valid email format
- contactNumber: 10 digits
- dateOfBirth: Valid date, not future
- password: Minimum 8 characters

### Transaction Creation
- amount: Must be positive number
- accountId: Required
- modeOfTransaction: Required
- type: Must be CREDIT or DEBIT

### Loan Application
- email: Valid email format
- contactNumber: Exactly 10 digits
- loanAmount: Must be positive
- durationMonths: Must be positive integer

---

## 9. ERROR HANDLING

### Common HTTP Status Codes
- 200: Success
- 201: Created
- 400: Bad Request (validation errors)
- 401: Unauthorized
- 403: Forbidden
- 404: Not Found
- 500: Internal Server Error

### Angular Error Service Example
```typescript
handleError(error: HttpErrorResponse) {
  let errorMessage = 'An error occurred';
  if (error.error instanceof ErrorEvent) {
    errorMessage = error.error.message;
  } else {
    errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
  }
  return throwError(errorMessage);
}
```

---

## 10. SECURITY CONSIDERATIONS

1. Store JWT token securely (localStorage/sessionStorage)
2. Implement token refresh mechanism
3. Add CSRF protection for forms
4. Validate all inputs on frontend
5. Use HTTPS in production
6. Implement proper logout (clear tokens)
7. Add request/response interceptors for token handling

This documentation provides everything needed to implement a complete Angular frontend for the Bank Management System.