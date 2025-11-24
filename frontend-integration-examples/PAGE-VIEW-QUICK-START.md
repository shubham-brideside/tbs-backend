# Page View Tracking - Quick Start Guide

## 🚀 Quick Integration (5 Minutes)

### Step 1: Add Functions to Your API Service

Add these functions to your `blog-api-service.ts` or `blog-api-service.js`:

```typescript
// Add to your API service file
const PAGE_VIEW_BASE_URL = 'http://localhost:8080/api/page-views'; // Update with your backend URL

export async function trackPageView(pagePath: string, pageType?: string, entityId?: number) {
  const response = await fetch(`${PAGE_VIEW_BASE_URL}/track`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ page_path: pagePath, page_type: pageType, entity_id: entityId }),
  });
  return await response.json();
  // Returns: { tracked: boolean, view_count: number, unique_visitors: number }
}

export async function getViewCount(pagePath: string) {
  const response = await fetch(`${PAGE_VIEW_BASE_URL}/count?page_path=${encodeURIComponent(pagePath)}`);
  return await response.json();
  // Returns: { page_path: string, view_count: number, unique_visitors: number }
}

// NEW: Get unique visitor count only
export async function getUniqueVisitorCount(pagePath: string) {
  const response = await fetch(`${PAGE_VIEW_BASE_URL}/unique-visitors?page_path=${encodeURIComponent(pagePath)}`);
  return await response.json();
  // Returns: { page_path: string, unique_visitors: number }
}
```

### Step 2: Track Views in Your Components

**For Blog Posts:**
```tsx
import { useEffect } from 'react';
import { trackPageView } from '../services/blog-api-service';

function BlogPostPage({ post }) {
  useEffect(() => {
    trackPageView(`/blog/post/${post.slug}`, 'blog_post', post.id)
      .catch(err => console.warn('Tracking failed:', err));
  }, [post.slug, post.id]);

  return <div>{/* Your post content */}</div>;
}
```

**For Any Page:**
```tsx
import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { trackPageView } from '../services/blog-api-service';

function App() {
  const location = useLocation();
  
  useEffect(() => {
    trackPageView(location.pathname).catch(console.error);
  }, [location.pathname]);

  return <div>{/* Your app */}</div>;
}
```

### Step 3: Display View Counts (Optional)

```tsx
import { useEffect, useState } from 'react';
import { getViewCount } from '../services/blog-api-service';

function ViewCount({ pagePath }) {
  const [viewCount, setViewCount] = useState(0);
  const [uniqueVisitors, setUniqueVisitors] = useState(0);
  
  useEffect(() => {
    getViewCount(pagePath).then(data => {
      setViewCount(data.view_count);
      setUniqueVisitors(data.unique_visitors);  // NEW: Get unique visitors
    });
  }, [pagePath]);
  
  return (
    <span>
      👁️ {viewCount} views | 👥 {uniqueVisitors} unique visitors
    </span>
  );
}
```

## 📝 Common Page Types

- `blog_post` - Blog post pages
- `blog_category` - Category pages  
- `deal` - Deal pages
- `home` - Home page
- `about` - About page

## 🎯 What You Get

The API now returns **both** total views and unique visitors:

```json
{
  "tracked": true,
  "view_count": 42,        // Total page views
  "unique_visitors": 35    // Unique visitors (by IP address)
}
```

## ✅ That's It!

Your pages are now being tracked with unique visitor counts! Check the Network tab in DevTools to see the API calls.

For detailed examples, see `PAGE-VIEW-TRACKING-FRONTEND.md`

