package com.brideside.backend.repository;

import com.brideside.backend.entity.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Integer> {
    
    /**
     * Find category by slug
     * @param slug the slug to search for
     * @return optional category
     */
    Optional<BlogCategory> findBySlug(String slug);
    
    /**
     * Find all active categories
     * @return list of active categories
     */
    List<BlogCategory> findByIsActiveTrue();
    
    /**
     * Find category by name
     * @param name the name to search for
     * @return optional category
     */
    Optional<BlogCategory> findByName(String name);
}

