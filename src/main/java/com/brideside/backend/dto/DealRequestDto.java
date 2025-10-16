package com.brideside.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DealRequestDto {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Contact number is required")
    @JsonProperty("contact_number")
    private String contactNumber;
    
    @NotEmpty(message = "Categories list cannot be empty")
    @Valid
    private List<CategoryDto> categories;
    
    // Default constructor
    public DealRequestDto() {}
    
    // Constructor
    public DealRequestDto(String name, String contactNumber, List<CategoryDto> categories) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.categories = categories;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public List<CategoryDto> getCategories() {
        return categories;
    }
    
    public void setCategories(List<CategoryDto> categories) {
        this.categories = categories;
    }
    
    @Override
    public String toString() {
        return "DealRequestDto{" +
                "name='" + name + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", categories=" + categories +
                '}';
    }
    
    // Inner class for Category
    public static class CategoryDto {
        
        @NotBlank(message = "Category name is required")
        private String name;
        
        @NotNull(message = "Event date is required")
        @JsonProperty("event_date")
        private LocalDate eventDate;
        
        private String venue;
        
        @PositiveOrZero(message = "Budget must be positive or zero")
        private BigDecimal budget;
        
        @Positive(message = "Expected gathering must be positive")
        @JsonProperty("expected_gathering")
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
