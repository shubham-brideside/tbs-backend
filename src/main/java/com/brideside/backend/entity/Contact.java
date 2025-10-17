package com.brideside.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacts", 
       uniqueConstraints = @UniqueConstraint(name = "ix_contacts_contact_name", columnNames = "contact_name"),
       indexes = @Index(name = "ix_contacts_id", columnList = "id"))
public class Contact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "contact_name", length = 255)
    private String contactName;
    
    @Column(name = "pipedrive_contact_id", length = 100)
    private String pipedriveContactId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Contact() {}
    
    // Constructor with all fields
    public Contact(String contactName, String pipedriveContactId) {
        this.contactName = contactName;
        this.pipedriveContactId = pipedriveContactId;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getContactName() {
        return contactName;
    }
    
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
    
    public String getPipedriveContactId() {
        return pipedriveContactId;
    }
    
    public void setPipedriveContactId(String pipedriveContactId) {
        this.pipedriveContactId = pipedriveContactId;
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
    
    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", contactName='" + contactName + '\'' +
                ", pipedriveContactId='" + pipedriveContactId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
