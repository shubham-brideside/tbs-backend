package com.brideside.backend.service;

import com.brideside.backend.config.WhatsAppProperties;
import com.brideside.backend.dto.DealUpdateRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    @Autowired
    private WhatsAppProperties whatsAppProperties;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Send WhatsApp confirmation after deal details are updated (Meta Cloud API).
     *
     * @return {@code true} if Meta accepted the message; {@code false} if skipped or failed
     */
    public boolean sendDealConfirmation(String contactNumber, String userName,
                                        List<DealUpdateRequestDto.CategoryDto> categories,
                                        LocalDate eventDate, String venue) {
        try {
            return sendTemplateMessage(contactNumber, "tbs_confirmation_message", "en", List.of());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid phone number format: {}", contactNumber, e);
            return false;
        } catch (Exception e) {
            logger.error("Failed to send WhatsApp message to: {}. Error: {}", contactNumber, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send a Meta WhatsApp template with optional body text parameters (in order).
     *
     * @param contactNumber raw or E.164 phone
     * @param templateName  approved template name in Meta Business Manager
     * @param languageCode  e.g. {@code en}, {@code en_US}
     * @param bodyTexts     one entry per {{1}}, {{2}}, … in the template body
     */
    public void sendTemplateMessageOrThrow(String contactNumber, String templateName, String languageCode,
                                           List<String> bodyTexts) {
        assertMetaConfigured();
        String phoneNumber = formatPhoneNumberForMeta(contactNumber);
        Map<String, Object> messageBody = buildMetaTemplateMessage(phoneNumber, templateName, languageCode, bodyTexts);
        sendMetaWhatsAppMessage(messageBody);
        logger.info("WhatsApp template '{}' sent to {}", templateName, maskPhone(phoneNumber));
    }

    public boolean isMetaConfigured() {
        return whatsAppProperties != null
                && whatsAppProperties.getBaseUrl() != null
                && whatsAppProperties.getPhoneNumberId() != null
                && whatsAppProperties.getAccessToken() != null;
    }

    private boolean sendTemplateMessage(String contactNumber, String templateName, String languageCode,
                                        List<String> bodyTexts) {
        if (!isMetaConfigured()) {
            logger.warn("WhatsApp (Meta) properties are not configured. Skipping WhatsApp message send.");
            return false;
        }
        if (restTemplate == null) {
            logger.warn("RestTemplate is not configured. Skipping WhatsApp message send.");
            return false;
        }
        sendTemplateMessageOrThrow(contactNumber, templateName, languageCode, bodyTexts);
        return true;
    }

    private void assertMetaConfigured() {
        if (!isMetaConfigured()) {
            throw new IllegalStateException("WhatsApp (Meta) is not configured (base URL, phone number id, access token).");
        }
        if (restTemplate == null) {
            throw new IllegalStateException("RestTemplate is not configured.");
        }
    }

    private Map<String, Object> buildMetaTemplateMessage(String phoneNumber, String templateName, String languageCode,
                                                         List<String> bodyTexts) {
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", phoneNumber);
        body.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", templateName);
        template.put("language", Map.of("code", languageCode));

        if (!CollectionUtils.isEmpty(bodyTexts)) {
            List<Map<String, Object>> parameters = new ArrayList<>();
            for (String text : bodyTexts) {
                Map<String, Object> p = new HashMap<>();
                p.put("type", "text");
                p.put("text", text != null ? text : "");
                parameters.add(p);
            }
            template.put("components", List.of(
                    Map.of("type", "body", "parameters", parameters)
            ));
        }

        body.put("template", template);
        return body;
    }

    private void sendMetaWhatsAppMessage(Map<String, Object> messageBody) {
        String url = whatsAppProperties.getBaseUrl() + "/"
                + whatsAppProperties.getPhoneNumberId() + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(whatsAppProperties.getAccessToken());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(messageBody, headers);

        logger.info("Sending WhatsApp message to URL: {}", url);
        logger.debug("WhatsApp message body: {}", messageBody);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>)
                restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            logger.info("WhatsApp message sent successfully. Response: {}", response.getBody());
        } else {
            logger.error("WhatsApp API returned unexpected status: {}. Response body: {}",
                    response.getStatusCode(), response.getBody());
            throw new RuntimeException("WhatsApp API returned status: " + response.getStatusCode());
        }
    }

    private String formatPhoneNumberForMeta(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");
        if (!cleaned.startsWith("91")) {
            logger.warn("Phone number doesn't start with 91 (India country code): {}", phoneNumber);
        }
        logger.info("Formatted phone number: {} -> {}", phoneNumber, cleaned);
        return cleaned;
    }

    private static String maskPhone(String digits) {
        if (digits == null || digits.length() < 6) {
            return "****";
        }
        return digits.substring(0, 4) + "****" + digits.substring(digits.length() - 2);
    }
}
