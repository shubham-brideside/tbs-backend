package com.brideside.backend.repository;

import com.brideside.backend.entity.BlogPost;
import com.brideside.backend.entity.BlogCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Integer> {
    
    /**
     * Find post by slug
     * @param slug the slug to search for
     * @return optional post
     */
    Optional<BlogPost> findBySlug(String slug);
    
    /**
     * Find all published posts
     * @return list of published posts
     */
    List<BlogPost> findByIsPublishedTrue();
    
    /**
     * Find all published posts by category
     * @param category the category
     * @return list of published posts in the category
     */
    List<BlogPost> findByCategoryAndIsPublishedTrue(BlogCategory category);
    
    /**
     * Find all posts by category (including unpublished)
     * @param category the category
     * @return list of posts in the category
     */
    List<BlogPost> findByCategory(BlogCategory category);
    
    /**
     * Find all published posts ordered by published date descending
     * @return list of published posts
     */
    List<BlogPost> findByIsPublishedTrueOrderByPublishedAtDesc();
    
    /**
     * Find all published posts by category ordered by published date descending
     * @param category the category
     * @return list of published posts
     */
    List<BlogPost> findByCategoryAndIsPublishedTrueOrderByPublishedAtDesc(BlogCategory category);
    
    /**
     * Find latest published posts
     * @param pageable pagination information
     * @return list of latest published posts
     */
    @Query("SELECT p FROM BlogPost p WHERE p.isPublished = true ORDER BY p.publishedAt DESC")
    List<BlogPost> findLatestPublishedPosts(Pageable pageable);
    
    /**
     * Increment view count
     * @param postId the post ID
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE BlogPost p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Integer postId);
}

