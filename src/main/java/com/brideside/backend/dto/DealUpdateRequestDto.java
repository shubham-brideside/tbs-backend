package com.brideside.backend.dto;

import com.brideside.backend.jackson.StringOrNumberAsStringDeserializer;
import com.brideside.backend.jackson.TrimStringDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Request DTO for updating a deal with name and categories (contact number not required)")
public class DealUpdateRequestDto {
    
    @NotBlank(message = "Name is required")
    @Schema(description = "Name of the user", example = "John Doe", required = true)
    private String name;
    
    @NotEmpty(message = "Categories list cannot be empty")
    @Valid
    @Schema(description = "List of categories for the deal", required = true)
    private List<CategoryDto> categories;
    
    // Default constructor
    public DealUpdateRequestDto() {}
    
    // Constructor
    public DealUpdateRequestDto(String name, List<CategoryDto> categories) {
        this.name = name;
        this.categories = categories;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<CategoryDto> getCategories() {
        return categories;
    }
    
    public void setCategories(List<CategoryDto> categories) {
        this.categories = categories;
    }
    
    @Override
    public String toString() {
        return "DealUpdateRequestDto{" +
                "name='" + name + '\'' +
                ", categories=" + categories +
                '}';
    }
    
    // Inner class for Category (reusing from DealRequestDto)
    @Schema(description = "Category information for the deal")
    public static class CategoryDto {
        
        @NotBlank(message = "Category name is required")
        @JsonDeserialize(using = TrimStringDeserializer.class)
        @Pattern(
                regexp = "^(?i)(Photography|Wedding Photography|Makeup|Planning and Decor|Planning & Decor)$",
                message = "Category name must be Photography, Makeup, or Planning and Decor (Planning & Decor accepted)"
        )
        @Schema(description = "Service from the planning form", example = "Photography", required = true)
        private String name;
        
        @JsonProperty("event_date")
        @Schema(description = "Event date when confirmed (YYYY-MM-DD)", example = "2026-11-22")
        private LocalDate eventDate;
        
        @Schema(description = "Venue for the event", example = "Grand Hotel Ballroom")
        private String venue;
        
        @PositiveOrZero(message = "Budget must be positive or zero")
        @Schema(description = "Budget for this category", example = "5000.00")
        private BigDecimal budget;
        
        @JsonProperty("expected_gathering")
        @JsonDeserialize(using = StringOrNumberAsStringDeserializer.class)
        @Size(max = 64, message = "Expected gathering must be at most 64 characters")
        @Schema(description = "Guest count or range, e.g. 250 or 100-300", example = "100-300")
        private String expectedGathering;
        
        // Default constructor
        public CategoryDto() {}
        
        // Constructor
        public CategoryDto(String name, LocalDate eventDate, String venue, 
                          BigDecimal budget, String expectedGathering) {
            this.name = name;
            this.eventDate = eventDate;
            this.venue = venue;
            this.budget = budget;
            this.expectedGathering = expectedGathering;
        }
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public LocalDate getEventDate() {
            return eventDate;
        }
        
        public void setEventDate(LocalDate eventDate) {
            this.eventDate = eventDate;
        }
        
        public String getVenue() {
            return venue;
        }
        
        public void setVenue(String venue) {
            this.venue = venue;
        }
        
        public BigDecimal getBudget() {
            return budget;
        }
        
        public void setBudget(BigDecimal budget) {
            this.budget = budget;
        }
        
        public String getExpectedGathering() {
            return expectedGathering;
        }
        
        public void setExpectedGathering(String expectedGathering) {
            this.expectedGathering = expectedGathering;
        }
        
        @Override
        public String toString() {
            return "CategoryDto{" +
                    "name='" + name + '\'' +
                    ", eventDate=" + eventDate +
                    ", venue='" + venue + '\'' +
                    ", budget=" + budget +
                    ", expectedGathering=" + expectedGathering +
                    '}';
        }
    }
}
