# Test script for multiple categories deal creation
# This demonstrates the two-step process: initialize with phone, then update with multiple categories

Write-Host "=== Testing Multiple Categories Deal Creation API ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080/api/deals"
$contactNumber = "+1234567890"

# Step 1: Initialize a deal with contact number
Write-Host "`nStep 1: Initializing deal with contact number: $contactNumber" -ForegroundColor Yellow

$initRequest = @{
    contact_number = $contactNumber
} | ConvertTo-Json

Write-Host "Request: $initRequest"

try {
    $initResponse = Invoke-RestMethod -Uri "$baseUrl/init" -Method POST -Body $initRequest -ContentType "application/json"
    Write-Host "Response: $($initResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Green
    
    $dealId = $initResponse.deal_id
    Write-Host "Deal ID: $dealId" -ForegroundColor Cyan
} catch {
    Write-Host "Error in Step 1: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Update the deal with multiple categories
Write-Host "`nStep 2: Updating deal with multiple categories..." -ForegroundColor Yellow

$updateRequest = @{
    name = "John Doe"
    categories = @(
        @{
            name = "Wedding Photography"
            event_date = "2024-06-15"
            venue = "Grand Hotel"
            budget = 5000
            expected_gathering = 150
        },
        @{
            name = "Makeup"
            event_date = "2024-06-15"
            venue = "Grand Hotel"
            budget = 3000
            expected_gathering = 150
        },
        @{
            name = "Decor"
            event_date = "2024-06-15"
            venue = "Grand Hotel"
            budget = 4000
            expected_gathering = 150
        }
    )
} | ConvertTo-Json

Write-Host "Request: $updateRequest"

try {
    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/$dealId/details" -Method PUT -Body $updateRequest -ContentType "application/json"
    Write-Host "Response: $($updateResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Green
} catch {
    Write-Host "Error in Step 2: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 3: Verify all deals were created with the same contact number
Write-Host "`nStep 3: Verifying all deals were created..." -ForegroundColor Yellow

try {
    $allDeals = Invoke-RestMethod -Uri "$baseUrl" -Method GET
    Write-Host "All deals: $($allDeals | ConvertTo-Json -Depth 3)" -ForegroundColor Green
    
    # Filter deals by contact number
    $dealsForContact = $allDeals | Where-Object { $_.contact_number -eq $contactNumber }
    
    Write-Host "`nDeals for contact number $contactNumber :" -ForegroundColor Cyan
    foreach ($deal in $dealsForContact) {
        Write-Host "- ID: $($deal.id), Name: $($deal.user_name), Category: $($deal.category), Budget: $($deal.budget)" -ForegroundColor White
    }
    
    if ($dealsForContact.Count -eq 3) {
        Write-Host "`n✅ SUCCESS: All 3 categories created as separate deals with the same contact number!" -ForegroundColor Green
    } else {
        Write-Host "`n❌ FAILURE: Expected 3 deals, but found $($dealsForContact.Count)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "Error in Step 3: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green
