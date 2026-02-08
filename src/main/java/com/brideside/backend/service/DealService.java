package com.brideside.backend.service;

import com.brideside.backend.dto.DealRequestDto;
import com.brideside.backend.dto.DealResponseDto;
import com.brideside.backend.dto.DealInitRequestDto;
import com.brideside.backend.dto.DealUpdateRequestDto;
import com.brideside.backend.entity.Deal;
import com.brideside.backend.entity.Person;
import com.brideside.backend.enums.CreatedBy;
import com.brideside.backend.enums.DealStatus;
import com.brideside.backend.enums.DealSubSource;
import com.brideside.backend.repository.DealRepository;
import com.brideside.backend.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private PersonRepository personRepository;
    
    @Autowired
    private WhatsAppService whatsAppService;
    
    // Default values for deal creation
    private static final Long DEFAULT_PIPELINE_ID = 67L;
    private static final Long DEFAULT_ORGANIZATION_ID = 68L;
    private static final DealStatus DEFAULT_STATUS = DealStatus.IN_PROGRESS;
    private static final Long DEFAULT_CATEGORY_ID = 3L;
    private static final String DEFAULT_DEAL_SOURCE = "DIRECT";
    private static final DealSubSource DEFAULT_DEAL_SUB_SOURCE = DealSubSource.LANDING_PAGE;
    private static final CreatedBy DEFAULT_CREATED_BY = CreatedBy.USER;
    private static final String DEFAULT_CREATED_BY_NAME = "Saloni";
    private static final Long DEFAULT_CREATED_BY_USER_ID = 69L;
    private static final Long DEFAULT_STAGE_ID = 338L;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Default values for person creation
    private static final String DEFAULT_PERSON_SOURCE = "DIRECT";
    private static final DealSubSource DEFAULT_PERSON_SUB_SOURCE = DealSubSource.LANDING_PAGE;
    private static final Long DEFAULT_PERSON_ORGANIZATION_ID = 68L;
    private static final Long DEFAULT_PERSON_OWNER_ID = 69L;
    private static final Long DEFAULT_PERSON_CATEGORY_ID = 3L;
    
    /**
     * Create multiple deals from a single request
     * Each category in the request will create a separate deal entry
     * @param dealRequest the request containing user info and categories
     * @return response with created deals
     */
    @CacheEvict(value = "deals", allEntries = true)
    public DealResponseDto createDeals(DealRequestDto dealRequest) {
        List<DealResponseDto.DealDto> createdDeals = new ArrayList<>();
        
        // Create or get person for the contact
        LocalDate firstEventDate = dealRequest.getCategories().isEmpty() ? null : 
                dealRequest.getCategories().get(0).getEventDate();
        String firstVenue = dealRequest.getCategories().isEmpty() ? null : 
                dealRequest.getCategories().get(0).getVenue();
        Person person = createOrGetPerson(dealRequest.getName(), dealRequest.getContactNumber(), 
                firstVenue, firstEventDate);
        
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
            
            // Set required fields with default values
            setDefaultDealFields(deal);
            
            // Set person_id
            deal.setPersonId(person.getId());
            
            Deal savedDeal = dealRepository.save(deal);
            createdDeals.add(convertToDealDto(savedDeal));
        }
        
        String message = String.format("Successfully created %d deal(s) for user %s", 
                                     createdDeals.size(), dealRequest.getName());
        
        return new DealResponseDto(message, createdDeals);
    }
    
    /**
     * Create or get a person for the given contact information
     * @param name the person's name
     * @param contactNumber the contact number
     * @param venue the venue (optional)
     * @param eventDate the event date (optional)
     * @return the created or existing person
     */
    private Person createOrGetPerson(String name, String contactNumber, String venue, LocalDate eventDate) {
        // Check if person already exists with this phone number
        Person existingPerson = personRepository.findByPhoneAndIsDeleted(contactNumber, false)
                .orElse(null);
        
        if (existingPerson != null) {
            logger.info("Found existing person with ID: {} for phone: {}", existingPerson.getId(), contactNumber);
            return existingPerson;
        }
        
        // Create new person
        Person person = new Person();
        person.setName(name);
        person.setPhone(contactNumber);
        person.setPhoneNum(contactNumber);
        person.setSource(DEFAULT_PERSON_SOURCE);
        person.setSubSource(DEFAULT_PERSON_SUB_SOURCE);
        person.setVenue(venue);
        
        // Convert event date to string format (yyyy-MM-dd)
        if (eventDate != null) {
            person.setWeddingDate(eventDate.format(DATE_FORMATTER));
        }
        
        person.setLeadDate(LocalDate.now());
        person.setOrganizationId(DEFAULT_PERSON_ORGANIZATION_ID);
        person.setOwnerId(DEFAULT_PERSON_OWNER_ID);
        person.setCategoryId(DEFAULT_PERSON_CATEGORY_ID);
        person.setIsDeleted(false);
        
        Person savedPerson = personRepository.save(person);
        logger.info("Created new person with ID: {} for phone: {}", savedPerson.getId(), contactNumber);
        return savedPerson;
    }
    
    /**
     * Set default fields for a deal
     * @param deal the deal to set fields on
     */
    private void setDefaultDealFields(Deal deal) {
        deal.setPipelineId(DEFAULT_PIPELINE_ID);
        deal.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        deal.setStatus(DEFAULT_STATUS);
        deal.setCategoryId(DEFAULT_CATEGORY_ID);
        deal.setDealSource(DEFAULT_DEAL_SOURCE);
        deal.setDealSubSource(DEFAULT_DEAL_SUB_SOURCE);
        deal.setCreatedBy(DEFAULT_CREATED_BY);
        deal.setCreatedByName(DEFAULT_CREATED_BY_NAME);
        deal.setCreatedByUserId(DEFAULT_CREATED_BY_USER_ID);
        deal.setStageId(DEFAULT_STAGE_ID);
        // Set value from budget if budget is provided, otherwise set to ZERO
        if (deal.getBudget() != null) {
            deal.setValue(deal.getBudget());
        } else if (deal.getValue() == null) {
            deal.setValue(BigDecimal.ZERO);
        }
        // Set event_dates from event_date if event_date is provided
        if (deal.getEventDate() != null) {
            List<LocalDate> eventDates = new ArrayList<>();
            eventDates.add(deal.getEventDate());
            deal.setEventDates(eventDates);
        }
    }
    
    /**
     * Initialize a deal with just contact number
     * If a deal with the same contact number already exists, it updates the updated_at timestamp
     * Otherwise, it creates a new basic deal entry that can be updated later with full details
     * @param dealInitRequest the request containing only contact number
     * @return the deal ID (existing or newly created)
     */
    @Transactional
    @CacheEvict(value = "deals", allEntries = true)
    public Integer initializeDeal(DealInitRequestDto dealInitRequest) {
        // Check if a deal with the same contact number already exists
        List<Deal> existingDeals = dealRepository.findByContactNumber(dealInitRequest.getContactNumber());
        
        logger.info("Checking for existing deals with contact number: {}, found: {}", 
                   dealInitRequest.getContactNumber(), existingDeals.size());
        
        if (!existingDeals.isEmpty()) {
            // If deals exist with the same contact number, update the first one's timestamp
            Deal existingDeal = existingDeals.get(0);
            logger.info("Found existing deal with ID: {}, contact_number: {}", 
                       existingDeal.getId(), existingDeal.getContactNumber());
            
            // Ensure default fields are set if missing
            if (existingDeal.getStatus() == null) {
                setDefaultDealFields(existingDeal);
            }
            
            // The @UpdateTimestamp annotation will automatically update the updatedAt field
            Deal updatedDeal = dealRepository.save(existingDeal);
            logger.info("Updated existing deal {} timestamp", updatedDeal.getId());
            return updatedDeal.getId();
        } else {
            // Create or get person for the contact
            Person person = createOrGetPerson("TBS", dealInitRequest.getContactNumber(), null, null);
            
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
            
            // Set required fields with default values
            setDefaultDealFields(deal);
            
            // Set person_id
            deal.setPersonId(person.getId());
            
            Deal savedDeal = dealRepository.save(deal);
            logger.info("Created new deal with ID: {}", savedDeal.getId());
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
            
            // Create or get person for the contact
            Person person = createOrGetPerson(dealRequest.getName(), dealRequest.getContactNumber(), 
                    category.getVenue(), category.getEventDate());
            existingDeal.setPersonId(person.getId());
            
            existingDeal.setUserName(dealRequest.getName());
            existingDeal.setContactNumber(dealRequest.getContactNumber());
            existingDeal.setCategory(category.getName());
            existingDeal.setEventDate(category.getEventDate());
            existingDeal.setVenue(category.getVenue());
            existingDeal.setBudget(category.getBudget());
            existingDeal.setExpectedGathering(category.getExpectedGathering());
            // Set value from budget
            if (category.getBudget() != null) {
                existingDeal.setValue(category.getBudget());
            }
            // Set event_dates from event_date
            if (category.getEventDate() != null) {
                List<LocalDate> eventDates = new ArrayList<>();
                eventDates.add(category.getEventDate());
                existingDeal.setEventDates(eventDates);
            }
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
        
        // Create or get person for the contact
        DealUpdateRequestDto.CategoryDto firstCategory = dealUpdateRequest.getCategories().get(0);
        Person person = createOrGetPerson(userName, contactNumber, firstCategory.getVenue(), firstCategory.getEventDate());
        
        // Update person with latest information if it was a placeholder
        if ("TBS".equals(person.getName()) || person.getName().startsWith("TBS")) {
            person.setName(userName);
            person.setVenue(firstCategory.getVenue());
            if (firstCategory.getEventDate() != null) {
                person.setWeddingDate(firstCategory.getEventDate().format(DATE_FORMATTER));
            }
            personRepository.save(person);
        }
        
        // Update the original deal with the first category (primary deal)
        
        // Update the existing deal with real data
        existingDeal.setUserName(userName);
        existingDeal.setCategory(firstCategory.getName());
        existingDeal.setEventDate(firstCategory.getEventDate());
        existingDeal.setVenue(firstCategory.getVenue());
        existingDeal.setBudget(firstCategory.getBudget());
        existingDeal.setExpectedGathering(firstCategory.getExpectedGathering());
        // Set value from budget
        if (firstCategory.getBudget() != null) {
            existingDeal.setValue(firstCategory.getBudget());
        }
        // Set event_dates from event_date
        if (firstCategory.getEventDate() != null) {
            List<LocalDate> eventDates = new ArrayList<>();
            eventDates.add(firstCategory.getEventDate());
            existingDeal.setEventDates(eventDates);
        }
        
        // Ensure default fields are set if missing
        if (existingDeal.getStatus() == null) {
            setDefaultDealFields(existingDeal);
        }
        
        // Set person_id
        existingDeal.setPersonId(person.getId());
        
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
                
                // Set required fields with default values
                setDefaultDealFields(additionalDeal);
                
                // Set person_id
                additionalDeal.setPersonId(person.getId());
                
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
