package com.brideside.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class BlogCategoryRequestDto {
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    private String slug;
    
    private String description;
    
    @JsonProperty("featured_image_url")
    private String featuredImageUrl;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    // Default constructor
    public BlogCategoryRequestDto() {}
    
    // Constructor
    public BlogCategoryRequestDto(String name, String slug, String description, String featuredImageUrl, Boolean isActive) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.featuredImageUrl = featuredImageUrl;
        this.isActive = isActive;
    }
    
    // Getters and Setters
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
    
    @Override
    public String toString() {
        return "BlogCategoryRequestDto{" +
                "name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", description='" + description + '\'' +
                ", featuredImageUrl='" + featuredImageUrl + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}

