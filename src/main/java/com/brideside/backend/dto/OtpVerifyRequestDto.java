package com.brideside.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Verify the OTP the user received on SMS or WhatsApp")
public class OtpVerifyRequestDto {

    @NotBlank(message = "Phone number is required")
    @JsonProperty("phone_number")
    @Schema(example = "+919876543210")
    private String phoneNumber;

    @NotBlank(message = "OTP is required")
    @JsonProperty("otp")
    @Schema(description = "6-digit code from SMS/WhatsApp", example = "123456")
    private String otp;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
