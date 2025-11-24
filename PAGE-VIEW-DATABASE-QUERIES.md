# Page View Database Queries

## Table Name

**`page_views`** - This table stores all individual page view records.

## Table Structure

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

## Useful SQL Queries

### 1. View All Page Views

```sql
SELECT * FROM page_views ORDER BY viewed_at DESC;
```

### 2. Get Total View Count for a Specific Page

```sql
SELECT page_path, COUNT(*) as view_count 
FROM page_views 
WHERE page_path = '/blog/post/my-article'
GROUP BY page_path;
```

### 3. Get View Count for All Pages (Most Viewed First)

```sql
SELECT page_path, COUNT(*) as view_count 
FROM page_views 
GROUP BY page_path 
ORDER BY view_count DESC;
```

### 4. Get View Count by Page Type

```sql
SELECT page_type, COUNT(*) as view_count 
FROM page_views 
WHERE page_type IS NOT NULL
GROUP BY page_type 
ORDER BY view_count DESC;
```

### 5. Get View Count for a Specific Entity (e.g., Blog Post ID 123)

```sql
SELECT page_path, page_type, entity_id, COUNT(*) as view_count 
FROM page_views 
WHERE page_type = 'blog_post' AND entity_id = 123
GROUP BY page_path, page_type, entity_id;
```

### 6. Get Views Today

```sql
SELECT page_path, COUNT(*) as views_today 
FROM page_views 
WHERE DATE(viewed_at) = CURDATE()
GROUP BY page_path 
ORDER BY views_today DESC;
```

### 7. Get Views This Week

```sql
SELECT page_path, COUNT(*) as views_this_week 
FROM page_views 
WHERE viewed_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY page_path 
ORDER BY views_this_week DESC;
```

### 8. Get Views This Month

```sql
SELECT page_path, COUNT(*) as views_this_month 
FROM page_views 
WHERE MONTH(viewed_at) = MONTH(NOW()) 
  AND YEAR(viewed_at) = YEAR(NOW())
GROUP BY page_path 
ORDER BY views_this_month DESC;
```

### 9. Get Total View Count Across All Pages

```sql
SELECT COUNT(*) as total_views FROM page_views;
```

### 10. Get Recent Views (Last 10)

```sql
SELECT page_path, page_type, entity_id, ip_address, viewed_at 
FROM page_views 
ORDER BY viewed_at DESC 
LIMIT 10;
```

### 11. Get Views by IP Address (to see unique visitors)

```sql
SELECT page_path, COUNT(DISTINCT ip_address) as unique_visitors, COUNT(*) as total_views 
FROM page_views 
GROUP BY page_path 
ORDER BY total_views DESC;
```

### 12. Get Views for a Specific Date Range

```sql
SELECT page_path, COUNT(*) as view_count 
FROM page_views 
WHERE viewed_at BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59'
GROUP BY page_path 
ORDER BY view_count DESC;
```

### 13. Get Most Viewed Pages (Top 10)

```sql
SELECT page_path, COUNT(*) as view_count 
FROM page_views 
GROUP BY page_path 
ORDER BY view_count DESC 
LIMIT 10;
```

### 14. Get View Statistics for a Specific Page (Complete Stats)

```sql
SELECT 
    page_path,
    COUNT(*) as total_views,
    COUNT(DISTINCT ip_address) as unique_visitors,
    COUNT(CASE WHEN DATE(viewed_at) = CURDATE() THEN 1 END) as views_today,
    COUNT(CASE WHEN viewed_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 END) as views_this_week,
    COUNT(CASE WHEN MONTH(viewed_at) = MONTH(NOW()) AND YEAR(viewed_at) = YEAR(NOW()) THEN 1 END) as views_this_month,
    MIN(viewed_at) as first_view,
    MAX(viewed_at) as last_view
FROM page_views 
WHERE page_path = '/blog/post/my-article'
GROUP BY page_path;
```

### 15. Get Views by Hour (to see peak traffic times)

```sql
SELECT 
    HOUR(viewed_at) as hour,
    COUNT(*) as view_count 
FROM page_views 
WHERE DATE(viewed_at) = CURDATE()
GROUP BY HOUR(viewed_at) 
ORDER BY hour;
```

## Quick Reference

| What You Want | SQL Query |
|--------------|-----------|
| **Total views for a page** | `SELECT COUNT(*) FROM page_views WHERE page_path = '/your/page/path';` |
| **All page view counts** | `SELECT page_path, COUNT(*) as count FROM page_views GROUP BY page_path ORDER BY count DESC;` |
| **Views today** | `SELECT COUNT(*) FROM page_views WHERE DATE(viewed_at) = CURDATE();` |
| **Most viewed pages** | `SELECT page_path, COUNT(*) as count FROM page_views GROUP BY page_path ORDER BY count DESC LIMIT 10;` |
| **Views by type** | `SELECT page_type, COUNT(*) FROM page_views WHERE page_type IS NOT NULL GROUP BY page_type;` |

## Notes

- **Individual Records**: Each row in `page_views` represents ONE page view
- **Counts are Calculated**: View counts are calculated using `COUNT(*)` - they're not stored as a number
- **Indexes**: The table has indexes on `page_path` and `viewed_at` for fast queries
- **Time Zone**: All timestamps are stored in IST (Asia/Kolkata) timezone

## Example: Check if Tracking is Working

1. **View a page** in your application
2. **Run this query** to see if the view was recorded:
   ```sql
   SELECT * FROM page_views ORDER BY viewed_at DESC LIMIT 5;
   ```
3. **You should see** a new row with the page path, IP address, and timestamp

