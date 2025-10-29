package com.brideside.backend.service;

import com.brideside.backend.config.PipedriveProperties;
import com.brideside.backend.entity.Contact;
import com.brideside.backend.entity.Deal;
import com.brideside.backend.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PipedriveService {
    
    private static final Logger logger = LoggerFactory.getLogger(PipedriveService.class);
    
    @Autowired
    private PipedriveProperties pipedriveProperties;
    
    @Autowired
    private ContactRepository contactRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Create a person in Pipedrive
     * @param contactName the contact name
     * @param contactNumber the contact number
     * @return the created contact with Pipedrive ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Contact createPerson(String contactName, String contactNumber) {
        try {
            // Use phone number to make contactName unique in local database
            String uniqueContactName = contactName + "_" + contactNumber;
            
            // Always create a new person in Pipedrive for each call
            // This ensures we don't link to deleted contacts
            
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", contactName);
            requestBody.put("phone", contactNumber);
            requestBody.put("org_id", pipedriveProperties.getApi().getOrgId());
            
            // Add Person Source field (dropdown - Website option)
            String personSourceField = pipedriveProperties.getPerson().getCustomFields().getPersonSource();
            if (personSourceField != null && !personSourceField.isEmpty()) {
                // Use string value "Website" for the Person Source dropdown
                requestBody.put(personSourceField, "Website");
            } else {
                // Fallback to hardcoded field key if not configured
                requestBody.put("bb67874cea1b01f4eadb549eda0033ff530ea0ba", "Website");
            }
            
            // Make API call to Pipedrive
            String url = pipedriveProperties.getApi().getBaseUrl() + "/api/v1/persons?api_token=" + pipedriveProperties.getApi().getToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
                String pipedriveContactId = String.valueOf(responseData.get("id"));
                
                // Save contact to database with unique name (phone number appended)
                Contact contact = new Contact(uniqueContactName, pipedriveContactId);
                Contact savedContact = contactRepository.save(contact);
                
                logger.info("Successfully created person in Pipedrive with ID: {} for contact: {}", pipedriveContactId, contactName);
                return savedContact;
            } else {
                logger.error("Failed to create person in Pipedrive. Response: {}", response.getBody());
                throw new RuntimeException("Failed to create person in Pipedrive");
            }
            
        } catch (Exception e) {
            logger.error("Error creating person in Pipedrive for contact: {}", contactName, e);
            throw new RuntimeException("Error creating person in Pipedrive: " + e.getMessage());
        }
    }
    
    /**
     * Create a deal in Pipedrive
     * @param contact the contact associated with the deal
     * @param dealTitle the deal title
     * @param dealValue the deal value
     * @return the Pipedrive deal ID
     */
    public String createDeal(Contact contact, String dealTitle, Integer dealValue) {
        try {
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("title", dealTitle);
            requestBody.put("value", dealValue);
            requestBody.put("currency", "USD");
            requestBody.put("person_id", contact.getPipedriveContactId());
            requestBody.put("pipeline_id", pipedriveProperties.getDeal().getPipelineId());
            requestBody.put("org_id", pipedriveProperties.getApi().getOrgId());
            requestBody.put("status", "open");
            
            // Add Deal Source field to initial deal creation
            String dealSourceField = pipedriveProperties.getDeal().getCustomFields().getDealSource();
            if (dealSourceField != null && !dealSourceField.isEmpty()) {
                requestBody.put(dealSourceField, 105); // Website option ID for "TBS Landing Page"
            }
            
            // Make API call to Pipedrive
            String url = pipedriveProperties.getApi().getBaseUrl() + "/api/v1/deals?api_token=" + pipedriveProperties.getApi().getToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
                String pipedriveDealId = String.valueOf(responseData.get("id"));
                
                logger.info("Successfully created deal in Pipedrive with ID: {} for contact: {}", pipedriveDealId, contact.getContactName());
                return pipedriveDealId;
            } else {
                logger.error("Failed to create deal in Pipedrive. Response: {}", response.getBody());
                throw new RuntimeException("Failed to create deal in Pipedrive");
            }
            
        } catch (Exception e) {
            logger.error("Error creating deal in Pipedrive for contact: {}", contact.getContactName(), e);
            throw new RuntimeException("Error creating deal in Pipedrive: " + e.getMessage());
        }
    }
    
    /**
     * Update deal with custom fields in Pipedrive
     * @param pipedriveDealId the Pipedrive deal ID
     * @param category the event type/category
     * @param eventDate the event date
     * @param venue the venue
     * @param fullName the full name for the deal custom field
     * @param budget the budget value for the deal
     */
    public void updateDealCustomFields(String pipedriveDealId, String category, LocalDate eventDate, String venue, String fullName, BigDecimal budget) {
        try {
            // Prepare request body with custom fields
            Map<String, Object> requestBody = new HashMap<>();
            
            // Update the deal title to include the full name
            requestBody.put("title", fullName + " - " + category);
            
            requestBody.put(pipedriveProperties.getDeal().getCustomFields().getEventType(), category);
            requestBody.put(pipedriveProperties.getDeal().getCustomFields().getEventDate(), 
                          eventDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            requestBody.put(pipedriveProperties.getDeal().getCustomFields().getVenue(), venue);
            
            // Add Deal Source field
            String dealSourceField = pipedriveProperties.getDeal().getCustomFields().getDealSource();
            if (dealSourceField != null && !dealSourceField.isEmpty()) {
                requestBody.put(dealSourceField, 105); // Website option ID for "TBS Landing Page"
            }
            
            // Add Full Name custom field 
            requestBody.put("84ab8ec8732455ab7cf75f5661f2c027c7b1e5cd", fullName);
            
            // Add budget value if provided
            if (budget != null) {
                requestBody.put("value", budget.intValue());
            }
            
            // Make API call to Pipedrive
            String url = pipedriveProperties.getApi().getBaseUrl() + "/api/v1/deals/" + pipedriveDealId + "?api_token=" + pipedriveProperties.getApi().getToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Successfully updated deal {} with custom fields including Deal Source: TBS Landing Page", pipedriveDealId);
            } else {
                logger.error("Failed to update deal {} with custom fields. Response: {}", pipedriveDealId, response.getBody());
                throw new RuntimeException("Failed to update deal with custom fields in Pipedrive");
            }
            
        } catch (Exception e) {
            logger.error("Error updating deal {} with custom fields in Pipedrive", pipedriveDealId, e);
            throw new RuntimeException("Error updating deal with custom fields in Pipedrive: " + e.getMessage());
        }
    }
    
    /**
     * Update person name in Pipedrive
     * @param pipedriveContactId the Pipedrive contact ID
     * @param newName the new name for the person
     */
    public void updatePersonName(String pipedriveContactId, String newName) {
        try {
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", newName);
            
            // Make API call to Pipedrive
            String url = pipedriveProperties.getApi().getBaseUrl() + "/api/v1/persons/" + pipedriveContactId + "?api_token=" + pipedriveProperties.getApi().getToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Successfully updated person {} with name: {}", pipedriveContactId, newName);
            } else {
                logger.error("Failed to update person {} with name. Response: {}", pipedriveContactId, response.getBody());
                throw new RuntimeException("Failed to update person name in Pipedrive");
            }
            
        } catch (Exception e) {
            logger.error("Error updating person {} with name in Pipedrive", pipedriveContactId, e);
            throw new RuntimeException("Error updating person name in Pipedrive: " + e.getMessage());
        }
    }
    
    /**
     * Update person first name in Pipedrive
     * @param pipedriveContactId the Pipedrive contact ID
     * @param firstName the new first name for the person
     */
    public void updatePersonFirstName(String pipedriveContactId, String firstName) {
        try {
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("first_name", firstName);
            
            // Make API call to Pipedrive
            String url = pipedriveProperties.getApi().getBaseUrl() + "/api/v1/persons/" + pipedriveContactId + "?api_token=" + pipedriveProperties.getApi().getToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Successfully updated person {} with first_name: {}", pipedriveContactId, firstName);
            } else {
                logger.error("Failed to update person {} with first_name. Response: {}", pipedriveContactId, response.getBody());
                throw new RuntimeException("Failed to update person first name in Pipedrive");
            }
            
        } catch (Exception e) {
            logger.error("Error updating person {} with first_name in Pipedrive", pipedriveContactId, e);
            throw new RuntimeException("Error updating person first name in Pipedrive: " + e.getMessage());
        }
    }
}
