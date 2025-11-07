# View Count Not Increasing - Troubleshooting Guide

## Problem
View counts are not increasing when opening blog posts.

## Quick Checks

### 1. Verify Backend Endpoint is Working

Test the endpoint directly:

```bash
# Check current view count
curl http://localhost:8080/api/blog/posts/slug/YOUR-POST-SLUG

# Track a view
curl -X POST http://localhost:8080/api/blog/posts/slug/YOUR-POST-SLUG/view -H "Content-Type: application/json"

# Check view count again (should be +1)
curl http://localhost:8080/api/blog/posts/slug/YOUR-POST-SLUG
```

If the backend works but frontend doesn't, the issue is in the frontend.

### 2. Check Frontend Implementation

**Make sure your frontend is calling `trackPostView()`:**

```typescript
// ✅ CORRECT - Call trackPostView after loading post
useEffect(() => {
  async function loadPost() {
    const post = await blogApi.getPostBySlug(postSlug);
    setPost(post);
    
    // IMPORTANT: Call trackPostView to increment view count
    blogApi.trackPostView(postSlug).catch(err => {
      console.error('View tracking failed:', err);
    });
  }
  loadPost();
}, [postSlug]);
```

**❌ WRONG - Only calling getPostBySlug won't increment views:**
```typescript
// This will NOT increment view count
const post = await blogApi.getPostBySlug(postSlug);
```

### 3. Check Browser Console

Open your browser's Developer Tools (F12) and check:

1. **Network Tab**: Look for a `POST` request to `/api/blog/posts/slug/{slug}/view`
   - If you don't see this request, the frontend isn't calling it
   - If you see it but it fails, check the error message

2. **Console Tab**: Look for errors like:
   - `blogApi.trackPostView is not a function` - API service not updated
   - `CORS error` - Backend CORS configuration issue
   - `404 Not Found` - Wrong endpoint URL

### 4. Verify API Service is Updated

Make sure your `blogApi.ts` or `blogApi.js` includes the `trackPostView` method:

```typescript
// blog-api-service.ts should have:
export async function trackPostView(slug: string): Promise<{ tracked: boolean; message: string }> {
  return apiRequest<{ tracked: boolean; message: string }>(`/posts/slug/${slug}/view`, {
    method: 'POST',
  });
}
```

### 5. Check Backend Logs

Look at your Spring Boot application logs for:
- `View count incremented for post X (slug: Y)` - Confirms the endpoint was called
- Any error messages

### 6. Common Issues

#### Issue: Frontend not calling trackPostView
**Solution**: Update your component to call `blogApi.trackPostView(postSlug)` after loading the post.

#### Issue: trackPostView method doesn't exist
**Solution**: Copy the `trackPostView` method from `frontend-integration-examples/blog-api-service.ts` to your frontend API service file.

#### Issue: CORS error
**Solution**: Check that your backend CORS configuration allows POST requests from your frontend origin.

#### Issue: Rate limiting (view not tracked)
**Solution**: The endpoint returns `{"tracked": false, "message": "View rate-limited"}` if called within 5 seconds. This is normal behavior to prevent duplicate counts.

### 7. Complete Working Example

```tsx
import React, { useEffect, useState, useRef } from 'react';
import blogApi from './services/blogApi';

function BlogPostPage({ postSlug }) {
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const viewTrackedRef = useRef(false);

  useEffect(() => {
    let mounted = true;
    
    async function loadPost() {
      try {
        // 1. Load post data
        const postData = await blogApi.getPostBySlug(postSlug);
        
        if (mounted) {
          setPost(postData);
          setLoading(false);
          
          // 2. Track view (only once per mount)
          if (!viewTrackedRef.current) {
            viewTrackedRef.current = true;
            blogApi.trackPostView(postSlug)
              .then(result => {
                console.log('View tracked:', result);
                // Optionally refresh post to show updated view count
                return blogApi.getPostBySlug(postSlug);
              })
              .then(updatedPost => {
                if (mounted) {
                  setPost(updatedPost);
                }
              })
              .catch(err => {
                console.error('View tracking failed:', err);
              });
          }
        }
      } catch (error) {
        if (mounted) {
          console.error('Error loading post:', error);
          setLoading(false);
        }
      }
    }
    
    loadPost();
    
    return () => {
      mounted = false;
    };
  }, [postSlug]);

  if (loading) return <div>Loading...</div>;
  if (!post) return <div>Post not found</div>;

  return (
    <div>
      <h1>{post.title}</h1>
      <p>Views: {post.view_count}</p>
      {/* rest of your component */}
    </div>
  );
}
```

### 8. Testing Checklist

- [ ] Backend endpoint works when tested with curl
- [ ] Frontend API service has `trackPostView` method
- [ ] Frontend component calls `trackPostView` after loading post
- [ ] Browser Network tab shows POST request to `/view` endpoint
- [ ] No CORS errors in browser console
- [ ] Backend logs show "View count incremented" message
- [ ] View count increases in database

### Still Not Working?

If none of the above helps, check:
1. Is your backend server running and accessible?
2. Is the frontend pointing to the correct backend URL?
3. Are there any network errors in the browser console?
4. Check the backend application logs for any exceptions

