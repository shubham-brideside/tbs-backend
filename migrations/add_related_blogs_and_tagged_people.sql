-- Migration: Add related_blogs_urls and tagged_people to blog_posts table
-- Date: 2025-01-27
-- Description: Adds support for Related Articles and Tagged People features

-- Add columns for related blogs URLs and tagged people
ALTER TABLE blog_posts 
ADD COLUMN related_blogs_urls JSON DEFAULT NULL COMMENT 'Array of URLs to related blog posts',
ADD COLUMN tagged_people JSON DEFAULT NULL COMMENT 'Array of tagged people with name and instagram_url';

-- Note: For MySQL 5.6 or earlier, use TEXT instead of JSON:
-- ALTER TABLE blog_posts 
-- ADD COLUMN related_blogs_urls TEXT DEFAULT NULL COMMENT 'Array of URLs to related blog posts (JSON string)',
-- ADD COLUMN tagged_people TEXT DEFAULT NULL COMMENT 'Array of tagged people with name and instagram_url (JSON string)';

