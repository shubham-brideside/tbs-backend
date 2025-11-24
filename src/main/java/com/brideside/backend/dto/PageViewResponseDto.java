package com.brideside.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PageViewResponseDto {
    
    private Long id;
    
    @JsonProperty("page_path")
    private String pagePath;
    
    @JsonProperty("page_type")
    private String pageType;
    
    @JsonProperty("entity_id")
    private Integer entityId;
    
    @JsonProperty("ip_address")
    private String ipAddress;
    
    @JsonProperty("viewed_at")
    private String viewedAt;
    
    // Default constructor
    public PageViewResponseDto() {}
    
    // Constructor
    public PageViewResponseDto(Long id, String pagePath, String pageType, Integer entityId, 
                              String ipAddress, String viewedAt) {
        this.id = id;
        this.pagePath = pagePath;
        this.pageType = pageType;
        this.entityId = entityId;
        this.ipAddress = ipAddress;
        this.viewedAt = viewedAt;
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
    
    public String getViewedAt() {
        return viewedAt;
    }
    
    public void setViewedAt(String viewedAt) {
        this.viewedAt = viewedAt;
    }
    
    // Inner class for view statistics
    public static class ViewStatisticsDto {
        
        @JsonProperty("total_views")
        private Long totalViews;
        
        @JsonProperty("unique_visitors")
        private Long uniqueVisitors;
        
        @JsonProperty("page_path")
        private String pagePath;
        
        @JsonProperty("page_type")
        private String pageType;
        
        @JsonProperty("entity_id")
        private Integer entityId;
        
        @JsonProperty("views_today")
        private Long viewsToday;
        
        @JsonProperty("unique_visitors_today")
        private Long uniqueVisitorsToday;
        
        @JsonProperty("views_this_week")
        private Long viewsThisWeek;
        
        @JsonProperty("unique_visitors_this_week")
        private Long uniqueVisitorsThisWeek;
        
        @JsonProperty("views_this_month")
        private Long viewsThisMonth;
        
        @JsonProperty("unique_visitors_this_month")
        private Long uniqueVisitorsThisMonth;
        
        // Default constructor
        public ViewStatisticsDto() {}
        
        // Constructor
        public ViewStatisticsDto(Long totalViews, Long uniqueVisitors, String pagePath, String pageType, 
                                Integer entityId, Long viewsToday, Long uniqueVisitorsToday,
                                Long viewsThisWeek, Long uniqueVisitorsThisWeek,
                                Long viewsThisMonth, Long uniqueVisitorsThisMonth) {
            this.totalViews = totalViews;
            this.uniqueVisitors = uniqueVisitors;
            this.pagePath = pagePath;
            this.pageType = pageType;
            this.entityId = entityId;
            this.viewsToday = viewsToday;
            this.uniqueVisitorsToday = uniqueVisitorsToday;
            this.viewsThisWeek = viewsThisWeek;
            this.uniqueVisitorsThisWeek = uniqueVisitorsThisWeek;
            this.viewsThisMonth = viewsThisMonth;
            this.uniqueVisitorsThisMonth = uniqueVisitorsThisMonth;
        }
        
        // Getters and Setters
        public Long getTotalViews() {
            return totalViews;
        }
        
        public void setTotalViews(Long totalViews) {
            this.totalViews = totalViews;
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
        
        public Long getViewsToday() {
            return viewsToday;
        }
        
        public void setViewsToday(Long viewsToday) {
            this.viewsToday = viewsToday;
        }
        
        public Long getViewsThisWeek() {
            return viewsThisWeek;
        }
        
        public void setViewsThisWeek(Long viewsThisWeek) {
            this.viewsThisWeek = viewsThisWeek;
        }
        
        public Long getViewsThisMonth() {
            return viewsThisMonth;
        }
        
        public void setViewsThisMonth(Long viewsThisMonth) {
            this.viewsThisMonth = viewsThisMonth;
        }
        
        public Long getUniqueVisitors() {
            return uniqueVisitors;
        }
        
        public void setUniqueVisitors(Long uniqueVisitors) {
            this.uniqueVisitors = uniqueVisitors;
        }
        
        public Long getUniqueVisitorsToday() {
            return uniqueVisitorsToday;
        }
        
        public void setUniqueVisitorsToday(Long uniqueVisitorsToday) {
            this.uniqueVisitorsToday = uniqueVisitorsToday;
        }
        
        public Long getUniqueVisitorsThisWeek() {
            return uniqueVisitorsThisWeek;
        }
        
        public void setUniqueVisitorsThisWeek(Long uniqueVisitorsThisWeek) {
            this.uniqueVisitorsThisWeek = uniqueVisitorsThisWeek;
        }
        
        public Long getUniqueVisitorsThisMonth() {
            return uniqueVisitorsThisMonth;
        }
        
        public void setUniqueVisitorsThisMonth(Long uniqueVisitorsThisMonth) {
            this.uniqueVisitorsThisMonth = uniqueVisitorsThisMonth;
        }
    }
    
    // Inner class for tracking response
    public static class TrackViewResponseDto {
        
        private boolean tracked;
        private String message;
        
        @JsonProperty("view_count")
        private Long viewCount;
        
        @JsonProperty("unique_visitors")
        private Long uniqueVisitors;
        
        // Default constructor
        public TrackViewResponseDto() {}
        
        // Constructor
        public TrackViewResponseDto(boolean tracked, String message, Long viewCount, Long uniqueVisitors) {
            this.tracked = tracked;
            this.message = message;
            this.viewCount = viewCount;
            this.uniqueVisitors = uniqueVisitors;
        }
        
        // Getters and Setters
        public boolean isTracked() {
            return tracked;
        }
        
        public void setTracked(boolean tracked) {
            this.tracked = tracked;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public Long getViewCount() {
            return viewCount;
        }
        
        public void setViewCount(Long viewCount) {
            this.viewCount = viewCount;
        }
        
        public Long getUniqueVisitors() {
            return uniqueVisitors;
        }
        
        public void setUniqueVisitors(Long uniqueVisitors) {
            this.uniqueVisitors = uniqueVisitors;
        }
    }
}

