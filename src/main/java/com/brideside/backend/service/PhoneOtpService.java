package com.brideside.backend.service;

import com.brideside.backend.config.OtpProperties;
import com.brideside.backend.dto.OtpSendResponseDto;
import com.brideside.backend.dto.OtpVerifyResponseDto;
import com.brideside.backend.entity.PhoneOtpChallenge;
import com.brideside.backend.enums.OtpChannel;
import com.brideside.backend.repository.PhoneOtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PhoneOtpService {

    private static final Logger log = LoggerFactory.getLogger(PhoneOtpService.class);

    private final PhoneOtpRepository phoneOtpRepository;
    private final PhoneOtpPersistenceHelper persistence;
    private final OtpProperties otpProperties;
    private final PasswordEncoder passwordEncoder;
    private final WhatsAppService whatsAppService;
    private final Msg91SmsService msg91SmsService;
    private final SecureRandom random = new SecureRandom();

    public PhoneOtpService(
            PhoneOtpRepository phoneOtpRepository,
            PhoneOtpPersistenceHelper persistence,
            OtpProperties otpProperties,
            PasswordEncoder passwordEncoder,
            WhatsAppService whatsAppService,
            Msg91SmsService msg91SmsService) {
        this.phoneOtpRepository = phoneOtpRepository;
        this.persistence = persistence;
        this.otpProperties = otpProperties;
        this.passwordEncoder = passwordEncoder;
        this.whatsAppService = whatsAppService;
        this.msg91SmsService = msg91SmsService;
    }

    public OtpSendResponseDto sendOtp(String rawPhone, OtpChannel channel) {
        String digits = normalizeDigits(rawPhone);
        enforceResendCooldown(digits);
        assertChannelConfigured(channel);

        String code = String.format("%06d", random.nextInt(1_000_000));

        PhoneOtpChallenge challenge = new PhoneOtpChallenge();
        challenge.setPhoneDigits(digits);
        challenge.setChannel(channel);
        challenge.setCodeHash(passwordEncoder.encode(code));
        challenge.setExpiresAt(LocalDateTime.now().plusMinutes(otpProperties.getCodeTtlMinutes()));
        challenge.setConsumed(false);
        challenge.setFailedAttempts(0);

        PhoneOtpChallenge saved = persistence.createNewChallenge(challenge);
        try {
            if (channel == OtpChannel.WHATSAPP) {
                whatsAppService.sendTemplateMessageOrThrow(
                        rawPhone,
                        otpProperties.getWhatsapp().getTemplateName(),
                        otpProperties.getWhatsapp().getLanguageCode(),
                        List.of(code));
            } else {
                msg91SmsService.sendOtp(digits, code);
            }
        } catch (Exception e) {
            persistence.deleteChallenge(saved.getId());
            log.warn("OTP delivery failed for +{}****: {}", digits.substring(0, Math.min(4, digits.length())), e.getMessage());
            throw e;
        }

        log.info("OTP sent via {} to +{}****", channel, digits.substring(0, Math.min(4, digits.length())));
        return new OtpSendResponseDto("sent", "+" + digits, channel.name().toLowerCase(Locale.ROOT));
    }

    public OtpVerifyResponseDto verifyOtp(String rawPhone, String rawOtp) {
        String digits = normalizeDigits(rawPhone);
        String otp = rawOtp == null ? "" : rawOtp.trim();
        if (otp.isEmpty()) {
            return new OtpVerifyResponseDto(false, "invalid");
        }

        Optional<PhoneOtpChallenge> opt = phoneOtpRepository
                .findTopByPhoneDigitsAndConsumedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        digits, LocalDateTime.now());

        if (opt.isEmpty()) {
            return new OtpVerifyResponseDto(false, "expired_or_missing");
        }

        PhoneOtpChallenge c = opt.get();
        if (c.getFailedAttempts() >= otpProperties.getMaxVerifyAttempts()) {
            return new OtpVerifyResponseDto(false, "too_many_attempts");
        }

        if (!passwordEncoder.matches(otp, c.getCodeHash())) {
            c.setFailedAttempts(c.getFailedAttempts() + 1);
            persistence.saveChallenge(c);
            return new OtpVerifyResponseDto(false, "invalid");
        }

        c.setConsumed(true);
        persistence.saveChallenge(c);
        return new OtpVerifyResponseDto(true, "approved");
    }

    public static OtpChannel parseChannel(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("channel is required");
        }
        String v = raw.trim().toLowerCase(Locale.ROOT);
        if ("whatsapp".equals(v)) {
            return OtpChannel.WHATSAPP;
        }
        if ("sms".equals(v)) {
            return OtpChannel.SMS;
        }
        throw new IllegalArgumentException("channel must be whatsapp or sms");
    }

    public String normalizeDigits(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        if (digits.length() == 10) {
            digits = "91" + digits;
        }
        if (digits.length() < 10) {
            throw new IllegalArgumentException("Invalid phone number");
        }
        return digits;
    }

    private void enforceResendCooldown(String digits) {
        phoneOtpRepository.findTopByPhoneDigitsOrderByCreatedAtDesc(digits).ifPresent(last -> {
            LocalDateTime boundary = LocalDateTime.now().minusSeconds(otpProperties.getResendCooldownSeconds());
            if (last.getCreatedAt().isAfter(boundary)) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS, "Please wait before requesting another code.");
            }
        });
    }

    private void assertChannelConfigured(OtpChannel channel) {
        if (channel == OtpChannel.WHATSAPP) {
            if (!whatsAppService.isMetaConfigured()) {
                throw new IllegalStateException(
                        "WhatsApp OTP is not available: configure WHATSAPP_BASE_URL, WHATSAPP_PHONE_NUMBER_ID, WHATSAPP_ACCESS_TOKEN and approve otp template in Meta.");
            }
            return;
        }
        if (!otpProperties.getSms().isSmsChannelAvailable()) {
            throw new IllegalStateException(
                    "SMS OTP is not configured. Production: otp.sms.enabled=true, MSG91_AUTH_KEY, MSG91_FLOW_TEMPLATE_ID "
                            + "(and MSG91_BODY_VAR_KEY if not VAR1). Local dev: OTP_SMS_SIMULATE=true or run with profile "
                            + "'dev' (see application-dev.yml) — OTP is logged, not sent.");
        }
    }
}
