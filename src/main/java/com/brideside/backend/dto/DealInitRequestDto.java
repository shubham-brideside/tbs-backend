package com.brideside.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request DTO for initializing a deal with contact number")
public class DealInitRequestDto {
    
    @NotBlank(message = "Contact number is required")
    @JsonProperty("contact_number")
    @Schema(description = "Contact number for the deal", example = "+1234567890", required = true)
    private String contactNumber;
    
    // Default constructor
    public DealInitRequestDto() {}
    
    // Constructor
    public DealInitRequestDto(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    // Getters and Setters
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    @Override
    public String toString() {
        return "DealInitRequestDto{" +
                "contactNumber='" + contactNumber + '\'' +
                '}';
    }
}
