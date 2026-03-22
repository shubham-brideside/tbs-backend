package com.brideside.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OTP verification outcome")
public class OtpVerifyResponseDto {

    @Schema(description = "Whether the code matched a non-expired challenge")
    private boolean valid;

    @Schema(description = "approved | invalid | expired_or_missing | too_many_attempts")
    private String status;

    public OtpVerifyResponseDto() {}

    public OtpVerifyResponseDto(boolean valid, String status) {
        this.valid = valid;
        this.status = status;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
