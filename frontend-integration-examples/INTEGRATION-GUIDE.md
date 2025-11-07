# Blog API Frontend Integration Guide

This guide explains how to integrate the Brideside Blog API with your frontend application.

## 📋 Table of Contents

1. [API Base URL](#api-base-url)
2. [Quick Start](#quick-start)
3. [API Endpoints](#api-endpoints)
4. [Example Code](#example-code)
5. [CORS Configuration](#cors-configuration)
6. [Error Handling](#error-handling)
7. [TypeScript Support](#typescript-support)

## 🌐 API Base URL

- **Development**: `http://localhost:8080/api/blog`
- **Production**: Replace with your production URL (e.g., `https://yourdomain.com/api/blog`)

## 🚀 Quick Start

### For React/JavaScript Projects

1. **Copy the API service file** to your project:
   ```bash
   # For JavaScript projects
   cp frontend-integration-examples/blog-api-service.js src/services/
   
   # For TypeScript projects
   cp frontend-integration-examples/blog-api-service.ts src/services/
   ```

2. **Update the BASE_URL** in the service file:
   ```javascript
   const BASE_URL = 'http://localhost:8080/api/blog'; // Change to your backend URL
   ```

3. **Use in your components**:
   ```javascript
   import blogApi from './services/blog-api-service';
   
   // Get all categories
   const categories = await blogApi.getCategories();
   
   // Get category with posts
   const category = await blogApi.getCategoryBySlug('wedding');
   
   // Get a blog post
   const post = await blogApi.getPostBySlug('100-latest-haldi-decoration-ideas');
   ```

### For Vue.js Projects

Create a similar service file or use a composable:

```javascript
// composables/useBlogApi.js
import { ref } from 'vue';
import blogApi from '@/services/blog-api-service';

export function useBlogApi() {
  const categories = ref([]);
  const posts = ref([]);
  const loading = ref(false);
  const error = ref(null);

  const fetchCategories = async () => {
    loading.value = true;
    try {
      categories.value = await blogApi.getCategories();
      error.value = null;
    } catch (err) {
      error.value = err.message;
    } finally {
      loading.value = false;
    }
  };

  return {
    categories,
    posts,
    loading,
    error,
    fetchCategories,
  };
}
```

### For Angular Projects

Create a service:

```typescript
// services/blog-api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BlogApiService {
  private baseUrl = 'http://localhost:8080/api/blog';

  constructor(private http: HttpClient) {}

  getCategories(): Observable<BlogCategory[]> {
    return this.http.get<BlogCategory[]>(`${this.baseUrl}/categories`);
  }

  getCategoryBySlug(slug: string): Observable<BlogCategory> {
    return this.http.get<BlogCategory>(`${this.baseUrl}/categories/slug/${slug}`);
  }

  getPostBySlug(slug: string): Observable<BlogPost> {
    return this.http.get<BlogPost>(`${this.baseUrl}/posts/slug/${slug}`);
  }
  
  // Add other methods...
}
```

## 📡 API Endpoints

### Category Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/categories` | Get all active categories |
| GET | `/categories/slug/{slug}` | Get category by slug with posts |
| GET | `/categories/{id}` | Get category by ID |
| POST | `/categories` | Create category (admin) |
| PUT | `/categories/{id}` | Update category (admin) |
| DELETE | `/categories/{id}` | Delete category (admin) |

### Post Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/posts` | Get all published posts |
| GET | `/posts/slug/{slug}` | Get post by slug (increments view count) |
| GET | `/posts/{id}` | Get post by ID |
| GET | `/posts/category/{categorySlug}` | Get posts by category |
| POST | `/posts` | Create post (admin) |
| PUT | `/posts/{id}` | Update post (admin) |
| DELETE | `/posts/{id}` | Delete post (admin) |

## 💻 Example Code

### Example 1: Display Categories List

```javascript
import React, { useEffect, useState } from 'react';
import blogApi from './services/blog-api-service';

function CategoriesList() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadCategories() {
      try {
        const data = await blogApi.getCategories();
        setCategories(data);
      } catch (error) {
        console.error('Failed to load categories:', error);
      } finally {
        setLoading(false);
      }
    }
    loadCategories();
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h1>Blog Categories</h1>
      {categories.map(category => (
        <div key={category.id}>
          <h2>{category.name}</h2>
          <p>{category.description}</p>
          <a href={`/blog/category/${category.slug}`}>View Posts</a>
        </div>
      ))}
    </div>
  );
}
```

### Example 2: Display Category Page with Posts

```javascript
import React, { useEffect, useState } from 'react';
import blogApi from './services/blog-api-service';

function CategoryPage({ categorySlug }) {
  const [category, setCategory] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadCategory() {
      try {
        const data = await blogApi.getCategoryBySlug(categorySlug);
        setCategory(data);
      } catch (error) {
        console.error('Failed to load category:', error);
      } finally {
        setLoading(false);
      }
    }
    loadCategory();
  }, [categorySlug]);

  if (loading) return <div>Loading...</div>;
  if (!category) return <div>Category not found</div>;

  return (
    <div>
      <h1>{category.name}</h1>
      <p>{category.description}</p>
      
      {category.featuredImageUrl && (
        <img src={category.featuredImageUrl} alt={category.name} />
      )}

      <div className="posts-grid">
        {category.posts?.map(post => (
          <article key={post.id} className="post-card">
            <img src={post.featuredImageUrl} alt={post.title} />
            <h3>{post.title}</h3>
            <a href={`/blog/${post.slug}`}>Read More</a>
          </article>
        ))}
      </div>
    </div>
  );
}
```

### Example 3: Display Single Blog Post

```javascript
import React, { useEffect, useState } from 'react';
import blogApi from './services/blog-api-service';

function BlogPost({ postSlug }) {
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadPost() {
      try {
        const data = await blogApi.getPostBySlug(postSlug);
        setPost(data);
      } catch (error) {
        console.error('Failed to load post:', error);
      } finally {
        setLoading(false);
      }
    }
    loadPost();
  }, [postSlug]);

  if (loading) return <div>Loading...</div>;
  if (!post) return <div>Post not found</div>;

  return (
    <article>
      <h1>{post.title}</h1>
      <div className="meta">
        <span>Category: {post.category.name}</span>
        <span>Views: {post.viewCount}</span>
      </div>
      
      {post.featuredImageUrl && (
        <img src={post.featuredImageUrl} alt={post.title} />
      )}

      <div dangerouslySetInnerHTML={{ __html: post.content }} />
    </article>
  );
}
```

## 🔧 CORS Configuration

The backend is already configured to allow CORS from any origin (`@CrossOrigin(origins = "*")`). For production, you may want to restrict this to your specific domain.

If you encounter CORS issues, check:
1. The backend is running
2. The API URL is correct
3. You're using the correct HTTP method
4. Content-Type header is set to `application/json` for POST/PUT requests

## ⚠️ Error Handling

Always wrap API calls in try-catch blocks:

```javascript
try {
  const post = await blogApi.getPostBySlug('some-slug');
  // Handle success
} catch (error) {
  if (error.message.includes('404')) {
    // Handle not found
  } else if (error.message.includes('500')) {
    // Handle server error
  } else {
    // Handle other errors
  }
}
```

## 📝 TypeScript Support

For TypeScript projects, use the TypeScript version of the service file which includes type definitions:

```typescript
import blogApi, { BlogPost, BlogCategory } from './services/blog-api-service';

// TypeScript will provide autocomplete and type checking
const post: BlogPost = await blogApi.getPostBySlug('some-slug');
const categories: BlogCategory[] = await blogApi.getCategories();
```

## 🎨 Styling Examples

### Basic CSS for Post Cards

```css
.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
  margin-top: 2rem;
}

.post-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.post-card:hover {
  transform: translateY(-4px);
}

.post-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.post-card h3 {
  padding: 1rem;
  margin: 0;
}
```

## 📚 Additional Resources

- **Swagger Documentation**: Visit `http://localhost:8080/swagger-ui.html` for interactive API documentation
- **API Testing**: Use Postman or curl to test endpoints before integrating
- **Backend Repository**: Check the backend code for detailed DTO structures

## 🆘 Troubleshooting

### Issue: "Network Error" or "Failed to fetch"
- **Solution**: Check if the backend is running and the URL is correct

### Issue: "CORS Error"
- **Solution**: The backend already allows CORS. Check your browser console for specific errors

### Issue: "404 Not Found"
- **Solution**: Verify the endpoint path and slug/ID are correct

### Issue: "500 Internal Server Error"
- **Solution**: Check backend logs for detailed error messages

## 📞 Support

For issues or questions:
1. Check the Swagger documentation at `/swagger-ui.html`
2. Review the backend logs
3. Verify your request payload matches the expected format

