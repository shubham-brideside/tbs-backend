# CORS 403 Error Troubleshooting Guide

If you're still getting 403 errors after updating the SecurityConfig, follow these steps:

## Step 1: Verify Changes Are Deployed

The changes to `SecurityConfig.java` must be rebuilt and redeployed to Azure:

```bash
# Build the application
mvn clean package -DskipTests

# Deploy to Azure (use your deployment script)
./deploy-azure.sh
# OR
.\deploy-azure.ps1
```

## Step 2: Check Azure App Service CORS Settings

Azure App Service has platform-level CORS settings that can override your application settings:

1. Go to Azure Portal → Your App Service
2. Navigate to **API** → **CORS**
3. Check if CORS is enabled at the platform level
4. If enabled, either:
   - **Disable platform CORS** (recommended - let Spring handle it)
   - **OR** add `http://localhost:5173` to the allowed origins list

## Step 3: Clear Browser Cache

Sometimes browsers cache CORS responses:
- Clear browser cache
- Try in an incognito/private window
- Hard refresh (Ctrl+Shift+R or Cmd+Shift+R)

## Step 4: Verify the Request

Check the Network tab in browser DevTools:
- Look for the `OPTIONS` preflight request
- Check the `Access-Control-Allow-Origin` header in the response
- Verify the `Origin` header in the request matches an allowed pattern

## Step 5: Test with curl

Test the API directly to verify CORS headers:

```bash
# Test OPTIONS preflight request
curl -X OPTIONS \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v \
  https://thebrideside-agdnavgxhhcffpby.centralindia-01.azurewebsites.net/api/blog/posts

# Check for Access-Control-Allow-Origin in response
```

## Step 6: Check Application Logs

Check Azure App Service logs for errors:
- Azure Portal → Your App Service → Log stream
- Look for CORS-related errors or security exceptions

## Step 7: Alternative: Disable Spring Security for Blog Endpoints

If the issue persists, you can create a separate security configuration that bypasses security entirely for blog endpoints (not recommended for production, but useful for testing):

```java
// Add this to SecurityConfig if needed for testing
.requestMatchers("/api/blog/**").permitAll()
```

## Quick Fix: Test Locally First

Test the configuration locally before deploying:

```bash
# Run locally
mvn spring-boot:run

# In another terminal, test with curl
curl -X GET \
  -H "Origin: http://localhost:5173" \
  -v \
  http://localhost:8080/api/blog/posts
```

## Common Issues

1. **Azure Platform CORS**: Azure App Service CORS settings override application settings
2. **Deployment Delay**: Changes take a few minutes to deploy
3. **Cache Issues**: Browser or Azure CDN caching old responses
4. **Port Mismatch**: Frontend running on different port than expected

## Verify Configuration

After deployment, verify the configuration is active:

```bash
# Check if the application is running
curl https://thebrideside-agdnavgxhhcffpby.centralindia-01.azurewebsites.net/actuator/health

# Test a blog endpoint
curl https://thebrideside-agdnavgxhhcffpby.centralindia-01.azurewebsites.net/api/blog/categories
```

