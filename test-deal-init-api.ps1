# Test script for the deal initialization API
# This demonstrates the two-step process: initialize with phone, then update with details

Write-Host "=== Testing Deal Initialization API ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080/api/deals"

# Step 1: Initialize a deal with just phone number
Write-Host "`nStep 1: Initializing deal with phone number..." -ForegroundColor Yellow

$initRequest = @{
    contact_number = "+1234567890"
} | ConvertTo-Json

Write-Host "Request: $initRequest"

try {
    $initResponse = Invoke-RestMethod -Uri "$baseUrl/init" -Method POST -Body $initRequest -ContentType "application/json"
    Write-Host "Response: $($initResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Green
    
    # Extract deal ID from response
    $dealId = $initResponse.deal_id
    Write-Host "Deal ID: $dealId" -ForegroundColor Cyan
} catch {
    Write-Host "Error in Step 1: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Update the deal with full details using existing update API
Write-Host "`nStep 2: Updating deal with full details using existing update API..." -ForegroundColor Yellow

$updateRequest = @{
    name = "John Doe"
    contact_number = "+1234567890"
    categories = @(
        @{
            name = "Wedding Photography"
            event_date = "2024-06-15"
            venue = "Grand Hotel Ballroom"
            budget = 5000.00
            expected_gathering = 150
        }
    )
} | ConvertTo-Json

Write-Host "Request: $updateRequest"

try {
    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/$dealId" -Method PUT -Body $updateRequest -ContentType "application/json"
    Write-Host "Response: $($updateResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Green
} catch {
    Write-Host "Error in Step 2: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 3: Verify the deal was updated
Write-Host "`nStep 3: Verifying updated deal..." -ForegroundColor Yellow

try {
    $dealDetails = Invoke-RestMethod -Uri "$baseUrl/$dealId" -Method GET
    Write-Host "Deal details: $($dealDetails | ConvertTo-Json -Depth 3)" -ForegroundColor Green
} catch {
    Write-Host "Error getting deal details: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green
Write-Host "The frontend can now use the deal_id from step 1 to update the deal with full details in step 2!" -ForegroundColor Cyan
