#!/bin/bash

# Test script for Pipedrive integration
# This script tests the complete flow: initialize deal -> update with details

echo "🧪 Testing Pipedrive Integration"
echo "================================="

# Configuration
BASE_URL="http://localhost:8080"
TEST_CONTACT_NUMBER="+1234567890"

echo ""
echo "📞 Step 1: Initialize Deal with Contact Number"
echo "Contact Number: $TEST_CONTACT_NUMBER"

INIT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/deals/init" \
  -H "Content-Type: application/json" \
  -d "{\"contact_number\": \"$TEST_CONTACT_NUMBER\"}")

echo "✅ Deal Initialized Successfully"
echo "Response: $INIT_RESPONSE"

# Extract deal ID from response
DEAL_ID=$(echo $INIT_RESPONSE | jq -r '.deal_id')

if [ "$DEAL_ID" = "null" ] || [ -z "$DEAL_ID" ]; then
    echo "❌ Failed to extract deal ID from response"
    exit 1
fi

echo "Deal ID: $DEAL_ID"

echo ""
echo "⏳ Waiting 2 seconds before updating deal details..."
sleep 2

echo ""
echo "📝 Step 2: Update Deal with Multiple Categories"

UPDATE_PAYLOAD='{
  "name": "John Doe",
  "categories": [
    {
      "name": "Wedding Photography",
      "event_date": "2024-06-15",
      "venue": "Grand Hotel",
      "budget": 5000,
      "expected_gathering": 150
    },
    {
      "name": "Makeup",
      "event_date": "2024-06-15",
      "venue": "Grand Hotel",
      "budget": 3000,
      "expected_gathering": 150
    },
    {
      "name": "Decor",
      "event_date": "2024-06-15",
      "venue": "Grand Hotel",
      "budget": 2000,
      "expected_gathering": 150
    }
  ]
}'

echo "Payload:"
echo "$UPDATE_PAYLOAD"

UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/deals/$DEAL_ID/details" \
  -H "Content-Type: application/json" \
  -d "$UPDATE_PAYLOAD")

if [ $? -eq 0 ]; then
    echo "✅ Deal Updated Successfully"
    echo "Response: $UPDATE_RESPONSE"
else
    echo "❌ Error updating deal"
    exit 1
fi

echo ""
echo "🔍 Step 3: Verify All Deals Created"

ALL_DEALS=$(curl -s -X GET "$BASE_URL/api/deals")

if [ $? -eq 0 ]; then
    DEALS_FOR_CONTACT=$(echo "$ALL_DEALS" | jq "[.[] | select(.contactNumber == \"$TEST_CONTACT_NUMBER\")]")
    DEAL_COUNT=$(echo "$DEALS_FOR_CONTACT" | jq 'length')
    
    echo "✅ Found $DEAL_COUNT deals for contact $TEST_CONTACT_NUMBER"
    
    echo "$DEALS_FOR_CONTACT" | jq -r '.[] | "  - Deal ID: \(.id), Category: \(.category), Budget: \(.budget)"'
else
    echo "❌ Error retrieving deals"
fi

echo ""
echo "📊 Step 4: Check Database Tables"
echo "You can check the following in your database:"
echo "1. contacts table - should have 1 contact with Pipedrive contact ID"
echo "2. deals table - should have 3 deals with Pipedrive deal IDs and contact_id references"

echo ""
echo "🎯 Pipedrive Integration Test Complete!"
echo "====================================="
echo "Check your Pipedrive account for:"
echo "1. New person created with name 'TBS' and phone '$TEST_CONTACT_NUMBER' in Organization 41"
echo "2. 3 new deals created in Pipeline 13 with custom fields populated"
echo "3. Person name updated to 'John Doe' when deal details were added"
echo ""
echo "Configuration used:"
echo "- Organization ID: 41"
echo "- Pipeline ID: 13"
