# ==========================================
# BRIDESIDE BACKEND API - CRUD TESTING (Windows PowerShell)
# ==========================================

$BaseUrl = "http://localhost:8080/api/deals"

Write-Host "🚀 Testing Brideside Backend API CRUD Operations" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green

# ==========================================
# 1. CREATE - Add New Deals (POST)
# ==========================================
Write-Host ""
Write-Host "📝 1. CREATE - Adding new deals..." -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow

$CreateData1 = @{
    name = "Shubham"
    contact_number = "9304683214"
    categories = @(
        @{
            name = "Photography"
            event_date = "2025-10-20"
            venue = "The Leela Palace, New Delhi"
            budget = 200000
            expected_gathering = 200
        },
        @{
            name = "Makeup"
            event_date = "2025-10-25"
            venue = "Taj Palace, New Delhi"
            budget = 150000
            expected_gathering = 180
        }
    )
} | ConvertTo-Json -Depth 3

try {
    $Response1 = Invoke-RestMethod -Uri $BaseUrl -Method POST -Body $CreateData1 -ContentType "application/json"
    Write-Host "✅ Successfully created deals for Shubham" -ForegroundColor Green
    $Response1 | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Error creating deals: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "📝 Adding another user's deals..." -ForegroundColor Yellow

$CreateData2 = @{
    name = "Priya"
    contact_number = "9876543210"
    categories = @(
        @{
            name = "Decor"
            event_date = "2025-11-01"
            venue = "The Oberoi, Gurgaon"
            budget = 300000
            expected_gathering = 250
        },
        @{
            name = "Catering"
            event_date = "2025-11-01"
            venue = "The Oberoi, Gurgaon"
            budget = 500000
            expected_gathering = 250
        }
    )
} | ConvertTo-Json -Depth 3

try {
    $Response2 = Invoke-RestMethod -Uri $BaseUrl -Method POST -Body $CreateData2 -ContentType "application/json"
    Write-Host "✅ Successfully created deals for Priya" -ForegroundColor Green
    $Response2 | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Error creating deals: $($_.Exception.Message)" -ForegroundColor Red
}

# ==========================================
# 2. READ - Get All Deals (GET)
# ==========================================
Write-Host ""
Write-Host "📖 2. READ - Getting all deals..." -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow

try {
    $AllDeals = Invoke-RestMethod -Uri $BaseUrl -Method GET
    Write-Host "✅ Successfully retrieved all deals" -ForegroundColor Green
    $AllDeals | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Error getting all deals: $($_.Exception.Message)" -ForegroundColor Red
}

# ==========================================
# 3. READ - Get Deals by User Name (GET)
# ==========================================
Write-Host ""
Write-Host "👤 3. READ - Getting deals by user name (Shubham)..." -ForegroundColor Yellow
Write-Host "----------------------------------------------------" -ForegroundColor Yellow

try {
    $UserDeals = Invoke-RestMethod -Uri "$BaseUrl/user/Shubham" -Method GET
    Write-Host "✅ Successfully retrieved deals for Shubham" -ForegroundColor Green
    $UserDeals | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Error getting deals by user: $($_.Exception.Message)" -ForegroundColor Red
}

# ==========================================
# 4. READ - Get Deals by Contact Number (GET)
# ==========================================
Write-Host ""
Write-Host "📞 4. READ - Getting deals by contact number..." -ForegroundColor Yellow
Write-Host "-----------------------------------------------" -ForegroundColor Yellow

try {
    $ContactDeals = Invoke-RestMethod -Uri "$BaseUrl/contact/9304683214" -Method GET
    Write-Host "✅ Successfully retrieved deals by contact number" -ForegroundColor Green
    $ContactDeals | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Error getting deals by contact: $($_.Exception.Message)" -ForegroundColor Red
}

# ==========================================
# 5. READ - Get Deals by Category (GET)
# ==========================================
Write-Host ""
Write-Host "🏷️  5. READ - Getting deals by category (Photography)..." -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Yellow

try {
    $CategoryDeals = Invoke-RestMethod -Uri "$BaseUrl/category/Photography" -Method GET
    Write-Host "✅ Successfully retrieved deals by category" -ForegroundColor Green
    $CategoryDeals | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Error getting deals by category: $($_.Exception.Message)" -ForegroundColor Red
}

# ==========================================
# 6. ERROR TESTING - Invalid Data (POST)
# ==========================================
Write-Host ""
Write-Host "❌ 6. ERROR TESTING - Testing validation..." -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Yellow

$InvalidData = @{
    name = ""
    contact_number = ""
    categories = @()
} | ConvertTo-Json

try {
    $ErrorResponse = Invoke-RestMethod -Uri $BaseUrl -Method POST -Body $InvalidData -ContentType "application/json"
    Write-Host "⚠️  Unexpected success with invalid data" -ForegroundColor Yellow
} catch {
    Write-Host "✅ Validation working correctly - rejected invalid data" -ForegroundColor Green
    Write-Host "Error details: $($_.Exception.Message)" -ForegroundColor Red
}

# ==========================================
# 7. HEALTH CHECK
# ==========================================
Write-Host ""
Write-Host "🏥 7. HEALTH CHECK - Application status..." -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Yellow

try {
    $Health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET
    Write-Host "✅ Application is healthy" -ForegroundColor Green
    $Health | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "✅ CRUD Testing Complete!" -ForegroundColor Green
Write-Host "=========================" -ForegroundColor Green
