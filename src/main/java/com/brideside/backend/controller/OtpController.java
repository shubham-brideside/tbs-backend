package com.brideside.backend.controller;

import com.brideside.backend.dto.OtpSendRequestDto;
import com.brideside.backend.dto.OtpSendResponseDto;
import com.brideside.backend.dto.OtpVerifyRequestDto;
import com.brideside.backend.dto.OtpVerifyResponseDto;
import com.brideside.backend.enums.OtpChannel;
import com.brideside.backend.service.PhoneOtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@Tag(name = "OTP", description = "SMS (MSG91) or WhatsApp (Meta) one-time codes")
public class OtpController {

    private static final Logger log = LoggerFactory.getLogger(OtpController.class);

    private final PhoneOtpService phoneOtpService;

    public OtpController(PhoneOtpService phoneOtpService) {
        this.phoneOtpService = phoneOtpService;
    }

    @Operation(summary = "Send OTP", description = "Delivers a 6-digit code via WhatsApp (Meta template) or SMS (MSG91). Approve/configure templates in Meta Business Manager and MSG91 respectively.")
    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OtpSendRequestDto request) {
        try {
            OtpChannel channel = PhoneOtpService.parseChannel(request.getChannel());
            OtpSendResponseDto body = phoneOtpService.sendOtp(request.getPhoneNumber(), channel);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "error", e.getMessage()));
        } catch (Exception e) {
            log.error("OTP send failed", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Delivery failed"));
        }
    }

    @Operation(summary = "Verify OTP", description = "Checks the code; use response.valid and response.status on the client.")
    @PostMapping("/verify")
    public ResponseEntity<OtpVerifyResponseDto> verifyOtp(@Valid @RequestBody OtpVerifyRequestDto request) {
        try {
            OtpVerifyResponseDto body = phoneOtpService.verifyOtp(request.getPhoneNumber(), request.getOtp());
            if (!body.isValid()) {
                HttpStatus st = "too_many_attempts".equals(body.getStatus())
                        ? HttpStatus.TOO_MANY_REQUESTS
                        : HttpStatus.OK;
                return ResponseEntity.status(st).body(body);
            }
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new OtpVerifyResponseDto(false, "invalid"));
        }
    }
}
