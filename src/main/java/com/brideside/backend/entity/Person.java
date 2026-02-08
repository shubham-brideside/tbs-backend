package com.brideside.backend.entity;

import com.brideside.backend.enums.DealSubSource;
import com.brideside.backend.enums.PersonSource;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "persons")
public class Person {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "phone", length = 50)
    private String phone;
    
    @Column(name = "phone_num", length = 255)
    private String phoneNum;
    
    @Column(name = "source", length = 255)
    private String source;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "person_source")
    private PersonSource personSource;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sub_source")
    private DealSubSource subSource;
    
    @Column(name = "venue", columnDefinition = "TEXT")
    private String venue;
    
    @Column(name = "wedding_date", length = 255)
    private String weddingDate;
    
    @Column(name = "lead_date")
    private LocalDate leadDate;
    
    @Column(name = "organization_id")
    private Long organizationId;
    
    @Column(name = "owner_id")
    private Long ownerId;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "is_deleted", nullable = false)
    @org.hibernate.annotations.ColumnDefault("0")
    private Boolean isDeleted = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        // Explicitly set timestamps in IST timezone to ensure correct storage
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        // Explicitly set updated timestamp in IST timezone
        updatedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }
    
    // Default constructor
    public Person() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getPhoneNum() {
        return phoneNum;
    }
    
    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public PersonSource getPersonSource() {
        return personSource;
    }
    
    public void setPersonSource(PersonSource personSource) {
        this.personSource = personSource;
    }
    
    public DealSubSource getSubSource() {
        return subSource;
    }
    
    public void setSubSource(DealSubSource subSource) {
        this.subSource = subSource;
    }
    
    public String getVenue() {
        return venue;
    }
    
    public void setVenue(String venue) {
        this.venue = venue;
    }
    
    public String getWeddingDate() {
        return weddingDate;
    }
    
    public void setWeddingDate(String weddingDate) {
        this.weddingDate = weddingDate;
    }
    
    public LocalDate getLeadDate() {
        return leadDate;
    }
    
    public void setLeadDate(LocalDate leadDate) {
        this.leadDate = leadDate;
    }
    
    public Long getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
    
    public Long getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
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
}

