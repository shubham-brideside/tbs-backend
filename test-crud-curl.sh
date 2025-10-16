#!/bin/bash

# Comprehensive CRUD Operations Test Script using curl
# This script tests all Create, Read, Update, Delete operations

BASE_URL="http://localhost:8080/api/deals"
HEALTH_URL="http://localhost:8080/actuator/health"

echo "=== COMPREHENSIVE CRUD OPERATIONS TEST (curl) ==="
echo "Base URL: $BASE_URL"
echo ""

# Function to make HTTP requests with error handling
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local expected_status=$4
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method -H "Content-Type: application/json" -d "$data" "$url")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method "$url")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "$expected_status" ]; then
        echo "✅ SUCCESS (Status: $http_code)"
        echo "Response:"
        echo "$body" | jq . 2>/dev/null || echo "$body"
    else
        echo "❌ FAILED (Status: $http_code)"
        echo "Response: $body"
    fi
    echo ""
}

# 1. HEALTH CHECK
echo "1. HEALTH CHECK"
make_request "GET" "$HEALTH_URL" "" "200"

# 2. CREATE OPERATIONS (POST)
echo "2. CREATE OPERATIONS (POST)"

# Create single deal
single_deal='{
    "name": "John Doe",
    "contact_number": "9876543210",
    "categories": [
        {
            "name": "Photography",
            "event_date": "2025-12-25",
            "venue": "Grand Hotel, Mumbai",
            "budget": 50000,
            "expected_gathering": 150
        }
    ]
}'

echo "Creating single deal..."
make_request "POST" "$BASE_URL" "$single_deal" "201"

# Create multiple deals
multiple_deals='{
    "name": "Jane Smith",
    "contact_number": "8765432109",
    "categories": [
        {
            "name": "Catering",
            "event_date": "2025-11-15",
            "venue": "Taj Hotel, Delhi",
            "budget": 75000,
            "expected_gathering": 100
        },
        {
            "name": "Decoration",
            "event_date": "2025-11-15",
            "venue": "Taj Hotel, Delhi",
            "budget": 30000,
            "expected_gathering": 100
        }
    ]
}'

echo "Creating multiple deals..."
make_request "POST" "$BASE_URL" "$multiple_deals" "201"

# 3. READ OPERATIONS (GET)
echo "3. READ OPERATIONS (GET)"

echo "Getting all deals..."
make_request "GET" "$BASE_URL" "" "200"

echo "Getting deal by ID (1)..."
make_request "GET" "$BASE_URL/1" "" "200"

echo "Getting deals by user (John Doe)..."
make_request "GET" "$BASE_URL/user/John%20Doe" "" "200"

echo "Getting deals by contact (9876543210)..."
make_request "GET" "$BASE_URL/contact/9876543210" "" "200"

echo "Getting deals by category (Photography)..."
make_request "GET" "$BASE_URL/category/Photography" "" "200"

# 4. UPDATE OPERATIONS (PUT)
echo "4. UPDATE OPERATIONS (PUT)"

update_deal='{
    "name": "John Doe Updated",
    "contact_number": "9876543210",
    "categories": [
        {
            "name": "Photography",
            "event_date": "2025-12-30",
            "venue": "Updated Grand Hotel, Mumbai",
            "budget": 60000,
            "expected_gathering": 200
        }
    ]
}'

echo "Updating deal by ID (1)..."
make_request "PUT" "$BASE_URL/1" "$update_deal" "200"

# 5. DELETE OPERATIONS (DELETE)
echo "5. DELETE OPERATIONS (DELETE)"

echo "Deleting deal by ID (2)..."
make_request "DELETE" "$BASE_URL/2" "" "200"

echo "Deleting all deals for user (Jane Smith)..."
make_request "DELETE" "$BASE_URL/user/Jane%20Smith" "" "200"

# 6. FINAL VERIFICATION
echo "6. FINAL VERIFICATION"

echo "Getting all remaining deals..."
make_request "GET" "$BASE_URL" "" "200"

echo "=== CRUD OPERATIONS TEST COMPLETE ==="
echo "Check the results above to verify all operations are working correctly."
