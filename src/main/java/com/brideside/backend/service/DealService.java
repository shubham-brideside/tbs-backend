package com.brideside.backend.service;

import com.brideside.backend.dto.DealRequestDto;
import com.brideside.backend.dto.DealResponseDto;
import com.brideside.backend.dto.DealInitRequestDto;
import com.brideside.backend.dto.DealUpdateRequestDto;
import com.brideside.backend.entity.Deal;
import com.brideside.backend.entity.Contact;
import com.brideside.backend.repository.DealRepository;
import com.brideside.backend.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(DealService.class);
    
    @Autowired
    private DealRepository dealRepository;
    
    @Autowired
    private ContactRepository contactRepository;
    
    @Autowired
    private PipedriveService pipedriveService;
    
    @Autowired
    private WhatsAppService whatsAppService;
    
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
        
        logger.info("Checking for existing deals with contact number: {}, found: {}", 
                   dealInitRequest.getContactNumber(), existingDeals.size());
        
        if (!existingDeals.isEmpty()) {
            // If deals exist with the same contact number, update the first one's timestamp
            Deal existingDeal = existingDeals.get(0);
            logger.info("Found existing deal with ID: {}, contact_number: {}, pipedrive_deal_id: {}, contact_id: {}", 
                       existingDeal.getId(), existingDeal.getContactNumber(), 
                       existingDeal.getPipedriveDealId(), existingDeal.getContactId());
            
            // If existing deal doesn't have Pipedrive IDs, create them now
            if (existingDeal.getPipedriveDealId() == null || existingDeal.getContactId() == null) {
                logger.info("Existing deal {} doesn't have Pipedrive IDs. Creating them now...", existingDeal.getId());
                try {
                    // Get or create contact in Pipedrive
                    Contact contact = null;
                    if (existingDeal.getContactId() != null) {
                        contact = contactRepository.findById(existingDeal.getContactId()).orElse(null);
                    }
                    
                    if (contact == null || contact.getPipedriveContactId() == null) {
                        // Create new contact in Pipedrive
                        contact = pipedriveService.createPerson("TBS", existingDeal.getContactNumber());
                        existingDeal.setContactId(contact.getId());
                    }
                    
                    // Create deal in Pipedrive if missing
                    if (existingDeal.getPipedriveDealId() == null) {
                        String pipedriveDealId = pipedriveService.createDeal(contact, "TBS Deal", 0);
                        existingDeal.setPipedriveDealId(pipedriveDealId);
                        logger.info("Successfully created Pipedrive deal {} for existing deal {}", pipedriveDealId, existingDeal.getId());
                    }
                } catch (Exception e) {
                    logger.error("Error creating Pipedrive IDs for existing deal {}: {}", existingDeal.getId(), e.getMessage(), e);
                    // Continue and save the deal anyway
                }
            }
            
            // The @UpdateTimestamp annotation will automatically update the updatedAt field
            Deal updatedDeal = dealRepository.save(existingDeal);
            logger.info("Updated existing deal {} timestamp", updatedDeal.getId());
            return updatedDeal.getId();
        } else {
            try {
                // Create contact in Pipedrive
                Contact contact = pipedriveService.createPerson("TBS", dealInitRequest.getContactNumber());
                
                // Create deal in Pipedrive
                String pipedriveDealId = pipedriveService.createDeal(contact, "TBS Deal", 0);
                
                // Create a new basic deal with just contact number and placeholder values
                Deal deal = new Deal(
                    "TBS", // Placeholder for name - will be updated later
                    dealInitRequest.getContactNumber(),
                    "TBS", // Placeholder for category - will be updated later
                    null, // Event date - will be updated later
                    null, // Venue - will be updated later
                    null, // Budget - will be updated later
                    null  // Expected gathering - will be updated later
                );
                // Store the contact ID and Pipedrive deal ID
                deal.setContactId(contact.getId());
                deal.setPipedriveDealId(pipedriveDealId);
                
                Deal savedDeal = dealRepository.save(deal);
                return savedDeal.getId();
                
            } catch (Exception e) {
                logger.error("Error creating deal in Pipedrive for contact: {}. Error: {}. Creating deal locally without Pipedrive integration.", 
                           dealInitRequest.getContactNumber(), e.getMessage(), e);
                // If Pipedrive integration fails, still create the deal locally
                // This ensures the system remains functional even if Pipedrive is down
                Deal deal = new Deal(
                    "TBS", // Placeholder for name - will be updated later
                    dealInitRequest.getContactNumber(),
                    "TBS", // Placeholder for category - will be updated later
                    null, // Event date - will be updated later
                    null, // Venue - will be updated later
                    null, // Budget - will be updated later
                    null  // Expected gathering - will be updated later
                );
                
                Deal savedDeal = dealRepository.save(deal);
                logger.warn("Deal created locally (ID: {}) without Pipedrive integration due to error", savedDeal.getId());
                return savedDeal.getId();
            }
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
     * Updates the original deal with the first category and creates additional deals for remaining categories
     * @param id the deal ID
     * @param dealUpdateRequest the updated deal data (without contact number)
     * @return the updated original deal
     */
    @CacheEvict(value = "deals", allEntries = true)
    public DealResponseDto.DealDto updateDealWithoutContactNumber(Integer id, DealUpdateRequestDto dealUpdateRequest) {
        Deal existingDeal = dealRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Deal not found with id: " + id));
        
        // Check if this deal was initialized (has placeholder values)
        if (!"TBS".equals(existingDeal.getUserName()) || !"TBS".equals(existingDeal.getCategory())) {
            throw new RuntimeException("Deal with id " + id + " has already been fully configured");
        }
        
        if (dealUpdateRequest.getCategories().isEmpty()) {
            throw new RuntimeException("At least one category is required");
        }
        
        String contactNumber = existingDeal.getContactNumber();
        String userName = dealUpdateRequest.getName();
        
        // Get the contact from the existing deal
        Contact contact = null;
        if (existingDeal.getContactId() != null) {
            contact = contactRepository.findById(existingDeal.getContactId()).orElse(null);
        }
        
        // If no contact exists, create one in Pipedrive
        if (contact == null) {
            try {
                contact = pipedriveService.createPerson(userName, contactNumber);
                // Update the existing deal with the contact ID
                existingDeal.setContactId(contact.getId());
                dealRepository.save(existingDeal);
            } catch (Exception e) {
                logger.error("Error creating contact in Pipedrive for contact: {}", contactNumber, e);
                // If Pipedrive fails, continue without Pipedrive integration
                contact = null;
            }
        } else {
            // Update contact name in Pipedrive if it was "TBS" or starts with "TBS_"
            if (contact.getContactName().startsWith("TBS")) {
                try {
                    // Update contact name locally with unique format
                    String uniqueContactName = userName + "_" + contactNumber;
                    contact.setContactName(uniqueContactName);
                    contactRepository.save(contact);
                    
                    // Update person's first name in Pipedrive
                    if (contact.getPipedriveContactId() != null) {
                        logger.info("Updating person {} first_name in Pipedrive to: {}", contact.getPipedriveContactId(), userName);
                        pipedriveService.updatePersonFirstName(contact.getPipedriveContactId(), userName);
                        logger.info("Successfully updated person {} first_name in Pipedrive", contact.getPipedriveContactId());
                    } else {
                        logger.warn("Cannot update person first_name: pipedriveContactId is null for contact {}", contact.getId());
                    }
                } catch (Exception e) {
                    logger.error("Error updating contact name for contact: {}", contactNumber, e);
                    // If update fails, continue with local update
                    String uniqueContactName = userName + "_" + contactNumber;
                    contact.setContactName(uniqueContactName);
                    contactRepository.save(contact);
                }
            } else {
                // Even if contact name doesn't start with "TBS", update the first name in Pipedrive with the new userName
                try {
                    if (contact.getPipedriveContactId() != null) {
                        logger.info("Updating person {} first_name in Pipedrive to: {} (contact name doesn't start with TBS)", 
                                   contact.getPipedriveContactId(), userName);
                        pipedriveService.updatePersonFirstName(contact.getPipedriveContactId(), userName);
                        logger.info("Successfully updated person {} first_name in Pipedrive", contact.getPipedriveContactId());
                    } else {
                        logger.warn("Cannot update person first_name: pipedriveContactId is null for contact {}", contact.getId());
                    }
                } catch (Exception e) {
                    logger.error("Error updating person first name in Pipedrive for contact: {}", contactNumber, e.getMessage(), e);
                    // Continue without Pipedrive update
                }
            }
        }
        
        // Update the original deal with the first category (primary deal)
        DealUpdateRequestDto.CategoryDto firstCategory = dealUpdateRequest.getCategories().get(0);
        
        // Update the existing deal with real data
        existingDeal.setUserName(userName);
        existingDeal.setCategory(firstCategory.getName());
        existingDeal.setEventDate(firstCategory.getEventDate());
        existingDeal.setVenue(firstCategory.getVenue());
        existingDeal.setBudget(firstCategory.getBudget());
        existingDeal.setExpectedGathering(firstCategory.getExpectedGathering());
        
        // Update Pipedrive deal if contact is available
        if (contact != null && existingDeal.getPipedriveDealId() != null) {
            try {
                logger.info("Updating Pipedrive deal {} with userName: {}, category: {}", 
                           existingDeal.getPipedriveDealId(), userName, firstCategory.getName());
                // Update the existing Pipedrive deal with custom fields including full name and title
                pipedriveService.updateDealCustomFields(existingDeal.getPipedriveDealId(), 
                    firstCategory.getName(), 
                    firstCategory.getEventDate(), 
                    firstCategory.getVenue(),
                    userName,
                    firstCategory.getBudget());
                logger.info("Successfully updated Pipedrive deal {} with user details", existingDeal.getPipedriveDealId());
            } catch (Exception e) {
                logger.error("Error updating Pipedrive deal {}: {}", existingDeal.getPipedriveDealId(), e.getMessage(), e);
                // If Pipedrive fails, continue without Pipedrive integration
            }
        } else {
            if (contact == null) {
                logger.warn("Cannot update Pipedrive deal {}: Contact is null", existingDeal.getPipedriveDealId());
            }
            if (existingDeal.getPipedriveDealId() == null) {
                logger.warn("Cannot update Pipedrive deal: pipedriveDealId is null for deal {}", existingDeal.getId());
            }
        }
        
        // Save the updated deal to database
        logger.info("Saving updated deal {} to database with userName: {}", existingDeal.getId(), userName);
        Deal updatedDeal = dealRepository.save(existingDeal);
        logger.info("Successfully saved deal {} to database", updatedDeal.getId());
        
        // If there are additional categories, create separate deals for them
        if (dealUpdateRequest.getCategories().size() > 1) {
            for (int i = 1; i < dealUpdateRequest.getCategories().size(); i++) {
                DealUpdateRequestDto.CategoryDto category = dealUpdateRequest.getCategories().get(i);
                
                Deal additionalDeal = new Deal(
                    userName,
                    contactNumber,
                    category.getName(),
                    category.getEventDate(),
                    category.getVenue(),
                    category.getBudget(),
                    category.getExpectedGathering()
                );
                
                // Set contact ID if available
                if (contact != null) {
                    additionalDeal.setContactId(contact.getId());
                }
                
                // Create additional deal in Pipedrive if contact is available
                if (contact != null) {
                    try {
                        String pipedriveDealId = pipedriveService.createDeal(contact, category.getName(), 
                            category.getBudget() != null ? category.getBudget().intValue() : 0);
                        additionalDeal.setPipedriveDealId(pipedriveDealId);
                        
                        // Update Pipedrive deal with custom fields including full name and title
                        pipedriveService.updateDealCustomFields(pipedriveDealId, 
                            category.getName(), 
                            category.getEventDate(), 
                            category.getVenue(),
                            userName,
                            category.getBudget());
                    } catch (Exception e) {
                        logger.error("Error creating additional Pipedrive deal for category: {}", category.getName(), e);
                        // If Pipedrive fails, continue without Pipedrive integration
                    }
                }
                
                dealRepository.save(additionalDeal);
            }
        }
        
        // Send WhatsApp confirmation message
        try {
            whatsAppService.sendDealConfirmation(
                contactNumber,
                userName,
                dealUpdateRequest.getCategories(),
                firstCategory.getEventDate(),
                firstCategory.getVenue()
            );
            logger.info("WhatsApp confirmation sent successfully to: {}", contactNumber);
        } catch (Exception e) {
            logger.error("Failed to send WhatsApp confirmation to: {}. Error: {}", contactNumber, e.getMessage());
            // Don't fail the deal update if WhatsApp fails
        }
        
        return convertToDealDto(updatedDeal);
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
