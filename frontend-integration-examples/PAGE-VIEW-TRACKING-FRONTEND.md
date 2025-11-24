# Frontend Integration Guide - Page View Tracking

This guide explains how to integrate the page view tracking feature into your frontend application.

## 📋 Table of Contents

1. [Overview](#overview)
2. [Step 1: Update API Service](#step-1-update-api-service)
3. [Step 2: Integrate in Components](#step-2-integrate-in-components)
4. [Step 3: Display View Counts](#step-3-display-view-counts)
5. [Complete Examples](#complete-examples)
6. [Best Practices](#best-practices)

## Overview

The page view tracking system allows you to:
- Track when users view any page
- Get view counts and statistics
- Display view counts to users
- Analyze most popular pages

**Important:** You need to call the tracking API when a page is viewed. The backend won't automatically track views - you must make the API call from your frontend.

---

## Step 1: Update API Service

Add page view tracking functions to your existing API service file.

### For TypeScript Projects (`blog-api-service.ts`)

Add these functions to your `blog-api-service.ts` file:

```typescript
// ==================== PAGE VIEW TRACKING ====================

export interface PageViewRequest {
  page_path: string;
  page_type?: string;
  entity_id?: number;
}

export interface TrackViewResponse {
  tracked: boolean;
  message: string;
  view_count: number;
  unique_visitors: number;  // NEW: Number of unique visitors
}

export interface ViewCountResponse {
  page_path: string;
  view_count: number;
  unique_visitors: number;  // NEW: Number of unique visitors
}

export interface ViewStatistics {
  total_views: number;
  unique_visitors: number;  // NEW: Total unique visitors
  page_path: string;
  page_type?: string;
  entity_id?: number;
  views_today: number;
  unique_visitors_today: number;  // NEW: Unique visitors today
  views_this_week: number;
  unique_visitors_this_week: number;  // NEW: Unique visitors this week
  views_this_month: number;
  unique_visitors_this_month: number;  // NEW: Unique visitors this month
}

/**
 * Track a page view
 * @param pagePath - The path/URL of the page (e.g., "/blog/post/my-article")
 * @param pageType - Optional: Type of page (e.g., "blog_post", "deal", "home")
 * @param entityId - Optional: ID of the related entity (e.g., blog post ID, deal ID)
 */
export async function trackPageView(
  pagePath: string,
  pageType?: string,
  entityId?: number
): Promise<TrackViewResponse> {
  const PAGE_VIEW_BASE_URL = 'http://localhost:8080/api/page-views'; // Update with your backend URL
  
  const response = await fetch(`${PAGE_VIEW_BASE_URL}/track`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      page_path: pagePath,
      page_type: pageType,
      entity_id: entityId,
    }),
  });

  if (!response.ok) {
    throw new Error(`Failed to track page view: ${response.statusText}`);
  }

  return await response.json();
}

/**
 * Get view count for a specific page path
 */
export async function getViewCount(pagePath: string): Promise<ViewCountResponse> {
  const PAGE_VIEW_BASE_URL = 'http://localhost:8080/api/page-views'; // Update with your backend URL
  
  const response = await fetch(
    `${PAGE_VIEW_BASE_URL}/count?page_path=${encodeURIComponent(pagePath)}`
  );

  if (!response.ok) {
    throw new Error(`Failed to get view count: ${response.statusText}`);
  }

  return await response.json();
}

/**
 * Get unique visitor count for a specific page path
 */
export async function getUniqueVisitorCount(pagePath: string): Promise<{ page_path: string; unique_visitors: number }> {
  const PAGE_VIEW_BASE_URL = 'http://localhost:8080/api/page-views'; // Update with your backend URL
  
  const response = await fetch(
    `${PAGE_VIEW_BASE_URL}/unique-visitors?page_path=${encodeURIComponent(pagePath)}`
  );

  if (!response.ok) {
    throw new Error(`Failed to get unique visitor count: ${response.statusText}`);
  }

  return await response.json();
}

/**
 * Get detailed statistics for a page path
 */
export async function getPageStatistics(pagePath: string): Promise<ViewStatistics> {
  const PAGE_VIEW_BASE_URL = 'http://localhost:8080/api/page-views'; // Update with your backend URL
  
  const response = await fetch(
    `${PAGE_VIEW_BASE_URL}/statistics?page_path=${encodeURIComponent(pagePath)}`
  );

  if (!response.ok) {
    throw new Error(`Failed to get page statistics: ${response.statusText}`);
  }

  return await response.json();
}

/**
 * Get view count for an entity (page type + entity ID)
 */
export async function getEntityViewCount(
  pageType: string,
  entityId: number
): Promise<{ page_type: string; entity_id: number; view_count: number }> {
  const PAGE_VIEW_BASE_URL = 'http://localhost:8080/api/page-views'; // Update with your backend URL
  
  const response = await fetch(
    `${PAGE_VIEW_BASE_URL}/count/entity?page_type=${encodeURIComponent(pageType)}&entity_id=${entityId}`
  );

  if (!response.ok) {
    throw new Error(`Failed to get entity view count: ${response.statusText}`);
  }

  return await response.json();
}
```

**Update the default export** to include the new functions:

```typescript
const blogApi = {
  // ... existing methods ...
  trackPostView,
  
  // Page View Tracking
  trackPageView,
  getViewCount,
  getPageStatistics,
  getEntityViewCount,
};

export default blogApi;
```

### For JavaScript Projects (`blog-api-service.js`)

Add these functions to your `blog-api-service.js` file:

```javascript
// ==================== PAGE VIEW TRACKING ====================

const PAGE_VIEW_BASE_URL = 'http://localhost:8080/api/page-views'; // Update with your backend URL

/**
 * Track a page view
 */
export async function trackPageView(pagePath, pageType = null, entityId = null) {
  const response = await fetch(`${PAGE_VIEW_BASE_URL}/track`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      page_path: pagePath,
      page_type: pageType,
      entity_id: entityId,
    }),
  });

  if (!response.ok) {
    throw new Error(`Failed to track page view: ${response.statusText}`);
  }

  return await response.json();
}

/**
 * Get view count for a specific page path
 */
export async function getViewCount(pagePath) {
  const response = await fetch(
    `${PAGE_VIEW_BASE_URL}/count?page_path=${encodeURIComponent(pagePath)}`
  );

  if (!response.ok) {
    throw new Error(`Failed to get view count: ${response.statusText}`);
  }

  return await response.json();
}

/**
 * Get detailed statistics for a page path
 */
export async function getPageStatistics(pagePath) {
  const response = await fetch(
    `${PAGE_VIEW_BASE_URL}/statistics?page_path=${encodeURIComponent(pagePath)}`
  );

  if (!response.ok) {
    throw new Error(`Failed to get page statistics: ${response.statusText}`);
  }

  return await response.json();
}
```

---

## Step 2: Integrate in Components

### Example 1: Track Blog Post Views (React)

Update your blog post page component to track views when the page loads:

```tsx
// BlogPostPage.tsx
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import blogApi, { trackPageView } from '../services/blog-api-service';

function BlogPostPage() {
  const { slug } = useParams();
  const [post, setPost] = useState(null);
  const [viewCount, setViewCount] = useState(0);
  const [uniqueVisitors, setUniqueVisitors] = useState(0);  // NEW: Track unique visitors
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadPost() {
      try {
        setLoading(true);
        
        // Load post data
        const postData = await blogApi.getPostBySlug(slug);
        setPost(postData);
        
        // Track page view
        const pagePath = `/blog/post/${slug}`;
        const trackResult = await trackPageView(
          pagePath,
          'blog_post',  // page type
          postData.id   // entity ID (blog post ID)
        );
        
        // Update view count and unique visitors
        if (trackResult.tracked) {
          setViewCount(trackResult.view_count);
          setUniqueVisitors(trackResult.unique_visitors);  // NEW: Set unique visitors
        } else {
          // If rate-limited, get current count
          const countData = await blogApi.getViewCount(pagePath);
          setViewCount(countData.view_count);
          setUniqueVisitors(countData.unique_visitors);  // NEW: Set unique visitors
        }
      } catch (error) {
        console.error('Error loading post:', error);
      } finally {
        setLoading(false);
      }
    }

    if (slug) {
      loadPost();
    }
  }, [slug]);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!post) {
    return <div>Post not found</div>;
  }

  return (
    <div>
      <h1>{post.title}</h1>
      <div className="post-stats">
        <p>👁️ {viewCount} views</p>
        <p>👥 {uniqueVisitors} unique visitors</p>  {/* NEW: Display unique visitors */}
      </div>
      <div dangerouslySetInnerHTML={{ __html: post.content }} />
    </div>
  );
}

export default BlogPostPage;
```

### Example 2: Track Deal Page Views

```tsx
// DealPage.tsx
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { trackPageView, getViewCount } from '../services/blog-api-service';

function DealPage() {
  const { dealId } = useParams();
  const [deal, setDeal] = useState(null);
  const [viewCount, setViewCount] = useState(0);

  useEffect(() => {
    async function loadDeal() {
      try {
        // Load deal data (your existing API call)
        // const dealData = await getDealById(dealId);
        // setDeal(dealData);
        
        // Track page view
        const pagePath = `/deals/${dealId}`;
        await trackPageView(pagePath, 'deal', parseInt(dealId));
        
        // Get and display view count
        const countData = await getViewCount(pagePath);
        setViewCount(countData.view_count);
      } catch (error) {
        console.error('Error loading deal:', error);
      }
    }

    if (dealId) {
      loadDeal();
    }
  }, [dealId]);

  return (
    <div>
      <h1>Deal Details</h1>
      <p>Views: {viewCount}</p>
      {/* Deal content */}
    </div>
  );
}
```

### Example 3: Track Home Page Views

```tsx
// HomePage.tsx
import { useEffect } from 'react';
import { trackPageView } from '../services/blog-api-service';

function HomePage() {
  useEffect(() => {
    // Track home page view
    trackPageView('/', 'home').catch(error => {
      console.error('Error tracking home page view:', error);
    });
  }, []);

  return (
    <div>
      <h1>Welcome to Brideside</h1>
      {/* Home page content */}
    </div>
  );
}
```

### Example 4: Track Category Page Views

```tsx
// CategoryPage.tsx
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import blogApi, { trackPageView, getViewCount } from '../services/blog-api-service';

function CategoryPage() {
  const { categorySlug } = useParams();
  const [category, setCategory] = useState(null);
  const [viewCount, setViewCount] = useState(0);

  useEffect(() => {
    async function loadCategory() {
      try {
        // Load category data
        const categoryData = await blogApi.getCategoryBySlug(categorySlug);
        setCategory(categoryData);
        
        // Track page view
        const pagePath = `/blog/category/${categorySlug}`;
        await trackPageView(pagePath, 'blog_category', categoryData.id);
        
        // Get view count
        const countData = await getViewCount(pagePath);
        setViewCount(countData.view_count);
      } catch (error) {
        console.error('Error loading category:', error);
      }
    }

    if (categorySlug) {
      loadCategory();
    }
  }, [categorySlug]);

  return (
    <div>
      <h1>{category?.name}</h1>
      <p>Views: {viewCount}</p>
      {/* Category content */}
    </div>
  );
}
```

### Example 5: Using React Router (Track on Route Change)

If you want to automatically track views when routes change:

```tsx
// App.tsx or Router component
import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { trackPageView } from './services/blog-api-service';

function App() {
  const location = useLocation();

  useEffect(() => {
    // Track page view on route change
    const pagePath = location.pathname;
    
    // Determine page type based on path
    let pageType = 'page';
    let entityId = null;
    
    if (pagePath.startsWith('/blog/post/')) {
      pageType = 'blog_post';
      // Extract slug and get entity ID if needed
    } else if (pagePath.startsWith('/blog/category/')) {
      pageType = 'blog_category';
    } else if (pagePath.startsWith('/deals/')) {
      pageType = 'deal';
      const dealId = pagePath.split('/').pop();
      entityId = dealId ? parseInt(dealId) : null;
    } else if (pagePath === '/') {
      pageType = 'home';
    }
    
    trackPageView(pagePath, pageType, entityId).catch(error => {
      console.error('Error tracking page view:', error);
    });
  }, [location.pathname]);

  return (
    // Your app content
  );
}
```

---

## Step 3: Display View Counts

### Display View Count in Blog Post List

```tsx
// BlogPostCard.tsx
import { useEffect, useState } from 'react';
import { getViewCount } from '../services/blog-api-service';

function BlogPostCard({ post }) {
  const [viewCount, setViewCount] = useState(0);

  useEffect(() => {
    async function fetchViewCount() {
      try {
        const pagePath = `/blog/post/${post.slug}`;
        const countData = await getViewCount(pagePath);
        setViewCount(countData.view_count);
      } catch (error) {
        console.error('Error fetching view count:', error);
      }
    }

    fetchViewCount();
  }, [post.slug]);

  return (
    <div className="blog-post-card">
      <h3>{post.title}</h3>
      <p>{post.excerpt}</p>
      <div className="post-meta">
        <span>👁️ {viewCount} views</span>
        <span>{post.publishedAt}</span>
      </div>
    </div>
  );
}
```

### Display View Statistics (Admin Dashboard)

```tsx
// ViewStatistics.tsx
import { useEffect, useState } from 'react';
import { getPageStatistics } from '../services/blog-api-service';

function ViewStatistics({ pagePath }) {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchStats() {
      try {
        const statistics = await getPageStatistics(pagePath);
        setStats(statistics);
      } catch (error) {
        console.error('Error fetching statistics:', error);
      } finally {
        setLoading(false);
      }
    }

    if (pagePath) {
      fetchStats();
    }
  }, [pagePath]);

  if (loading) {
    return <div>Loading statistics...</div>;
  }

  if (!stats) {
    return <div>No statistics available</div>;
  }

  return (
    <div className="view-statistics">
      <h3>View Statistics</h3>
      <div className="stats-grid">
        <div className="stat-item">
          <span className="stat-label">Total Views</span>
          <span className="stat-value">{stats.total_views}</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Unique Visitors</span>
          <span className="stat-value">{stats.unique_visitors}</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Today</span>
          <span className="stat-value">{stats.views_today} views ({stats.unique_visitors_today} unique)</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">This Week</span>
          <span className="stat-value">{stats.views_this_week} views ({stats.unique_visitors_this_week} unique)</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">This Month</span>
          <span className="stat-value">{stats.views_this_month} views ({stats.unique_visitors_this_month} unique)</span>
        </div>
      </div>
    </div>
  );
}
```

---

## Complete Examples

### Complete Blog Post Page with Tracking

```tsx
// BlogPostPage.tsx (Complete Example)
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import blogApi, { trackPageView, getViewCount } from '../services/blog-api-service';

interface BlogPost {
  id: number;
  title: string;
  slug: string;
  content: string;
  viewCount: number;
  // ... other fields
}

function BlogPostPage() {
  const { slug } = useParams<{ slug: string }>();
  const [post, setPost] = useState<BlogPost | null>(null);
  const [currentViewCount, setCurrentViewCount] = useState<number>(0);
  const [uniqueVisitors, setUniqueVisitors] = useState<number>(0);  // NEW: Track unique visitors
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadPostAndTrack() {
      try {
        setLoading(true);
        setError(null);

        // 1. Load the blog post
        const postData = await blogApi.getPostBySlug(slug);
        
        if (!isMounted) return;
        
        setPost(postData);
        setCurrentViewCount(postData.viewCount || 0);

        // 2. Track the page view
        const pagePath = `/blog/post/${slug}`;
        try {
          const trackResult = await trackPageView(
            pagePath,
            'blog_post',
            postData.id
          );

          if (trackResult.tracked && isMounted) {
            // Update view count and unique visitors if successfully tracked
            setCurrentViewCount(trackResult.view_count);
            setUniqueVisitors(trackResult.unique_visitors);  // NEW: Set unique visitors
          } else if (isMounted) {
            // If rate-limited, fetch current count
            const countData = await getViewCount(pagePath);
            setCurrentViewCount(countData.view_count);
            setUniqueVisitors(countData.unique_visitors);  // NEW: Set unique visitors
          }
        } catch (trackError) {
          // Don't fail the page load if tracking fails
          console.warn('Failed to track page view:', trackError);
        }
      } catch (err) {
        if (isMounted) {
          setError(err instanceof Error ? err.message : 'Failed to load post');
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }

    if (slug) {
      loadPostAndTrack();
    }

    return () => {
      isMounted = false;
    };
  }, [slug]);

  if (loading) {
    return (
      <div className="loading-container">
        <p>Loading post...</p>
      </div>
    );
  }

  if (error || !post) {
    return (
      <div className="error-container">
        <p>{error || 'Post not found'}</p>
      </div>
    );
  }

  return (
    <article className="blog-post">
      <header>
        <h1>{post.title}</h1>
        <div className="post-meta">
          <span className="view-count">👁️ {currentViewCount} views</span>
          <span className="unique-visitors">👥 {uniqueVisitors} unique visitors</span>
          <span className="published-date">
            {post.publishedAt ? new Date(post.publishedAt).toLocaleDateString() : ''}
          </span>
        </div>
      </header>
      <div 
        className="post-content" 
        dangerouslySetInnerHTML={{ __html: post.content }} 
      />
    </article>
  );
}

export default BlogPostPage;
```

---

## Best Practices

### 1. **Don't Block Page Rendering**

Track views asynchronously and don't wait for the tracking API call to complete before showing content:

```tsx
// ✅ Good - Non-blocking
useEffect(() => {
  trackPageView(pagePath, pageType, entityId).catch(console.error);
}, []);

// ❌ Bad - Blocks rendering
useEffect(() => {
  await trackPageView(pagePath, pageType, entityId); // Don't do this
}, []);
```

### 2. **Handle Errors Gracefully**

Don't let tracking failures break your page:

```tsx
try {
  await trackPageView(pagePath, pageType, entityId);
} catch (error) {
  // Log but don't show error to user
  console.warn('Page view tracking failed:', error);
}
```

### 3. **Use Consistent Page Paths**

Use consistent path formats across your application:

```tsx
// ✅ Good - Consistent format
const pagePath = `/blog/post/${slug}`;
const pagePath = `/deals/${dealId}`;
const pagePath = `/blog/category/${categorySlug}`;

// ❌ Bad - Inconsistent
const pagePath = `/blog/${slug}`; // Sometimes
const pagePath = `/posts/${slug}`; // Other times
```

### 4. **Track Once Per Page Load**

The backend has rate limiting, but you should still only track once per page load:

```tsx
useEffect(() => {
  // This will only run once when component mounts
  trackPageView(pagePath, pageType, entityId);
}, []); // Empty dependency array = run once
```

### 5. **Use Environment Variables**

Store your API base URL in environment variables:

```typescript
// .env
VITE_PAGE_VIEW_API_URL=http://localhost:8080/api/page-views

// In your service file
const PAGE_VIEW_BASE_URL = import.meta.env.VITE_PAGE_VIEW_API_URL || 'http://localhost:8080/api/page-views';
```

### 6. **Optimize View Count Fetching**

If you're displaying view counts in lists, consider:
- Caching view counts
- Fetching in batches
- Using the view count from the post data if available

```tsx
// Use view count from post data if available
const [viewCount, setViewCount] = useState(post.viewCount || 0);

useEffect(() => {
  // Only fetch if not available in post data
  if (!post.viewCount) {
    getViewCount(pagePath).then(data => setViewCount(data.view_count));
  }
}, [post]);
```

---

## Testing

### Test Page View Tracking

1. **Open your browser's Developer Tools** (F12)
2. **Go to the Network tab**
3. **Navigate to a page** (e.g., a blog post)
4. **Look for a POST request** to `/api/page-views/track`
5. **Check the response** - should return `{ tracked: true, view_count: X }`

### Verify View Counts

1. **Call the tracking endpoint** multiple times
2. **Check that rate limiting works** - second call within 5 seconds should return `tracked: false`
3. **Verify view count increases** - call `/api/page-views/count` to see the count

---

## Troubleshooting

### Views Not Being Tracked

1. **Check Network Tab** - Is the API call being made?
2. **Check CORS** - Are you getting CORS errors?
3. **Check API URL** - Is the base URL correct?
4. **Check Console** - Are there any JavaScript errors?

### View Count Not Updating

1. **Rate Limiting** - Wait 5 seconds between views from the same IP
2. **Check API Response** - Is `tracked: true` in the response?
3. **Refresh View Count** - Call `getViewCount()` after tracking

### CORS Issues

If you get CORS errors, make sure your backend allows requests from your frontend domain. Check your Spring Boot CORS configuration.

---

## Summary

1. ✅ Add page view tracking functions to your API service
2. ✅ Call `trackPageView()` when pages load
3. ✅ Optionally display view counts to users
4. ✅ Handle errors gracefully
5. ✅ Test the integration

That's it! Your page view tracking is now integrated. 🎉

