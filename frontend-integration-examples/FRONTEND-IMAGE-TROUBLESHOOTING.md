# Frontend Image Display Troubleshooting Guide

## ✅ Backend Status: Working Correctly
- ✅ Images are accessible from Azure Blob Storage
- ✅ API returns correct `featured_image_url` in responses
- ✅ URLs are properly formatted

## 🔍 Frontend Issues to Check

### Issue 1: Field Name Mismatch

**Check:** Make sure your frontend is using the correct field name from the API response.

**API Response Field:** `featured_image_url` (snake_case)

**Common Mistakes:**
```javascript
// ❌ WRONG - field name mismatch
<img src={post.featuredImage} />
<img src={post.image} />
<img src={post.imageUrl} />

// ✅ CORRECT
<img src={post.featured_image_url} />
// OR if you're transforming the data:
<img src={post.featuredImageUrl} />
```

### Issue 2: URL Encoding

The URLs contain `%26` which is the URL-encoded version of `&`. This is correct, but make sure your frontend isn't double-encoding them.

**Example URL:**
```
https://bridesideimages.blob.core.windows.net/tbs-website-images/SHREY%26MAHIMA_MEHENDI%26HALDI1164.jpg
```

**Check:** Don't use `encodeURI()` or `encodeURIComponent()` on URLs that are already encoded.

### Issue 3: Image Not Rendering in Component

**Check your React/component code:**

```jsx
// ✅ CORRECT Implementation
function BlogPostCard({ post }) {
  return (
    <div className="post-card">
      {/* Make sure you're using the correct field name */}
      {post.featured_image_url && (
        <img 
          src={post.featured_image_url} 
          alt={post.title}
          onError={(e) => {
            console.error('Image failed to load:', post.featured_image_url);
            e.target.style.display = 'none'; // Hide broken image
          }}
        />
      )}
      <h3>{post.title}</h3>
    </div>
  );
}
```

### Issue 4: CORS on Azure Blob Storage

**Check Network Tab:**
1. Open Chrome DevTools → Network tab
2. Click on "Img" filter (to see image requests)
3. Look for failed image requests
4. Check if there are CORS errors for Azure Blob Storage

**Solution:** If you see CORS errors, you need to configure CORS on Azure Blob Storage:
- Go to Azure Portal → Storage Account → CORS
- Add allowed origins: `http://localhost:5173` (and your production domain)

### Issue 5: Check Image Loading in Browser

**Test directly in browser:**
1. Open this URL directly in your browser:
   ```
   https://bridesideimages.blob.core.windows.net/tbs-website-images/SHREY%26MAHIMA_MEHENDI%26HALDI1164.jpg
   ```
2. If it loads → Image is accessible, issue is in frontend code
3. If it doesn't load → Azure Blob Storage access issue

## 🔧 Quick Fixes

### Fix 1: Verify Field Name in Frontend

Check your frontend code where you're rendering blog posts:

```typescript
// In your blogApi.ts or component
interface BlogPost {
  id: number;
  title: string;
  featured_image_url: string; // ← Make sure this matches API response
  // ... other fields
}
```

### Fix 2: Add Error Handling

Add error handling to see what's happening:

```jsx
function BlogPostCard({ post }) {
  const [imageError, setImageError] = useState(false);
  
  return (
    <div className="post-card">
      {post.featured_image_url && !imageError ? (
        <img 
          src={post.featured_image_url} 
          alt={post.title}
          onError={() => {
            console.error('Image failed:', post.featured_image_url);
            setImageError(true);
          }}
          onLoad={() => console.log('Image loaded:', post.featured_image_url)}
        />
      ) : (
        <div className="placeholder">No image available</div>
      )}
      <h3>{post.title}</h3>
    </div>
  );
}
```

### Fix 3: Check Console for Errors

Open browser console and look for:
- Image loading errors
- CORS errors
- 404 errors for image URLs

## 📝 Example: Correct Frontend Implementation

```jsx
// Blog.tsx - Correct implementation
import React, { useEffect, useState } from 'react';
import blogApi from './services/blogApi';

function Blog() {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadPosts() {
      try {
        const data = await blogApi.getPosts();
        setPosts(data);
      } catch (error) {
        console.error('Error loading posts:', error);
      } finally {
        setLoading(false);
      }
    }
    loadPosts();
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <div className="blog-posts">
      {posts.map(post => (
        <article key={post.id} className="post-card">
          {/* ✅ CORRECT: Using featured_image_url from API */}
          {post.featured_image_url && (
            <img 
              src={post.featured_image_url}
              alt={post.title}
              className="post-image"
            />
          )}
          <h2>{post.title}</h2>
          <p>{post.excerpt}</p>
        </article>
      ))}
    </div>
  );
}
```

## 🎯 Most Likely Issues

Based on your description, the most likely issues are:

1. **Field name mismatch** - Frontend using `featuredImage` instead of `featured_image_url`
2. **Not rendering img tag** - Frontend code not creating `<img>` elements
3. **Wrong data transformation** - Frontend transforming API response but not mapping image field correctly

## 🔍 Debugging Steps

1. **Check Network Tab → Img filter:**
   - Are image requests being made?
   - What status codes do they return?
   - Any CORS errors?

2. **Check Console:**
   - Any JavaScript errors?
   - Log the post object to see what fields are available

3. **Check React DevTools:**
   - Inspect the component props
   - Verify the `post` object has `featured_image_url`

4. **Test URL directly:**
   - Copy the image URL from API response
   - Paste in browser address bar
   - Does it load?

## ✅ Verification Checklist

- [ ] API returns `featured_image_url` field (check Network → Response tab)
- [ ] Image URLs are accessible (test directly in browser)
- [ ] Frontend code uses `post.featured_image_url` (not `post.image` or similar)
- [ ] `<img>` tags are being rendered in the component
- [ ] No CORS errors in Network tab for image requests
- [ ] No JavaScript errors in Console

If all backend checks pass but images still don't show, it's 100% a frontend rendering issue.

