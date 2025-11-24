# Page View Tracking Feature

## Overview

A comprehensive page view tracking system has been implemented to track how many times pages are being opened in your application. This system can track views for any page or route in your application.

## Features

- ✅ Track page views for any page/route
- ✅ **Track unique visitors** (based on IP address)
- ✅ Rate limiting to prevent duplicate tracking (5-second cooldown)
- ✅ IP address and user agent tracking
- ✅ View statistics (total, today, this week, this month)
- ✅ **Unique visitor statistics** (total, today, this week, this month)
- ✅ Most viewed pages analytics
- ✅ **Most visited pages by unique visitors**
- ✅ View counts by page type
- ✅ Entity-based tracking (e.g., track views for specific blog posts or deals)

## Database Table

A new `page_views` table has been created with the following structure:

- `id` - Primary key
- `page_path` - The path/URL of the page (e.g., "/blog/post/my-article")
- `page_type` - Type of page (e.g., "blog_post", "deal", "home", "category")
- `entity_id` - ID of the related entity (e.g., blog post ID, deal ID)
- `ip_address` - IP address of the viewer
- `user_agent` - Browser user agent
- `referrer` - Referrer URL
- `viewed_at` - Timestamp when the page was viewed

## API Endpoints

### 1. Track a Page View

**POST** `/api/page-views/track`

Track a page view. This endpoint should be called when a page is viewed.

**Request Body:**
```json
{
  "page_path": "/blog/post/my-article",
  "page_type": "blog_post",
  "entity_id": 123
}
```

**Response:**
```json
{
  "tracked": true,
  "message": "View tracked successfully",
  "view_count": 42,
  "unique_visitors": 35
}
```

**Note:** `page_type` and `entity_id` are optional. `page_path` is required.

### 2. Get View Count by Page Path

**GET** `/api/page-views/count?page_path=/blog/post/my-article`

Get the total view count for a specific page path.

**Response:**
```json
{
  "page_path": "/blog/post/my-article",
  "view_count": 42,
  "unique_visitors": 35
}
```

### 2a. Get Unique Visitor Count by Page Path

**GET** `/api/page-views/unique-visitors?page_path=/blog/post/my-article`

Get the number of unique visitors for a specific page path.

**Response:**
```json
{
  "page_path": "/blog/post/my-article",
  "unique_visitors": 35
}
```

### 3. Get View Count by Entity

**GET** `/api/page-views/count/entity?page_type=blog_post&entity_id=123`

Get the total view count for a specific entity (page type + entity ID).

**Response:**
```json
{
  "page_type": "blog_post",
  "entity_id": 123,
  "view_count": 42,
  "unique_visitors": 35
}
```

### 4. Get Page Statistics

**GET** `/api/page-views/statistics?page_path=/blog/post/my-article`

Get detailed statistics for a page path.

**Response:**
```json
{
  "total_views": 100,
  "unique_visitors": 75,
  "page_path": "/blog/post/my-article",
  "page_type": "blog_post",
  "entity_id": 123,
  "views_today": 5,
  "unique_visitors_today": 4,
  "views_this_week": 20,
  "unique_visitors_this_week": 15,
  "views_this_month": 50,
  "unique_visitors_this_month": 35
}
```

### 5. Get Entity Statistics

**GET** `/api/page-views/statistics/entity?page_type=blog_post&entity_id=123`

Get detailed statistics for an entity.

**Response:**
```json
{
  "total_views": 100,
  "page_path": "/blog/post/my-article",
  "page_type": "blog_post",
  "entity_id": 123,
  "views_today": 5,
  "views_this_week": 20,
  "views_this_month": 50
}
```

### 6. Get Total View Count

**GET** `/api/page-views/total`

Get the total number of page views across all pages.

**Response:**
```json
{
  "total_views": 10000,
  "total_unique_visitors": 7500
}
```

### 7. Get Most Viewed Pages

**GET** `/api/page-views/most-viewed?limit=10`

Get the most viewed pages with their view counts.

**Response:**
```json
{
  "/blog/post/article-1": 500,
  "/blog/post/article-2": 300,
  "/home": 200
}
```

### 8. Get View Count by Page Type

**GET** `/api/page-views/by-type`

Get view counts grouped by page type.

**Response:**
```json
{
  "blog_post": 5000,
  "deal": 2000,
  "home": 1000
}
```

### 9. Get Most Visited Pages by Unique Visitors

**GET** `/api/page-views/most-visited?limit=10`

Get the most visited pages ranked by unique visitor count.

**Response:**
```json
{
  "/blog/post/article-1": 450,
  "/blog/post/article-2": 280,
  "/home": 200
}
```

## Frontend Integration

### Example: Track a Page View

```typescript
// Track a page view when a page loads
async function trackPageView(pagePath: string, pageType?: string, entityId?: number) {
  try {
    const response = await fetch('/api/page-views/track', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        page_path: pagePath,
        page_type: pageType,
        entity_id: entityId
      })
    });
    
    const data = await response.json();
    console.log('View tracked:', data);
  } catch (error) {
    console.error('Error tracking page view:', error);
  }
}

// Example usage in a React component
useEffect(() => {
  // Track view when component mounts
  trackPageView('/blog/post/my-article', 'blog_post', 123);
}, []);
```

### Example: Get View Count

```typescript
async function getViewCount(pagePath: string) {
  try {
    const response = await fetch(`/api/page-views/count?page_path=${encodeURIComponent(pagePath)}`);
    const data = await response.json();
    return data.view_count;
  } catch (error) {
    console.error('Error getting view count:', error);
    return 0;
  }
}
```

## Rate Limiting

The system includes built-in rate limiting to prevent duplicate tracking:
- **Cooldown Period:** 5 seconds
- **Tracking Method:** Based on page path + IP address
- **Behavior:** If the same page is viewed from the same IP within 5 seconds, the view is not tracked again

This prevents:
- Double-counting from React StrictMode double-renders
- Accidental spam of view tracking
- Bot traffic inflation

## Page Types

Common page types you might use:
- `blog_post` - Blog post pages
- `blog_category` - Blog category pages
- `deal` - Deal pages
- `home` - Home page
- `about` - About page
- `contact` - Contact page

You can use any string value for `page_type` that makes sense for your application.

## Migration

To create the database table, you'll need to run a migration or create the table manually:

```sql
CREATE TABLE page_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    page_path VARCHAR(500) NOT NULL,
    page_type VARCHAR(100),
    entity_id INT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    referrer VARCHAR(500),
    viewed_at DATETIME NOT NULL,
    INDEX idx_page_path (page_path),
    INDEX idx_viewed_at (viewed_at)
);
```

If you're using JPA/Hibernate with auto-ddl enabled, the table will be created automatically.

## Notes

- **Unique User-Page Combinations**: The system only stores unique combinations of IP address + page path. If the same user (same IP) visits the same page multiple times, only one record is stored in the database.
- **View Count = Unique Visitors**: Since each record represents a unique user-page combination, the view count and unique visitor count are the same (each record = 1 unique visitor).
- **No Duplicate Storage**: The system checks if a record already exists for a specific IP + page path combination before saving. This prevents duplicate entries in the database.
- The system automatically extracts IP address, user agent, and referrer from the HTTP request
- All timestamps are stored in IST (Asia/Kolkata) timezone
- The system is designed to handle high traffic with efficient database queries and caching
- **Important**: Unique visitors are identified by IP address. Users behind the same NAT/proxy will be counted as one visitor

