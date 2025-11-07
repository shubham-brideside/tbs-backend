# Frontend API Configuration Fix

## Problem
Your frontend is still calling the Azure backend URL instead of localhost.

## Solution

### Update Your Frontend API Service

Find your `blogApi.ts` or `blogApi.js` file in your frontend project and update the `BASE_URL`:

**For Local Development:**
```typescript
const BASE_URL = 'http://localhost:8080/api/blog';
```

**For Production (after deploying to Azure):**
```typescript
const BASE_URL = 'https://thebrideside-agdnavgxhhcffpby.centralindia-01.azurewebsites.net/api/blog';
```

### Better Solution: Use Environment Variables

Create a `.env` file in your frontend project root:

```env
# .env (for local development)
VITE_API_BASE_URL=http://localhost:8080/api/blog
# OR
REACT_APP_API_BASE_URL=http://localhost:8080/api/blog
```

Then in your `blogApi.ts`:

```typescript
// For Vite projects
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/blog';

// For Create React App
const BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/blog';
```

### Quick Fix Steps

1. **Find your API service file:**
   - Look for `blogApi.ts` or `blogApi.js` in your frontend `src` folder
   - It should be in `src/services/` or `src/api/` or similar

2. **Update the BASE_URL:**
   ```typescript
   // Change from:
   const BASE_URL = 'https://thebrideside-agdnavgxhhcffpby.centralindia-01.azurewebsites.net/api/blog';
   
   // To:
   const BASE_URL = 'http://localhost:8080/api/blog';
   ```

3. **Save and restart your frontend dev server:**
   ```bash
   # Stop the server (Ctrl+C)
   # Then restart:
   npm run dev
   # OR
   npm start
   ```

4. **Test again:**
   - Refresh your browser
   - Check the Network tab - requests should go to `localhost:8080`
   - Should see 200 OK responses instead of 403

## Verification

After making the change, check the Network tab:
- ✅ Requests should go to `http://localhost:8080/api/blog/...`
- ✅ Status should be `200 OK` (not 403)
- ✅ Response should contain blog data

## Environment Setup (Recommended)

For a better setup, use environment variables:

**`.env.local`** (for local development):
```env
VITE_API_BASE_URL=http://localhost:8080/api/blog
```

**`.env.production`** (for production):
```env
VITE_API_BASE_URL=https://thebrideside-agdnavgxhhcffpby.centralindia-01.azurewebsites.net/api/blog
```

This way you don't have to change the code when switching between local and production!

