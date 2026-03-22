package com.brideside.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Start OTP delivery to a phone via WhatsApp (Meta) or SMS (MSG91)")
public class OtpSendRequestDto {

    @NotBlank(message = "Phone number is required")
    @JsonProperty("phone_number")
    @Schema(description = "Mobile number; normalized to country code + digits (e.g. 10-digit India → 91…)", example = "+919876543210")
    private String phoneNumber;

    @NotBlank(message = "Channel is required")
    @Pattern(regexp = "^(?i)(whatsapp|sms)$", message = "channel must be whatsapp or sms")
    @Schema(description = "whatsapp (Meta Cloud API template) or sms (MSG91 Flow)", example = "whatsapp", allowableValues = {"whatsapp", "sms"})
    private String channel;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
