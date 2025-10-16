# JMeter Load Testing Script for Deals API
# This script helps you run different types of load tests easily

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("simple", "comprehensive", "light", "medium", "heavy", "spike")]
    [string]$TestType = "simple",
    
    [Parameter(Mandatory=$false)]
    [int]$Users = 0,
    
    [Parameter(Mandatory=$false)]
    [int]$RampUp = 0,
    
    [Parameter(Mandatory=$false)]
    [int]$Duration = 0,
    
    [Parameter(Mandatory=$false)]
    [switch]$GUI,
    
    [Parameter(Mandatory=$false)]
    [switch]$Help
)

# Display help
if ($Help) {
    Write-Host "JMeter Load Testing Script for Deals API" -ForegroundColor Green
    Write-Host ""
    Write-Host "Usage:" -ForegroundColor Yellow
    Write-Host "  .\run-jmeter-tests.ps1 -TestType <type> [-Users <number>] [-RampUp <seconds>] [-Duration <seconds>] [-GUI]"
    Write-Host ""
    Write-Host "Test Types:" -ForegroundColor Yellow
    Write-Host "  simple        - Simple test (20 users, 30s ramp-up, 5 iterations)"
    Write-Host "  comprehensive - Full CRUD test (50 users, 60s ramp-up, 10 iterations)"
    Write-Host "  light         - Light load test (10 users, 30s ramp-up, 5 minutes)"
    Write-Host "  medium        - Medium load test (50 users, 60s ramp-up, 10 minutes)"
    Write-Host "  heavy         - Heavy load test (100 users, 120s ramp-up, 15 minutes)"
    Write-Host "  spike         - Spike test (200 users, 10s ramp-up, 5 minutes)"
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "  -Users <number>    - Override number of users"
    Write-Host "  -RampUp <seconds>  - Override ramp-up time"
    Write-Host "  -Duration <seconds> - Override test duration"
    Write-Host "  -GUI               - Run in GUI mode"
    Write-Host "  -Help              - Show this help"
    Write-Host ""
    Write-Host "Examples:" -ForegroundColor Yellow
    Write-Host "  .\run-jmeter-tests.ps1 -TestType simple"
    Write-Host "  .\run-jmeter-tests.ps1 -TestType medium -GUI"
    Write-Host "  .\run-jmeter-tests.ps1 -TestType custom -Users 75 -RampUp 90 -Duration 600"
    exit 0
}

# Check if JMeter is installed
$jmeterPath = Get-Command jmeter -ErrorAction SilentlyContinue
if (-not $jmeterPath) {
    Write-Host "❌ JMeter not found! Please install JMeter and add it to your PATH." -ForegroundColor Red
    Write-Host "Download from: https://jmeter.apache.org/download_jmeter.cgi" -ForegroundColor Yellow
    exit 1
}

# Check if application is running
Write-Host "🔍 Checking if application is running..." -ForegroundColor Yellow
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
    Write-Host "Please start your Spring Boot application first!" -ForegroundColor Yellow
    exit 1
}

# Test configurations
$testConfigs = @{
    "simple" = @{
        file = "simple-jmeter-test.jmx"
        users = 20
        rampUp = 30
        duration = 0
        description = "Simple test (Create + Read operations)"
    }
    "comprehensive" = @{
        file = "jmeter-deals-api.jmx"
        users = 50
        rampUp = 60
        duration = 0
        description = "Comprehensive test (All CRUD operations)"
    }
    "light" = @{
        file = "jmeter-deals-api.jmx"
        users = 10
        rampUp = 30
        duration = 300
        description = "Light load test (5 minutes)"
    }
    "medium" = @{
        file = "jmeter-deals-api.jmx"
        users = 50
        rampUp = 60
        duration = 600
        description = "Medium load test (10 minutes)"
    }
    "heavy" = @{
        file = "jmeter-deals-api.jmx"
        users = 100
        rampUp = 120
        duration = 900
        description = "Heavy load test (15 minutes)"
    }
    "spike" = @{
        file = "jmeter-deals-api.jmx"
        users = 200
        rampUp = 10
        duration = 300
        description = "Spike test (5 minutes)"
    }
}

# Get test configuration
$config = $testConfigs[$TestType]
if (-not $config) {
    Write-Host "❌ Invalid test type: $TestType" -ForegroundColor Red
    Write-Host "Use -Help to see available test types" -ForegroundColor Yellow
    exit 1
}

# Override with custom values if provided
if ($Users -gt 0) { $config.users = $Users }
if ($RampUp -gt 0) { $config.rampUp = $RampUp }
if ($Duration -gt 0) { $config.duration = $Duration }

# Display test configuration
Write-Host ""
Write-Host "🚀 Starting JMeter Load Test" -ForegroundColor Green
Write-Host "Test Type: $TestType" -ForegroundColor Cyan
Write-Host "Description: $($config.description)" -ForegroundColor Cyan
Write-Host "Users: $($config.users)" -ForegroundColor Cyan
Write-Host "Ramp-up: $($config.rampUp) seconds" -ForegroundColor Cyan
if ($config.duration -gt 0) {
    Write-Host "Duration: $($config.duration) seconds" -ForegroundColor Cyan
}
Write-Host ""

# Check if test file exists
$testFile = "load-testing/$($config.file)"
if (-not (Test-Path $testFile)) {
    Write-Host "❌ Test file not found: $testFile" -ForegroundColor Red
    exit 1
}

# Create results directory
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$resultsDir = "load-testing/results-$TestType-$timestamp"
New-Item -ItemType Directory -Path $resultsDir -Force | Out-Null

# Generate results file names
$resultsFile = "$resultsDir/results.jtl"
$reportDir = "$resultsDir/report"

Write-Host "📁 Results will be saved to: $resultsDir" -ForegroundColor Yellow
Write-Host ""

# Run JMeter test
if ($GUI) {
    Write-Host "🖥️  Starting JMeter in GUI mode..." -ForegroundColor Green
    Write-Host "Please run the test manually in the JMeter GUI" -ForegroundColor Yellow
    Write-Host "Test file: $testFile" -ForegroundColor Cyan
    Start-Process "jmeter" -ArgumentList "-t", $testFile
} else {
    Write-Host "⚡ Starting JMeter load test..." -ForegroundColor Green
    Write-Host "This may take a while depending on the test configuration..." -ForegroundColor Yellow
    Write-Host ""
    
    # Build JMeter command
    $jmeterArgs = @(
        "-n"
        "-t", $testFile
        "-l", $resultsFile
        "-e"
        "-o", $reportDir
    )
    
    # Add custom properties if specified
    if ($Users -gt 0) { $jmeterArgs += "-Jusers=$Users" }
    if ($RampUp -gt 0) { $jmeterArgs += "-Jrampup=$RampUp" }
    if ($Duration -gt 0) { $jmeterArgs += "-Jduration=$Duration" }
    
    # Run the test
    try {
        $process = Start-Process "jmeter" -ArgumentList $jmeterArgs -Wait -PassThru -NoNewWindow
        
        if ($process.ExitCode -eq 0) {
            Write-Host ""
            Write-Host "✅ Load test completed successfully!" -ForegroundColor Green
            Write-Host ""
            Write-Host "📊 Results Summary:" -ForegroundColor Yellow
            Write-Host "Results file: $resultsFile" -ForegroundColor Cyan
            Write-Host "HTML report: $reportDir/index.html" -ForegroundColor Cyan
            Write-Host ""
            Write-Host "🌐 Open the HTML report in your browser to view detailed results" -ForegroundColor Green
            Write-Host "File: $reportDir/index.html" -ForegroundColor Cyan
            
            # Try to open the report
            if (Test-Path "$reportDir/index.html") {
                Write-Host ""
                Write-Host "🔍 Opening HTML report..." -ForegroundColor Yellow
                Start-Process "$reportDir/index.html"
            }
        } else {
            Write-Host "❌ Load test failed with exit code: $($process.ExitCode)" -ForegroundColor Red
        }
    } catch {
        Write-Host "❌ Error running JMeter: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "📋 Test completed!" -ForegroundColor Green

