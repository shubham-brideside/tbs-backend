# Test POST API Script
Write-Host "=== Testing POST API ===" -ForegroundColor Yellow

# Check if application is running
Write-Host "Checking if application is running on port 8080..." -ForegroundColor Cyan
$portCheck = netstat -ano | findstr :8080
if ($portCheck) {
    Write-Host "✅ Application is running!" -ForegroundColor Green
} else {
    Write-Host "❌ Application is not running. Please start it first." -ForegroundColor Red
    exit 1
}

# Test data
$body = @{
    name = "Test User"
    contact_number = "1234567890"
    categories = @(
        @{
            name = "Photography"
            event_date = "2025-10-20"
            venue = "Test Venue"
            budget = 100000
            expected_gathering = 100
        }
    )
} | ConvertTo-Json -Depth 3

Write-Host "`nSending POST request to http://localhost:8080/api/deals" -ForegroundColor Cyan
Write-Host "Request body:" -ForegroundColor Cyan
Write-Host $body -ForegroundColor White

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/deals" -Method POST -Body $body -ContentType "application/json"
    Write-Host "`n✅ SUCCESS!" -ForegroundColor Green
    Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
} catch {
    Write-Host "`n❌ ERROR!" -ForegroundColor Red
    Write-Host "Error Message: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Yellow
