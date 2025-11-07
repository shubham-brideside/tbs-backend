package com.brideside.backend.controller;

import com.brideside.backend.dto.*;
import com.brideside.backend.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blog")
@Tag(name = "Blog Management", description = "APIs for managing blog categories and posts")
public class BlogController {
    
    @Autowired
    private BlogService blogService;
    
    // ==================== Category Endpoints ====================
    
    /**
     * Create a new blog category
     */
    @Operation(summary = "Create blog category", description = "Create a new blog category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@Valid @RequestBody BlogCategoryRequestDto request) {
        try {
            BlogCategoryResponseDto response = blogService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error creating category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get all active categories
     */
    @Operation(summary = "Get all active categories", description = "Retrieve all active blog categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved categories"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/categories")
    public ResponseEntity<List<BlogCategoryResponseDto>> getAllActiveCategories() {
        try {
            List<BlogCategoryResponseDto> categories = blogService.getAllActiveCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all categories (including inactive) - for admin
     */
    @Operation(summary = "Get all categories", description = "Retrieve all blog categories including inactive ones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved categories"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/categories/all")
    public ResponseEntity<List<BlogCategoryResponseDto>> getAllCategories() {
        try {
            List<BlogCategoryResponseDto> categories = blogService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get category by slug with posts
     */
    @Operation(summary = "Get category by slug", description = "Retrieve a blog category by slug with its published posts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved category"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/categories/slug/{slug}")
    public ResponseEntity<BlogCategoryResponseDto> getCategoryBySlug(@PathVariable String slug) {
        try {
            BlogCategoryResponseDto category = blogService.getCategoryBySlug(slug);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get category by ID
     */
    @Operation(summary = "Get category by ID", description = "Retrieve a blog category by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved category"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/categories/{id}")
    public ResponseEntity<BlogCategoryResponseDto> getCategoryById(@PathVariable Integer id) {
        try {
            BlogCategoryResponseDto category = blogService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update category
     */
    @Operation(summary = "Update category", description = "Update an existing blog category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, 
                                          @Valid @RequestBody BlogCategoryRequestDto request) {
        try {
            BlogCategoryResponseDto response = blogService.updateCategory(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error updating category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Delete category
     */
    @Operation(summary = "Delete category", description = "Delete a blog category (only if it has no posts)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "400", description = "Category has existing posts"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable Integer id) {
        try {
            blogService.deleteCategory(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Category deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            if (e.getMessage().contains("not found")) {
                errorResponse.put("error", "Category not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            } else if (e.getMessage().contains("existing posts")) {
                errorResponse.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error deleting category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // ==================== Post Endpoints ====================
    
    /**
     * Create a new blog post
     */
    @Operation(summary = "Create blog post", description = "Create a new blog post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@Valid @RequestBody BlogPostRequestDto request) {
        try {
            BlogPostResponseDto response = blogService.createPost(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error creating post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get all published posts
     */
    @Operation(summary = "Get all published posts", description = "Retrieve all published blog posts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved posts"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/posts")
    public ResponseEntity<List<BlogPostResponseDto>> getAllPublishedPosts() {
        try {
            List<BlogPostResponseDto> posts = blogService.getAllPublishedPosts();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all posts (including unpublished) - for admin
     */
    @Operation(summary = "Get all posts", description = "Retrieve all blog posts including unpublished ones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved posts"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/posts/all")
    public ResponseEntity<List<BlogPostResponseDto>> getAllPosts() {
        try {
            List<BlogPostResponseDto> posts = blogService.getAllPosts();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get post by slug
     */
    @Operation(summary = "Get post by slug", description = "Retrieve a published blog post by slug")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved post"),
            @ApiResponse(responseCode = "404", description = "Post not found or not published"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/posts/slug/{slug}")
    public ResponseEntity<BlogPostResponseDto> getPostBySlug(@PathVariable String slug) {
        try {
            BlogPostResponseDto post = blogService.getPostBySlug(slug);
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found") || e.getMessage().contains("not published")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Track a post view (increment view count)
     * This endpoint should be called separately to track views
     * Includes rate limiting to prevent duplicate increments
     */
    @Operation(summary = "Track post view", description = "Increment view count for a blog post (rate-limited to prevent duplicates)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "View tracked successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/posts/slug/{slug}/view")
    public ResponseEntity<Map<String, Object>> trackPostView(@PathVariable String slug) {
        try {
            boolean tracked = blogService.trackPostView(slug);
            Map<String, Object> response = new HashMap<>();
            response.put("tracked", tracked);
            response.put("message", tracked ? "View tracked" : "View rate-limited (already tracked recently)");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>(errorResponse));
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error tracking view: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>(errorResponse));
        }
    }
    
    /**
     * Get post by ID (for admin, includes unpublished)
     */
    @Operation(summary = "Get post by ID", description = "Retrieve a blog post by its ID (includes unpublished posts)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved post"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/posts/{id}")
    public ResponseEntity<BlogPostResponseDto> getPostById(@PathVariable Integer id) {
        try {
            BlogPostResponseDto post = blogService.getPostById(id);
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get posts by category slug
     */
    @Operation(summary = "Get posts by category slug", description = "Retrieve all published posts for a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved posts"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/posts/category/{categorySlug}")
    public ResponseEntity<List<BlogPostResponseDto>> getPostsByCategorySlug(@PathVariable String categorySlug) {
        try {
            List<BlogPostResponseDto> posts = blogService.getPostsByCategorySlug(categorySlug);
            return ResponseEntity.ok(posts);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update post
     */
    @Operation(summary = "Update post", description = "Update an existing blog post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/posts/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Integer id, 
                                        @Valid @RequestBody BlogPostRequestDto request) {
        try {
            BlogPostResponseDto response = blogService.updatePost(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error updating post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Delete post
     */
    @Operation(summary = "Delete post", description = "Delete a blog post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable Integer id) {
        try {
            blogService.deletePost(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Post deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            if (e.getMessage().contains("not found")) {
                errorResponse.put("error", "Post not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error deleting post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

