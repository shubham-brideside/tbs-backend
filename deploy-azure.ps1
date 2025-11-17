# Azure Deployment Script for Brideside Backend (PowerShell)
# This script automates the deployment process to Azure App Service

param(
    [string]$ResourceGroup = "brideside-rg",
    [string]$AppName = "brideside-backend",
    [string]$RegistryName = "brideside",
    [string]$Location = "East US",
    [string]$Sku = "B1"
)

# Function to print colored output
function Write-Status {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Check prerequisites
function Test-Prerequisites {
    Write-Status "Checking prerequisites..."
    
    if (-not (Get-Command az -ErrorAction SilentlyContinue)) {
        Write-Error "Azure CLI is not installed. Please install it first."
        Write-Status "Installation guide: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli"
        exit 1
    }
    
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-Error "Docker is not installed. Please install it first."
        exit 1
    }
    
    Write-Success "Prerequisites check passed"
}

# Login to Azure
function Connect-Azure {
    Write-Status "Logging into Azure..."
    az login
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to login to Azure"
        exit 1
    }
    Write-Success "Successfully logged into Azure"
}

# Create resource group
function New-ResourceGroup {
    Write-Status "Creating resource group: $ResourceGroup"
    
    $existingGroup = az group show --name $ResourceGroup 2>$null
    if ($existingGroup) {
        Write-Warning "Resource group $ResourceGroup already exists"
    } else {
        az group create --name $ResourceGroup --location $Location
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Failed to create resource group"
            exit 1
        }
        Write-Success "Resource group created successfully"
    }
}

# Create Azure Container Registry
function New-ContainerRegistry {
    Write-Status "Creating Azure Container Registry: $RegistryName"
    
    $existingRegistry = az acr show --name $RegistryName --resource-group $ResourceGroup 2>$null
    if ($existingRegistry) {
        Write-Warning "Container registry $RegistryName already exists"
    } else {
        az acr create --resource-group $ResourceGroup --name $RegistryName --sku Basic --admin-enabled true
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Failed to create container registry"
            exit 1
        }
        Write-Success "Container registry created successfully"
    }
}

# Login to Azure Container Registry
function Connect-ContainerRegistry {
    Write-Status "Logging into Azure Container Registry..."
    az acr login --name $RegistryName
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to login to container registry"
        exit 1
    }
    Write-Success "Successfully logged into container registry"
}

# Build and push Docker image
function Build-PushImage {
    Write-Status "Building application..."
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to build application"
        exit 1
    }
    
    Write-Status "Building Docker image..."
    docker build -t "$RegistryName.azurecr.io/brideside-backend:latest" .
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to build Docker image"
        exit 1
    }
    
    Write-Status "Pushing Docker image to registry..."
    docker push "$RegistryName.azurecr.io/brideside-backend:latest"
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to push Docker image"
        exit 1
    }
    
    Write-Success "Docker image built and pushed successfully"
}

# Create App Service Plan
function New-AppServicePlan {
    Write-Status "Creating App Service Plan..."
    
    $existingPlan = az appservice plan show --name "$AppName-plan" --resource-group $ResourceGroup 2>$null
    if ($existingPlan) {
        Write-Warning "App Service Plan already exists"
    } else {
        az appservice plan create --name "$AppName-plan" --resource-group $ResourceGroup --is-linux --sku $Sku
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Failed to create App Service Plan"
            exit 1
        }
        Write-Success "App Service Plan created successfully"
    }
}

# Create Web App
function New-WebApp {
    Write-Status "Creating Web App..."
    
    $existingApp = az webapp show --name $AppName --resource-group $ResourceGroup 2>$null
    if ($existingApp) {
        Write-Warning "Web App already exists"
    } else {
        az webapp create --resource-group $ResourceGroup --plan "$AppName-plan" --name $AppName --deployment-container-image-name "$RegistryName.azurecr.io/brideside-backend:latest"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Failed to create Web App"
            exit 1
        }
        Write-Success "Web App created successfully"
    }
}

# Configure app settings
function Set-AppSettings {
    Write-Status "Configuring app settings..."
    
    # Get registry credentials
    $registryUrl = az acr show --name $RegistryName --resource-group $ResourceGroup --query loginServer --output tsv
    $registryUsername = az acr credential show --name $RegistryName --resource-group $ResourceGroup --query username --output tsv
    $registryPassword = az acr credential show --name $RegistryName --resource-group $ResourceGroup --query passwords[0].value --output tsv
    
    az webapp config appsettings set --resource-group $ResourceGroup --name $AppName --settings `
        DOCKER_REGISTRY_SERVER_URL="https://$registryUrl" `
        DOCKER_REGISTRY_SERVER_USERNAME="$registryUsername" `
        DOCKER_REGISTRY_SERVER_PASSWORD="$registryPassword" `
        WEBSITES_ENABLE_APP_SERVICE_STORAGE="false" `
        SPRING_PROFILES_ACTIVE="azure" `
        TZ="Asia/Kolkata" `
        JAVA_OPTS="-Duser.timezone=Asia/Kolkata" `
        DB_HOST="thebrideside.mysql.database.azure.com:3306" `
        DB_DATABASE="thebrideside" `
        DB_USER="thebrideside" `
        DB_PASSWORD="TheBride@260799" `
        DB_SSL="true"
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to configure app settings"
        exit 1
    }
    Write-Success "App settings configured successfully"
}

# Deploy the application
function Start-Deployment {
    Write-Status "Deploying application..."
    az webapp restart --name $AppName --resource-group $ResourceGroup
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to restart application"
        exit 1
    }
    Write-Success "Application deployed successfully"
}

# Get application URL
function Get-AppUrl {
    $appUrl = az webapp show --name $AppName --resource-group $ResourceGroup --query defaultHostName --output tsv
    Write-Success "Application is available at: https://$appUrl"
    Write-Success "Swagger UI: https://$appUrl/swagger-ui.html"
    Write-Success "Health Check: https://$appUrl/actuator/health"
}

# Main deployment function
function Start-AzureDeployment {
    Write-Status "Starting Azure deployment for Brideside Backend..."
    
    Test-Prerequisites
    Connect-Azure
    New-ResourceGroup
    New-ContainerRegistry
    Connect-ContainerRegistry
    Build-PushImage
    New-AppServicePlan
    New-WebApp
    Set-AppSettings
    Start-Deployment
    Get-AppUrl
    
    Write-Success "Deployment completed successfully!"
    Write-Warning "Remember to configure your database connection string in Azure App Service settings"
}

# Run main function
Start-AzureDeployment
