package com.brideside.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OTP send accepted; code is delivered only on the device — never returned here")
public class OtpSendResponseDto {

    @Schema(description = "Delivery state", example = "sent")
    private String status;

    @Schema(description = "Normalized destination (E.164 without spaces)", example = "+919876543210")
    private String to;

    @Schema(description = "Channel used", example = "whatsapp")
    private String channel;

    public OtpSendResponseDto() {}

    public OtpSendResponseDto(String status, String to, String channel) {
        this.status = status;
        this.to = to;
        this.channel = channel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
