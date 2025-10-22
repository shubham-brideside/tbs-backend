#!/bin/bash

# Test Azure Credentials Script
# This script helps verify that your Azure credentials are properly formatted

echo "🔍 Testing Azure Credentials Format"
echo "=================================="

# Check if AZURE_CREDENTIALS environment variable is set
if [ -z "$AZURE_CREDENTIALS" ]; then
    echo "❌ AZURE_CREDENTIALS environment variable is not set"
    echo "Please set it with your service principal JSON:"
    echo "export AZURE_CREDENTIALS='{\"clientId\":\"...\",\"clientSecret\":\"...\",\"subscriptionId\":\"...\",\"tenantId\":\"...\"}'"
    exit 1
fi

echo "✅ AZURE_CREDENTIALS is set"

# Validate JSON format
if ! echo "$AZURE_CREDENTIALS" | jq . > /dev/null 2>&1; then
    echo "❌ AZURE_CREDENTIALS is not valid JSON"
    echo "Current value: $AZURE_CREDENTIALS"
    exit 1
fi

echo "✅ AZURE_CREDENTIALS is valid JSON"

# Check required fields
REQUIRED_FIELDS=("clientId" "clientSecret" "subscriptionId" "tenantId")

for field in "${REQUIRED_FIELDS[@]}"; do
    if ! echo "$AZURE_CREDENTIALS" | jq -e ".$field" > /dev/null 2>&1; then
        echo "❌ Missing required field: $field"
        exit 1
    fi
    
    value=$(echo "$AZURE_CREDENTIALS" | jq -r ".$field")
    if [ "$value" = "null" ] || [ -z "$value" ]; then
        echo "❌ Field $field is null or empty"
        exit 1
    fi
    
    echo "✅ Field $field is present and not empty"
done

echo ""
echo "🎉 All Azure credentials are properly formatted!"
echo ""
echo "📋 Credentials Summary:"
echo "======================"
echo "Client ID: $(echo "$AZURE_CREDENTIALS" | jq -r '.clientId')"
echo "Subscription ID: $(echo "$AZURE_CREDENTIALS" | jq -r '.subscriptionId')"
echo "Tenant ID: $(echo "$AZURE_CREDENTIALS" | jq -r '.tenantId')"
echo "Client Secret: $(echo "$AZURE_CREDENTIALS" | jq -r '.clientSecret' | cut -c1-8)..."
echo ""
echo "🚀 Ready for deployment!"
