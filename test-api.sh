#!/bin/bash

# ==========================================
# BRIDESIDE BACKEND API - CRUD TESTING
# ==========================================

BASE_URL="http://localhost:8080/api/deals"

echo "🚀 Testing Brideside Backend API CRUD Operations"
echo "================================================"

# ==========================================
# 1. CREATE - Add New Deals (POST)
# ==========================================
echo ""
echo "📝 1. CREATE - Adding new deals..."
echo "-----------------------------------"

curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Shubham",
    "contact_number": "9304683214",
    "categories": [
      {
        "name": "Photography",
        "event_date": "2025-10-20",
        "venue": "The Leela Palace, New Delhi",
        "budget": 200000,
        "expected_gathering": 200
      },
      {
        "name": "Makeup",
        "event_date": "2025-10-25",
        "venue": "Taj Palace, New Delhi",
        "budget": 150000,
        "expected_gathering": 180
      }
    ]
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo ""
echo "📝 Adding another user's deals..."

curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Priya",
    "contact_number": "9876543210",
    "categories": [
      {
        "name": "Decor",
        "event_date": "2025-11-01",
        "venue": "The Oberoi, Gurgaon",
        "budget": 300000,
        "expected_gathering": 250
      },
      {
        "name": "Catering",
        "event_date": "2025-11-01",
        "venue": "The Oberoi, Gurgaon",
        "budget": 500000,
        "expected_gathering": 250
      }
    ]
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# ==========================================
# 2. READ - Get All Deals (GET)
# ==========================================
echo ""
echo "📖 2. READ - Getting all deals..."
echo "-----------------------------------"

curl -X GET "$BASE_URL" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# ==========================================
# 3. READ - Get Deals by User Name (GET)
# ==========================================
echo ""
echo "👤 3. READ - Getting deals by user name (Shubham)..."
echo "----------------------------------------------------"

curl -X GET "$BASE_URL/user/Shubham" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo ""
echo "👤 Getting deals by user name (Priya)..."

curl -X GET "$BASE_URL/user/Priya" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# ==========================================
# 4. READ - Get Deals by Contact Number (GET)
# ==========================================
echo ""
echo "📞 4. READ - Getting deals by contact number..."
echo "-----------------------------------------------"

curl -X GET "$BASE_URL/contact/9304683214" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# ==========================================
# 5. READ - Get Deals by Category (GET)
# ==========================================
echo ""
echo "🏷️  5. READ - Getting deals by category..."
echo "------------------------------------------"

curl -X GET "$BASE_URL/category/Photography" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo ""
echo "🏷️  Getting deals by category (Makeup)..."

curl -X GET "$BASE_URL/category/Makeup" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# ==========================================
# 6. ERROR TESTING - Invalid Data (POST)
# ==========================================
echo ""
echo "❌ 6. ERROR TESTING - Testing validation..."
echo "------------------------------------------"

curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "contact_number": "",
    "categories": []
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

# ==========================================
# 7. HEALTH CHECK
# ==========================================
echo ""
echo "🏥 7. HEALTH CHECK - Application status..."
echo "------------------------------------------"

curl -X GET "http://localhost:8080/actuator/health" \
  -H "Accept: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo ""
echo "✅ CRUD Testing Complete!"
echo "========================="
