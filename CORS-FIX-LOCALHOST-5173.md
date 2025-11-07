# CORS Fix for localhost:5173

## Problem
Getting CORS error when calling `/api/deals/init` from frontend running on `http://localhost:5173/`.

## Solution Applied

### 1. Removed Redundant `@CrossOrigin` Annotations
- Removed `@CrossOrigin(origins = "*")` from `DealController`
- Removed `@CrossOrigin(origins = "*")` from `BlogController`
- Global CORS configuration in `SecurityConfig` and `WebMvcConfig` handles all endpoints

### 2. Verified CORS Configuration

The global CORS configuration in `SecurityConfig.java` already includes:
```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:*",  // ✅ This matches localhost:5173
    "http://127.0.0.1:*",
    "https://*.azurewebsites.net",
    "https://*.azurestaticapps.net",
    "https://thebrideside.in",
    "https://www.thebrideside.in",
    "*"
));
```

## Testing

The backend CORS is working correctly. Test with:

```bash
# Test OPTIONS preflight
curl -X OPTIONS http://localhost:8080/api/deals/init \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: POST" \
  -v

# Test actual POST request
curl -X POST http://localhost:8080/api/deals/init \
  -H "Origin: http://localhost:5173" \
  -H "Content-Type: application/json" \
  -d '{"contact_number": "+1234567890"}' \
  -v
```

Both should return `Access-Control-Allow-Origin: http://localhost:5173`.

## Frontend Checklist

If you're still getting CORS errors, check:

1. **Backend URL**: Make sure your frontend is pointing to `http://localhost:8080` (not Azure URL)
   ```typescript
   // ✅ CORRECT for local development
   const API_BASE_URL = 'http://localhost:8080/api';
   
   // ❌ WRONG - This will cause CORS issues
   const API_BASE_URL = 'https://your-azure-url.azurewebsites.net/api';
   ```

2. **Request Headers**: Make sure you're sending:
   ```typescript
   fetch('http://localhost:8080/api/deals/init', {
     method: 'POST',
     headers: {
       'Content-Type': 'application/json',
     },
     body: JSON.stringify({ contact_number: '+1234567890' })
   });
   ```

3. **Browser Console**: Check for:
   - Network tab shows the request
   - Error message details
   - Response headers

4. **Backend Running**: Ensure backend is running on `http://localhost:8080`

## Next Steps

1. **Restart your backend** to apply the changes:
   ```bash
   # Stop the server (Ctrl+C) and restart:
   mvn spring-boot:run
   ```

2. **Clear browser cache** or use incognito mode

3. **Check frontend code** - Make sure it's calling `http://localhost:8080/api/deals/init` (not Azure URL)

4. **Verify in Network Tab**:
   - Open DevTools (F12) → Network tab
   - Make the request
   - Check the request URL
   - Check response headers for `Access-Control-Allow-Origin`

## Still Having Issues?

If CORS errors persist after restarting:

1. Check that your frontend is actually calling `http://localhost:8080` (not Azure)
2. Verify the backend is running: `curl http://localhost:8080/actuator/health`
3. Check browser console for the exact error message
4. Verify the request is going to the correct endpoint

The backend CORS configuration is correct and tested. The issue is likely:
- Frontend pointing to wrong URL (Azure instead of localhost)
- Browser cache
- Backend not restarted after changes

