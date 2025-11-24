# Fix Duplicate Entries Issue

## Problem

You're seeing duplicate entries in the `page_views` table for the same user (IP address) visiting the same page. This happens due to a **race condition** when multiple requests come in simultaneously.

## Solution

I've implemented two fixes:

### 1. Database-Level Unique Constraint

Added a unique constraint on `(page_path, ip_address)` at the database level. This prevents duplicates even in race conditions.

### 2. Exception Handling

The service now catches `DataIntegrityViolationException` and handles it gracefully if a duplicate is attempted.

## Steps to Fix Existing Data

### Option 1: Run the SQL Script (Recommended)

1. **Backup your database first!**

2. **Run the SQL script:**
   ```bash
   mysql -u your_username -p your_database < add-unique-constraint-page-views.sql
   ```

   Or run it directly in your MySQL client:
   ```sql
   -- Remove duplicates (keeps oldest entry)
   DELETE pv1 FROM page_views pv1
   INNER JOIN page_views pv2 
   WHERE pv1.id > pv2.id 
     AND pv1.page_path = pv2.page_path 
     AND pv1.ip_address = pv2.ip_address
     AND pv1.ip_address IS NOT NULL;

   -- Add unique constraint
   ALTER TABLE page_views
   ADD CONSTRAINT uk_page_path_ip UNIQUE (page_path, ip_address);
   ```

### Option 2: Manual Cleanup

If you prefer to do it manually:

1. **Check for duplicates:**
   ```sql
   SELECT page_path, ip_address, COUNT(*) as count
   FROM page_views
   WHERE ip_address IS NOT NULL
   GROUP BY page_path, ip_address
   HAVING COUNT(*) > 1;
   ```

2. **Remove duplicates (keep the first one):**
   ```sql
   DELETE pv1 FROM page_views pv1
   INNER JOIN page_views pv2 
   WHERE pv1.id > pv2.id 
     AND pv1.page_path = pv2.page_path 
     AND pv1.ip_address = pv2.ip_address
     AND pv1.ip_address IS NOT NULL;
   ```

3. **Add the unique constraint:**
   ```sql
   ALTER TABLE page_views
   ADD CONSTRAINT uk_page_path_ip UNIQUE (page_path, ip_address);
   ```

## Verify the Fix

After running the script, verify:

1. **Check that duplicates are removed:**
   ```sql
   SELECT page_path, ip_address, COUNT(*) as count
   FROM page_views
   WHERE ip_address IS NOT NULL
   GROUP BY page_path, ip_address
   HAVING COUNT(*) > 1;
   ```
   Should return 0 rows.

2. **Check the constraint exists:**
   ```sql
   SHOW CREATE TABLE page_views;
   ```
   Should show `UNIQUE KEY 'uk_page_path_ip' (page_path, ip_address)`

3. **Test the application:**
   - Visit the same page multiple times from the same IP
   - Check the database - should only see 1 entry

## How It Works Now

1. **Application Check**: First checks if the combination exists (for performance)
2. **Database Constraint**: If two requests come in simultaneously, the unique constraint prevents the duplicate
3. **Exception Handling**: If a duplicate is attempted, it's caught and handled gracefully

## Important Notes

- **NULL IP Addresses**: The unique constraint only works for non-NULL IP addresses. If you have NULL IPs, they can still create duplicates. The code should always set an IP address, so this shouldn't be an issue.
- **Existing Duplicates**: You need to clean up existing duplicates before adding the constraint, otherwise the ALTER TABLE will fail.
- **Performance**: The unique constraint adds a small overhead on inserts, but provides data integrity.

## After Fixing

Once the constraint is in place:
- ✅ No more duplicate entries will be created
- ✅ Race conditions are handled at the database level
- ✅ Each unique user-page combination = exactly 1 record

