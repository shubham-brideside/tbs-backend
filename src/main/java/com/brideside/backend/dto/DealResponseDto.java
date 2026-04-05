package com.brideside.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DealResponseDto {
    
    private String message;
    private List<DealDto> createdDeals;
    private LocalDateTime timestamp;
    
    // Default constructor
    public DealResponseDto() {}
    
    // Constructor
    public DealResponseDto(String message, List<DealDto> createdDeals) {
        this.message = message;
        this.createdDeals = createdDeals;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<DealDto> getCreatedDeals() {
        return createdDeals;
    }
    
    public void setCreatedDeals(List<DealDto> createdDeals) {
        this.createdDeals = createdDeals;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "DealResponseDto{" +
                "message='" + message + '\'' +
                ", createdDeals=" + createdDeals +
                ", timestamp=" + timestamp +
                '}';
    }
    
    // Inner class for Deal
    public static class DealDto {
        
        private Integer id;
        private String userName;
        private String contactNumber;
        private Long categoryId;
        private String eventDate;
        private String venue;
        private String budget;
        private String expectedGathering;
        private String createdAt;
        private String updatedAt;
        
        // Default constructor
        public DealDto() {}
        
        // Constructor
        public DealDto(Integer id, String userName, String contactNumber, Long categoryId,
                      String eventDate, String venue, String budget, String expectedGathering,
                      String createdAt, String updatedAt) {
            this.id = id;
            this.userName = userName;
            this.contactNumber = contactNumber;
            this.categoryId = categoryId;
            this.eventDate = eventDate;
            this.venue = venue;
            this.budget = budget;
            this.expectedGathering = expectedGathering;
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
        
        public String getUserName() {
            return userName;
        }
        
        public void setUserName(String userName) {
            this.userName = userName;
        }
        
        public String getContactNumber() {
            return contactNumber;
        }
        
        public void setContactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
        }
        
        public Long getCategoryId() {
            return categoryId;
        }
        
        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }
        
        public String getEventDate() {
            return eventDate;
        }
        
        public void setEventDate(String eventDate) {
            this.eventDate = eventDate;
        }
        
        public String getVenue() {
            return venue;
        }
        
        public void setVenue(String venue) {
            this.venue = venue;
        }
        
        public String getBudget() {
            return budget;
        }
        
        public void setBudget(String budget) {
            this.budget = budget;
        }
        
        public String getExpectedGathering() {
            return expectedGathering;
        }
        
        public void setExpectedGathering(String expectedGathering) {
            this.expectedGathering = expectedGathering;
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
        
        @Override
        public String toString() {
            return "DealDto{" +
                    "id=" + id +
                    ", userName='" + userName + '\'' +
                    ", contactNumber='" + contactNumber + '\'' +
                    ", categoryId=" + categoryId +
                    ", eventDate='" + eventDate + '\'' +
                    ", venue='" + venue + '\'' +
                    ", budget='" + budget + '\'' +
                    ", expectedGathering=" + expectedGathering +
                    ", createdAt='" + createdAt + '\'' +
                    ", updatedAt='" + updatedAt + '\'' +
                    '}';
        }
    }
}
