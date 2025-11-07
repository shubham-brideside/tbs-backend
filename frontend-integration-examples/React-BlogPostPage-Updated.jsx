/**
 * React Component Example - Blog Post Page (Updated with View Tracking)
 * 
 * This component displays a single blog post and properly tracks views
 * 
 * Usage:
 * <BlogPostPage postSlug="100-latest-haldi-decoration-ideas" />
 */

import React, { useState, useEffect, useRef } from 'react';
import blogApi from './blog-api-service'; // Adjust import path as needed

function BlogPostPage({ postSlug }) {
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const viewTrackedRef = useRef(false); // Prevent duplicate view tracking

  useEffect(() => {
    let mounted = true;
    
    async function fetchPost() {
      try {
        setLoading(true);
        
        // Load post data (doesn't increment views)
        const postData = await blogApi.getPostBySlug(postSlug);
        
        if (mounted) {
          setPost(postData);
          setError(null);
          setLoading(false);
          
          // Track view separately (rate-limited, safe from React StrictMode double-renders)
          // Only track once per component mount
          if (!viewTrackedRef.current) {
            viewTrackedRef.current = true;
            blogApi.trackPostView(postSlug).catch(err => {
              // Silent fail - view tracking is not critical for UX
              console.debug('View tracking failed:', err);
            });
          }
        }
      } catch (err) {
        if (mounted) {
          setError(err.message);
          console.error('Error fetching post:', err);
          setLoading(false);
        }
      }
    }

    if (postSlug) {
      fetchPost();
    }
    
    return () => {
      mounted = false;
    };
  }, [postSlug]);

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  if (error) {
    return <div className="error">Error: {error}</div>;
  }

  if (!post) {
    return <div>Post not found</div>;
  }

  // Parse related links if they're stored as JSON
  let relatedLinks = [];
  try {
    if (post.relatedLinks) {
      relatedLinks = JSON.parse(post.relatedLinks);
    }
  } catch (e) {
    // If not JSON, treat as plain text
    relatedLinks = post.relatedLinks ? [post.relatedLinks] : [];
  }

  return (
    <div className="blog-post-page">
      {/* Breadcrumb Navigation */}
      <nav className="breadcrumb">
        <a href="/">The Wedding Company</a> / 
        <a href={`/blog/category/${post.category.slug}`}>{post.category.name}</a> / 
        <span>{post.title}</span>
      </nav>

      {/* Main Title */}
      <h1 className="post-title">{post.title}</h1>

      {/* Category Tag */}
      <div className="category-tag">
        <span className="tag">{post.category.name.toUpperCase()}</span>
      </div>

      {/* Social Share Icons */}
      <div className="social-share">
        <button className="share-icon" aria-label="Share on Twitter">X</button>
        <button className="share-icon" aria-label="Share on Facebook">FB</button>
        <button className="share-icon" aria-label="Share on Instagram">IG</button>
        <button className="share-icon" aria-label="Copy link">🔗</button>
      </div>

      {/* Excerpt/Introduction */}
      {post.excerpt && (
        <div className="post-excerpt">
          <p>{post.excerpt}</p>
        </div>
      )}

      {/* Featured Image */}
      {post.featured_image_url && (
        <div className="featured-image">
          <img 
            src={post.featured_image_url} 
            alt={post.title}
          />
        </div>
      )}

      {/* Post Content */}
      <div 
        className="post-content"
        dangerouslySetInnerHTML={{ __html: post.content }}
      />

      {/* View Count */}
      <div className="post-meta">
        <span className="view-count">{post.viewCount} views</span>
        {post.publishedAt && (
          <span className="published-date">
            Published: {new Date(post.publishedAt).toLocaleDateString()}
          </span>
        )}
      </div>

      {/* Related Links */}
      {relatedLinks.length > 0 && (
        <div className="related-links">
          <h3>Check also:</h3>
          <ul>
            {relatedLinks.map((link, index) => (
              <li key={index}>
                <a href={typeof link === 'string' ? link : link.url}>
                  {typeof link === 'string' ? link : link.text}
                </a>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

export default BlogPostPage;

