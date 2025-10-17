# Test script for Pipedrive integration
# This script tests the complete flow: initialize deal -> update with details

Write-Host "🧪 Testing Pipedrive Integration" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

# Configuration
$baseUrl = "http://localhost:8080"
$testContactNumber = "+1234567890"

Write-Host "`n📞 Step 1: Initialize Deal with Contact Number" -ForegroundColor Yellow
Write-Host "Contact Number: $testContactNumber"

$initResponse = Invoke-RestMethod -Uri "$baseUrl/api/deals/init" -Method POST -ContentType "application/json" -Body (@{
    contact_number = $testContactNumber
} | ConvertTo-Json)

Write-Host "✅ Deal Initialized Successfully" -ForegroundColor Green
Write-Host "Deal ID: $($initResponse.deal_id)"
Write-Host "Message: $($initResponse.message)"

$dealId = $initResponse.deal_id

Write-Host "`n⏳ Waiting 2 seconds before updating deal details..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

Write-Host "`n📝 Step 2: Update Deal with Multiple Categories" -ForegroundColor Yellow

$updatePayload = @{
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
            budget = 2000
            expected_gathering = 150
        }
    )
} | ConvertTo-Json -Depth 3

Write-Host "Payload:"
Write-Host $updatePayload

try {
    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/api/deals/$dealId/details" -Method PUT -ContentType "application/json" -Body $updatePayload
    
    Write-Host "✅ Deal Updated Successfully" -ForegroundColor Green
    Write-Host "Updated Deal ID: $($updateResponse.id)"
    Write-Host "User Name: $($updateResponse.userName)"
    Write-Host "Contact Number: $($updateResponse.contactNumber)"
    Write-Host "Category: $($updateResponse.category)"
    Write-Host "Event Date: $($updateResponse.eventDate)"
    Write-Host "Venue: $($updateResponse.venue)"
    Write-Host "Budget: $($updateResponse.budget)"
    
} catch {
    Write-Host "❌ Error updating deal: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.Exception.Response)" -ForegroundColor Red
}

Write-Host "`n🔍 Step 3: Verify All Deals Created" -ForegroundColor Yellow

try {
    $allDeals = Invoke-RestMethod -Uri "$baseUrl/api/deals" -Method GET
    
    $dealsForContact = $allDeals | Where-Object { $_.contactNumber -eq $testContactNumber }
    
    Write-Host "✅ Found $($dealsForContact.Count) deals for contact $testContactNumber" -ForegroundColor Green
    
    foreach ($deal in $dealsForContact) {
        Write-Host "  - Deal ID: $($deal.id), Category: $($deal.category), Budget: $($deal.budget)"
    }
    
} catch {
    Write-Host "❌ Error retrieving deals: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n📊 Step 4: Check Database Tables" -ForegroundColor Yellow
Write-Host "You can check the following in your database:"
Write-Host "1. contacts table - should have 1 contact with Pipedrive contact ID"
Write-Host "2. deals table - should have 3 deals with Pipedrive deal IDs and contact_id references"

Write-Host "`n🎯 Pipedrive Integration Test Complete!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host "Check your Pipedrive account for:"
Write-Host "1. New person created with name 'TBD' and phone '$testContactNumber' in Organization 41"
Write-Host "2. 3 new deals created in Pipeline 13 with custom fields populated"
Write-Host "3. Person name updated to 'John Doe' when deal details were added"
Write-Host "`nConfiguration used:"
Write-Host "- Organization ID: 41"
Write-Host "- Pipeline ID: 13"
