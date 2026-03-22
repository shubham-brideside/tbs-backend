package com.brideside.backend.enums;

/**
 * Delivery channel for phone OTP (send + verify must use the same phone; channel is stored on the challenge).
 */
public enum OtpChannel {
    WHATSAPP,
    SMS
}
