#!/usr/bin/env python3
import requests
import json

print("🔥 Testing API Gateway")
print("=" * 50)

# Test 1: Health check
print("1. Testing health endpoint...")
try:
    response = requests.get("http://localhost:8080/health")
    print(f"   Status: {response.status_code}")
    print(f"   Response: {response.text}")
    print("   ✅ PASSED\n")
except Exception as e:
    print(f"   ❌ FAILED: {e}\n")

# Test 2: Customer login through gateway
print("2. Testing customer login through gateway...")
try:
    login_data = {"ssnId": "1001001", "password": "password123"}
    response = requests.post("http://localhost:8080/api/customers/login", 
                           json=login_data,
                           headers={"Content-Type": "application/json"})
    print(f"   Status: {response.status_code}")
    
    if response.status_code == 200:
        response_data = response.json()
        token = response_data.get('token')
        print(f"   Token received: {token[:50]}..." if token else "   No token in response")
        print("   ✅ PASSED\n")
    else:
        print(f"   Response: {response.text}")
        print("   ❌ FAILED\n")
        token = None
        
except Exception as e:
    print(f"   ❌ FAILED: {e}\n")
    token = None

# Test 3: Loan count through gateway (if we have token)
if token:
    print("3. Testing loan count through gateway...")
    try:
        headers = {"Authorization": f"Bearer {token}"}
        response = requests.get("http://localhost:8080/api/loans/count", headers=headers)
        print(f"   Status: {response.status_code}")
        
        if response.status_code == 200:
            response_data = response.json()
            print(f"   Loan count: {response_data.get('count', 'No count field')}")
            print("   ✅ PASSED\n")
        else:
            print(f"   Response: {response.text}")
            print("   ❌ FAILED\n")
            
    except Exception as e:
        print(f"   ❌ FAILED: {e}\n")

    # Test 4: Transaction count through gateway
    print("4. Testing transaction count through gateway...")
    try:
        response = requests.get("http://localhost:8080/api/transactions/count", headers=headers)
        print(f"   Status: {response.status_code}")
        
        if response.status_code == 200:
            response_data = response.json()
            print(f"   Transaction count: {response_data.get('count', 'No count field')}")
            print("   ✅ PASSED\n")
        else:
            print(f"   Response: {response.text}")
            print("   ❌ FAILED\n")
            
    except Exception as e:
        print(f"   ❌ FAILED: {e}\n")

else:
    print("3. Skipping authenticated tests - no token\n")

print("🏁 Test completed!")