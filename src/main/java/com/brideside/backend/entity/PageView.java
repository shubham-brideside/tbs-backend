package com.brideside.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "page_views", 
    indexes = {
        @Index(name = "idx_page_path", columnList = "page_path"),
        @Index(name = "idx_viewed_at", columnList = "viewed_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_page_path_ip", columnNames = {"page_path", "ip_address"})
    }
)
public class PageView {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "page_path", nullable = false, length = 500)
    private String pagePath;
    
    @Column(name = "page_type", length = 100)
    private String pageType; // e.g., "blog_post", "deal", "home", "category", etc.
    
    @Column(name = "entity_id")
    private Integer entityId; // ID of the related entity (e.g., blog post ID, deal ID)
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "referrer", length = 500)
    private String referrer;
    
    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;
    
    @PrePersist
    protected void onCreate() {
        if (viewedAt == null) {
            viewedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        }
    }
    
    // Default constructor
    public PageView() {}
    
    // Constructor
    public PageView(String pagePath, String pageType, Integer entityId) {
        this.pagePath = pagePath;
        this.pageType = pageType;
        this.entityId = entityId;
        this.viewedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getReferrer() {
        return referrer;
    }
    
    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
    
    public LocalDateTime getViewedAt() {
        return viewedAt;
    }
    
    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
}

