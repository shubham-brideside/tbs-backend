package com.brideside.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public class BlogPostRequestDto {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String slug;
    
    private String excerpt;
    
    private String content;
    
    @JsonProperty("featured_image_url")
    private String featuredImageUrl;

    @JsonProperty("author_name")
    @JsonAlias({"authorName"})
    private String authorName;
    
    @NotNull(message = "Category ID is required")
    @JsonProperty("category_id")
    private Integer categoryId;
    
    @JsonProperty("meta_description")
    private String metaDescription;
    
    @JsonProperty("meta_keywords")
    private String metaKeywords;
    
    @JsonProperty("is_published")
    private Boolean isPublished;
    
    @JsonProperty("related_links")
    private String relatedLinks;
    
    @JsonProperty("related_blogs_urls")
    @Valid
    private List<@URL String> relatedBlogsUrls;
    
    @JsonProperty("tagged_people")
    @Valid
    private List<@Valid TaggedPerson> taggedPeople;
    
    // Default constructor
    public BlogPostRequestDto() {}
    
    // Constructor
    public BlogPostRequestDto(String title, String slug, String excerpt, String content,
                             String featuredImageUrl, String authorName, Integer categoryId, String metaDescription,
                             String metaKeywords, Boolean isPublished, String relatedLinks) {
        this.title = title;
        this.slug = slug;
        this.excerpt = excerpt;
        this.content = content;
        this.featuredImageUrl = featuredImageUrl;
        this.authorName = authorName;
        this.categoryId = categoryId;
        this.metaDescription = metaDescription;
        this.metaKeywords = metaKeywords;
        this.isPublished = isPublished;
        this.relatedLinks = relatedLinks;
    }
    
    // Getters and Setters
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
    
    public String getFeaturedImageUrl() {
        return featuredImageUrl;
    }
    
    public void setFeaturedImageUrl(String featuredImageUrl) {
        this.featuredImageUrl = featuredImageUrl;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
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
    
    public String getRelatedLinks() {
        return relatedLinks;
    }
    
    public void setRelatedLinks(String relatedLinks) {
        this.relatedLinks = relatedLinks;
    }
    
    public List<String> getRelatedBlogsUrls() {
        return relatedBlogsUrls;
    }
    
    public void setRelatedBlogsUrls(List<String> relatedBlogsUrls) {
        this.relatedBlogsUrls = relatedBlogsUrls;
    }
    
    public List<TaggedPerson> getTaggedPeople() {
        return taggedPeople;
    }
    
    public void setTaggedPeople(List<TaggedPerson> taggedPeople) {
        this.taggedPeople = taggedPeople;
    }
    
    @Override
    public String toString() {
        return "BlogPostRequestDto{" +
                "title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", excerpt='" + excerpt + '\'' +
                ", authorName='" + authorName + '\'' +
                ", categoryId=" + categoryId +
                ", isPublished=" + isPublished +
                '}';
    }
}

