# Unique User-Page Tracking Implementation

## Overview

The page view tracking system has been updated to **only store unique user-page combinations** in the database. This means:

- ✅ **Same user + Same page = Only 1 record stored**
- ✅ **Different user + Same page = New record stored**
- ✅ **Same user + Different page = New record stored**

## How It Works

### Before (Old Behavior)
- Every page view was stored, even from the same user
- Same IP visiting the same page 10 times = 10 database records
- View count = Total number of records
- Unique visitors = Count of distinct IPs

### After (New Behavior)
- Only unique user-page combinations are stored
- Same IP visiting the same page 10 times = 1 database record
- View count = Number of unique user-page combinations
- Unique visitors = Same as view count (each record = 1 unique visitor)

## Implementation Details

### Database Check
Before saving a page view, the system checks:
```java
boolean exists = pageViewRepository.existsByPagePathAndIpAddress(pagePath, ipAddress);
```

If `exists = true`, the view is **not saved** and the existing record is used.

### API Response
The API response remains the same, but now:
- `view_count` = Number of unique user-page combinations
- `unique_visitors` = Same as view_count (since each record is unique)

```json
{
  "tracked": true,
  "view_count": 35,        // 35 unique user-page combinations
  "unique_visitors": 35    // Same as view_count
}
```

## Benefits

1. **Reduced Database Size**: No duplicate entries for the same user visiting the same page
2. **Accurate Unique Visitor Count**: Each record represents exactly one unique visitor
3. **Better Performance**: Fewer records to query and process
4. **Cleaner Data**: Database only contains unique combinations

## Example Scenarios

### Scenario 1: Same User, Same Page
- User A (IP: 192.168.1.1) visits `/blog/post/article-1` 5 times
- **Result**: Only 1 record stored in database
- View count: 1
- Unique visitors: 1

### Scenario 2: Different Users, Same Page
- User A (IP: 192.168.1.1) visits `/blog/post/article-1`
- User B (IP: 192.168.1.2) visits `/blog/post/article-1`
- **Result**: 2 records stored in database
- View count: 2
- Unique visitors: 2

### Scenario 3: Same User, Different Pages
- User A (IP: 192.168.1.1) visits `/blog/post/article-1`
- User A (IP: 192.168.1.1) visits `/blog/post/article-2`
- **Result**: 2 records stored in database
- View count: 2 (one for each page)
- Unique visitors: 1 (same IP, but different pages)

## Database Query

You can verify this behavior with SQL:

```sql
-- Check if a specific IP has viewed a specific page
SELECT COUNT(*) 
FROM page_views 
WHERE page_path = '/blog/post/article-1' 
  AND ip_address = '192.168.1.1';

-- Result will always be 0 or 1 (never more than 1)
```

## Rate Limiting

The system still includes rate limiting (5-second cooldown) for performance:
- Prevents rapid repeated API calls
- Reduces database queries
- Works in combination with the unique check

## Migration Notes

### Existing Data
- Existing duplicate records in the database will remain
- New tracking will prevent new duplicates
- To clean existing data, you can run:

```sql
-- Remove duplicate entries (keep only the first one)
DELETE pv1 FROM page_views pv1
INNER JOIN page_views pv2 
WHERE pv1.id > pv2.id 
  AND pv1.page_path = pv2.page_path 
  AND pv1.ip_address = pv2.ip_address;
```

**⚠️ Warning**: Run this carefully and backup your data first!

## API Behavior

### Track Page View Response

**First Visit (New User-Page Combination):**
```json
{
  "tracked": true,
  "message": "View tracked successfully",
  "view_count": 1,
  "unique_visitors": 1
}
```

**Subsequent Visits (Same User, Same Page):**
```json
{
  "tracked": false,
  "message": "View already tracked for this user and page",
  "view_count": 1,
  "unique_visitors": 1
}
```

## Summary

✅ **Only unique user-page combinations are stored**
✅ **No duplicate entries in database**
✅ **View count = Unique visitor count**
✅ **Cleaner, more efficient data storage**
✅ **Same API interface (backward compatible)**

The system now provides accurate unique visitor tracking by preventing duplicate storage at the database level!

