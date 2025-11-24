package com.brideside.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class PageViewRequestDto {
    
    @NotBlank(message = "Page path is required")
    @JsonProperty("page_path")
    private String pagePath;
    
    @JsonProperty("page_type")
    private String pageType; // e.g., "blog_post", "deal", "home", "category", etc.
    
    @JsonProperty("entity_id")
    private Integer entityId; // ID of the related entity (e.g., blog post ID, deal ID)
    
    // Default constructor
    public PageViewRequestDto() {}
    
    // Constructor
    public PageViewRequestDto(String pagePath, String pageType, Integer entityId) {
        this.pagePath = pagePath;
        this.pageType = pageType;
        this.entityId = entityId;
    }
    
    // Getters and Setters
    public String getPagePath() {
        return pagePath;
    }
    
    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }
    
    public String getPageType() {
        return pageType;
    }
    
    public void setPageType(String pageType) {
        this.pageType = pageType;
    }
    
    public Integer getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }
}

