#!/bin/bash

# Azure Deployment Script for Brideside Backend
# This script automates the deployment process to Azure App Service

set -e  # Exit on any error

# Configuration variables
RESOURCE_GROUP="brideside-rg"
APP_NAME="brideside-backend"
REGISTRY_NAME="brideside"
LOCATION="East US"
SKU="B1"  # Basic tier - change to P1V2 for production

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Azure CLI is installed
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    if ! command -v az &> /dev/null; then
        print_error "Azure CLI is not installed. Please install it first."
        print_status "Installation guide: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli"
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install it first."
        exit 1
    fi
    
    print_success "Prerequisites check passed"
}

# Login to Azure
azure_login() {
    print_status "Logging into Azure..."
    az login
    print_success "Successfully logged into Azure"
}

# Create resource group
create_resource_group() {
    print_status "Creating resource group: $RESOURCE_GROUP"
    
    if az group show --name $RESOURCE_GROUP &> /dev/null; then
        print_warning "Resource group $RESOURCE_GROUP already exists"
    else
        az group create --name $RESOURCE_GROUP --location "$LOCATION"
        print_success "Resource group created successfully"
    fi
}

# Create Azure Container Registry
create_container_registry() {
    print_status "Creating Azure Container Registry: $REGISTRY_NAME"
    
    if az acr show --name $REGISTRY_NAME --resource-group $RESOURCE_GROUP &> /dev/null; then
        print_warning "Container registry $REGISTRY_NAME already exists"
    else
        az acr create \
            --resource-group $RESOURCE_GROUP \
            --name $REGISTRY_NAME \
            --sku Basic \
            --admin-enabled true
        print_success "Container registry created successfully"
    fi
}

# Login to Azure Container Registry
login_to_registry() {
    print_status "Logging into Azure Container Registry..."
    az acr login --name $REGISTRY_NAME
    print_success "Successfully logged into container registry"
}

# Build and push Docker image
build_and_push_image() {
    print_status "Building Docker image..."
    
    # Build the application
    mvn clean package -DskipTests
    
    # Build Docker image
    docker build -t $REGISTRY_NAME.azurecr.io/brideside-backend:latest .
    
    print_status "Pushing Docker image to registry..."
    docker push $REGISTRY_NAME.azurecr.io/brideside-backend:latest
    
    print_success "Docker image built and pushed successfully"
}

# Create App Service Plan
create_app_service_plan() {
    print_status "Creating App Service Plan..."
    
    if az appservice plan show --name $APP_NAME-plan --resource-group $RESOURCE_GROUP &> /dev/null; then
        print_warning "App Service Plan already exists"
    else
        az appservice plan create \
            --name $APP_NAME-plan \
            --resource-group $RESOURCE_GROUP \
            --is-linux \
            --sku $SKU
        print_success "App Service Plan created successfully"
    fi
}

# Create Web App
create_web_app() {
    print_status "Creating Web App..."
    
    if az webapp show --name $APP_NAME --resource-group $RESOURCE_GROUP &> /dev/null; then
        print_warning "Web App already exists"
    else
        az webapp create \
            --resource-group $RESOURCE_GROUP \
            --plan $APP_NAME-plan \
            --name $APP_NAME \
            --deployment-container-image-name $REGISTRY_NAME.azurecr.io/brideside-backend:latest
        print_success "Web App created successfully"
    fi
}

# Configure app settings
configure_app_settings() {
    print_status "Configuring app settings..."
    
    # Get registry credentials
    REGISTRY_URL=$(az acr show --name $REGISTRY_NAME --resource-group $RESOURCE_GROUP --query loginServer --output tsv)
    REGISTRY_USERNAME=$(az acr credential show --name $REGISTRY_NAME --resource-group $RESOURCE_GROUP --query username --output tsv)
    REGISTRY_PASSWORD=$(az acr credential show --name $REGISTRY_NAME --resource-group $RESOURCE_GROUP --query passwords[0].value --output tsv)
    
    az webapp config appsettings set \
        --resource-group $RESOURCE_GROUP \
        --name $APP_NAME \
        --settings \
            DOCKER_REGISTRY_SERVER_URL="https://$REGISTRY_URL" \
            DOCKER_REGISTRY_SERVER_USERNAME="$REGISTRY_USERNAME" \
            DOCKER_REGISTRY_SERVER_PASSWORD="$REGISTRY_PASSWORD" \
            WEBSITES_ENABLE_APP_SERVICE_STORAGE="false" \
            SPRING_PROFILES_ACTIVE="azure" \
            TZ="Asia/Kolkata" \
            JAVA_OPTS="-Duser.timezone=Asia/Kolkata" \
            DB_HOST="thebrideside.mysql.database.azure.com:3306" \
            DB_DATABASE="thebrideside" \
            DB_USER="thebrideside" \
            DB_PASSWORD="TheBride@260799" \
            DB_SSL="true"
    
    print_success "App settings configured successfully"
}

# Deploy the application
deploy_application() {
    print_status "Deploying application..."
    
    az webapp restart --name $APP_NAME --resource-group $RESOURCE_GROUP
    
    print_success "Application deployed successfully"
}

# Get application URL
get_app_url() {
    APP_URL=$(az webapp show --name $APP_NAME --resource-group $RESOURCE_GROUP --query defaultHostName --output tsv)
    print_success "Application is available at: https://$APP_URL"
    print_success "Swagger UI: https://$APP_URL/swagger-ui.html"
    print_success "Health Check: https://$APP_URL/actuator/health"
}

# Main deployment function
main() {
    print_status "Starting Azure deployment for Brideside Backend..."
    
    check_prerequisites
    azure_login
    create_resource_group
    create_container_registry
    login_to_registry
    build_and_push_image
    create_app_service_plan
    create_web_app
    configure_app_settings
    deploy_application
    get_app_url
    
    print_success "Deployment completed successfully!"
    print_warning "Remember to configure your database connection string in Azure App Service settings"
}

# Run main function
main "$@"
