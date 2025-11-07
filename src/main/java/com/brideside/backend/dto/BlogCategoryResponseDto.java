package com.brideside.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BlogCategoryResponseDto {
    
    private Integer id;
    private String name;
    private String slug;
    private String description;
    
    @JsonProperty("featured_image_url")
    private String featuredImageUrl;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    private List<BlogPostCardDto> posts;
    
    // Default constructor
    public BlogCategoryResponseDto() {}
    
    // Constructor
    public BlogCategoryResponseDto(Integer id, String name, String slug, String description, 
                                  String featuredImageUrl, Boolean isActive, 
                                  String createdAt, String updatedAt, List<BlogPostCardDto> posts) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.featuredImageUrl = featuredImageUrl;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.posts = posts;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getFeaturedImageUrl() {
        return featuredImageUrl;
    }
    
    public void setFeaturedImageUrl(String featuredImageUrl) {
        this.featuredImageUrl = featuredImageUrl;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<BlogPostCardDto> getPosts() {
        return posts;
    }
    
    public void setPosts(List<BlogPostCardDto> posts) {
        this.posts = posts;
    }
    
    // Inner class for blog post card (used in category page)
    public static class BlogPostCardDto {
        private Integer id;
        private String title;
        private String slug;
        
        @JsonProperty("featured_image_url")
        private String featuredImageUrl;
        
        // Default constructor
        public BlogPostCardDto() {}
        
        // Constructor
        public BlogPostCardDto(Integer id, String title, String slug, String featuredImageUrl) {
            this.id = id;
            this.title = title;
            this.slug = slug;
            this.featuredImageUrl = featuredImageUrl;
        }
        
        // Getters and Setters
        public Integer getId() {
            return id;
        }
        
        public void setId(Integer id) {
            this.id = id;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getSlug() {
            return slug;
        }
        
        public void setSlug(String slug) {
            this.slug = slug;
        }
        
        public String getFeaturedImageUrl() {
            return featuredImageUrl;
        }
        
        public void setFeaturedImageUrl(String featuredImageUrl) {
            this.featuredImageUrl = featuredImageUrl;
        }
    }
}

