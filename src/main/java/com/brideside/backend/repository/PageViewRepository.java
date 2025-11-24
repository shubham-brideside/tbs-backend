package com.brideside.backend.repository;

import com.brideside.backend.entity.PageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PageViewRepository extends JpaRepository<PageView, Long> {
    
    /**
     * Count views for a specific page path
     * @param pagePath the page path
     * @return count of views
     */
    long countByPagePath(String pagePath);
    
    /**
     * Count views for a specific page type
     * @param pageType the page type
     * @return count of views
     */
    long countByPageType(String pageType);
    
    /**
     * Count views for a specific entity (page type + entity ID)
     * @param pageType the page type
     * @param entityId the entity ID
     * @return count of views
     */
    long countByPageTypeAndEntityId(String pageType, Integer entityId);
    
    /**
     * Get all views for a specific page path
     * @param pagePath the page path
     * @return list of page views
     */
    List<PageView> findByPagePath(String pagePath);
    
    /**
     * Get all views for a specific page type
     * @param pageType the page type
     * @return list of page views
     */
    List<PageView> findByPageType(String pageType);
    
    /**
     * Get all views for a specific entity
     * @param pageType the page type
     * @param entityId the entity ID
     * @return list of page views
     */
    List<PageView> findByPageTypeAndEntityId(String pageType, Integer entityId);
    
    /**
     * Count views within a date range
     * @param startDate start date
     * @param endDate end date
     * @return count of views
     */
    long countByViewedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count views for a specific page path within a date range
     * @param pagePath the page path
     * @param startDate start date
     * @param endDate end date
     * @return count of views
     */
    long countByPagePathAndViewedAtBetween(String pagePath, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get most viewed pages
     * @param limit number of results to return
     * @return list of page paths with view counts
     */
    @Query(value = "SELECT pv.page_path, COUNT(pv.id) as view_count FROM page_views pv GROUP BY pv.page_path ORDER BY view_count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findMostViewedPages(@Param("limit") int limit);
    
    /**
     * Get view count by page type
     * @return list of page types with view counts
     */
    @Query("SELECT pv.pageType, COUNT(pv) as viewCount FROM PageView pv GROUP BY pv.pageType ORDER BY viewCount DESC")
    List<Object[]> getViewCountByPageType();
    
    /**
     * Get total view count
     * @return total number of views
     */
    @Query("SELECT COUNT(pv) FROM PageView pv")
    long getTotalViewCount();
    
    /**
     * Count unique visitors (distinct IP addresses) for a specific page path
     * @param pagePath the page path
     * @return count of unique visitors
     */
    @Query("SELECT COUNT(DISTINCT pv.ipAddress) FROM PageView pv WHERE pv.pagePath = :pagePath AND pv.ipAddress IS NOT NULL")
    long countUniqueVisitorsByPagePath(@Param("pagePath") String pagePath);
    
    /**
     * Count unique visitors for a specific entity (page type + entity ID)
     * @param pageType the page type
     * @param entityId the entity ID
     * @return count of unique visitors
     */
    @Query("SELECT COUNT(DISTINCT pv.ipAddress) FROM PageView pv WHERE pv.pageType = :pageType AND pv.entityId = :entityId AND pv.ipAddress IS NOT NULL")
    long countUniqueVisitorsByEntity(@Param("pageType") String pageType, @Param("entityId") Integer entityId);
    
    /**
     * Count unique visitors for a specific page path within a date range
     * @param pagePath the page path
     * @param startDate start date
     * @param endDate end date
     * @return count of unique visitors
     */
    @Query("SELECT COUNT(DISTINCT pv.ipAddress) FROM PageView pv WHERE pv.pagePath = :pagePath AND pv.viewedAt BETWEEN :startDate AND :endDate AND pv.ipAddress IS NOT NULL")
    long countUniqueVisitorsByPagePathAndDateRange(@Param("pagePath") String pagePath, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get total unique visitors across all pages
     * @return total number of unique visitors
     */
    @Query("SELECT COUNT(DISTINCT pv.ipAddress) FROM PageView pv WHERE pv.ipAddress IS NOT NULL")
    long getTotalUniqueVisitors();
    
    /**
     * Get most visited pages by unique visitors
     * @param limit number of results to return
     * @return list of page paths with unique visitor counts
     */
    @Query(value = "SELECT pv.page_path, COUNT(DISTINCT pv.ip_address) as unique_visitors FROM page_views pv WHERE pv.ip_address IS NOT NULL GROUP BY pv.page_path ORDER BY unique_visitors DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findMostVisitedPagesByUniqueVisitors(@Param("limit") int limit);
    
    /**
     * Check if a page view already exists for a specific IP address and page path
     * @param pagePath the page path
     * @param ipAddress the IP address
     * @return true if a view exists, false otherwise
     */
    @Query("SELECT COUNT(pv) > 0 FROM PageView pv WHERE pv.pagePath = :pagePath AND pv.ipAddress = :ipAddress")
    boolean existsByPagePathAndIpAddress(@Param("pagePath") String pagePath, @Param("ipAddress") String ipAddress);
}

