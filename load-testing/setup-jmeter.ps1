# JMeter Setup Script for Deals API Load Testing
# This script helps you set up JMeter for load testing

Write-Host "🚀 JMeter Setup for Deals API Load Testing" -ForegroundColor Green
Write-Host ""

# Check if JMeter is installed
Write-Host "🔍 Checking JMeter installation..." -ForegroundColor Yellow
$jmeterPath = Get-Command jmeter -ErrorAction SilentlyContinue

if ($jmeterPath) {
    Write-Host "✅ JMeter is already installed!" -ForegroundColor Green
    Write-Host "Version: $($jmeterPath.Version)" -ForegroundColor Cyan
} else {
    Write-Host "❌ JMeter not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "📥 Please install JMeter:" -ForegroundColor Yellow
    Write-Host "1. Download from: https://jmeter.apache.org/download_jmeter.cgi" -ForegroundColor Cyan
    Write-Host "2. Extract to a folder (e.g., C:\apache-jmeter-5.5)" -ForegroundColor Cyan
    Write-Host "3. Add the bin folder to your PATH environment variable" -ForegroundColor Cyan
    Write-Host "4. Restart your terminal/command prompt" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "🔧 Alternative: Install via Chocolatey (if you have it):" -ForegroundColor Yellow
    Write-Host "choco install jmeter" -ForegroundColor Cyan
    exit 1
}

# Check if application is running
Write-Host ""
Write-Host "🔍 Checking if your Spring Boot application is running..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Application is running on port 8080" -ForegroundColor Green
    } else {
        Write-Host "❌ Application is not responding properly" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Application is not running on port 8080" -ForegroundColor Red
    Write-Host ""
    Write-Host "🚀 Please start your Spring Boot application first:" -ForegroundColor Yellow
    Write-Host "1. Open IntelliJ IDEA" -ForegroundColor Cyan
    Write-Host "2. Run BridesideBackendApplication.java" -ForegroundColor Cyan
    Write-Host "3. Wait for 'Started BridesideBackendApplication' message" -ForegroundColor Cyan
    Write-Host "4. Then run this setup script again" -ForegroundColor Cyan
    exit 1
}

# Test API endpoints
Write-Host ""
Write-Host "🧪 Testing API endpoints..." -ForegroundColor Yellow

$endpoints = @(
    @{url="http://localhost:8080/actuator/health"; method="GET"; expected=200},
    @{url="http://localhost:8080/api/deals"; method="GET"; expected=200},
    @{url="http://localhost:8080/swagger-ui/index.html"; method="GET"; expected=200}
)

$allEndpointsWorking = $true

foreach ($endpoint in $endpoints) {
    try {
        $response = Invoke-WebRequest -Uri $endpoint.url -Method $endpoint.method -TimeoutSec 5
        if ($response.StatusCode -eq $endpoint.expected) {
            Write-Host "✅ $($endpoint.url) - OK" -ForegroundColor Green
        } else {
            Write-Host "❌ $($endpoint.url) - Expected $($endpoint.expected), got $($response.StatusCode)" -ForegroundColor Red
            $allEndpointsWorking = $false
        }
    } catch {
        Write-Host "❌ $($endpoint.url) - Error: $($_.Exception.Message)" -ForegroundColor Red
        $allEndpointsWorking = $false
    }
}

if (-not $allEndpointsWorking) {
    Write-Host ""
    Write-Host "❌ Some API endpoints are not working properly" -ForegroundColor Red
    Write-Host "Please check your application logs and ensure all endpoints are accessible" -ForegroundColor Yellow
    exit 1
}

# Check test files
Write-Host ""
Write-Host "📁 Checking test files..." -ForegroundColor Yellow

$testFiles = @(
    "jmeter-deals-api.jmx",
    "simple-jmeter-test.jmx",
    "run-jmeter-tests.ps1",
    "JMETER_LOAD_TESTING_GUIDE.md"
)

$allFilesExist = $true
foreach ($file in $testFiles) {
    if (Test-Path "load-testing/$file") {
        Write-Host "✅ $file" -ForegroundColor Green
    } else {
        Write-Host "❌ $file - Missing" -ForegroundColor Red
        $allFilesExist = $false
    }
}

if (-not $allFilesExist) {
    Write-Host ""
    Write-Host "❌ Some test files are missing" -ForegroundColor Red
    Write-Host "Please ensure all test files are in the load-testing directory" -ForegroundColor Yellow
    exit 1
}

# Create results directory
Write-Host ""
Write-Host "📁 Creating results directory..." -ForegroundColor Yellow
$resultsDir = "load-testing/results"
if (-not (Test-Path $resultsDir)) {
    New-Item -ItemType Directory -Path $resultsDir -Force | Out-Null
    Write-Host "✅ Created results directory: $resultsDir" -ForegroundColor Green
} else {
    Write-Host "✅ Results directory already exists: $resultsDir" -ForegroundColor Green
}

# Display setup summary
Write-Host ""
Write-Host "🎉 Setup Complete!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 What's Ready:" -ForegroundColor Yellow
Write-Host "✅ JMeter installed and working" -ForegroundColor Green
Write-Host "✅ Spring Boot application running" -ForegroundColor Green
Write-Host "✅ API endpoints accessible" -ForegroundColor Green
Write-Host "✅ Test files ready" -ForegroundColor Green
Write-Host "✅ Results directory created" -ForegroundColor Green
Write-Host ""
Write-Host "🚀 Ready to Run Load Tests!" -ForegroundColor Green
Write-Host ""
Write-Host "📖 Quick Start Commands:" -ForegroundColor Yellow
Write-Host "1. Simple test:     .\load-testing\run-jmeter-tests.ps1 -TestType simple" -ForegroundColor Cyan
Write-Host "2. Medium test:     .\load-testing\run-jmeter-tests.ps1 -TestType medium" -ForegroundColor Cyan
Write-Host "3. GUI mode:        .\load-testing\run-jmeter-tests.ps1 -TestType simple -GUI" -ForegroundColor Cyan
Write-Host "4. Custom test:     .\load-testing\run-jmeter-tests.ps1 -TestType custom -Users 75 -RampUp 90" -ForegroundColor Cyan
Write-Host ""
Write-Host "📚 For detailed instructions, see: load-testing/JMETER_LOAD_TESTING_GUIDE.md" -ForegroundColor Yellow
Write-Host ""
Write-Host "🎯 Recommended first test:" -ForegroundColor Green
Write-Host ".\load-testing\run-jmeter-tests.ps1 -TestType simple" -ForegroundColor Cyan

