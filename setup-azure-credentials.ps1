# Azure Setup Script for GitHub Actions (PowerShell)
# This script helps you create the necessary Azure resources and get the credentials

Write-Host "🚀 Azure Setup for GitHub Actions" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green

# Check if Azure CLI is installed
try {
    $azVersion = az version --output tsv 2>$null
    if (-not $azVersion) {
        throw "Azure CLI not found"
    }
    Write-Host "✅ Azure CLI is installed" -ForegroundColor Green
} catch {
    Write-Host "❌ Azure CLI is not installed. Please install it first:" -ForegroundColor Red
    Write-Host "   https://docs.microsoft.com/en-us/cli/azure/install-azure-cli" -ForegroundColor Yellow
    exit 1
}

# Check if logged in
try {
    $account = az account show --output json 2>$null | ConvertFrom-Json
    if (-not $account) {
        throw "Not logged in"
    }
    Write-Host "✅ Logged in to Azure" -ForegroundColor Green
} catch {
    Write-Host "🔐 Please login to Azure first:" -ForegroundColor Yellow
    az login
    $account = az account show --output json | ConvertFrom-Json
}

$SUBSCRIPTION_ID = $account.id
$SUBSCRIPTION_NAME = $account.name

Write-Host "📋 Current subscription: $SUBSCRIPTION_NAME ($SUBSCRIPTION_ID)" -ForegroundColor Cyan

# Set variables
$RESOURCE_GROUP = "brideside-rg"
$LOCATION = "Central India"
$SERVICE_PRINCIPAL_NAME = "brideside-github-actions"
$APP_NAME = "brideside-backend"
$ACR_NAME = "brideside"
$PLAN_NAME = "brideside-plan"

Write-Host ""
Write-Host "🔧 Creating Azure resources..." -ForegroundColor Yellow

# Create resource group
Write-Host "Creating resource group: $RESOURCE_GROUP" -ForegroundColor Cyan
az group create --name $RESOURCE_GROUP --location "$LOCATION" --output none

# Create App Service Plan
Write-Host "Creating App Service Plan: $PLAN_NAME" -ForegroundColor Cyan
az appservice plan create `
    --name $PLAN_NAME `
    --resource-group $RESOURCE_GROUP `
    --sku B1 `
    --is-linux `
    --output none

# Create App Service
Write-Host "Creating App Service: $APP_NAME" -ForegroundColor Cyan
az webapp create `
    --resource-group $RESOURCE_GROUP `
    --plan $PLAN_NAME `
    --name $APP_NAME `
    --deployment-local-git `
    --output none

# Create Container Registry
Write-Host "Creating Container Registry: $ACR_NAME" -ForegroundColor Cyan
az acr create `
    --resource-group $RESOURCE_GROUP `
    --name $ACR_NAME `
    --sku Basic `
    --admin-enabled true `
    --output none

# Create Service Principal
Write-Host "Creating Service Principal: $SERVICE_PRINCIPAL_NAME" -ForegroundColor Cyan
$SERVICE_PRINCIPAL_JSON = az ad sp create-for-rbac `
    --name $SERVICE_PRINCIPAL_NAME `
    --role contributor `
    --scopes "/subscriptions/$SUBSCRIPTION_ID/resourceGroups/$RESOURCE_GROUP" `
    --sdk-auth

Write-Host ""
Write-Host "✅ Azure resources created successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "🔑 GitHub Secrets Setup:" -ForegroundColor Yellow
Write-Host "========================" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Go to your GitHub repository" -ForegroundColor White
Write-Host "2. Navigate to Settings > Secrets and variables > Actions" -ForegroundColor White
Write-Host "3. Add these secrets:" -ForegroundColor White
Write-Host ""
Write-Host "Secret Name: AZURE_CREDENTIALS" -ForegroundColor Cyan
Write-Host "Secret Value:" -ForegroundColor Cyan
Write-Host $SERVICE_PRINCIPAL_JSON -ForegroundColor White
Write-Host ""

# Get publish profile
Write-Host "Getting App Service publish profile..." -ForegroundColor Yellow
$PUBLISH_PROFILE = az webapp deployment list-publishing-profiles `
    --name $APP_NAME `
    --resource-group $RESOURCE_GROUP `
    --xml

Write-Host "Secret Name: AZUREAPPSERVICE_PUBLISHPROFILE" -ForegroundColor Cyan
Write-Host "Secret Value:" -ForegroundColor Cyan
Write-Host $PUBLISH_PROFILE -ForegroundColor White
Write-Host ""

Write-Host "🎯 Next Steps:" -ForegroundColor Yellow
Write-Host "==============" -ForegroundColor Yellow
Write-Host "1. Add the secrets above to your GitHub repository" -ForegroundColor White
Write-Host "2. Push your code to trigger the GitHub Action" -ForegroundColor White
Write-Host "3. Monitor the deployment in the Actions tab" -ForegroundColor White
Write-Host ""
Write-Host "🌐 Your app will be available at:" -ForegroundColor Green
Write-Host "   https://$APP_NAME.azurewebsites.net" -ForegroundColor White
Write-Host "   https://$APP_NAME.azurewebsites.net/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "✨ Setup complete! Happy deploying! 🚀" -ForegroundColor Green
