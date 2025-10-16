# Azure App Service Deployment Guide for Brideside Backend

This guide provides step-by-step instructions for deploying your Spring Boot application to Azure App Service using Docker containers.

## 📋 Prerequisites

Before starting the deployment, ensure you have the following installed:

1. **Azure CLI** - [Download here](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)
2. **Docker Desktop** - [Download here](https://www.docker.com/products/docker-desktop)
3. **Java 21 JDK** - Already configured in your project
4. **Maven** - Already configured in your project

## 🚀 Quick Deployment (Automated)

### Option 1: Using Bash Script (Linux/macOS)
```bash
chmod +x deploy-azure.sh
./deploy-azure.sh
```

### Option 2: Using PowerShell Script (Windows)
```powershell
.\deploy-azure.ps1
```

## 📝 Manual Step-by-Step Deployment

### Step 1: Prepare Your Environment

1. **Login to Azure CLI**
   ```bash
   az login
   ```

2. **Set your subscription (if you have multiple)**
   ```bash
   az account set --subscription "Your-Subscription-ID"
   ```

### Step 2: Create Azure Resources

#### 2.1 Create Resource Group
```bash
az group create --name brideside-rg --location "East US"
```

#### 2.2 Create Azure Container Registry
```bash
az acr create \
  --resource-group brideside-rg \
  --name brideside \
  --sku Basic \
  --admin-enabled true
```

#### 2.3 Login to Container Registry
```bash
az acr login --name brideside
```

### Step 3: Build and Push Docker Image

#### 3.1 Build the Application
```bash
mvn clean package -DskipTests
```

#### 3.2 Build Docker Image
```bash
docker build -t brideside.azurecr.io/brideside-backend:latest .
```

#### 3.3 Push to Azure Container Registry
```bash
docker push brideside.azurecr.io/brideside-backend:latest
```

### Step 4: Create App Service

#### 4.1 Create App Service Plan
```bash
az appservice plan create \
  --name brideside-backend-plan \
  --resource-group brideside-rg \
  --is-linux \
  --sku B1
```

#### 4.2 Create Web App
```bash
az webapp create \
  --resource-group brideside-rg \
  --plan brideside-backend-plan \
  --name brideside-backend \
  --deployment-container-image-name brideside.azurecr.io/brideside-backend:latest
```

### Step 5: Configure Application Settings

#### 5.1 Get Container Registry Credentials
```bash
# Get registry URL
az acr show --name brideside --resource-group brideside-rg --query loginServer --output tsv

# Get username
az acr credential show --name brideside --resource-group brideside-rg --query username --output tsv

# Get password
az acr credential show --name brideside --resource-group brideside-rg --query passwords[0].value --output tsv
```

#### 5.2 Configure App Settings
```bash
az webapp config appsettings set \
  --resource-group brideside-rg \
  --name brideside-backend \
  --settings \
    DOCKER_REGISTRY_SERVER_URL="https://brideside.azurecr.io" \
    DOCKER_REGISTRY_SERVER_USERNAME="brideside" \
    DOCKER_REGISTRY_SERVER_PASSWORD="your-registry-password" \
    WEBSITES_ENABLE_APP_SERVICE_STORAGE="false" \
    SPRING_PROFILES_ACTIVE="azure" \
    DB_HOST="thebrideside.mysql.database.azure.com:3306" \
    DB_DATABASE="thebrideside" \
    DB_USER="thebrideside" \
    DB_PASSWORD="TheBride@260799" \
    DB_SSL="true"
```

### Step 6: Deploy and Test

#### 6.1 Restart the Application
```bash
az webapp restart --name brideside-backend --resource-group brideside-rg
```

#### 6.2 Get Application URL
```bash
az webapp show --name brideside-backend --resource-group brideside-rg --query defaultHostName --output tsv
```

#### 6.3 Test Your Deployment
- **Application URL**: `https://brideside-backend.azurewebsites.net`
- **Swagger UI**: `https://brideside-backend.azurewebsites.net/swagger-ui.html`
- **Health Check**: `https://brideside-backend.azurewebsites.net/actuator/health`

## 🔧 Configuration Details

### Environment Variables

Your application uses the following environment variables in Azure:

| Variable | Value | Description |
|----------|-------|-------------|
| `SPRING_PROFILES_ACTIVE` | `azure` | Activates Azure-specific configuration |
| `DB_HOST` | `thebrideside.mysql.database.azure.com:3306` | Database host |
| `DB_DATABASE` | `thebrideside` | Database name |
| `DB_USER` | `thebrideside` | Database username |
| `DB_PASSWORD` | `TheBride@260799` | Database password |
| `DB_SSL` | `true` | Enable SSL for database connection |

### Application Profiles

The application includes an Azure-specific profile (`application-azure.yml`) that:
- Configures logging to Azure's log directory
- Sets appropriate connection pool sizes
- Disables verbose SQL logging for production
- Configures management endpoints for monitoring

## 📊 Monitoring and Logs

### View Application Logs
```bash
az webapp log tail --name brideside-backend --resource-group brideside-rg
```

### Download Logs
```bash
az webapp log download --name brideside-backend --resource-group brideside-rg --log-file logs.zip
```

### Monitor Application Metrics
- Go to Azure Portal → App Services → brideside-backend → Monitoring
- View metrics like CPU, Memory, Requests, etc.

## 🔄 Continuous Deployment

### Option 1: GitHub Actions (Recommended)

Create `.github/workflows/azure-deploy.yml`:

```yaml
name: Deploy to Azure App Service

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 21
      uses: actions/setup-java@v2
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Cache Maven packages
      uses: actions/cache@v2
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Build with Maven
      run: mvn clean package -DskipTests
    
    - name: Azure Login
      uses: azure/login@v1
      with:
        creds: ${{ secrets.AZURE_CREDENTIALS }}
    
    - name: Build and push Docker image
      run: |
        az acr login --name brideside
        docker build -t brideside.azurecr.io/brideside-backend:${{ github.sha }} .
        docker push brideside.azurecr.io/brideside-backend:${{ github.sha }}
    
    - name: Deploy to Azure App Service
      uses: azure/webapps-deploy@v2
      with:
        app-name: 'brideside-backend'
        slot-name: 'production'
        publish-profile: ${{ secrets.AZUREAPPSERVICE_PUBLISHPROFILE }}
```

### Option 2: Azure DevOps

1. Create a new pipeline in Azure DevOps
2. Connect to your repository
3. Use the provided `azure-pipelines.yml` template

## 🛠️ Troubleshooting

### Common Issues

#### 1. Application Won't Start
- Check logs: `az webapp log tail --name brideside-backend --resource-group brideside-rg`
- Verify environment variables are set correctly
- Ensure database connection is accessible from Azure

#### 2. Database Connection Issues
- Verify your Azure MySQL server allows connections from Azure App Service
- Check firewall rules in Azure Database for MySQL
- Ensure SSL is properly configured

#### 3. Container Registry Issues
- Verify you're logged into the correct registry
- Check registry credentials in app settings
- Ensure the image was pushed successfully

#### 4. Performance Issues
- Upgrade to a higher App Service Plan (P1V2, P2V2, etc.)
- Enable Application Insights for detailed monitoring
- Review database query performance

### Useful Commands

```bash
# View app settings
az webapp config appsettings list --name brideside-backend --resource-group brideside-rg

# Update app setting
az webapp config appsettings set --name brideside-backend --resource-group brideside-rg --settings NEW_SETTING="value"

# View deployment logs
az webapp deployment log show --name brideside-backend --resource-group brideside-rg

# Scale up/down
az appservice plan update --name brideside-backend-plan --resource-group brideside-rg --sku P1V2
```

## 💰 Cost Optimization

### App Service Plans

| Plan | Price/Month | CPU | RAM | Best For |
|------|-------------|-----|-----|----------|
| B1 | ~$13 | 1 core | 1.75 GB | Development/Testing |
| P1V2 | ~$73 | 1 core | 3.5 GB | Production (small) |
| P2V2 | ~$146 | 2 cores | 7 GB | Production (medium) |
| P3V2 | ~$292 | 4 cores | 14 GB | Production (large) |

### Recommendations
- Use **B1** for development and testing
- Use **P1V2** or higher for production
- Enable **Always On** for production apps
- Consider **Auto-scaling** for variable workloads

## 🔒 Security Best Practices

1. **Use Azure Key Vault** for sensitive configuration
2. **Enable HTTPS only** in App Service settings
3. **Configure custom domains** with SSL certificates
4. **Use managed identities** instead of connection strings
5. **Regular security updates** for base images
6. **Enable Azure Security Center** monitoring

## 📞 Support

If you encounter issues:
1. Check the [Azure App Service documentation](https://docs.microsoft.com/en-us/azure/app-service/)
2. Review application logs
3. Check Azure Service Health
4. Contact Azure Support if needed

---

**Happy Deploying! 🚀**
