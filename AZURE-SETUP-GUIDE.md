# Azure Setup Guide for GitHub Actions

This guide will help you set up Azure authentication for your GitHub Actions workflow.

## 🔧 **Step 1: Create Azure Service Principal**

### Option A: Using Azure CLI (Recommended)

```bash
# Login to Azure
az login

# Set your subscription (replace with your subscription ID)
az account set --subscription "your-subscription-id"

# Create a resource group (if not exists)
az group create --name brideside-rg --location "Central India"

# Create service principal
az ad sp create-for-rbac --name "brideside-github-actions" \
  --role contributor \
  --scopes /subscriptions/your-subscription-id/resourceGroups/brideside-rg \
  --sdk-auth
```

### Option B: Using Azure Portal

1. Go to [Azure Portal](https://portal.azure.com)
2. Navigate to **Azure Active Directory** > **App registrations**
3. Click **New registration**
4. Name: `brideside-github-actions`
5. Click **Register**
6. Note down the **Application (client) ID** and **Directory (tenant) ID**
7. Go to **Certificates & secrets** > **New client secret**
8. Add description: `GitHub Actions Secret`
9. Note down the **Value** (this is your client secret)

## 🔑 **Step 2: Set GitHub Secrets**

Go to your GitHub repository > **Settings** > **Secrets and variables** > **Actions**

Add these secrets:

### Required Secrets:

1. **`AZURE_CREDENTIALS`** - The JSON output from the service principal creation:
```json
{
  "clientId": "your-client-id",
  "clientSecret": "your-client-secret",
  "subscriptionId": "your-subscription-id",
  "tenantId": "your-tenant-id",
  "activeDirectoryEndpointUrl": "https://login.microsoftonline.com",
  "resourceManagerEndpointUrl": "https://management.azure.com/",
  "activeDirectoryGraphResourceId": "https://graph.windows.net/",
  "sqlManagementEndpointUrl": "https://management.core.windows.net:8443/",
  "galleryEndpointUrl": "https://gallery.azure.com/",
  "managementEndpointUrl": "https://management.core.windows.net/"
}
```

2. **`AZUREAPPSERVICE_PUBLISHPROFILE`** - Get this from your App Service:
   - Go to Azure Portal > App Services > your-app-name
   - Click **Get publish profile**
   - Copy the entire XML content

### Optional Secrets (if using different values):

3. **`PIPEDRIVE_API_TOKEN`** - Your Pipedrive API token
4. **`DB_PASSWORD`** - Your database password
5. **`DB_HOST`** - Your database host

## 🚀 **Step 3: Update Azure Resources**

Make sure these Azure resources exist:

### App Service:
```bash
az webapp create --resource-group brideside-rg \
  --plan brideside-plan \
  --name brideside-backend \
  --deployment-local-git
```

### Container Registry:
```bash
az acr create --resource-group brideside-rg \
  --name brideside \
  --sku Basic \
  --admin-enabled true
```

### App Service Plan:
```bash
az appservice plan create --name brideside-plan \
  --resource-group brideside-rg \
  --sku B1 \
  --is-linux
```

## 🔍 **Step 4: Verify Setup**

1. **Check GitHub Secrets**: Go to your repo > Settings > Secrets and variables > Actions
2. **Test Azure Login**: Run this locally to test:
```bash
az login --service-principal \
  --username your-client-id \
  --password your-client-secret \
  --tenant your-tenant-id
```

## 🐛 **Troubleshooting**

### Common Issues:

1. **"Not all values are present"**:
   - Check that `AZURE_CREDENTIALS` contains all required fields
   - Ensure JSON is properly formatted (no trailing commas)

2. **"Authentication failed"**:
   - Verify the service principal has Contributor role
   - Check that the subscription ID is correct

3. **"Resource group not found"**:
   - Create the resource group: `az group create --name brideside-rg --location "Central India"`

4. **"Container registry not found"**:
   - Create the ACR: `az acr create --resource-group brideside-rg --name brideside --sku Basic`

### Quick Fix Commands:

```bash
# Get your subscription ID
az account show --query id --output tsv

# List resource groups
az group list --output table

# List app services
az webapp list --output table

# List container registries
az acr list --output table
```

## 📋 **Checklist**

- [ ] Service principal created with Contributor role
- [ ] `AZURE_CREDENTIALS` secret added to GitHub
- [ ] `AZUREAPPSERVICE_PUBLISHPROFILE` secret added to GitHub
- [ ] Resource group `brideside-rg` exists
- [ ] App Service `brideside-backend` exists
- [ ] Container Registry `brideside` exists
- [ ] App Service Plan `brideside-plan` exists

## 🎯 **Next Steps**

1. Push your code to trigger the GitHub Action
2. Monitor the workflow in the **Actions** tab
3. Check the deployment logs for any errors
4. Verify the app is running at your Azure URL

---

**Need Help?** Check the [Azure GitHub Actions documentation](https://docs.microsoft.com/en-us/azure/developer/github/connect-from-azure) for more details.
