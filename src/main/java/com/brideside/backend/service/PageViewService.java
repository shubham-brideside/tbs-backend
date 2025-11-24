package com.brideside.backend.service;

import com.brideside.backend.dto.PageViewRequestDto;
import com.brideside.backend.dto.PageViewResponseDto;
import com.brideside.backend.entity.PageView;
import com.brideside.backend.repository.PageViewRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class PageViewService {
    
    private static final Logger logger = LoggerFactory.getLogger(PageViewService.class);
    
    @Autowired
    private PageViewRepository pageViewRepository;
    
    // In-memory cache to prevent duplicate view increments within 5 seconds
    // Key: pagePath + ipAddress, Value: timestamp of last increment
    private final Map<String, Long> viewIncrementCache = new ConcurrentHashMap<>();
    private static final long VIEW_INCREMENT_COOLDOWN_MS = 5000; // 5 seconds
    
    /**
     * Track a page view
     * Only stores unique user-page combinations (same IP + same page = only one record)
     * @param request DTO containing page path, type, and entity ID
     * @param httpRequest HTTP request to extract IP address and user agent
     * @return response DTO with tracking status and view count
     */
    public PageViewResponseDto.TrackViewResponseDto trackPageView(PageViewRequestDto request, HttpServletRequest httpRequest) {
        String pagePath = request.getPagePath();
        String ipAddress = getClientIpAddress(httpRequest);
        String cacheKey = pagePath + "|" + ipAddress;
        
        long currentTime = System.currentTimeMillis();
        
        // Check if we've tracked this page view recently (within cooldown period) - for performance
        Long lastTrackTime = viewIncrementCache.get(cacheKey);
        if (lastTrackTime != null && (currentTime - lastTrackTime) < VIEW_INCREMENT_COOLDOWN_MS) {
            logger.debug("Page view tracking rate-limited for path {} from IP {} (last track was {}ms ago)", 
                        pagePath, ipAddress, currentTime - lastTrackTime);
            
            // Return current view count even if rate-limited
            long viewCount = pageViewRepository.countByPagePath(pagePath);
            long uniqueVisitors = pageViewRepository.countUniqueVisitorsByPagePath(pagePath);
            return new PageViewResponseDto.TrackViewResponseDto(
                false, 
                "View rate-limited (already tracked recently)", 
                viewCount,
                uniqueVisitors
            );
        }
        
        // Check if this IP has already viewed this page (unique user-page combination check)
        // This check helps avoid unnecessary save attempts, but the unique constraint is the real protection
        boolean alreadyExists = pageViewRepository.existsByPagePathAndIpAddress(pagePath, ipAddress);
        
        if (alreadyExists) {
            logger.debug("Page view already exists for path {} from IP {} - skipping duplicate entry", 
                        pagePath, ipAddress);
            
            // Return current counts without creating a new entry
            long viewCount = pageViewRepository.countByPagePath(pagePath);
            long uniqueVisitors = pageViewRepository.countUniqueVisitorsByPagePath(pagePath);
            return new PageViewResponseDto.TrackViewResponseDto(
                false, 
                "View already tracked for this user and page", 
                viewCount,
                uniqueVisitors
            );
        }
        
        // Create and save page view (only if it's a new unique combination)
        // The unique constraint at database level will prevent duplicates even in race conditions
        PageView pageView = new PageView();
        pageView.setPagePath(pagePath);
        pageView.setPageType(request.getPageType());
        pageView.setEntityId(request.getEntityId());
        pageView.setIpAddress(ipAddress);
        pageView.setUserAgent(httpRequest.getHeader("User-Agent"));
        pageView.setReferrer(httpRequest.getHeader("Referer"));
        pageView.setViewedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        
        try {
            pageViewRepository.save(pageView);
        } catch (DataIntegrityViolationException e) {
            // Handle race condition: if two requests come in simultaneously,
            // the unique constraint will prevent the duplicate
            logger.debug("Duplicate page view prevented by unique constraint for path {} from IP {}", 
                        pagePath, ipAddress);
            
            // Return current counts without creating a new entry
            long viewCount = pageViewRepository.countByPagePath(pagePath);
            long uniqueVisitors = pageViewRepository.countUniqueVisitorsByPagePath(pagePath);
            return new PageViewResponseDto.TrackViewResponseDto(
                false, 
                "View already tracked for this user and page", 
                viewCount,
                uniqueVisitors
            );
        }
        
        // Update cache with current timestamp
        viewIncrementCache.put(cacheKey, currentTime);
        
        // Clean up old cache entries (older than cooldown period) to prevent memory leak
        viewIncrementCache.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > VIEW_INCREMENT_COOLDOWN_MS);
        
        long viewCount = pageViewRepository.countByPagePath(pagePath);
        long uniqueVisitors = pageViewRepository.countUniqueVisitorsByPagePath(pagePath);
        logger.info("Page view tracked for path: {} (type: {}, entityId: {}) from IP: {}. Total unique views: {}, Unique visitors: {}", 
                   pagePath, request.getPageType(), request.getEntityId(), ipAddress, viewCount, uniqueVisitors);
        
        return new PageViewResponseDto.TrackViewResponseDto(true, "View tracked successfully", viewCount, uniqueVisitors);
    }
    
    /**
     * Get view count for a specific page path
     * @param pagePath the page path
     * @return view count
     */
    @Transactional(readOnly = true)
    public long getViewCount(String pagePath) {
        return pageViewRepository.countByPagePath(pagePath);
    }
    
    /**
     * Get unique visitor count for a specific page path
     * @param pagePath the page path
     * @return unique visitor count
     */
    @Transactional(readOnly = true)
    public long getUniqueVisitorCount(String pagePath) {
        return pageViewRepository.countUniqueVisitorsByPagePath(pagePath);
    }
    
    /**
     * Get view count for a specific entity (page type + entity ID)
     * @param pageType the page type
     * @param entityId the entity ID
     * @return view count
     */
    @Transactional(readOnly = true)
    public long getViewCountByEntity(String pageType, Integer entityId) {
        return pageViewRepository.countByPageTypeAndEntityId(pageType, entityId);
    }
    
    /**
     * Get unique visitor count for a specific entity (page type + entity ID)
     * @param pageType the page type
     * @param entityId the entity ID
     * @return unique visitor count
     */
    @Transactional(readOnly = true)
    public long getUniqueVisitorCountByEntity(String pageType, Integer entityId) {
        return pageViewRepository.countUniqueVisitorsByEntity(pageType, entityId);
    }
    
    /**
     * Get detailed statistics for a page path
     * @param pagePath the page path
     * @return statistics DTO
     */
    @Transactional(readOnly = true)
    public PageViewResponseDto.ViewStatisticsDto getPageStatistics(String pagePath) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        
        long totalViews = pageViewRepository.countByPagePath(pagePath);
        long uniqueVisitors = pageViewRepository.countUniqueVisitorsByPagePath(pagePath);
        long viewsToday = pageViewRepository.countByPagePathAndViewedAtBetween(pagePath, startOfDay, now);
        long uniqueVisitorsToday = pageViewRepository.countUniqueVisitorsByPagePathAndDateRange(pagePath, startOfDay, now);
        long viewsThisWeek = pageViewRepository.countByPagePathAndViewedAtBetween(pagePath, startOfWeek, now);
        long uniqueVisitorsThisWeek = pageViewRepository.countUniqueVisitorsByPagePathAndDateRange(pagePath, startOfWeek, now);
        long viewsThisMonth = pageViewRepository.countByPagePathAndViewedAtBetween(pagePath, startOfMonth, now);
        long uniqueVisitorsThisMonth = pageViewRepository.countUniqueVisitorsByPagePathAndDateRange(pagePath, startOfMonth, now);
        
        // Get page type and entity ID from the most recent view
        PageView latestView = pageViewRepository.findByPagePath(pagePath).stream()
            .findFirst()
            .orElse(null);
        
        String pageType = latestView != null ? latestView.getPageType() : null;
        Integer entityId = latestView != null ? latestView.getEntityId() : null;
        
        return new PageViewResponseDto.ViewStatisticsDto(
            totalViews,
            uniqueVisitors,
            pagePath,
            pageType,
            entityId,
            viewsToday,
            uniqueVisitorsToday,
            viewsThisWeek,
            uniqueVisitorsThisWeek,
            viewsThisMonth,
            uniqueVisitorsThisMonth
        );
    }
    
    /**
     * Get statistics for an entity (page type + entity ID)
     * @param pageType the page type
     * @param entityId the entity ID
     * @return statistics DTO
     */
    @Transactional(readOnly = true)
    public PageViewResponseDto.ViewStatisticsDto getEntityStatistics(String pageType, Integer entityId) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        
        List<PageView> views = pageViewRepository.findByPageTypeAndEntityId(pageType, entityId);
        String pagePath = views.stream()
            .map(PageView::getPagePath)
            .findFirst()
            .orElse(null);
        
        long totalViews = views.size();
        long uniqueVisitors = pageViewRepository.countUniqueVisitorsByEntity(pageType, entityId);
        
        List<PageView> viewsToday = views.stream()
            .filter(v -> v.getViewedAt().isAfter(startOfDay))
            .collect(Collectors.toList());
        long viewsTodayCount = viewsToday.size();
        long uniqueVisitorsToday = viewsToday.stream()
            .map(PageView::getIpAddress)
            .filter(ip -> ip != null)
            .distinct()
            .count();
        
        List<PageView> viewsThisWeek = views.stream()
            .filter(v -> v.getViewedAt().isAfter(startOfWeek))
            .collect(Collectors.toList());
        long viewsThisWeekCount = viewsThisWeek.size();
        long uniqueVisitorsThisWeek = viewsThisWeek.stream()
            .map(PageView::getIpAddress)
            .filter(ip -> ip != null)
            .distinct()
            .count();
        
        List<PageView> viewsThisMonth = views.stream()
            .filter(v -> v.getViewedAt().isAfter(startOfMonth))
            .collect(Collectors.toList());
        long viewsThisMonthCount = viewsThisMonth.size();
        long uniqueVisitorsThisMonth = viewsThisMonth.stream()
            .map(PageView::getIpAddress)
            .filter(ip -> ip != null)
            .distinct()
            .count();
        
        return new PageViewResponseDto.ViewStatisticsDto(
            totalViews,
            uniqueVisitors,
            pagePath,
            pageType,
            entityId,
            viewsTodayCount,
            uniqueVisitorsToday,
            viewsThisWeekCount,
            uniqueVisitorsThisWeek,
            viewsThisMonthCount,
            uniqueVisitorsThisMonth
        );
    }
    
    /**
     * Get total view count across all pages
     * @return total view count
     */
    @Transactional(readOnly = true)
    public long getTotalViewCount() {
        return pageViewRepository.getTotalViewCount();
    }
    
    /**
     * Get most viewed pages
     * @param limit number of results to return
     * @return map of page paths to view counts
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getMostViewedPages(int limit) {
        List<Object[]> results = pageViewRepository.findMostViewedPages(limit);
        return results.stream()
            .collect(Collectors.toMap(
                result -> (String) result[0],
                result -> {
                    Object count = result[1];
                    if (count instanceof Number) {
                        return ((Number) count).longValue();
                    }
                    return Long.parseLong(count.toString());
                }
            ));
    }
    
    /**
     * Get view count by page type
     * @return map of page types to view counts
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getViewCountByPageType() {
        List<Object[]> results = pageViewRepository.getViewCountByPageType();
        return results.stream()
            .collect(Collectors.toMap(
                result -> (String) result[0],
                result -> {
                    Object count = result[1];
                    if (count instanceof Number) {
                        return ((Number) count).longValue();
                    }
                    return Long.parseLong(count.toString());
                }
            ));
    }
    
    /**
     * Get total unique visitors across all pages
     * @return total number of unique visitors
     */
    @Transactional(readOnly = true)
    public long getTotalUniqueVisitors() {
        return pageViewRepository.getTotalUniqueVisitors();
    }
    
    /**
     * Get most visited pages by unique visitors
     * @param limit number of results to return
     * @return map of page paths to unique visitor counts
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getMostVisitedPagesByUniqueVisitors(int limit) {
        List<Object[]> results = pageViewRepository.findMostVisitedPagesByUniqueVisitors(limit);
        return results.stream()
            .collect(Collectors.toMap(
                result -> (String) result[0],
                result -> {
                    Object count = result[1];
                    if (count instanceof Number) {
                        return ((Number) count).longValue();
                    }
                    return Long.parseLong(count.toString());
                }
            ));
    }
    
    /**
     * Extract client IP address from HTTP request
     * Handles proxies and load balancers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // If multiple IPs are present, take the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress;
    }
}

