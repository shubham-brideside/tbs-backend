package com.brideside.backend.service;

import com.brideside.backend.config.WhatsAppProperties;
import com.brideside.backend.dto.DealUpdateRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WhatsAppService {
    
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);
    
    @Autowired
    private WhatsAppProperties whatsAppProperties;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Send WhatsApp confirmation message after deal details are updated
     */
    public void sendDealConfirmation(String contactNumber, String userName, 
                                    List<DealUpdateRequestDto.CategoryDto> categories,
                                    LocalDate eventDate, String venue) {
        try {
            String phoneNumber = formatPhoneNumber(contactNumber);
            logger.info("Preparing to send WhatsApp message to: {}", phoneNumber);
            
            // Build the message body
            Map<String, Object> messageBody = buildMessageBody(phoneNumber, userName, categories, eventDate, venue);
            
            // Send the message
            sendWhatsAppMessage(messageBody);
            
            logger.info("WhatsApp confirmation message sent successfully to: {}", phoneNumber);
            
        } catch (Exception e) {
            logger.error("Failed to send WhatsApp message to: {}", contactNumber, e);
            // Don't rethrow the exception to avoid breaking the deal update process
        }
    }
    
    /**
     * Build the WhatsApp message body
     */
    private Map<String, Object> buildMessageBody(String phoneNumber, String userName,
                                                  List<DealUpdateRequestDto.CategoryDto> categories,
                                                  LocalDate eventDate, String venue) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", phoneNumber);
        body.put("type", "template");
        
        // Template structure
        Map<String, Object> template = new HashMap<>();
        template.put("name", "hello_world");
        
        Map<String, String> language = new HashMap<>();
        language.put("code", "en_US");
        template.put("language", language);
        
        body.put("template", template);
        
        return body;
    }
    
    /**
     * Send WhatsApp message via Graph API
     */
    private void sendWhatsAppMessage(Map<String, Object> messageBody) {
        try {
            // Construct the API URL
            String url = whatsAppProperties.getBaseUrl() + "/" + 
                        whatsAppProperties.getPhoneNumberId() + "/messages";
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(whatsAppProperties.getAccessToken());
            
            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(messageBody, headers);
            
            // Make API call
            logger.info("Sending WhatsApp message to URL: {}", url);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                logger.info("WhatsApp message sent successfully. Response: {}", response.getBody());
            } else {
                logger.error("WhatsApp API returned unexpected status: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error sending WhatsApp message", e);
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage(), e);
        }
    }
    
    /**
     * Format phone number for WhatsApp API (remove + and spaces)
     * WhatsApp expects phone numbers in format: country code + number (e.g., 919304683214)
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        
        // Remove all non-numeric characters
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");
        
        // Ensure it starts with country code (for India it should start with 91)
        if (!cleaned.startsWith("91")) {
            logger.warn("Phone number doesn't start with 91 (India country code): {}", phoneNumber);
        }
        
        logger.info("Formatted phone number: {} -> {}", phoneNumber, cleaned);
        return cleaned;
    }
}

