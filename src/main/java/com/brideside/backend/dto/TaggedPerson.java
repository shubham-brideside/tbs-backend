package com.brideside.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * DTO for tagged people in blog posts.
 * Represents a person tagged with their name and Instagram profile URL.
 */
public class TaggedPerson {
    
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    @JsonProperty("name")
    private String name;
    
    @NotBlank(message = "Instagram URL is required")
    @URL(message = "Instagram URL must be a valid URL")
    @JsonProperty("instagram_url")
    private String instagramUrl;
    
    // Default constructor
    public TaggedPerson() {}
    
    // Constructor
    public TaggedPerson(String name, String instagramUrl) {
        this.name = name;
        this.instagramUrl = instagramUrl;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getInstagramUrl() {
        return instagramUrl;
    }
    
    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }
    
    @Override
    public String toString() {
        return "TaggedPerson{" +
                "name='" + name + '\'' +
                ", instagramUrl='" + instagramUrl + '\'' +
                '}';
    }
}

