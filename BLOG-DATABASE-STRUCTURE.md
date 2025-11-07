# Blog Database Structure

## Database Information
- **Database Type**: MySQL
- **Database Name**: `thebrideside`
- **Host**: `thebrideside.mysql.database.azure.com:3306`
- **Connection**: Via JDBC with HikariCP connection pooling

## Tables

### 1. `blog_categories` Table
Stores blog categories (e.g., "Wedding", "Photography", etc.)

**Columns:**
- `id` (INT, Primary Key, Auto Increment)
- `name` (VARCHAR(100), NOT NULL, UNIQUE) - Category name
- `slug` (VARCHAR(100), NOT NULL, UNIQUE) - URL-friendly identifier
- `description` (TEXT) - Category description
- `featured_image_url` (VARCHAR(500)) - Featured image URL
- `is_active` (BOOLEAN, NOT NULL, DEFAULT true) - Whether category is active
- `created_at` (DATETIME, NOT NULL) - Creation timestamp
- `updated_at` (DATETIME, NOT NULL) - Last update timestamp

### 2. `blog_posts` Table
Stores individual blog posts/articles

**Columns:**
- `id` (INT, Primary Key, Auto Increment)
- `title` (VARCHAR(255), NOT NULL) - Post title
- `slug` (VARCHAR(255), NOT NULL, UNIQUE) - URL-friendly identifier
- `excerpt` (TEXT) - Short description/excerpt
- `content` (LONGTEXT) - Full post content (HTML)
- `featured_image_url` (VARCHAR(500)) - Featured image URL
- `category_id` (INT, NOT NULL, Foreign Key → blog_categories.id) - Category reference
- `meta_description` (VARCHAR(500)) - SEO meta description
- `meta_keywords` (VARCHAR(255)) - SEO keywords
- `is_published` (BOOLEAN, NOT NULL, DEFAULT false) - Publication status
- `published_at` (DATETIME) - Publication date
- `view_count` (INT, NOT NULL, DEFAULT 0) - Number of views
- `related_links` (TEXT) - JSON string for related links
- `created_at` (DATETIME, NOT NULL) - Creation timestamp
- `updated_at` (DATETIME, NOT NULL) - Last update timestamp

## Relationship
- **One-to-Many**: One `blog_categories` record can have many `blog_posts` records
- Foreign Key: `blog_posts.category_id` → `blog_categories.id`

## Data Flow

### Inserting Data (POST APIs)
1. **Create Category**: `POST /api/blog/categories`
   - Data goes into → `blog_categories` table
   
2. **Create Post**: `POST /api/blog/posts`
   - Data goes into → `blog_posts` table
   - Requires `category_id` to link to a category

### Fetching Data (GET APIs)
1. **Get All Categories**: `GET /api/blog/categories`
   - Fetches from → `blog_categories` table
   - Filters: `is_active = true`

2. **Get Category by Slug**: `GET /api/blog/categories/slug/{slug}`
   - Fetches from → `blog_categories` table
   - Joins with → `blog_posts` table (to get posts in category)
   - Filters: `is_published = true`

3. **Get All Posts**: `GET /api/blog/posts`
   - Fetches from → `blog_posts` table
   - Joins with → `blog_categories` table (to get category info)
   - Filters: `is_published = true`

4. **Get Post by Slug**: `GET /api/blog/posts/slug/{slug}`
   - Fetches from → `blog_posts` table
   - Joins with → `blog_categories` table
   - Filters: `is_published = true`
   - Also increments `view_count`

5. **Get Posts by Category**: `GET /api/blog/posts/category/{categorySlug}`
   - Fetches from → `blog_posts` table
   - Joins with → `blog_categories` table
   - Filters by category slug and `is_published = true`
