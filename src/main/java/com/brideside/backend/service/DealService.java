package com.brideside.backend.service;

import com.brideside.backend.dto.DealRequestDto;
import com.brideside.backend.dto.DealResponseDto;
import com.brideside.backend.dto.DealInitRequestDto;
import com.brideside.backend.dto.DealUpdateRequestDto;
import com.brideside.backend.entity.Deal;
import com.brideside.backend.repository.DealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DealService {
    
    @Autowired
    private DealRepository dealRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Create multiple deals from a single request
     * Each category in the request will create a separate deal entry
     * @param dealRequest the request containing user info and categories
     * @return response with created deals
     */
    @CacheEvict(value = "deals", allEntries = true)
    public DealResponseDto createDeals(DealRequestDto dealRequest) {
        List<DealResponseDto.DealDto> createdDeals = new ArrayList<>();
        
        // Create a separate deal for each category
        for (DealRequestDto.CategoryDto category : dealRequest.getCategories()) {
            Deal deal = new Deal(
                dealRequest.getName(),
                dealRequest.getContactNumber(),
                category.getName(),
                category.getEventDate(),
                category.getVenue(),
                category.getBudget(),
                category.getExpectedGathering()
            );
            
            Deal savedDeal = dealRepository.save(deal);
            createdDeals.add(convertToDealDto(savedDeal));
        }
        
        String message = String.format("Successfully created %d deal(s) for user %s", 
                                     createdDeals.size(), dealRequest.getName());
        
        return new DealResponseDto(message, createdDeals);
    }
    
    /**
     * Initialize a deal with just contact number
     * If a deal with the same contact number already exists, it updates the updated_at timestamp
     * Otherwise, it creates a new basic deal entry that can be updated later with full details
     * @param dealInitRequest the request containing only contact number
     * @return the deal ID (existing or newly created)
     */
    @CacheEvict(value = "deals", allEntries = true)
    public Integer initializeDeal(DealInitRequestDto dealInitRequest) {
        // Check if a deal with the same contact number already exists
        List<Deal> existingDeals = dealRepository.findByContactNumber(dealInitRequest.getContactNumber());
        
        if (!existingDeals.isEmpty()) {
            // If deals exist with the same contact number, update the first one's timestamp
            Deal existingDeal = existingDeals.get(0);
            // The @UpdateTimestamp annotation will automatically update the updatedAt field
            Deal updatedDeal = dealRepository.save(existingDeal);
            return updatedDeal.getId();
        } else {
            // Create a new basic deal with just contact number and placeholder values
            Deal deal = new Deal(
                "TBD", // Placeholder for name - will be updated later
                dealInitRequest.getContactNumber(),
                "TBD", // Placeholder for category - will be updated later
                null, // Event date - will be updated later
                null, // Venue - will be updated later
                null, // Budget - will be updated later
                null  // Expected gathering - will be updated later
            );
            
            Deal savedDeal = dealRepository.save(deal);
            return savedDeal.getId();
        }
    }
    
    /**
     * Get all deals
     * @return list of all deals
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "deals", key = "'all'")
    public List<DealResponseDto.DealDto> getAllDeals() {
        List<Deal> deals = dealRepository.findAll();
        return deals.stream()
                   .map(this::convertToDealDto)
                   .toList();
    }
    
    /**
     * Get deals by user name
     * @param userName the user name to search for
     * @return list of deals for the given user
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "deals", key = "'user_' + #userName")
    public List<DealResponseDto.DealDto> getDealsByUserName(String userName) {
        List<Deal> deals = dealRepository.findByUserName(userName);
        return deals.stream()
                   .map(this::convertToDealDto)
                   .toList();
    }
    
    /**
     * Get deals by contact number
     * @param contactNumber the contact number to search for
     * @return list of deals for the given contact number
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "deals", key = "'contact_' + #contactNumber")
    public List<DealResponseDto.DealDto> getDealsByContactNumber(String contactNumber) {
        List<Deal> deals = dealRepository.findByContactNumber(contactNumber);
        return deals.stream()
                   .map(this::convertToDealDto)
                   .toList();
    }
    
    /**
     * Get deals by category
     * @param category the category to search for
     * @return list of deals for the given category
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "deals", key = "'category_' + #category")
    public List<DealResponseDto.DealDto> getDealsByCategory(String category) {
        List<Deal> deals = dealRepository.findByCategory(category);
        return deals.stream()
                   .map(this::convertToDealDto)
                   .toList();
    }
    
    /**
     * Get a single deal by ID
     * @param id the deal ID
     * @return the deal if found, null otherwise
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "deals", key = "'deal_' + #id")
    public DealResponseDto.DealDto getDealById(Integer id) {
        return dealRepository.findById(id)
                           .map(this::convertToDealDto)
                           .orElse(null);
    }
    
    /**
     * Update a deal by ID
     * @param id the deal ID
     * @param dealRequest the updated deal data
     * @return the updated deal
     */
    @CacheEvict(value = "deals", allEntries = true)
    public DealResponseDto.DealDto updateDeal(Integer id, DealRequestDto dealRequest) {
        Deal existingDeal = dealRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Deal not found with id: " + id));
        
        // Update the deal with new data (assuming single category for update)
        if (!dealRequest.getCategories().isEmpty()) {
            DealRequestDto.CategoryDto category = dealRequest.getCategories().get(0);
            existingDeal.setUserName(dealRequest.getName());
            existingDeal.setContactNumber(dealRequest.getContactNumber());
            existingDeal.setCategory(category.getName());
            existingDeal.setEventDate(category.getEventDate());
            existingDeal.setVenue(category.getVenue());
            existingDeal.setBudget(category.getBudget());
            existingDeal.setExpectedGathering(category.getExpectedGathering());
        }
        
        Deal updatedDeal = dealRepository.save(existingDeal);
        return convertToDealDto(updatedDeal);
    }
    
    /**
     * Update a deal by ID without requiring contact number
     * This is used for the two-step process where contact number was already set during initialization
     * Creates multiple deal entries for each category provided, all using the same contact number
     * @param id the deal ID
     * @param dealUpdateRequest the updated deal data (without contact number)
     * @return the first updated deal (for backward compatibility)
     */
    @CacheEvict(value = "deals", allEntries = true)
    public DealResponseDto.DealDto updateDealWithoutContactNumber(Integer id, DealUpdateRequestDto dealUpdateRequest) {
        Deal existingDeal = dealRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Deal not found with id: " + id));
        
        // Check if this deal was initialized (has placeholder values)
        if (!"TBD".equals(existingDeal.getUserName()) || !"TBD".equals(existingDeal.getCategory())) {
            throw new RuntimeException("Deal with id " + id + " has already been fully configured");
        }
        
        if (dealUpdateRequest.getCategories().isEmpty()) {
            throw new RuntimeException("At least one category is required");
        }
        
        String contactNumber = existingDeal.getContactNumber();
        String userName = dealUpdateRequest.getName();
        Deal firstUpdatedDeal = null;
        
        // Create a new deal entry for each category
        for (DealUpdateRequestDto.CategoryDto category : dealUpdateRequest.getCategories()) {
            Deal newDeal = new Deal(
                userName,
                contactNumber, // Use the same contact number from the initialized deal
                category.getName(),
                category.getEventDate(),
                category.getVenue(),
                category.getBudget(),
                category.getExpectedGathering()
            );
            
            Deal savedDeal = dealRepository.save(newDeal);
            
            // Keep reference to the first deal for return value
            if (firstUpdatedDeal == null) {
                firstUpdatedDeal = savedDeal;
            }
        }
        
        // Delete the original initialized deal since we've created new ones
        dealRepository.delete(existingDeal);
        
        return convertToDealDto(firstUpdatedDeal);
    }
    
    /**
     * Delete a deal by ID
     * @param id the deal ID
     * @return true if deleted, false if not found
     */
    @CacheEvict(value = "deals", allEntries = true)
    public boolean deleteDeal(Integer id) {
        if (dealRepository.existsById(id)) {
            dealRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * Delete all deals for a specific user
     * @param userName the user name
     * @return number of deals deleted
     */
    @CacheEvict(value = "deals", allEntries = true)
    public int deleteDealsByUserName(String userName) {
        List<Deal> deals = dealRepository.findByUserName(userName);
        dealRepository.deleteAll(deals);
        return deals.size();
    }
    
    /**
     * Convert Deal entity to DealDto
     * @param deal the deal entity
     * @return DealDto
     */
    private DealResponseDto.DealDto convertToDealDto(Deal deal) {
        return new DealResponseDto.DealDto(
            deal.getId(),
            deal.getUserName(),
            deal.getContactNumber(),
            deal.getCategory(),
            deal.getEventDate() != null ? deal.getEventDate().format(DATE_FORMATTER) : null,
            deal.getVenue(),
            deal.getBudget() != null ? deal.getBudget().toString() : null,
            deal.getExpectedGathering(),
            deal.getCreatedAt() != null ? deal.getCreatedAt().format(DATETIME_FORMATTER) : null,
            deal.getUpdatedAt() != null ? deal.getUpdatedAt().format(DATETIME_FORMATTER) : null
        );
    }
}
