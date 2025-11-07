/**
 * Blog API Service - JavaScript/React Example
 * 
 * This service handles all API calls to the blog backend
 * Replace BASE_URL with your backend URL (e.g., 'http://localhost:8080' or your production URL)
 */

const BASE_URL = 'http://localhost:8080/api/blog';

/**
 * Generic API request handler
 */
async function apiRequest(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  };

  try {
    const response = await fetch(url, config);
    
    if (!response.ok) {
      const error = await response.json().catch(() => ({ error: response.statusText }));
      throw new Error(error.error || `HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('API Request Error:', error);
    throw error;
  }
}

/**
 * ==================== CATEGORY API METHODS ====================
 */

// Get all active categories
export async function getCategories() {
  return apiRequest('/categories');
}

// Get category by slug with posts
export async function getCategoryBySlug(slug) {
  return apiRequest(`/categories/slug/${slug}`);
}

// Get category by ID
export async function getCategoryById(id) {
  return apiRequest(`/categories/${id}`);
}

// Create category (admin only)
export async function createCategory(categoryData) {
  return apiRequest('/categories', {
    method: 'POST',
    body: JSON.stringify(categoryData),
  });
}

// Update category (admin only)
export async function updateCategory(id, categoryData) {
  return apiRequest(`/categories/${id}`, {
    method: 'PUT',
    body: JSON.stringify(categoryData),
  });
}

// Delete category (admin only)
export async function deleteCategory(id) {
  return apiRequest(`/categories/${id}`, {
    method: 'DELETE',
  });
}

/**
 * ==================== POST API METHODS ====================
 */

// Get all published posts
export async function getPosts() {
  return apiRequest('/posts');
}

// Get post by slug (increments view count)
export async function getPostBySlug(slug) {
  return apiRequest(`/posts/slug/${slug}`);
}

// Get post by ID
export async function getPostById(id) {
  return apiRequest(`/posts/${id}`);
}

// Get posts by category slug
export async function getPostsByCategory(categorySlug) {
  return apiRequest(`/posts/category/${categorySlug}`);
}

// Create post (admin only)
export async function createPost(postData) {
  return apiRequest('/posts', {
    method: 'POST',
    body: JSON.stringify(postData),
  });
}

// Update post (admin only)
export async function updatePost(id, postData) {
  return apiRequest(`/posts/${id}`, {
    method: 'PUT',
    body: JSON.stringify(postData),
  });
}

// Delete post (admin only)
export async function deletePost(id) {
  return apiRequest(`/posts/${id}`, {
    method: 'DELETE',
  });
}

// Track a post view (increment view count)
// Rate-limited to prevent duplicate increments
export async function trackPostView(slug) {
  return apiRequest(`/posts/slug/${slug}/view`, {
    method: 'POST',
  });
}

// Export default object for convenience
export default {
  // Categories
  getCategories,
  getCategoryBySlug,
  getCategoryById,
  createCategory,
  updateCategory,
  deleteCategory,
  
  // Posts
  getPosts,
  getPostBySlug,
  getPostById,
  getPostsByCategory,
  createPost,
  updatePost,
  deletePost,
  trackPostView,
};

