/**
 * React Component Example - Blog Category Page
 * 
 * This component displays a category page with its posts (similar to the first image you provided)
 * 
 * Usage:
 * <BlogCategoryPage categorySlug="wedding" />
 */

import React, { useState, useEffect } from 'react';
import blogApi from './blog-api-service'; // Adjust import path as needed

function BlogCategoryPage({ categorySlug }) {
  const [category, setCategory] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function fetchCategory() {
      try {
        setLoading(true);
        const data = await blogApi.getCategoryBySlug(categorySlug);
        setCategory(data);
        setError(null);
      } catch (err) {
        setError(err.message);
        console.error('Error fetching category:', err);
      } finally {
        setLoading(false);
      }
    }

    if (categorySlug) {
      fetchCategory();
    }
  }, [categorySlug]);

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  if (error) {
    return <div className="error">Error: {error}</div>;
  }

  if (!category) {
    return <div>Category not found</div>;
  }

  return (
    <div className="blog-category-page">
      {/* Breadcrumb Navigation */}
      <nav className="breadcrumb">
        <a href="/">The Wedding Company</a> / <span>{category.name}</span>
      </nav>

      {/* Main Content Area */}
      <div className="category-header">
        <div className="category-content">
          <h1 className="category-title">{category.name}</h1>
          <p className="category-description">{category.description}</p>
        </div>
        
        {category.featuredImageUrl && (
          <div className="category-image">
            <img 
              src={category.featuredImageUrl} 
              alt={category.name}
              className="featured-image"
            />
          </div>
        )}
      </div>

      {/* Article Cards Grid */}
      <div className="posts-grid">
        {category.posts && category.posts.length > 0 ? (
          category.posts.map((post) => (
            <a 
              key={post.id} 
              href={`/blog/${post.slug}`}
              className="post-card"
            >
              {post.featuredImageUrl && (
                <div className="post-image">
                  <img 
                    src={post.featuredImageUrl} 
                    alt={post.title}
                  />
                </div>
              )}
              <div className="post-content">
                <h3 className="post-title">{post.title}</h3>
              </div>
            </a>
          ))
        ) : (
          <p>No posts found in this category.</p>
        )}
      </div>
    </div>
  );
}

export default BlogCategoryPage;

