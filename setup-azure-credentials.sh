#!/bin/bash

# Azure Setup Script for GitHub Actions
# This script helps you create the necessary Azure resources and get the credentials

set -e

echo "🚀 Azure Setup for GitHub Actions"
echo "=================================="

# Check if Azure CLI is installed
if ! command -v az &> /dev/null; then
    echo "❌ Azure CLI is not installed. Please install it first:"
    echo "   https://docs.microsoft.com/en-us/cli/azure/install-azure-cli"
    exit 1
fi

# Check if logged in
if ! az account show &> /dev/null; then
    echo "🔐 Please login to Azure first:"
    az login
fi

# Get subscription info
SUBSCRIPTION_ID=$(az account show --query id --output tsv)
SUBSCRIPTION_NAME=$(az account show --query name --output tsv)

echo "📋 Current subscription: $SUBSCRIPTION_NAME ($SUBSCRIPTION_ID)"

# Set variables
RESOURCE_GROUP="brideside-rg"
LOCATION="Central India"
SERVICE_PRINCIPAL_NAME="brideside-github-actions"
APP_NAME="brideside-backend"
ACR_NAME="brideside"
PLAN_NAME="brideside-plan"

echo ""
echo "🔧 Creating Azure resources..."

# Create resource group
echo "Creating resource group: $RESOURCE_GROUP"
az group create --name $RESOURCE_GROUP --location "$LOCATION" --output none

# Create App Service Plan
echo "Creating App Service Plan: $PLAN_NAME"
az appservice plan create \
    --name $PLAN_NAME \
    --resource-group $RESOURCE_GROUP \
    --sku B1 \
    --is-linux \
    --output none

# Create App Service
echo "Creating App Service: $APP_NAME"
az webapp create \
    --resource-group $RESOURCE_GROUP \
    --plan $PLAN_NAME \
    --name $APP_NAME \
    --deployment-local-git \
    --output none

# Create Container Registry
echo "Creating Container Registry: $ACR_NAME"
az acr create \
    --resource-group $RESOURCE_GROUP \
    --name $ACR_NAME \
    --sku Basic \
    --admin-enabled true \
    --output none

# Create Service Principal
echo "Creating Service Principal: $SERVICE_PRINCIPAL_NAME"
SERVICE_PRINCIPAL_JSON=$(az ad sp create-for-rbac \
    --name $SERVICE_PRINCIPAL_NAME \
    --role contributor \
    --scopes /subscriptions/$SUBSCRIPTION_ID/resourceGroups/$RESOURCE_GROUP \
    --sdk-auth)

echo ""
echo "✅ Azure resources created successfully!"
echo ""
echo "🔑 GitHub Secrets Setup:"
echo "========================"
echo ""
echo "1. Go to your GitHub repository"
echo "2. Navigate to Settings > Secrets and variables > Actions"
echo "3. Add these secrets:"
echo ""
echo "Secret Name: AZURE_CREDENTIALS"
echo "Secret Value:"
echo "$SERVICE_PRINCIPAL_JSON"
echo ""

# Get publish profile
echo "Getting App Service publish profile..."
PUBLISH_PROFILE=$(az webapp deployment list-publishing-profiles \
    --name $APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --xml)

echo "Secret Name: AZUREAPPSERVICE_PUBLISHPROFILE"
echo "Secret Value:"
echo "$PUBLISH_PROFILE"
echo ""

echo "🎯 Next Steps:"
echo "=============="
echo "1. Add the secrets above to your GitHub repository"
echo "2. Push your code to trigger the GitHub Action"
echo "3. Monitor the deployment in the Actions tab"
echo ""
echo "🌐 Your app will be available at:"
echo "   https://$APP_NAME.azurewebsites.net"
echo "   https://$APP_NAME.azurewebsites.net/swagger-ui.html"
echo ""
echo "✨ Setup complete! Happy deploying! 🚀"
