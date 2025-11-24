package com.brideside.backend.controller;

import com.brideside.backend.dto.PageViewRequestDto;
import com.brideside.backend.dto.PageViewResponseDto;
import com.brideside.backend.service.PageViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/page-views")
@Tag(name = "Page View Tracking", description = "APIs for tracking and retrieving page view statistics")
public class PageViewController {
    
    private static final Logger logger = LoggerFactory.getLogger(PageViewController.class);
    
    @Autowired
    private PageViewService pageViewService;
    
    /**
     * Track a page view
     * This endpoint should be called when a page is viewed
     * Includes rate limiting to prevent duplicate tracking
     */
    @Operation(summary = "Track page view", description = "Track a page view. Includes rate limiting to prevent duplicate tracking within 5 seconds.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "View tracked successfully or rate-limited"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/track")
    public ResponseEntity<PageViewResponseDto.TrackViewResponseDto> trackPageView(
            @Valid @RequestBody PageViewRequestDto request,
            HttpServletRequest httpRequest) {
        try {
            PageViewResponseDto.TrackViewResponseDto response = pageViewService.trackPageView(request, httpRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error tracking page view", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error tracking page view: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get view count for a specific page path
     */
    @Operation(summary = "Get view count by page path", description = "Retrieve the total view count for a specific page path")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved view count"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getViewCount(@RequestParam String pagePath) {
        try {
            long viewCount = pageViewService.getViewCount(pagePath);
            long uniqueVisitors = pageViewService.getUniqueVisitorCount(pagePath);
            Map<String, Object> response = new HashMap<>();
            response.put("page_path", pagePath);
            response.put("view_count", viewCount);
            response.put("unique_visitors", uniqueVisitors);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting view count for page path: {}", pagePath, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error getting view count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>(errorResponse));
        }
    }
    
    /**
     * Get unique visitor count for a specific page path
     */
    @Operation(summary = "Get unique visitor count by page path", description = "Retrieve the number of unique visitors for a specific page path")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved unique visitor count"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/unique-visitors")
    public ResponseEntity<Map<String, Object>> getUniqueVisitorCount(@RequestParam String pagePath) {
        try {
            long uniqueVisitors = pageViewService.getUniqueVisitorCount(pagePath);
            Map<String, Object> response = new HashMap<>();
            response.put("page_path", pagePath);
            response.put("unique_visitors", uniqueVisitors);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting unique visitor count for page path: {}", pagePath, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error getting unique visitor count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>(errorResponse));
        }
    }
    
    /**
     * Get view count for a specific entity (page type + entity ID)
     */
    @Operation(summary = "Get view count by entity", description = "Retrieve the total view count for a specific entity (page type + entity ID)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved view count"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/count/entity")
    public ResponseEntity<Map<String, Object>> getViewCountByEntity(
            @RequestParam String pageType,
            @RequestParam Integer entityId) {
        try {
            long viewCount = pageViewService.getViewCountByEntity(pageType, entityId);
            long uniqueVisitors = pageViewService.getUniqueVisitorCountByEntity(pageType, entityId);
            Map<String, Object> response = new HashMap<>();
            response.put("page_type", pageType);
            response.put("entity_id", entityId);
            response.put("view_count", viewCount);
            response.put("unique_visitors", uniqueVisitors);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting view count for entity: {} - {}", pageType, entityId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error getting view count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>(errorResponse));
        }
    }
    
    /**
     * Get detailed statistics for a page path
     */
    @Operation(summary = "Get page statistics", description = "Retrieve detailed statistics for a specific page path including views today, this week, and this month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/statistics")
    public ResponseEntity<PageViewResponseDto.ViewStatisticsDto> getPageStatistics(@RequestParam String pagePath) {
        try {
            PageViewResponseDto.ViewStatisticsDto statistics = pageViewService.getPageStatistics(pagePath);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting statistics for page path: {}", pagePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get statistics for an entity (page type + entity ID)
     */
    @Operation(summary = "Get entity statistics", description = "Retrieve detailed statistics for a specific entity including views today, this week, and this month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/statistics/entity")
    public ResponseEntity<PageViewResponseDto.ViewStatisticsDto> getEntityStatistics(
            @RequestParam String pageType,
            @RequestParam Integer entityId) {
        try {
            PageViewResponseDto.ViewStatisticsDto statistics = pageViewService.getEntityStatistics(pageType, entityId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting statistics for entity: {} - {}", pageType, entityId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get total view count across all pages
     */
    @Operation(summary = "Get total view count", description = "Retrieve the total number of page views across all pages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved total view count"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> getTotalViewCount() {
        try {
            long totalViews = pageViewService.getTotalViewCount();
            long totalUniqueVisitors = pageViewService.getTotalUniqueVisitors();
            Map<String, Object> response = new HashMap<>();
            response.put("total_views", totalViews);
            response.put("total_unique_visitors", totalUniqueVisitors);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting total view count", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error getting total view count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>(errorResponse));
        }
    }
    
    /**
     * Get most viewed pages
     */
    @Operation(summary = "Get most viewed pages", description = "Retrieve the most viewed pages with their view counts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved most viewed pages"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/most-viewed")
    public ResponseEntity<Map<String, Long>> getMostViewedPages(@RequestParam(defaultValue = "10") int limit) {
        try {
            Map<String, Long> mostViewed = pageViewService.getMostViewedPages(limit);
            return ResponseEntity.ok(mostViewed);
        } catch (Exception e) {
            logger.error("Error getting most viewed pages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get view count by page type
     */
    @Operation(summary = "Get view count by page type", description = "Retrieve view counts grouped by page type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved view counts by page type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/by-type")
    public ResponseEntity<Map<String, Long>> getViewCountByPageType() {
        try {
            Map<String, Long> viewCounts = pageViewService.getViewCountByPageType();
            return ResponseEntity.ok(viewCounts);
        } catch (Exception e) {
            logger.error("Error getting view count by page type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get most visited pages by unique visitors
     */
    @Operation(summary = "Get most visited pages by unique visitors", description = "Retrieve the most visited pages ranked by unique visitor count")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved most visited pages"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/most-visited")
    public ResponseEntity<Map<String, Long>> getMostVisitedPagesByUniqueVisitors(@RequestParam(defaultValue = "10") int limit) {
        try {
            Map<String, Long> mostVisited = pageViewService.getMostVisitedPagesByUniqueVisitors(limit);
            return ResponseEntity.ok(mostVisited);
        } catch (Exception e) {
            logger.error("Error getting most visited pages by unique visitors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

