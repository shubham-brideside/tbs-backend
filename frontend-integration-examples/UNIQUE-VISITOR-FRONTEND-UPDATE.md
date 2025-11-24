# Frontend Update Guide - Unique Visitor Tracking

## 🎯 What Changed

The page view tracking API now includes **unique visitor counts** in all responses. You can now see how many unique users have visited each page!

## 📋 Quick Summary

- ✅ **No breaking changes** - All existing code will continue to work
- ✅ **New data available** - API responses now include `unique_visitors` field
- ✅ **Optional updates** - You can update your UI to display unique visitors

## 🔄 API Response Changes

### Before (Still Works)
```json
{
  "view_count": 42
}
```

### After (Now Available)
```json
{
  "view_count": 42,
  "unique_visitors": 35  // NEW!
}
```

## 📝 Updated TypeScript Interfaces

If you're using TypeScript, update your interfaces:

```typescript
// Track View Response
export interface TrackViewResponse {
  tracked: boolean;
  message: string;
  view_count: number;
  unique_visitors: number;  // ADD THIS
}

// View Count Response
export interface ViewCountResponse {
  page_path: string;
  view_count: number;
  unique_visitors: number;  // ADD THIS
}

// View Statistics
export interface ViewStatistics {
  total_views: number;
  unique_visitors: number;  // ADD THIS
  views_today: number;
  unique_visitors_today: number;  // ADD THIS
  views_this_week: number;
  unique_visitors_this_week: number;  // ADD THIS
  views_this_month: number;
  unique_visitors_this_month: number;  // ADD THIS
}
```

## 🎨 Display Unique Visitors in Your UI

### Example 1: Simple Display

```tsx
function BlogPostPage() {
  const [viewCount, setViewCount] = useState(0);
  const [uniqueVisitors, setUniqueVisitors] = useState(0);

  useEffect(() => {
    trackPageView(pagePath, 'blog_post', postId).then(result => {
      setViewCount(result.view_count);
      setUniqueVisitors(result.unique_visitors);  // NEW
    });
  }, []);

  return (
    <div>
      <h1>Blog Post</h1>
      <p>👁️ {viewCount} views</p>
      <p>👥 {uniqueVisitors} unique visitors</p>  {/* NEW */}
    </div>
  );
}
```

### Example 2: Combined Display

```tsx
function ViewStats({ pagePath }) {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    getPageStatistics(pagePath).then(setStats);
  }, [pagePath]);

  return (
    <div>
      <div>
        <strong>Total:</strong> {stats?.total_views} views 
        ({stats?.unique_visitors} unique visitors)
      </div>
      <div>
        <strong>Today:</strong> {stats?.views_today} views 
        ({stats?.unique_visitors_today} unique)
      </div>
      <div>
        <strong>This Week:</strong> {stats?.views_this_week} views 
        ({stats?.unique_visitors_this_week} unique)
      </div>
    </div>
  );
}
```

### Example 3: Blog Post Card

```tsx
function BlogPostCard({ post }) {
  const [viewCount, setViewCount] = useState(0);
  const [uniqueVisitors, setUniqueVisitors] = useState(0);

  useEffect(() => {
    getViewCount(`/blog/post/${post.slug}`).then(data => {
      setViewCount(data.view_count);
      setUniqueVisitors(data.unique_visitors);
    });
  }, [post.slug]);

  return (
    <div className="blog-post-card">
      <h3>{post.title}</h3>
      <div className="post-meta">
        <span>👁️ {viewCount}</span>
        <span>👥 {uniqueVisitors} unique</span>
      </div>
    </div>
  );
}
```

## 🔍 New API Endpoint

### Get Unique Visitor Count Only

```typescript
// New endpoint: Get only unique visitor count
GET /api/page-views/unique-visitors?page_path=/blog/post/my-article

// Response:
{
  "page_path": "/blog/post/my-article",
  "unique_visitors": 35
}
```

**Usage:**
```typescript
export async function getUniqueVisitorCount(pagePath: string) {
  const response = await fetch(
    `${PAGE_VIEW_BASE_URL}/unique-visitors?page_path=${encodeURIComponent(pagePath)}`
  );
  return await response.json();
}
```

## 📊 Statistics Response

The statistics endpoint now includes unique visitor data:

```json
{
  "total_views": 100,
  "unique_visitors": 75,
  "views_today": 5,
  "unique_visitors_today": 4,
  "views_this_week": 20,
  "unique_visitors_this_week": 15,
  "views_this_month": 50,
  "unique_visitors_this_month": 35
}
```

## 🎯 What Are Unique Visitors?

- **Unique visitors** = Number of distinct IP addresses that visited the page
- **Total views** = Total number of times the page was viewed (includes repeat visits)

**Example:**
- User A visits 3 times → 3 views, 1 unique visitor
- User B visits 2 times → 2 views, 1 unique visitor
- **Total:** 5 views, 2 unique visitors

## ⚠️ Important Notes

1. **Backward Compatible**: Your existing code will continue to work
2. **Optional Update**: You don't have to update your code immediately
3. **IP-Based**: Unique visitors are identified by IP address
4. **Limitations**: Users behind the same NAT/proxy count as one visitor

## ✅ Migration Checklist

- [ ] Update TypeScript interfaces (if using TypeScript)
- [ ] Update UI components to display unique visitors (optional)
- [ ] Test the new unique visitor counts
- [ ] Update any analytics dashboards

## 🚀 Quick Start

1. **No changes required** - Your existing code works as-is
2. **Add unique visitor display** - Update your UI when ready
3. **Test it out** - Check the API responses in Network tab

That's it! The unique visitor tracking is now available in all API responses. 🎉

