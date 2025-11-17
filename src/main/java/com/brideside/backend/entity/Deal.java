package com.brideside.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "deals")
public class Deal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "User name is required")
    private String userName;
    
    @Column(name = "contact_number", nullable = false, length = 20)
    @NotBlank(message = "Contact number is required")
    private String contactNumber;
    
    @Column(name = "category", nullable = false, length = 50)
    @NotBlank(message = "Category is required")
    private String category;
    
    @Column(name = "event_date")
    private LocalDate eventDate;
    
    @Column(name = "venue", length = 255)
    private String venue;
    
    @Column(name = "budget", precision = 10, scale = 2)
    @PositiveOrZero(message = "Budget must be positive or zero")
    private BigDecimal budget;
    
    @Column(name = "\"value\"", nullable = false, precision = 12, scale = 2)
    private BigDecimal value = BigDecimal.ZERO;
    
    @Column(name = "expected_gathering")
    @Positive(message = "Expected gathering must be positive")
    private Integer expectedGathering;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "pipedrive_deal_id", length = 100)
    private String pipedriveDealId;
    
    @Column(name = "contact_id")
    private Integer contactId;
    
    // Default constructor
    public Deal() {}
    
    // Constructor with all fields
    public Deal(String userName, String contactNumber, String category, LocalDate eventDate, 
                String venue, BigDecimal budget, Integer expectedGathering) {
        this.userName = userName;
        this.contactNumber = contactNumber;
        this.category = category;
        this.eventDate = eventDate;
        this.venue = venue;
        this.budget = budget;
        this.expectedGathering = expectedGathering;
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
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
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
    
    public BigDecimal getValue() {
        return value;
    }
    
    public void setValue(BigDecimal value) {
        this.value = value;
    }
    
    public Integer getExpectedGathering() {
        return expectedGathering;
    }
    
    public void setExpectedGathering(Integer expectedGathering) {
        this.expectedGathering = expectedGathering;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getPipedriveDealId() {
        return pipedriveDealId;
    }
    
    public void setPipedriveDealId(String pipedriveDealId) {
        this.pipedriveDealId = pipedriveDealId;
    }
    
    public Integer getContactId() {
        return contactId;
    }
    
    public void setContactId(Integer contactId) {
        this.contactId = contactId;
    }
    
    @Override
    public String toString() {
        return "Deal{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", category='" + category + '\'' +
                ", eventDate=" + eventDate +
                ", venue='" + venue + '\'' +
                ", budget=" + budget +
                ", expectedGathering=" + expectedGathering +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", pipedriveDealId='" + pipedriveDealId + '\'' +
                ", contactId=" + contactId +
                '}';
    }
}
