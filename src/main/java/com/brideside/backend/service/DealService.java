package com.brideside.backend.service;

import com.brideside.backend.dto.DealRequestDto;
import com.brideside.backend.dto.DealResponseDto;
import com.brideside.backend.dto.DealInitRequestDto;
import com.brideside.backend.dto.DealInitResponseDto;
import com.brideside.backend.dto.DealUpdateRequestDto;
import com.brideside.backend.entity.Deal;
import com.brideside.backend.entity.Person;
import com.brideside.backend.enums.CreatedBy;
import com.brideside.backend.enums.DealStatus;
import com.brideside.backend.enums.DealSubSource;
import com.brideside.backend.entity.Organization;
import com.brideside.backend.repository.DealRepository;
import com.brideside.backend.repository.OrganizationRepository;
import com.brideside.backend.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DealService {
    
    private static final Logger logger = LoggerFactory.getLogger(DealService.class);
    
    @Autowired
    private DealRepository dealRepository;
    
    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private WhatsAppService whatsAppService;

    /**
     * FK to {@code brideside_vendors.id}. Override via {@code deal.defaults.contacted-to} or env {@code DEAL_DEFAULTS_CONTACTED_TO}.
     */
    @Value("${deal.defaults.contacted-to:34}")
    private Long defaultContactedTo;
    
    // Default values for deal creation
    private static final Long DEFAULT_PIPELINE_ID = 67L;
    private static final List<Long> DEFAULT_PIPELINE_HISTORY = List.of(67L);
    private static final Long DEFAULT_SOURCE_PIPELINE_ID = 67L;
    private static final Long DEFAULT_ORGANIZATION_ID = 68L;
    private static final DealStatus DEFAULT_STATUS = DealStatus.IN_PROGRESS;
    /** Default {@code deals.category_id}; matches {@code categories.id} for "Planning". */
    private static final Long DEFAULT_CATEGORY_ID = 4L;
    private static final String DEFAULT_CATEGORY_NAME = "Planning";
    private static final String DEFAULT_DEAL_SOURCE = "DIRECT";
    private static final DealSubSource DEFAULT_DEAL_SUB_SOURCE = DealSubSource.LANDING_PAGE;
    private static final CreatedBy DEFAULT_CREATED_BY = CreatedBy.BOT;
    private static final String DEFAULT_CREATED_BY_NAME = "BOT";
    private static final Long DEFAULT_STAGE_ID = 338L;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Default values for person creation
    private static final String DEFAULT_PERSON_SOURCE = "DIRECT";
    private static final DealSubSource DEFAULT_PERSON_SUB_SOURCE = DealSubSource.LANDING_PAGE;
    private static final Long DEFAULT_PERSON_ORGANIZATION_ID = 68L;
    private static final Long DEFAULT_PERSON_CATEGORY_ID = 4L;
    
    /**
     * Create multiple deals from a single request
     * Each category in the request will create a separate deal entry
     * @param dealRequest the request containing user info and categories
     * @return response with created deals
     */
    @CacheEvict(value = "deals", allEntries = true)
    public DealResponseDto createDeals(DealRequestDto dealRequest) {
        List<DealResponseDto.DealDto> createdDeals = new ArrayList<>();
        List<DealRequestDto.CategoryDto> categories = categoriesOrDefault(dealRequest.getCategories());
        
        // Create or get person for the contact
        LocalDate firstEventDate = categories.isEmpty() ? null : categories.get(0).getEventDate();
        String firstVenue = categories.isEmpty() ? null : categories.get(0).getVenue();
        Person person = createOrGetPerson(dealRequest.getName(), dealRequest.getContactNumber(), 
                firstVenue, firstEventDate);

        Optional<Deal> reusableDeal = findReusableDeal(dealRequest.getContactNumber());
        
        // Create a separate deal for each category
        for (int i = 0; i < categories.size(); i++) {
            DealRequestDto.CategoryDto category = categories.get(i);
            Deal deal;

            if (i == 0 && reusableDeal.isPresent()) {
                deal = reusableDeal.get();
                deal.setUserName(dealRequest.getName());
                deal.setContactNumber(dealRequest.getContactNumber());
                deal.setEventDate(category.getEventDate());
                deal.setVenue(category.getVenue());
                deal.setBudget(category.getBudget());
                deal.setExpectedGathering(category.getExpectedGathering());
                if (category.getBudget() != null) {
                    deal.setValue(category.getBudget());
                }
                if (category.getEventDate() != null) {
                    List<LocalDate> eventDates = new ArrayList<>();
                    eventDates.add(category.getEventDate());
                    deal.setEventDates(eventDates);
                }
                deal.setCategoryId(resolveCategoryIdFromName(category.getName()));
                deal.setPersonId(person.getId());
            } else {
                deal = buildNewDeal(
                        dealRequest.getName(),
                        dealRequest.getContactNumber(),
                        category.getEventDate(),
                        category.getVenue(),
                        category.getBudget(),
                        category.getExpectedGathering(),
                        person.getId());
                deal.setCategoryId(resolveCategoryIdFromName(category.getName()));
            }
            
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
        Optional<Person> existingPerson = personRepository
                .findFirstByPhoneAndIsDeletedOrderByCreatedAtDesc(contactNumber, false);

        if (existingPerson.isPresent()) {
            Person person = existingPerson.get();
            logger.info("Reusing latest person with ID: {} for phone: {}", person.getId(), contactNumber);
            return person;
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
        person.setOwnerId(resolveOwnerIdFromOrganization(DEFAULT_PERSON_ORGANIZATION_ID));
        person.setCategoryId(DEFAULT_PERSON_CATEGORY_ID);
        person.setIsDeleted(false);
        
        Person savedPerson = personRepository.save(person);
        logger.info("Created new person with ID: {} for phone: {}", savedPerson.getId(), contactNumber);
        return savedPerson;
    }
    
    /**
     * Maps planning-form {@code categories.name} to {@code deals.category_id}:
     * 1 Photography, 2 Makeup, 3 Planning and Decor, 4 Planning.
     */
    private Long resolveCategoryIdFromName(String name) {
        if (name == null || name.isBlank()) {
            return DEFAULT_CATEGORY_ID;
        }
        String n = name.trim();
        if (n.equalsIgnoreCase("Photography") || n.equalsIgnoreCase("Wedding Photography")) {
            return 1L;
        }
        if (n.equalsIgnoreCase("Makeup")) {
            return 2L;
        }
        if (n.equalsIgnoreCase("Planning and Decor") || n.equalsIgnoreCase("Planning & Decor")) {
            return 3L;
        }
        if (n.equalsIgnoreCase("Planning")) {
            return 4L;
        }
        throw new IllegalArgumentException(
                "Unknown category name: \"" + name + "\". Use Photography, Makeup, Planning and Decor, or Planning.");
    }

    private List<DealRequestDto.CategoryDto> categoriesOrDefault(List<DealRequestDto.CategoryDto> categories) {
        if (categories == null || categories.isEmpty()) {
            DealRequestDto.CategoryDto defaultCategory = new DealRequestDto.CategoryDto();
            defaultCategory.setName(DEFAULT_CATEGORY_NAME);
            return List.of(defaultCategory);
        }
        return categories;
    }

    private List<DealUpdateRequestDto.CategoryDto> updateCategoriesOrDefault(List<DealUpdateRequestDto.CategoryDto> categories) {
        if (categories == null || categories.isEmpty()) {
            DealUpdateRequestDto.CategoryDto defaultCategory = new DealUpdateRequestDto.CategoryDto();
            defaultCategory.setName(DEFAULT_CATEGORY_NAME);
            return List.of(defaultCategory);
        }
        return categories;
    }
    
    /**
     * Set default fields for a deal
     * @param deal the deal to set fields on
     */
    private void setDefaultDealFields(Deal deal) {
        deal.setPipelineId(DEFAULT_PIPELINE_ID);
        deal.setPipelineHistory(new ArrayList<>(DEFAULT_PIPELINE_HISTORY));
        deal.setSourcePipelineId(DEFAULT_SOURCE_PIPELINE_ID);
        deal.setContactedTo(defaultContactedTo);
        deal.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        deal.setStatus(DEFAULT_STATUS);
        deal.setCategoryId(DEFAULT_CATEGORY_ID);
        deal.setDealSource(DEFAULT_DEAL_SOURCE);
        deal.setDealSubSource(DEFAULT_DEAL_SUB_SOURCE);
        deal.setCreatedBy(DEFAULT_CREATED_BY);
        deal.setCreatedByName(DEFAULT_CREATED_BY_NAME);
        deal.setCreatedByUserId(null);
        deal.setStageId(DEFAULT_STAGE_ID);
        deal.setIsDeleted(false);
        deal.setDealOwnerOverride(false);
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
        applyOwnerFromOrganization(deal);
    }

    /**
     * Sets {@link Deal#setOwnerId(Long)} from {@link Organization#getOwnerId()} for the deal's {@link Deal#getOrganizationId()}.
     */
    private void applyOwnerFromOrganization(Deal deal) {
        deal.setOwnerId(resolveOwnerIdFromOrganization(deal.getOrganizationId()));
    }

    private Long resolveOwnerIdFromOrganization(Long organizationId) {
        if (organizationId == null) {
            return null;
        }
        return organizationRepository.findById(organizationId)
                .map(Organization::getOwnerId)
                .orElse(null);
    }

    /**
     * Active landing-page lead still in progress (matches /init reuse rules).
     */
    private boolean isActiveLandingPageDeal(Deal deal) {
        return !Boolean.TRUE.equals(deal.getIsDeleted())
                && deal.getStatus() == DealStatus.IN_PROGRESS
                && deal.getDealSubSource() == DealSubSource.LANDING_PAGE;
    }

    private boolean shouldReuseExistingDeal(Deal deal) {
        return isActiveLandingPageDeal(deal);
    }

    private Optional<Deal> findReusableDeal(String contactNumber) {
        return dealRepository.findByContactNumberOrderByCreatedAtDesc(contactNumber).stream()
                .filter(this::shouldReuseExistingDeal)
                .findFirst();
    }

    private Deal buildNewDeal(String userName, String contactNumber, LocalDate eventDate,
                              String venue, BigDecimal budget, String expectedGathering, Long personId) {
        Deal deal = new Deal(userName, contactNumber, eventDate, venue, budget, expectedGathering);
        setDefaultDealFields(deal);
        deal.setPersonId(personId);
        return deal;
    }
    
    /**
     * Initialize a deal with just contact number
     * If a deal with the same contact number already exists, it updates the updated_at timestamp
     * Otherwise, it creates a new basic deal entry that can be updated later with full details
     * @param dealInitRequest the request containing only contact number
     * @return init result with deal id and flags for the frontend
     */
    @Transactional
    @CacheEvict(value = "deals", allEntries = true)
    public DealInitResponseDto initializeDeal(DealInitRequestDto dealInitRequest) {
        String contactNumber = dealInitRequest.getContactNumber();

        Optional<Deal> reusableDeal = findReusableDeal(contactNumber);
        if (reusableDeal.isPresent()) {
            Deal existingDeal = reusableDeal.get();
            logger.info("Reusing deal {} for contact {} (IN_PROGRESS + LANDING_PAGE)",
                    existingDeal.getId(), contactNumber);
            dealRepository.save(existingDeal);
            return DealInitResponseDto.fromDeal(
                    existingDeal.getId(), false, existingDeal.getUserName(), contactNumber);
        }

        logger.info("Creating new deal for contact {} — no reusable IN_PROGRESS LANDING_PAGE deal found",
                contactNumber);

        Person person = createOrGetPerson("TBS", contactNumber, null, null);
        Deal savedDeal = dealRepository.save(buildNewDeal("TBS", contactNumber, null, null, null, null, person.getId()));
        logger.info("Created new deal with ID: {}", savedDeal.getId());
        return DealInitResponseDto.fromDeal(savedDeal.getId(), true, savedDeal.getUserName(), contactNumber);
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
     * Get deals by category id
     * @param categoryId FK to {@code categories.id}
     * @return list of deals for the given category
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "deals", key = "'category_' + #categoryId")
    public List<DealResponseDto.DealDto> getDealsByCategoryId(Long categoryId) {
        List<Deal> deals = dealRepository.findByCategoryId(categoryId);
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
        List<DealRequestDto.CategoryDto> categories = categoriesOrDefault(dealRequest.getCategories());
        DealRequestDto.CategoryDto category = categories.get(0);
        
        // Create or get person for the contact
        Person person = createOrGetPerson(dealRequest.getName(), dealRequest.getContactNumber(), 
                category.getVenue(), category.getEventDate());
        existingDeal.setPersonId(person.getId());
        
        existingDeal.setUserName(dealRequest.getName());
        existingDeal.setContactNumber(dealRequest.getContactNumber());
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
        existingDeal.setCategoryId(resolveCategoryIdFromName(category.getName()));
        
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
        
        if (!isActiveLandingPageDeal(existingDeal)) {
            throw new RuntimeException("Deal with id " + id + " cannot be updated through this endpoint");
        }
        
        List<DealUpdateRequestDto.CategoryDto> categories = updateCategoriesOrDefault(dealUpdateRequest.getCategories());
        
        String contactNumber = existingDeal.getContactNumber();
        String userName = dealUpdateRequest.getName();
        
        DealUpdateRequestDto.CategoryDto firstCategory = categories.get(0);
        Person person = createOrGetPerson(userName, contactNumber, firstCategory.getVenue(), firstCategory.getEventDate());
        
        person.setName(userName);
        if (firstCategory.getVenue() != null) {
            person.setVenue(firstCategory.getVenue());
        }
        if (firstCategory.getEventDate() != null) {
            person.setWeddingDate(firstCategory.getEventDate().format(DATE_FORMATTER));
        }
        personRepository.save(person);
        
        // Update the original deal with the first category (primary deal)
        
        // Update the existing deal with real data
        existingDeal.setUserName(userName);
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
        
        setDefaultDealFields(existingDeal);
        existingDeal.setCategoryId(resolveCategoryIdFromName(firstCategory.getName()));
        
        // Set person_id
        existingDeal.setPersonId(person.getId());
        
        // Save the updated deal to database
        logger.info("Saving updated deal {} to database with userName: {}", existingDeal.getId(), userName);
        Deal updatedDeal = dealRepository.save(existingDeal);
        logger.info("Successfully saved deal {} to database", updatedDeal.getId());
        
        // If there are additional categories, create separate deals for them
        if (categories.size() > 1) {
            for (int i = 1; i < categories.size(); i++) {
                DealUpdateRequestDto.CategoryDto category = categories.get(i);
                
                Deal additionalDeal = buildNewDeal(
                    userName,
                    contactNumber,
                    category.getEventDate(),
                    category.getVenue(),
                    category.getBudget(),
                    category.getExpectedGathering(),
                    person.getId()
                );
                additionalDeal.setCategoryId(resolveCategoryIdFromName(category.getName()));
                
                dealRepository.save(additionalDeal);
            }
        }
        
        // Send WhatsApp confirmation message
        try {
            boolean sent = whatsAppService.sendDealConfirmation(
                contactNumber,
                userName,
                categories,
                firstCategory.getEventDate(),
                firstCategory.getVenue()
            );
            if (sent) {
                logger.info("WhatsApp confirmation sent successfully to: {}", contactNumber);
            } else {
                logger.warn("WhatsApp confirmation was NOT sent for {} — check logs above (Meta WhatsApp config).",
                        contactNumber);
            }
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
            deal.getCategoryId(),
            deal.getEventDate() != null ? deal.getEventDate().format(DATE_FORMATTER) : null,
            deal.getVenue(),
            deal.getBudget() != null ? deal.getBudget().toString() : null,
            deal.getExpectedGathering(),
            deal.getCreatedAt() != null ? deal.getCreatedAt().format(DATETIME_FORMATTER) : null,
            deal.getUpdatedAt() != null ? deal.getUpdatedAt().format(DATETIME_FORMATTER) : null
        );
    }
}
