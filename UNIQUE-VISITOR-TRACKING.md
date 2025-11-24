# Unique Visitor Tracking - Implementation Summary

## Overview

The page view tracking system now tracks **unique visitors** in addition to total page views. Unique visitors are identified by their IP address.

## What Changed

### ✅ New Features

1. **Unique Visitor Counts** - All endpoints now return unique visitor counts
2. **Unique Visitor Statistics** - Statistics include unique visitors for today, this week, and this month
3. **Most Visited Pages** - New endpoint to get pages ranked by unique visitors

### 📊 Database

The `page_views` table stores IP addresses, which are used to calculate unique visitors:

```sql
-- Count unique visitors for a page
SELECT COUNT(DISTINCT ip_address) 
FROM page_views 
WHERE page_path = '/your/page/path' 
  AND ip_address IS NOT NULL;
```

## API Endpoints Updated

### 1. Track Page View
**Response now includes:**
```json
{
  "tracked": true,
  "view_count": 42,
  "unique_visitors": 35  // NEW
}
```

### 2. Get View Count
**Response now includes:**
```json
{
  "page_path": "/blog/post/my-article",
  "view_count": 42,
  "unique_visitors": 35  // NEW
}
```

### 3. Get Statistics
**Response now includes:**
```json
{
  "total_views": 100,
  "unique_visitors": 75,  // NEW
  "views_today": 5,
  "unique_visitors_today": 4,  // NEW
  "views_this_week": 20,
  "unique_visitors_this_week": 15,  // NEW
  "views_this_month": 50,
  "unique_visitors_this_month": 35  // NEW
}
```

### 4. New Endpoint: Get Unique Visitor Count
**GET** `/api/page-views/unique-visitors?page_path=/blog/post/my-article`

Returns only the unique visitor count for a page.

### 5. New Endpoint: Most Visited Pages by Unique Visitors
**GET** `/api/page-views/most-visited?limit=10`

Returns pages ranked by unique visitor count.

## SQL Queries for Unique Visitors

### Count Unique Visitors for a Page
```sql
SELECT COUNT(DISTINCT ip_address) as unique_visitors
FROM page_views
WHERE page_path = '/blog/post/my-article'
  AND ip_address IS NOT NULL;
```

### Count Unique Visitors Today
```sql
SELECT COUNT(DISTINCT ip_address) as unique_visitors_today
FROM page_views
WHERE page_path = '/blog/post/my-article'
  AND DATE(viewed_at) = CURDATE()
  AND ip_address IS NOT NULL;
```

### Most Visited Pages by Unique Visitors
```sql
SELECT 
    page_path,
    COUNT(DISTINCT ip_address) as unique_visitors
FROM page_views
WHERE ip_address IS NOT NULL
GROUP BY page_path
ORDER BY unique_visitors DESC
LIMIT 10;
```

## Important Notes

### How Unique Visitors Are Counted

- **Based on IP Address**: Each unique IP address is counted as one visitor
- **Limitations**: 
  - Users behind the same NAT/proxy will be counted as one visitor
  - Users with dynamic IPs may be counted as multiple visitors
  - This is a common limitation of IP-based tracking

### Best Practices

1. **Display Both Metrics**: Show both total views and unique visitors to users
2. **Use for Analytics**: Unique visitors give a better sense of actual user engagement
3. **Combine with Other Metrics**: Use unique visitors along with session duration, bounce rate, etc.

## Frontend Integration

The frontend code doesn't need to change - the API responses now automatically include unique visitor counts. You can update your UI to display them:

```tsx
// Example: Display unique visitors
function ViewStats({ pagePath }) {
  const [stats, setStats] = useState(null);
  
  useEffect(() => {
    getPageStatistics(pagePath).then(setStats);
  }, [pagePath]);
  
  return (
    <div>
      <p>Total Views: {stats?.total_views}</p>
      <p>Unique Visitors: {stats?.unique_visitors}</p>
      <p>Today: {stats?.unique_visitors_today} unique visitors</p>
    </div>
  );
}
```

## Summary

✅ **Unique visitor tracking is now fully implemented**
✅ **All existing endpoints return unique visitor counts**
✅ **New endpoints for unique visitor analytics**
✅ **No breaking changes** - existing code will continue to work
✅ **Backward compatible** - view counts still available

The system now provides comprehensive analytics with both total views and unique visitor metrics!

