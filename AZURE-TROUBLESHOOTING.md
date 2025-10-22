# GitHub Actions Azure Deployment Troubleshooting Guide

## 🚨 **Common Issues and Solutions**

### **Issue 1: "Unexpected input(s) 'enable-AZURE_HTTP_USER_AGENT'"**
**✅ FIXED:** Removed the invalid parameter from the workflow.

### **Issue 2: "Not all values are present. Ensure 'client-id' and 'tenant-id' are supplied"**

**Root Cause:** The `AZURE_CREDENTIALS` secret is missing or malformed.

**Solution:**
1. **Check GitHub Secrets:**
   - Go to your repo → Settings → Secrets and variables → Actions
   - Verify `AZURE_CREDENTIALS` exists and is properly formatted

2. **Correct Format:**
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

3. **Test Credentials:**
```bash
# Run the test script
./test-azure-credentials.sh
```

### **Issue 3: "Authentication failed"**

**Solutions:**
1. **Verify Service Principal Permissions:**
```bash
az role assignment list --assignee your-client-id --output table
```

2. **Recreate Service Principal:**
```bash
az ad sp create-for-rbac --name "brideside-github-actions" \
  --role contributor \
  --scopes /subscriptions/your-subscription-id/resourceGroups/brideside-rg \
  --sdk-auth
```

### **Issue 4: "Resource group not found"**

**Solution:**
```bash
az group create --name brideside-rg --location "Central India"
```

### **Issue 5: "Container registry not found"**

**Solution:**
```bash
az acr create --resource-group brideside-rg \
  --name brideside \
  --sku Basic \
  --admin-enabled true
```

### **Issue 6: "App Service not found"**

**Solution:**
```bash
# Create App Service Plan first
az appservice plan create --name brideside-plan \
  --resource-group brideside-rg \
  --sku B1 \
  --is-linux

# Create App Service
az webapp create --resource-group brideside-rg \
  --plan brideside-plan \
  --name brideside-backend \
  --deployment-local-git
```

## 🔧 **Quick Fix Commands**

### **Complete Setup (Run these in order):**
```bash
# 1. Login to Azure
az login

# 2. Set subscription
az account set --subscription "your-subscription-id"

# 3. Create resource group
az group create --name brideside-rg --location "Central India"

# 4. Create App Service Plan
az appservice plan create --name brideside-plan \
  --resource-group brideside-rg \
  --sku B1 \
  --is-linux

# 5. Create App Service
az webapp create --resource-group brideside-rg \
  --plan brideside-plan \
  --name brideside-backend \
  --deployment-local-git

# 6. Create Container Registry
az acr create --resource-group brideside-rg \
  --name brideside \
  --sku Basic \
  --admin-enabled true

# 7. Create Service Principal
az ad sp create-for-rbac --name "brideside-github-actions" \
  --role contributor \
  --scopes /subscriptions/your-subscription-id/resourceGroups/brideside-rg \
  --sdk-auth
```

### **Get Publish Profile:**
```bash
az webapp deployment list-publishing-profiles \
  --name brideside-backend \
  --resource-group brideside-rg \
  --xml
```

## 📋 **GitHub Secrets Checklist**

- [ ] `AZURE_CREDENTIALS` - Service principal JSON
- [ ] `AZUREAPPSERVICE_PUBLISHPROFILE` - App Service publish profile XML
- [ ] `PIPEDRIVE_API_TOKEN` - Your Pipedrive API token (optional)
- [ ] `DB_PASSWORD` - Database password (optional)
- [ ] `DB_HOST` - Database host (optional)

## 🎯 **Deployment Steps**

1. **Setup Azure Resources:**
   ```bash
   ./setup-azure-credentials.sh
   ```

2. **Test Credentials:**
   ```bash
   ./test-azure-credentials.sh
   ```

3. **Add GitHub Secrets:**
   - Copy the output from setup script
   - Add to GitHub repository secrets

4. **Deploy:**
   - Push code to trigger GitHub Action
   - Monitor in Actions tab

## 🔍 **Debugging Tips**

### **Check GitHub Action Logs:**
1. Go to your repo → Actions tab
2. Click on the failed workflow
3. Expand each step to see detailed logs

### **Test Azure CLI Locally:**
```bash
# Test service principal login
az login --service-principal \
  --username your-client-id \
  --password your-client-secret \
  --tenant your-tenant-id

# Test resource access
az group list
az webapp list --resource-group brideside-rg
az acr list --resource-group brideside-rg
```

### **Common Error Messages:**

| Error | Solution |
|-------|----------|
| `Not all values are present` | Check `AZURE_CREDENTIALS` secret format |
| `Authentication failed` | Verify service principal permissions |
| `Resource group not found` | Create resource group |
| `Container registry not found` | Create ACR |
| `App Service not found` | Create App Service |
| `Process terminated with exit code 137` | Out of memory - increase runner resources |

## 🚀 **Success Indicators**

✅ **Build Step:** Maven build completes successfully  
✅ **Test Step:** All tests pass  
✅ **Azure Login:** Login step completes without errors  
✅ **Docker Build:** Docker image builds and pushes to ACR  
✅ **Deploy Step:** App Service deployment succeeds  
✅ **Verify Step:** Health check passes  

## 📞 **Need Help?**

1. **Check the logs** in GitHub Actions
2. **Run the setup script** to recreate resources
3. **Verify secrets** are properly formatted
4. **Test Azure CLI** locally with the same credentials

---

**Remember:** The most common issue is malformed `AZURE_CREDENTIALS`. Make sure it's valid JSON with all required fields!
