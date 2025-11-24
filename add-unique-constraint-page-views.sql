-- SQL script to add unique constraint and clean up duplicate entries
-- Run this script to fix the duplicate entries issue

-- Step 1: Remove duplicate entries (keep only the first one for each page_path + ip_address combination)
-- This will keep the oldest entry for each unique combination
DELETE pv1 FROM page_views pv1
INNER JOIN page_views pv2 
WHERE pv1.id > pv2.id 
  AND pv1.page_path = pv2.page_path 
  AND pv1.ip_address = pv2.ip_address
  AND pv1.ip_address IS NOT NULL;

-- Step 2: Add unique constraint on (page_path, ip_address)
-- Note: This will only work if ip_address is NOT NULL
-- If you have NULL ip_addresses, you may need to handle them separately
ALTER TABLE page_views
ADD CONSTRAINT uk_page_path_ip UNIQUE (page_path, ip_address);

-- Verify the constraint was added
-- You can check with: SHOW CREATE TABLE page_views;

