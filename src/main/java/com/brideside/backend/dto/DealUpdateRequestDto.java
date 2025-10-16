package com.brideside.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
        @Schema(description = "Name of the category", example = "Wedding Photography", required = true)
        private String name;
        
        @NotNull(message = "Event date is required")
        @JsonProperty("event_date")
        @Schema(description = "Date of the event", example = "2024-06-15", required = true)
        private LocalDate eventDate;
        
        @Schema(description = "Venue for the event", example = "Grand Hotel Ballroom")
        private String venue;
        
        @PositiveOrZero(message = "Budget must be positive or zero")
        @Schema(description = "Budget for this category", example = "5000.00")
        private BigDecimal budget;
        
        @Positive(message = "Expected gathering must be positive")
        @JsonProperty("expected_gathering")
        @Schema(description = "Expected number of guests", example = "150", required = true)
        private Integer expectedGathering;
        
        // Default constructor
        public CategoryDto() {}
        
        // Constructor
        public CategoryDto(String name, LocalDate eventDate, String venue, 
                          BigDecimal budget, Integer expectedGathering) {
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
        
        public Integer getExpectedGathering() {
            return expectedGathering;
        }
        
        public void setExpectedGathering(Integer expectedGathering) {
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
