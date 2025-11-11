package com.brideside.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BlogPostResponseDto {
    
    private Integer id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    
    @JsonProperty("author_name")
    private String authorName;
    
    @JsonProperty("featured_image_url")
    private String featuredImageUrl;
    
    private CategoryDto category;
    
    @JsonProperty("meta_description")
    private String metaDescription;
    
    @JsonProperty("meta_keywords")
    private String metaKeywords;
    
    @JsonProperty("is_published")
    private Boolean isPublished;
    
    @JsonProperty("published_at")
    private String publishedAt;
    
    @JsonProperty("view_count")
    private Integer viewCount;
    
    @JsonProperty("related_links")
    private String relatedLinks;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    // Default constructor
    public BlogPostResponseDto() {}
    
    // Constructor
    public BlogPostResponseDto(Integer id, String title, String slug, String excerpt, String content,
                              String authorName, String featuredImageUrl, CategoryDto category, String metaDescription,
                              String metaKeywords, Boolean isPublished, String publishedAt,
                              Integer viewCount, String relatedLinks, String createdAt, String updatedAt) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.excerpt = excerpt;
        this.content = content;
        this.authorName = authorName;
        this.featuredImageUrl = featuredImageUrl;
        this.category = category;
        this.metaDescription = metaDescription;
        this.metaKeywords = metaKeywords;
        this.isPublished = isPublished;
        this.publishedAt = publishedAt;
        this.viewCount = viewCount;
        this.relatedLinks = relatedLinks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
    
    public String getExcerpt() {
        return excerpt;
    }
    
    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public String getFeaturedImageUrl() {
        return featuredImageUrl;
    }
    
    public void setFeaturedImageUrl(String featuredImageUrl) {
        this.featuredImageUrl = featuredImageUrl;
    }
    
    public CategoryDto getCategory() {
        return category;
    }
    
    public void setCategory(CategoryDto category) {
        this.category = category;
    }
    
    public String getMetaDescription() {
        return metaDescription;
    }
    
    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }
    
    public String getMetaKeywords() {
        return metaKeywords;
    }
    
    public void setMetaKeywords(String metaKeywords) {
        this.metaKeywords = metaKeywords;
    }
    
    public Boolean getIsPublished() {
        return isPublished;
    }
    
    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }
    
    public String getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    public String getRelatedLinks() {
        return relatedLinks;
    }
    
    public void setRelatedLinks(String relatedLinks) {
        this.relatedLinks = relatedLinks;
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
    
    // Inner class for category info
    public static class CategoryDto {
        private Integer id;
        private String name;
        private String slug;
        
        // Default constructor
        public CategoryDto() {}
        
        // Constructor
        public CategoryDto(Integer id, String name, String slug) {
            this.id = id;
            this.name = name;
            this.slug = slug;
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
    }
}

