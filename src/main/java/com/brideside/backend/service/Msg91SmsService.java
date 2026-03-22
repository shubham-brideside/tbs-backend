package com.brideside.backend.service;

import com.brideside.backend.config.OtpProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends OTP via MSG91 Flow API (India-focused transactional SMS).
 *
 * @see <a href="https://docs.msg91.com/">MSG91 docs</a>
 */
@Service
public class Msg91SmsService {

    private static final Logger log = LoggerFactory.getLogger(Msg91SmsService.class);
    private static final String FLOW_URL = "https://control.msg91.com/api/v5/flow/";

    private final OtpProperties otpProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public Msg91SmsService(OtpProperties otpProperties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.otpProperties = otpProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendOtp(String phoneDigits, String otpCode) {
        OtpProperties.Sms sms = otpProperties.getSms();
        if (sms.isSimulate()) {
            log.warn("SMS OTP simulate (no MSG91 call): +{} code={}", phoneDigits, otpCode);
            return;
        }
        if (!sms.isReady()) {
            throw new IllegalStateException(
                    "SMS OTP is not configured. Set otp.sms.enabled=true, MSG91_AUTH_KEY, MSG91_FLOW_TEMPLATE_ID, "
                            + "or for local dev only: OTP_SMS_SIMULATE=true or spring.profiles.active=dev.");
        }

        Map<String, Object> recipient = new HashMap<>();
        recipient.put("mobiles", "+" + phoneDigits);
        recipient.put(sms.getBodyVarKey(), otpCode);

        Map<String, Object> body = new HashMap<>();
        body.put("template_id", sms.getFlowTemplateId());
        body.put("short_url", "0");
        body.put("realTimeResponse", "1");
        body.put("recipients", List.of(recipient));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authkey", sms.getAuthKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.info("MSG91 Flow OTP request for +{}****", phoneDigits.length() > 4 ? phoneDigits.substring(0, 4) : "****");

        ResponseEntity<String> response = restTemplate.exchange(
                FLOW_URL, HttpMethod.POST, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("MSG91 HTTP " + response.getStatusCode() + ": " + response.getBody());
        }
        if (response.getBody() == null || response.getBody().isBlank()) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            String type = root.path("type").asText("");
            if ("success".equalsIgnoreCase(type)) {
                return;
            }
            String message = root.path("message").asText(response.getBody());
            log.warn("MSG91 response: {}", response.getBody());
            throw new RuntimeException("MSG91 error: " + message);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.debug("MSG91 body parse skipped: {}", response.getBody());
        }
    }
}
