# Comprehensive CRUD Operations Test Script for Deals API
# This script tests all Create, Read, Update, Delete operations

$BASE_URL = "http://localhost:8080/api/deals"
$HEALTH_URL = "http://localhost:8080/actuator/health"

Write-Host "=== COMPREHENSIVE CRUD OPERATIONS TEST ===" -ForegroundColor Magenta
Write-Host "Base URL: $BASE_URL" -ForegroundColor Cyan
Write-Host ""

# Function to make HTTP requests with error handling
function Invoke-ApiRequest {
    param(
        [string]$Uri,
        [string]$Method = "GET",
        [string]$Body = $null,
        [string]$ContentType = "application/json"
    )
    
    try {
        if ($Body) {
            $response = Invoke-WebRequest -Uri $Uri -Method $Method -Body $Body -ContentType $ContentType
        } else {
            $response = Invoke-WebRequest -Uri $Uri -Method $Method
        }
        
        return @{
            Success = $true
            StatusCode = $response.StatusCode
            Content = $response.Content
            Error = $null
        }
    } catch {
        return @{
            Success = $false
            StatusCode = $_.Exception.Response.StatusCode.value__
            Content = $null
            Error = $_.Exception.Message
        }
    }
}

# Function to display results
function Show-Result {
    param(
        [string]$Operation,
        [hashtable]$Result,
        [string]$ExpectedStatus = "200"
    )
    
    if ($Result.Success -and $Result.StatusCode -eq $ExpectedStatus) {
        Write-Host "✅ $Operation - SUCCESS (Status: $($Result.StatusCode))" -ForegroundColor Green
        if ($Result.Content) {
            Write-Host "Response:" -ForegroundColor Cyan
            try {
                $Result.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
            } catch {
                Write-Host $Result.Content
            }
        }
    } else {
        Write-Host "❌ $Operation - FAILED" -ForegroundColor Red
        Write-Host "Status: $($Result.StatusCode)" -ForegroundColor Red
        Write-Host "Error: $($Result.Error)" -ForegroundColor Red
        if ($Result.Content) {
            Write-Host "Response: $($Result.Content)" -ForegroundColor Red
        }
    }
    Write-Host ""
}

# 1. HEALTH CHECK
Write-Host "1. HEALTH CHECK" -ForegroundColor Yellow
$healthResult = Invoke-ApiRequest -Uri $HEALTH_URL
Show-Result -Operation "Health Check" -Result $healthResult

# 2. CREATE OPERATIONS (POST)
Write-Host "2. CREATE OPERATIONS (POST)" -ForegroundColor Yellow

# Create single deal
$singleDealBody = @{
    name = "John Doe"
    contact_number = "9876543210"
    categories = @(
        @{
            name = "Photography"
            event_date = "2025-12-25"
            venue = "Grand Hotel, Mumbai"
            budget = 50000
            expected_gathering = 150
        }
    )
} | ConvertTo-Json -Depth 3

$createResult = Invoke-ApiRequest -Uri $BASE_URL -Method POST -Body $singleDealBody
Show-Result -Operation "Create Single Deal" -Result $createResult -ExpectedStatus "201"

# Create multiple deals
$multipleDealsBody = @{
    name = "Jane Smith"
    contact_number = "8765432109"
    categories = @(
        @{
            name = "Catering"
            event_date = "2025-11-15"
            venue = "Taj Hotel, Delhi"
            budget = 75000
            expected_gathering = 100
        },
        @{
            name = "Decoration"
            event_date = "2025-11-15"
            venue = "Taj Hotel, Delhi"
            budget = 30000
            expected_gathering = 100
        }
    )
} | ConvertTo-Json -Depth 3

$createMultipleResult = Invoke-ApiRequest -Uri $BASE_URL -Method POST -Body $multipleDealsBody
Show-Result -Operation "Create Multiple Deals" -Result $createMultipleResult -ExpectedStatus "201"

# 3. READ OPERATIONS (GET)
Write-Host "3. READ OPERATIONS (GET)" -ForegroundColor Yellow

# Get all deals
$getAllResult = Invoke-ApiRequest -Uri $BASE_URL
Show-Result -Operation "Get All Deals" -Result $getAllResult

# Get deal by ID (assuming we have deal with ID 1)
$getByIdResult = Invoke-ApiRequest -Uri "$BASE_URL/1"
Show-Result -Operation "Get Deal by ID (1)" -Result $getByIdResult

# Get deals by user name
$getByUserResult = Invoke-ApiRequest -Uri "$BASE_URL/user/John Doe"
Show-Result -Operation "Get Deals by User (John Doe)" -Result $getByUserResult

# Get deals by contact number
$getByContactResult = Invoke-ApiRequest -Uri "$BASE_URL/contact/9876543210"
Show-Result -Operation "Get Deals by Contact (9876543210)" -Result $getByContactResult

# Get deals by category
$getByCategoryResult = Invoke-ApiRequest -Uri "$BASE_URL/category/Photography"
Show-Result -Operation "Get Deals by Category (Photography)" -Result $getByCategoryResult

# 4. UPDATE OPERATIONS (PUT)
Write-Host "4. UPDATE OPERATIONS (PUT)" -ForegroundColor Yellow

# Update deal by ID
$updateBody = @{
    name = "John Doe Updated"
    contact_number = "9876543210"
    categories = @(
        @{
            name = "Photography"
            event_date = "2025-12-30"
            venue = "Updated Grand Hotel, Mumbai"
            budget = 60000
            expected_gathering = 200
        }
    )
} | ConvertTo-Json -Depth 3

$updateResult = Invoke-ApiRequest -Uri "$BASE_URL/1" -Method PUT -Body $updateBody
Show-Result -Operation "Update Deal by ID (1)" -Result $updateResult

# 5. DELETE OPERATIONS (DELETE)
Write-Host "5. DELETE OPERATIONS (DELETE)" -ForegroundColor Yellow

# Delete deal by ID (assuming we have deal with ID 2)
$deleteByIdResult = Invoke-ApiRequest -Uri "$BASE_URL/2" -Method DELETE
Show-Result -Operation "Delete Deal by ID (2)" -Result $deleteByIdResult -ExpectedStatus "200"

# Delete all deals for a user
$deleteByUserResult = Invoke-ApiRequest -Uri "$BASE_URL/user/Jane Smith" -Method DELETE
Show-Result -Operation "Delete All Deals for User (Jane Smith)" -Result $deleteByUserResult -ExpectedStatus "200"

# 6. FINAL VERIFICATION
Write-Host "6. FINAL VERIFICATION" -ForegroundColor Yellow

# Get all remaining deals
$finalGetAllResult = Invoke-ApiRequest -Uri $BASE_URL
Show-Result -Operation "Final - Get All Remaining Deals" -Result $finalGetAllResult

Write-Host "=== CRUD OPERATIONS TEST COMPLETE ===" -ForegroundColor Magenta
Write-Host "Check the results above to verify all operations are working correctly." -ForegroundColor Cyan
