# GitHub Actions Azure Login Fix

## 🚨 Error: "Az CLI Login failed"

This error occurs when the `AZURE_CREDENTIALS` secret is missing, malformed, or the Azure login action is outdated.

## ✅ Solution

### Step 1: Update Workflow (Already Done)
The workflow has been updated to use `azure/login@v2` with proper permissions.

### Step 2: Set Up Azure Credentials Secret

You need to create a service principal and add it as a GitHub secret.

#### Option A: Using Azure CLI (Recommended)

1. **Login to Azure:**
   ```bash
   az login
   ```

2. **Set your subscription:**
   ```bash
   az account set --subscription "your-subscription-id"
   ```

3. **Create service principal:**
   ```bash
   az ad sp create-for-rbac --name "brideside-github-actions" \
     --role contributor \
     --scopes /subscriptions/your-subscription-id/resourceGroups/brideside-rg \
     --sdk-auth
   ```

4. **Copy the JSON output** - it will look like this:
   ```json
   {
     "clientId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
     "clientSecret": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
     "subscriptionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
     "tenantId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
     "activeDirectoryEndpointUrl": "https://login.microsoftonline.com",
     "resourceManagerEndpointUrl": "https://management.azure.com/",
     "activeDirectoryGraphResourceId": "https://graph.windows.net/",
     "sqlManagementEndpointUrl": "https://management.core.windows.net:8443/",
     "galleryEndpointUrl": "https://gallery.azure.com/",
     "managementEndpointUrl": "https://management.core.windows.net/"
   }
   ```

5. **Add to GitHub Secrets:**
   - Go to your repository on GitHub
   - Navigate to **Settings** → **Secrets and variables** → **Actions**
   - Click **New repository secret**
   - Name: `AZURE_CREDENTIALS`
   - Value: Paste the entire JSON output from step 3
   - Click **Add secret**

#### Option B: Using Setup Script

Run the provided setup script:

**Windows (PowerShell):**
```powershell
.\setup-azure-credentials.ps1
```

**Linux/Mac:**
```bash
chmod +x setup-azure-credentials.sh
./setup-azure-credentials.sh
```

The script will output the JSON you need to add as `AZURE_CREDENTIALS` secret.

### Step 3: Verify Secret Format

The `AZURE_CREDENTIALS` secret must be:
- ✅ Valid JSON format
- ✅ Include all required fields: `clientId`, `clientSecret`, `subscriptionId`, `tenantId`
- ✅ No extra whitespace or line breaks
- ✅ All values must be non-empty

### Step 4: Test the Workflow

1. Push a commit to trigger the workflow
2. Go to **Actions** tab in GitHub
3. Check the workflow run
4. The "Azure Login" step should now succeed

## 🔍 Troubleshooting

### Error: "Not all values are present"
- **Cause:** The `AZURE_CREDENTIALS` secret is missing required fields
- **Fix:** Recreate the service principal and ensure all fields are present in the JSON

### Error: "Authentication failed"
- **Cause:** Service principal doesn't have proper permissions
- **Fix:** Ensure the service principal has "Contributor" role on the resource group:
  ```bash
  az role assignment create \
    --assignee <client-id> \
    --role Contributor \
    --scope /subscriptions/<subscription-id>/resourceGroups/brideside-rg
  ```

### Error: "Resource group not found"
- **Cause:** Resource group doesn't exist
- **Fix:** Create the resource group:
  ```bash
  az group create --name brideside-rg --location "Central India"
  ```

## 📋 Quick Checklist

- [ ] Azure CLI installed and logged in
- [ ] Service principal created with `--sdk-auth` flag
- [ ] `AZURE_CREDENTIALS` secret added to GitHub (valid JSON)
- [ ] Service principal has Contributor role on resource group
- [ ] Resource group `brideside-rg` exists
- [ ] Workflow file uses `azure/login@v2`
- [ ] Workflow has `id-token: write` permission

## 🎯 Alternative: Use Separate Secrets (Like main_thebrideside.yml)

If you prefer using separate secrets instead of JSON, you can update the workflow to use:

```yaml
- name: 'Azure Login'
  uses: azure/login@v2
  with:
    client-id: ${{ secrets.AZURE_CLIENT_ID }}
    tenant-id: ${{ secrets.AZURE_TENANT_ID }}
    subscription-id: ${{ secrets.AZURE_SUBSCRIPTION_ID }}
```

Then add these three separate secrets to GitHub instead of `AZURE_CREDENTIALS`.

---

**Note:** The workflow has been updated to use `azure/login@v2` which is the latest version and more reliable than v1.4.3.

