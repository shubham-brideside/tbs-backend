# Blog Data Storage and Flow

## 📊 Database Tables

### 1. `blog_categories` Table
**Purpose**: Stores blog categories (e.g., "Wedding", "Photography")

**Columns**:
```
id                 INT (Primary Key, Auto Increment)
name               VARCHAR(100) NOT NULL UNIQUE
slug               VARCHAR(100) NOT NULL UNIQUE  
description        TEXT
featured_image_url VARCHAR(500)
is_active          BOOLEAN NOT NULL DEFAULT true
created_at         DATETIME NOT NULL
updated_at         DATETIME NOT NULL
```

### 2. `blog_posts` Table
**Purpose**: Stores individual blog posts/articles

**Columns**:
```
id                 INT (Primary Key, Auto Increment)
title              VARCHAR(255) NOT NULL
slug               VARCHAR(255) NOT NULL UNIQUE
excerpt            TEXT
content            LONGTEXT
featured_image_url VARCHAR(500)
category_id        INT NOT NULL (Foreign Key → blog_categories.id)
meta_description   VARCHAR(500)
meta_keywords      VARCHAR(255)
is_published       BOOLEAN NOT NULL DEFAULT false
published_at       DATETIME
view_count         INT NOT NULL DEFAULT 0
related_links      TEXT (JSON string)
created_at         DATETIME NOT NULL
updated_at         DATETIME NOT NULL
```

**Relationship**: 
- `blog_posts.category_id` → `blog_categories.id` (Many-to-One)
- One category can have many posts
- Each post belongs to one category

---

## 🔄 Data Flow

### ➡️ **INSERTING DATA** (Where data goes INTO the database)

#### 1. Create Category
**API**: `POST /api/blog/categories`

**Request Body**:
```json
{
  "name": "Wedding",
  "slug": "wedding",
  "description": "Wedding related articles...",
  "featured_image_url": "https://...",
  "is_active": true
}
```

**Data Storage**:
- ✅ Goes into → `blog_categories` table
- Creates a new row with auto-generated `id`

#### 2. Create Blog Post
**API**: `POST /api/blog/posts`

**Request Body**:
```json
{
  "title": "213+ Simple Mehndi Designs",
  "slug": "213-simple-mehndi-designs",
  "excerpt": "Discover beautiful mehndi designs...",
  "content": "<h2>Introduction</h2><p>...</p>",
  "featured_image_url": "https://...",
  "category_id": 1,
  "meta_description": "...",
  "meta_keywords": "...",
  "is_published": true
}
```

**Data Storage**:
- ✅ Goes into → `blog_posts` table
- Links to category via `category_id` (must reference existing category)
- Creates a new row with auto-generated `id`

---

### ⬅️ **FETCHING DATA** (Where data comes FROM the database)

#### 1. Get All Categories
**API**: `GET /api/blog/categories`

**Database Query**:
```sql
SELECT * FROM blog_categories 
WHERE is_active = true
ORDER BY created_at DESC;
```

**Returns**: All active categories from `blog_categories` table

---

#### 2. Get Category by Slug (with Posts)
**API**: `GET /api/blog/categories/slug/wedding`

**Database Query**:
```sql
-- First get category
SELECT * FROM blog_categories 
WHERE slug = 'wedding' AND is_active = true;

-- Then get posts in that category
SELECT * FROM blog_posts 
WHERE category_id = 1 AND is_published = true
ORDER BY published_at DESC;
```

**Returns**: 
- Category data from `blog_categories` table
- Posts list from `blog_posts` table (filtered by category)

---

#### 3. Get All Published Posts
**API**: `GET /api/blog/posts`

**Database Query**:
```sql
SELECT bp.*, bc.id as category_id, bc.name as category_name, bc.slug as category_slug
FROM blog_posts bp
INNER JOIN blog_categories bc ON bp.category_id = bc.id
WHERE bp.is_published = true
ORDER BY bp.published_at DESC;
```

**Returns**: 
- All posts from `blog_posts` table
- Joined with category info from `blog_categories` table
- Only published posts (`is_published = true`)

---

#### 4. Get Post by Slug
**API**: `GET /api/blog/posts/slug/213-simple-mehndi-designs`

**Database Query**:
```sql
-- Get post with category
SELECT bp.*, bc.id as category_id, bc.name as category_name, bc.slug as category_slug
FROM blog_posts bp
INNER JOIN blog_categories bc ON bp.category_id = bc.id
WHERE bp.slug = '213-simple-mehndi-designs' 
  AND bp.is_published = true;

-- Increment view count
UPDATE blog_posts 
SET view_count = view_count + 1 
WHERE id = 1;
```

**Returns**: 
- Single post from `blog_posts` table
- Category info from `blog_categories` table
- Also increments `view_count` in `blog_posts` table

---

#### 5. Get Posts by Category
**API**: `GET /api/blog/posts/category/wedding`

**Database Query**:
```sql
SELECT bp.*, bc.id as category_id, bc.name as category_name, bc.slug as category_slug
FROM blog_posts bp
INNER JOIN blog_categories bc ON bp.category_id = bc.id
WHERE bc.slug = 'wedding' 
  AND bp.is_published = true
ORDER BY bp.published_at DESC;
```

**Returns**: 
- Posts from `blog_posts` table
- Filtered by category slug from `blog_categories` table
- Only published posts

---

## 🗄️ Database Connection Details

**Database**: MySQL  
**Database Name**: `thebrideside`  
**Host**: `thebrideside.mysql.database.azure.com:3306`  
**Connection Pool**: HikariCP (configured in `application.yml`)

**Auto Table Creation**: 
- Tables are automatically created by JPA/Hibernate
- Configuration: `spring.jpa.hibernate.ddl-auto: update`
- Happens on application startup if tables don't exist

---

## 📝 Summary

| Action | API Endpoint | Table(s) | Operation |
|--------|-------------|----------|-----------|
| **Create Category** | `POST /api/blog/categories` | `blog_categories` | INSERT |
| **Create Post** | `POST /api/blog/posts` | `blog_posts` | INSERT |
| **Get Categories** | `GET /api/blog/categories` | `blog_categories` | SELECT |
| **Get Category + Posts** | `GET /api/blog/categories/slug/{slug}` | `blog_categories` + `blog_posts` | SELECT + JOIN |
| **Get All Posts** | `GET /api/blog/posts` | `blog_posts` + `blog_categories` | SELECT + JOIN |
| **Get Single Post** | `GET /api/blog/posts/slug/{slug}` | `blog_posts` + `blog_categories` | SELECT + JOIN + UPDATE |
| **Get Posts by Category** | `GET /api/blog/posts/category/{slug}` | `blog_posts` + `blog_categories` | SELECT + JOIN |

---

## 🔍 Current Data Status

To check your current data:

```bash
# View all categories
curl http://localhost:8080/api/blog/categories

# View all posts
curl http://localhost:8080/api/blog/posts

# View category with posts
curl http://localhost:8080/api/blog/categories/slug/wedding
```

All data is stored in **MySQL database** on Azure, accessed through **Spring Data JPA** repositories.

