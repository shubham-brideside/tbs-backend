/**
 * Blog API Service - TypeScript/React Example
 * 
 * This service handles all API calls to the blog backend with TypeScript types
 * Replace BASE_URL with your backend URL
 */

const BASE_URL = 'http://localhost:8080/api/blog';

// ==================== TYPE DEFINITIONS ====================

export interface BlogCategory {
  id: number;
  name: string;
  slug: string;
  description: string;
  featuredImageUrl: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  posts?: BlogPostCard[];
}

export interface BlogPostCard {
  id: number;
  title: string;
  slug: string;
  featuredImageUrl: string;
}

export interface BlogPost {
  id: number;
  title: string;
  slug: string;
  excerpt: string;
  content: string;
  featuredImageUrl: string;
  category: {
    id: number;
    name: string;
    slug: string;
  };
  metaDescription?: string;
  metaKeywords?: string;
  isPublished: boolean;
  publishedAt?: string;
  viewCount: number;
  relatedLinks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CategoryRequest {
  name: string;
  slug?: string;
  description?: string;
  featuredImageUrl?: string;
  isActive?: boolean;
}

export interface PostRequest {
  title: string;
  slug?: string;
  excerpt?: string;
  content?: string;
  featuredImageUrl?: string;
  categoryId: number;
  metaDescription?: string;
  metaKeywords?: string;
  isPublished?: boolean;
  relatedLinks?: string;
}

// ==================== API REQUEST HANDLER ====================

interface ApiOptions extends RequestInit {
  headers?: HeadersInit;
}

async function apiRequest<T>(endpoint: string, options: ApiOptions = {}): Promise<T> {
  const url = `${BASE_URL}${endpoint}`;
  const config: RequestInit = {
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
      throw new Error((error as { error?: string }).error || `HTTP error! status: ${response.status}`);
    }
    
    return await response.json() as T;
  } catch (error) {
    console.error('API Request Error:', error);
    throw error;
  }
}

// ==================== CATEGORY API METHODS ====================

export async function getCategories(): Promise<BlogCategory[]> {
  return apiRequest<BlogCategory[]>('/categories');
}

export async function getCategoryBySlug(slug: string): Promise<BlogCategory> {
  return apiRequest<BlogCategory>(`/categories/slug/${slug}`);
}

export async function getCategoryById(id: number): Promise<BlogCategory> {
  return apiRequest<BlogCategory>(`/categories/${id}`);
}

export async function createCategory(categoryData: CategoryRequest): Promise<BlogCategory> {
  return apiRequest<BlogCategory>('/categories', {
    method: 'POST',
    body: JSON.stringify(categoryData),
  });
}

export async function updateCategory(id: number, categoryData: Partial<CategoryRequest>): Promise<BlogCategory> {
  return apiRequest<BlogCategory>(`/categories/${id}`, {
    method: 'PUT',
    body: JSON.stringify(categoryData),
  });
}

export async function deleteCategory(id: number): Promise<{ message: string }> {
  return apiRequest<{ message: string }>(`/categories/${id}`, {
    method: 'DELETE',
  });
}

// ==================== POST API METHODS ====================

export async function getPosts(): Promise<BlogPost[]> {
  return apiRequest<BlogPost[]>('/posts');
}

export async function getPostBySlug(slug: string): Promise<BlogPost> {
  return apiRequest<BlogPost>(`/posts/slug/${slug}`);
}

export async function getPostById(id: number): Promise<BlogPost> {
  return apiRequest<BlogPost>(`/posts/${id}`);
}

export async function getPostsByCategory(categorySlug: string): Promise<BlogPost[]> {
  return apiRequest<BlogPost[]>(`/posts/category/${categorySlug}`);
}

export async function createPost(postData: PostRequest): Promise<BlogPost> {
  return apiRequest<BlogPost>('/posts', {
    method: 'POST',
    body: JSON.stringify(postData),
  });
}

export async function updatePost(id: number, postData: Partial<PostRequest>): Promise<BlogPost> {
  return apiRequest<BlogPost>(`/posts/${id}`, {
    method: 'PUT',
    body: JSON.stringify(postData),
  });
}

export async function deletePost(id: number): Promise<{ message: string }> {
  return apiRequest<{ message: string }>(`/posts/${id}`, {
    method: 'DELETE',
  });
}

/**
 * Track a post view (increment view count)
 * Rate-limited to prevent duplicate increments from React StrictMode double-renders
 */
export async function trackPostView(slug: string): Promise<{ tracked: boolean; message: string }> {
  return apiRequest<{ tracked: boolean; message: string }>(`/posts/slug/${slug}/view`, {
    method: 'POST',
  });
}

// Export default object for convenience
const blogApi = {
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

export default blogApi;

