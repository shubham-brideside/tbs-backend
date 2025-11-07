# View Count Fix - Frontend Update Guide

## Problem
View counts were increasing by +2 instead of +1 due to React StrictMode causing double API calls in development.

## Solution
View tracking has been separated from post retrieval. You now need to call a separate endpoint to track views.

## Backend Changes

### New Endpoint
- **POST** `/api/blog/posts/slug/{slug}/view` - Track a post view (rate-limited to prevent duplicates)

### Updated Endpoint
- **GET** `/api/blog/posts/slug/{slug}` - Now only retrieves post data (doesn't increment views)

## Frontend Update Required

### Option 1: Call View Tracking Separately (Recommended)

Update your frontend to call the view tracking endpoint after loading the post:

```typescript
// blogApi.ts - Add this method
export async function trackPostView(slug: string): Promise<{ tracked: boolean; message: string }> {
  return apiRequest(`/posts/slug/${slug}/view`, {
    method: 'POST',
  });
}
```

Then in your component:

```typescript
// BlogPost.tsx or similar
useEffect(() => {
  async function loadPost() {
    // Load post data (doesn't increment views)
    const post = await blogApi.getPostBySlug(postSlug);
    setPost(post);
    
    // Track view separately (rate-limited, won't duplicate)
    await blogApi.trackPostView(postSlug);
  }
  loadPost();
}, [postSlug]);
```

### Option 2: Automatic View Tracking (Simpler)

If you want automatic tracking but still want protection against duplicates, you can call both endpoints:

```typescript
useEffect(() => {
  async function loadPost() {
    // Load post and track view in parallel
    const [post, viewResult] = await Promise.all([
      blogApi.getPostBySlug(postSlug),
      blogApi.trackPostView(postSlug)
    ]);
    setPost(post);
  }
  loadPost();
}, [postSlug]);
```

## Rate Limiting Protection

The backend now includes rate limiting:
- ✅ Prevents duplicate increments within 5 seconds
- ✅ Works even with React StrictMode double-renders
- ✅ Prevents accidental spam of view tracking

## Testing

After updating your frontend:

1. Open a blog post
2. Check the view count - should increase by only 1
3. Refresh the page - should still only increase by 1 (not 2)
4. Open the same post again within 5 seconds - view count should NOT increase

## Example Implementation

```tsx
// Complete example
import React, { useEffect, useState } from 'react';
import blogApi from './services/blogApi';

function BlogPostPage({ postSlug }) {
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;
    
    async function loadPost() {
      try {
        // Load post data
        const postData = await blogApi.getPostBySlug(postSlug);
        
        if (mounted) {
          setPost(postData);
          setLoading(false);
          
          // Track view (rate-limited, safe to call multiple times)
          blogApi.trackPostView(postSlug).catch(err => {
            // Silent fail - view tracking is not critical
            console.debug('View tracking failed:', err);
          });
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

  // ... rest of component
}
```

## Benefits

✅ **Accurate view counts** - No more double-counting  
✅ **Rate limiting** - Prevents duplicate increments  
✅ **Flexible** - Frontend controls when to track views  
✅ **Performance** - Post loading is faster (read-only)  
✅ **Production-ready** - Works correctly even with React StrictMode

