package com.brideside.backend.service;

import com.brideside.backend.dto.*;
import com.brideside.backend.entity.BlogCategory;
import com.brideside.backend.entity.BlogPost;
import com.brideside.backend.repository.BlogCategoryRepository;
import com.brideside.backend.repository.BlogPostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class BlogService {
    
    private static final Logger logger = LoggerFactory.getLogger(BlogService.class);
    
    @Autowired
    private BlogCategoryRepository categoryRepository;
    
    @Autowired
    private BlogPostRepository postRepository;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // In-memory cache to prevent duplicate view increments within 5 seconds
    // Key: postId, Value: timestamp of last increment
    private final Map<Integer, Long> viewIncrementCache = new ConcurrentHashMap<>();
    private static final long VIEW_INCREMENT_COOLDOWN_MS = 5000; // 5 seconds
    
    // ==================== Category Methods ====================
    
    /**
     * Create a new blog category
     */
    @CacheEvict(value = {"blogCategories", "blogPosts"}, allEntries = true)
    public BlogCategoryResponseDto createCategory(BlogCategoryRequestDto request) {
        // Generate slug if not provided
        String slug = request.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlug(request.getName());
        }
        
        // Check if slug already exists
        if (categoryRepository.findBySlug(slug).isPresent()) {
            throw new RuntimeException("Category with slug '" + slug + "' already exists");
        }
        
        BlogCategory category = new BlogCategory(
            request.getName(),
            slug,
            request.getDescription(),
            request.getFeaturedImageUrl()
        );
        
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        
        BlogCategory savedCategory = categoryRepository.save(category);
        logger.info("Created blog category: {}", savedCategory.getName());
        
        return convertToCategoryResponseDto(savedCategory, null);
    }
    
    /**
     * Get all active categories
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "blogCategories", key = "'all_active'")
    public List<BlogCategoryResponseDto> getAllActiveCategories() {
        try {
            logger.info("getAllActiveCategories() - Starting to fetch active categories from database");
            List<BlogCategory> categories = categoryRepository.findByIsActiveTrue();
            logger.info("getAllActiveCategories() - Found {} active categories in database", categories.size());
            
            if (categories.isEmpty()) {
                logger.warn("getAllActiveCategories() - No active categories found in database");
                return List.of();
            }
            
            logger.debug("getAllActiveCategories() - Converting {} categories to DTOs", categories.size());
            List<BlogCategoryResponseDto> result = categories.stream()
                .map(category -> {
                    try {
                        logger.debug("getAllActiveCategories() - Converting category ID: {}, Name: {}", 
                                    category.getId(), category.getName());
                        BlogCategoryResponseDto dto = convertToCategoryResponseDto(category, null);
                        logger.debug("getAllActiveCategories() - Successfully converted category ID: {}", category.getId());
                        return dto;
                    } catch (Exception e) {
                        logger.error("getAllActiveCategories() - Error converting category ID: {}, Name: {} to DTO", 
                                    category.getId(), category.getName(), e);
                        throw e;
                    }
                })
                .collect(Collectors.toList());
            
            logger.info("getAllActiveCategories() - Successfully converted {} categories to DTOs", result.size());
            return result;
        } catch (Exception e) {
            logger.error("getAllActiveCategories() - Error fetching active categories", e);
            logger.error("getAllActiveCategories() - Exception type: {}, Message: {}", 
                        e.getClass().getName(), e.getMessage());
            if (e.getCause() != null) {
                logger.error("getAllActiveCategories() - Cause: {}, Cause message: {}", 
                            e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            throw e;
        }
    }
    
    /**
     * Get all categories (including inactive)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "blogCategories", key = "'all'")
    public List<BlogCategoryResponseDto> getAllCategories() {
        List<BlogCategory> categories = categoryRepository.findAll();
        return categories.stream()
            .map(category -> convertToCategoryResponseDto(category, null))
            .collect(Collectors.toList());
    }
    
    /**
     * Get category by slug with posts
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "blogCategories", key = "'slug_' + #slug")
    public BlogCategoryResponseDto getCategoryBySlug(String slug) {
        BlogCategory category = categoryRepository.findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Category not found with slug: " + slug));
        
        // Get published posts for this category
        List<BlogPost> posts = postRepository.findByCategoryAndIsPublishedTrueOrderByPublishedAtDesc(category);
        List<BlogCategoryResponseDto.BlogPostCardDto> postCards = posts.stream()
            .map(this::convertToPostCardDto)
            .collect(Collectors.toList());
        
        return convertToCategoryResponseDto(category, postCards);
    }
    
    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "blogCategories", key = "'id_' + #id")
    public BlogCategoryResponseDto getCategoryById(Integer id) {
        BlogCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        return convertToCategoryResponseDto(category, null);
    }
    
    /**
     * Update category
     */
    @CacheEvict(value = {"blogCategories", "blogPosts"}, allEntries = true)
    public BlogCategoryResponseDto updateCategory(Integer id, BlogCategoryRequestDto request) {
        BlogCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        
        if (request.getSlug() != null) {
            // Check if new slug already exists
            categoryRepository.findBySlug(request.getSlug())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException("Category with slug '" + request.getSlug() + "' already exists");
                    }
                });
            category.setSlug(request.getSlug());
        }
        
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        
        if (request.getFeaturedImageUrl() != null) {
            category.setFeaturedImageUrl(request.getFeaturedImageUrl());
        }
        
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        
        BlogCategory updatedCategory = categoryRepository.save(category);
        logger.info("Updated blog category: {}", updatedCategory.getName());
        
        return convertToCategoryResponseDto(updatedCategory, null);
    }
    
    /**
     * Delete category
     */
    @CacheEvict(value = {"blogCategories", "blogPosts"}, allEntries = true)
    public void deleteCategory(Integer id) {
        BlogCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        // Check if category has posts
        List<BlogPost> posts = postRepository.findByCategory(category);
        if (!posts.isEmpty()) {
            throw new RuntimeException("Cannot delete category with existing posts. Please delete or move posts first.");
        }
        
        categoryRepository.delete(category);
        logger.info("Deleted blog category: {}", category.getName());
    }
    
    // ==================== Post Methods ====================
    
    /**
     * Create a new blog post
     */
    @CacheEvict(value = {"blogCategories", "blogPosts"}, allEntries = true)
    public BlogPostResponseDto createPost(BlogPostRequestDto request) {
        BlogCategory category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
        
        // Generate slug if not provided
        String slug = request.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlug(request.getTitle());
        }
        
        // Check if slug already exists
        if (postRepository.findBySlug(slug).isPresent()) {
            throw new RuntimeException("Post with slug '" + slug + "' already exists");
        }
        
        BlogPost post = new BlogPost(
            request.getTitle(),
            slug,
            request.getExcerpt(),
            request.getContent(),
            request.getFeaturedImageUrl(),
            category
        );
        
        post.setAuthorName(request.getAuthorName());
        post.setMetaDescription(request.getMetaDescription());
        post.setMetaKeywords(request.getMetaKeywords());
        post.setRelatedLinks(request.getRelatedLinks());
        
        if (request.getIsPublished() != null && request.getIsPublished()) {
            post.setIsPublished(true);
            post.setPublishedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        }
        
        BlogPost savedPost = postRepository.save(post);
        logger.info("Created blog post: {}", savedPost.getTitle());
        
        return convertToPostResponseDto(savedPost);
    }
    
    /**
     * Get all published posts
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "blogPosts", key = "'all_published'")
    public List<BlogPostResponseDto> getAllPublishedPosts() {
        try {
            logger.info("getAllPublishedPosts() - Starting to fetch published posts from database");
            List<BlogPost> posts = postRepository.findByIsPublishedTrueOrderByPublishedAtDesc();
            logger.info("getAllPublishedPosts() - Found {} published posts in database", posts.size());
            
            if (posts.isEmpty()) {
                logger.warn("getAllPublishedPosts() - No published posts found in database");
                return List.of();
            }
            
            logger.debug("getAllPublishedPosts() - Converting {} posts to DTOs", posts.size());
            List<BlogPostResponseDto> result = posts.stream()
                .map(post -> {
                    try {
                        logger.debug("getAllPublishedPosts() - Converting post ID: {}, Title: {}", 
                                    post.getId(), post.getTitle());
                        BlogPostResponseDto dto = convertToPostResponseDto(post);
                        logger.debug("getAllPublishedPosts() - Successfully converted post ID: {}", post.getId());
                        return dto;
                    } catch (Exception e) {
                        logger.error("getAllPublishedPosts() - Error converting post ID: {}, Title: {} to DTO", 
                                    post.getId(), post.getTitle(), e);
                        throw e;
                    }
                })
                .collect(Collectors.toList());
            
            logger.info("getAllPublishedPosts() - Successfully converted {} posts to DTOs", result.size());
            return result;
        } catch (Exception e) {
            logger.error("getAllPublishedPosts() - Error fetching published posts", e);
            logger.error("getAllPublishedPosts() - Exception type: {}, Message: {}", 
                        e.getClass().getName(), e.getMessage());
            if (e.getCause() != null) {
                logger.error("getAllPublishedPosts() - Cause: {}, Cause message: {}", 
                            e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            throw e;
        }
    }
    
    /**
     * Get all posts (including unpublished)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "blogPosts", key = "'all'")
    public List<BlogPostResponseDto> getAllPosts() {
        List<BlogPost> posts = postRepository.findAll();
        return posts.stream()
            .map(this::convertToPostResponseDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Get post by slug
     * Note: View count is not incremented automatically to prevent double-counting
     * Use trackPostView() method separately to track views
     */
    @Transactional(readOnly = true)
    public BlogPostResponseDto getPostBySlug(String slug) {
        BlogPost post = postRepository.findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Post not found with slug: " + slug));
        
        // Only return published posts for public access
        if (!post.getIsPublished()) {
            throw new RuntimeException("Post not found or not published");
        }
        
        return convertToPostResponseDto(post);
    }
    
    /**
     * Track a post view (increment view count)
     * Includes rate limiting to prevent duplicate increments from React StrictMode double-renders
     * @param slug the post slug
     * @return true if view was tracked, false if it was rate-limited
     */
    @Transactional
    @CacheEvict(value = {"blogCategories", "blogPosts"}, allEntries = true)
    public boolean trackPostView(String slug) {
        BlogPost post = postRepository.findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Post not found with slug: " + slug));
        
        // Only track views for published posts
        if (!post.getIsPublished()) {
            return false;
        }
        
        Integer postId = post.getId();
        long currentTime = System.currentTimeMillis();
        
        // Check if we've incremented this post recently (within cooldown period)
        Long lastIncrementTime = viewIncrementCache.get(postId);
        if (lastIncrementTime != null && (currentTime - lastIncrementTime) < VIEW_INCREMENT_COOLDOWN_MS) {
            logger.debug("View increment rate-limited for post {} (last increment was {}ms ago)", 
                        postId, currentTime - lastIncrementTime);
            return false; // Rate-limited, don't increment
        }
        
        // Increment view count
        postRepository.incrementViewCount(postId);
        
        // Update cache with current timestamp
        viewIncrementCache.put(postId, currentTime);
        
        // Clean up old cache entries (older than cooldown period) to prevent memory leak
        viewIncrementCache.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > VIEW_INCREMENT_COOLDOWN_MS);
        
        logger.info("View count incremented for post {} (slug: {})", postId, slug);
        return true;
    }
    
    /**
     * Get post by ID (for admin, includes unpublished)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "blogPosts", key = "'id_' + #id")
    public BlogPostResponseDto getPostById(Integer id) {
        BlogPost post = postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        
        return convertToPostResponseDto(post);
    }
    
    /**
     * Get posts by category slug
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "blogPosts", key = "'category_' + #categorySlug")
    public List<BlogPostResponseDto> getPostsByCategorySlug(String categorySlug) {
        BlogCategory category = categoryRepository.findBySlug(categorySlug)
            .orElseThrow(() -> new RuntimeException("Category not found with slug: " + categorySlug));
        
        List<BlogPost> posts = postRepository.findByCategoryAndIsPublishedTrueOrderByPublishedAtDesc(category);
        return posts.stream()
            .map(this::convertToPostResponseDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Update post
     */
    @CacheEvict(value = {"blogCategories", "blogPosts"}, allEntries = true)
    public BlogPostResponseDto updatePost(Integer id, BlogPostRequestDto request) {
        BlogPost post = postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        
        if (request.getSlug() != null) {
            // Check if new slug already exists
            postRepository.findBySlug(request.getSlug())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException("Post with slug '" + request.getSlug() + "' already exists");
                    }
                });
            post.setSlug(request.getSlug());
        }
        
        if (request.getExcerpt() != null) {
            post.setExcerpt(request.getExcerpt());
        }
        
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        
        if (request.getFeaturedImageUrl() != null) {
            post.setFeaturedImageUrl(request.getFeaturedImageUrl());
        }

        if (request.getAuthorName() != null) {
            post.setAuthorName(request.getAuthorName());
        }
        
        if (request.getCategoryId() != null) {
            BlogCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
            post.setCategory(category);
        }
        
        if (request.getMetaDescription() != null) {
            post.setMetaDescription(request.getMetaDescription());
        }
        
        if (request.getMetaKeywords() != null) {
            post.setMetaKeywords(request.getMetaKeywords());
        }
        
        if (request.getRelatedLinks() != null) {
            post.setRelatedLinks(request.getRelatedLinks());
        }
        
        // Handle publishing
        if (request.getIsPublished() != null) {
            boolean wasPublished = post.getIsPublished();
            post.setIsPublished(request.getIsPublished());
            
            if (request.getIsPublished() && !wasPublished) {
                // Post is being published for the first time
                post.setPublishedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            } else if (!request.getIsPublished() && wasPublished) {
                // Post is being unpublished
                post.setPublishedAt(null);
            }
        }
        
        BlogPost updatedPost = postRepository.save(post);
        logger.info("Updated blog post: {}", updatedPost.getTitle());
        
        return convertToPostResponseDto(updatedPost);
    }
    
    /**
     * Delete post
     */
    @CacheEvict(value = {"blogCategories", "blogPosts"}, allEntries = true)
    public void deletePost(Integer id) {
        BlogPost post = postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        
        postRepository.delete(post);
        logger.info("Deleted blog post: {}", post.getTitle());
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Generate slug from title/name
     */
    private String generateSlug(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("Cannot generate slug from empty text");
        }
        
        return text.toLowerCase()
            .trim()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }
    
    /**
     * Convert BlogCategory to BlogCategoryResponseDto
     */
    private BlogCategoryResponseDto convertToCategoryResponseDto(BlogCategory category, 
                                                                List<BlogCategoryResponseDto.BlogPostCardDto> posts) {
        return new BlogCategoryResponseDto(
            category.getId(),
            category.getName(),
            category.getSlug(),
            category.getDescription(),
            category.getFeaturedImageUrl(),
            category.getIsActive(),
            category.getCreatedAt() != null ? category.getCreatedAt().format(DATETIME_FORMATTER) : null,
            category.getUpdatedAt() != null ? category.getUpdatedAt().format(DATETIME_FORMATTER) : null,
            posts
        );
    }
    
    /**
     * Convert BlogPost to BlogPostCardDto
     */
    private BlogCategoryResponseDto.BlogPostCardDto convertToPostCardDto(BlogPost post) {
        return new BlogCategoryResponseDto.BlogPostCardDto(
            post.getId(),
            post.getTitle(),
            post.getSlug(),
            post.getFeaturedImageUrl()
        );
    }
    
    /**
     * Convert BlogPost to BlogPostResponseDto
     */
    private BlogPostResponseDto convertToPostResponseDto(BlogPost post) {
        BlogPostResponseDto.CategoryDto categoryDto = null;
        if (post.getCategory() != null) {
            categoryDto = new BlogPostResponseDto.CategoryDto(
                post.getCategory().getId(),
                post.getCategory().getName(),
                post.getCategory().getSlug()
            );
        }
        
        return new BlogPostResponseDto(
            post.getId(),
            post.getTitle(),
            post.getSlug(),
            post.getExcerpt(),
            post.getContent(),
            post.getAuthorName(),
            post.getFeaturedImageUrl(),
            categoryDto,
            post.getMetaDescription(),
            post.getMetaKeywords(),
            post.getIsPublished(),
            post.getPublishedAt() != null ? post.getPublishedAt().format(DATETIME_FORMATTER) : null,
            post.getViewCount(),
            post.getRelatedLinks(),
            post.getCreatedAt() != null ? post.getCreatedAt().format(DATETIME_FORMATTER) : null,
            post.getUpdatedAt() != null ? post.getUpdatedAt().format(DATETIME_FORMATTER) : null
        );
    }
}

