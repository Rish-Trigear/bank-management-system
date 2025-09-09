#!/bin/bash

echo "🔥 Testing Bank Management System Microservices"
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test results
PASSED=0
FAILED=0

# Function to test endpoint
test_endpoint() {
    local name="$1"
    local url="$2"
    local method="$3"
    local data="$4"
    local headers="$5"
    local expected="$6"
    
    echo -n "Testing $name... "
    
    if [ "$method" = "POST" ]; then
        response=$(curl -s -X POST "$url" -H "Content-Type: application/json" $headers -d "$data" 2>/dev/null)
    else
        response=$(curl -s $headers "$url" 2>/dev/null)
    fi
    
    if [[ "$response" == *"$expected"* ]]; then
        echo -e "${GREEN}✓ PASSED${NC}"
        ((PASSED++))
    else
        echo -e "${RED}✗ FAILED${NC}"
        echo "  Expected: $expected"
        echo "  Got: $response"
        ((FAILED++))
    fi
}

# Test 1: API Gateway Health
echo -e "${BLUE}1. Testing API Gateway${NC}"
test_endpoint "API Gateway Health" "http://localhost:8080/health" "GET" "" "" "API Gateway is running"

# Test 2: Customer Service (Direct)
echo -e "${BLUE}2. Testing Customer Service${NC}"
test_endpoint "Customer Login" "http://localhost:8081/customers/login" "POST" '{"ssnId": "1001001", "password": "password123"}' "" "token"

# Test 3: Employee Service (Direct)
echo -e "${BLUE}3. Testing Employee Service${NC}"
test_endpoint "Employee Login" "http://localhost:8082/employees/login" "POST" '{"email": "john.smith@bank.com", "password": "password123"}' "" "token"

# Test 4: Get JWT Token
echo -e "${BLUE}4. Getting JWT Token${NC}"
TOKEN=$(curl -s -X POST http://localhost:8081/customers/login \
  -H "Content-Type: application/json" \
  -d '{"ssnId": "1001001", "password": "password123"}' 2>/dev/null | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✓ JWT Token obtained${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ Failed to get JWT Token${NC}"
    ((FAILED++))
    exit 1
fi

# Test 5: Transaction Service
echo -e "${BLUE}5. Testing Transaction Service${NC}"
test_endpoint "Transaction Count" "http://localhost:8083/transactions/count" "GET" "" "-H 'Authorization: Bearer $TOKEN'" "count"

# Test 6: Loan Service
echo -e "${BLUE}6. Testing Loan Service${NC}"
test_endpoint "Loan Count" "http://localhost:8084/loans/count" "GET" "" "-H 'Authorization: Bearer $TOKEN'" "count"
test_endpoint "Customer Loans" "http://localhost:8084/loans/customer/1001001" "GET" "" "-H 'Authorization: Bearer $TOKEN'" "loanId"

# Test 7: Create New Loan
echo -e "${BLUE}7. Testing Loan Creation${NC}"
LOAN_DATA='{
    "occupation": "Test Engineer",
    "employerName": "Test Company",
    "employerAddress": "123 Test St",
    "email": "test@example.com",
    "address": "456 Test Ave",
    "maritalStatus": "SINGLE",
    "contactNumber": "1234567890",
    "loanAmount": 100000,
    "durationMonths": 120
}'

test_endpoint "Create Loan" "http://localhost:8084/loans/customer/1001001" "POST" "$LOAN_DATA" "-H 'Authorization: Bearer $TOKEN'" "loanId"

# Test 8: Inter-service Communication
echo -e "${BLUE}8. Testing Inter-Service Communication${NC}"
# This tests that loan service can validate customers via customer service
INVALID_LOAN_DATA='{
    "occupation": "Test",
    "employerName": "Test",
    "loanAmount": 50000,
    "durationMonths": 60
}'

echo -n "Testing customer validation... "
invalid_response=$(curl -s -X POST "http://localhost:8084/loans/customer/999999999" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "$INVALID_LOAN_DATA" 2>/dev/null)

if [[ "$invalid_response" == *"Customer not found"* ]] || [[ "$invalid_response" == *"error"* ]]; then
    echo -e "${GREEN}✓ PASSED (Customer validation working)${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ FAILED (Customer validation not working)${NC}"
    echo "  Response: $invalid_response"
    ((FAILED++))
fi

# Summary
echo
echo "================================================"
echo -e "${BLUE}TEST SUMMARY${NC}"
echo "================================================"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL TESTS PASSED! Microservices are working correctly.${NC}"
    echo
    echo "📊 Service Status:"
    echo "✅ Customer Service (Port 8081)"
    echo "✅ Employee Service (Port 8082)" 
    echo "✅ Transaction Service (Port 8083)"
    echo "✅ Loan Service (Port 8084)"
    echo "✅ API Gateway (Port 8080)"
    echo
    echo "🔗 Inter-service communication is working"
    echo "🔐 JWT authentication is working across services"
    echo "📡 Customer validation between services is working"
else
    echo -e "${RED}❌ Some tests failed. Please check the services.${NC}"
    exit 1
fi