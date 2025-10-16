# Test script to demonstrate the new behavior of /api/deals/init
# This shows that calling the init API with the same contact number updates the timestamp instead of creating a new entry

Write-Host "=== Testing Duplicate Contact Number Behavior in Deal Init API ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080/api/deals"
$contactNumber = "+1234567890"

# Step 1: Initialize a deal with contact number (first time)
Write-Host "`nStep 1: First initialization with contact number: $contactNumber" -ForegroundColor Yellow

$initRequest = @{
    contact_number = $contactNumber
} | ConvertTo-Json

Write-Host "Request: $initRequest"

try {
    $initResponse1 = Invoke-RestMethod -Uri "$baseUrl/init" -Method POST -Body $initRequest -ContentType "application/json"
    Write-Host "Response: $($initResponse1 | ConvertTo-Json -Depth 3)" -ForegroundColor Green
    
    $dealId1 = $initResponse1.deal_id
    Write-Host "First Deal ID: $dealId1" -ForegroundColor Cyan
} catch {
    Write-Host "Error in Step 1: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Initialize again with the same contact number
Write-Host "`nStep 2: Second initialization with same contact number: $contactNumber" -ForegroundColor Yellow

try {
    $initResponse2 = Invoke-RestMethod -Uri "$baseUrl/init" -Method POST -Body $initRequest -ContentType "application/json"
    Write-Host "Response: $($initResponse2 | ConvertTo-Json -Depth 3)" -ForegroundColor Green
    
    $dealId2 = $initResponse2.deal_id
    Write-Host "Second Deal ID: $dealId2" -ForegroundColor Cyan
} catch {
    Write-Host "Error in Step 2: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 3: Verify that both calls returned the same deal ID
Write-Host "`nStep 3: Verifying behavior..." -ForegroundColor Yellow

if ($dealId1 -eq $dealId2) {
    Write-Host "✅ SUCCESS: Both calls returned the same deal ID ($dealId1)" -ForegroundColor Green
    Write-Host "This means the second call updated the existing deal's timestamp instead of creating a new entry." -ForegroundColor Green
} else {
    Write-Host "❌ FAILURE: Different deal IDs returned ($dealId1 vs $dealId2)" -ForegroundColor Red
    Write-Host "This means a new entry was created instead of updating the existing one." -ForegroundColor Red
}

# Step 4: Get the deal details to verify
Write-Host "`nStep 4: Getting deal details..." -ForegroundColor Yellow

try {
    $dealDetails = Invoke-RestMethod -Uri "$baseUrl/$dealId1" -Method GET
    Write-Host "Deal Details: $($dealDetails | ConvertTo-Json -Depth 3)" -ForegroundColor Green
} catch {
    Write-Host "Error getting deal details: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green
