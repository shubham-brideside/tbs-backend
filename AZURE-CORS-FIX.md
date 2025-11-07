# 🔧 Azure CORS 403 Error - Complete Fix Guide

## Problem
Getting `403 Forbidden` with "No 'Access-Control-Allow-Origin' header is present" when accessing blog API from `localhost:5173`.

## Root Cause
Azure App Service has **platform-level CORS** that runs BEFORE your Spring application. If enabled, it blocks requests before Spring Security can add CORS headers.

## ✅ Solution Steps

### Step 1: Disable Azure Platform CORS (CRITICAL)

1. **Go to Azure Portal**
   - Navigate to: https://portal.azure.com
   - Find your App Service: `thebrideside-agdnavgxhhcffpby`

2. **Disable Platform CORS**
   - Go to **API** → **CORS** (in left sidebar)
   - **UNCHECK** "Enable Access-Control-Allow-Credentials" if checked
   - **CLEAR** all allowed origins (leave empty)
   - **SAVE** the changes

   ⚠️ **IMPORTANT**: Azure platform CORS must be disabled or empty for Spring Security CORS to work!

### Step 2: Rebuild and Redeploy

After disabling Azure CORS, rebuild and deploy:

```bash
# Build the application
mvn clean package -DskipTests

# Deploy to Azure
./deploy-azure.sh
# OR on Windows:
.\deploy-azure.ps1
```

### Step 3: Wait for Deployment

- Wait 2-3 minutes for Azure to restart the application
- Check deployment status in Azure Portal → Deployment Center

### Step 4: Test the API

After deployment, test with curl:

```bash
curl -X GET \
  -H "Origin: http://localhost:5173" \
  -v \
  https://thebrideside-agdnavgxhhcffpby.centralindia-01.azurewebsites.net/api/blog/posts
```

Look for these headers in the response:
- `Access-Control-Allow-Origin: *`
- `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD`

### Step 5: Clear Browser Cache

- Clear browser cache
- Try in an incognito/private window
- Hard refresh (Ctrl+Shift+R or Cmd+Shift+R)

## 🔍 Verification Checklist

- [ ] Azure platform CORS is disabled/empty
- [ ] Application rebuilt with latest SecurityConfig
- [ ] Application redeployed to Azure
- [ ] Deployment completed (check Azure Portal)
- [ ] Browser cache cleared
- [ ] Tested in incognito window

## 📝 Alternative: Test Locally First

To verify the configuration works before deploying:

1. **Run backend locally:**
   ```bash
   mvn spring-boot:run
   ```

2. **Update frontend API URL:**
   Change `BASE_URL` in your frontend to:
   ```typescript
   const BASE_URL = 'http://localhost:8080/api/blog';
   ```

3. **Test locally:**
   - If it works locally but not on Azure → Azure CORS issue
   - If it doesn't work locally → Configuration issue

## 🚨 Common Issues

### Issue: Still getting 403 after following steps

**Possible causes:**
1. Azure CORS not fully disabled (refresh Azure Portal)
2. Deployment not completed (check Azure Portal → Deployment Center)
3. Browser cache (try incognito)
4. Wrong backend URL in frontend

### Issue: Works locally but not on Azure

**Solution:** Azure platform CORS is definitely the issue. Make sure:
- Azure CORS is completely disabled (empty allowed origins)
- No CORS rules are configured in Azure Portal

### Issue: CORS works but getting other errors

**Solution:** CORS is fixed! Other errors are separate issues (check API responses).

## 📞 Quick Test Command

Test if CORS headers are present:

```bash
curl -I -X OPTIONS \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: GET" \
  https://thebrideside-agdnavgxhhcffpby.centralindia-01.azurewebsites.net/api/blog/posts
```

Should return:
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD
Access-Control-Allow-Headers: *
```

## ✅ Expected Result

After following all steps:
- ✅ No more 403 errors
- ✅ `Access-Control-Allow-Origin` header present in responses
- ✅ Blog API works from `localhost:5173`
- ✅ Categories and posts load successfully

## 🎯 Summary

**The fix is simple:**
1. **Disable Azure platform CORS** (most important!)
2. Rebuild and redeploy
3. Test

The Spring Security configuration is correct - Azure CORS is just blocking requests before they reach your application.

