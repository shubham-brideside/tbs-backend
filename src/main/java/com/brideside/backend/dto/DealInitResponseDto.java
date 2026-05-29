package com.brideside.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response from POST /api/deals/init")
public class DealInitResponseDto {

    private static final String PLACEHOLDER_NAME = "TBS";

    @Schema(description = "Deal id (same value as deal_id and dealId)", example = "43765")
    private Integer id;

    @JsonProperty("deal_id")
    @Schema(description = "Deal id alias", example = "43765")
    private Integer dealIdSnake;

    @JsonProperty("dealId")
    @Schema(description = "Deal id alias", example = "43765")
    private Integer dealIdCamel;

    @JsonProperty("is_new_deal")
    @Schema(description = "True when a new deal row was created")
    private boolean isNewDeal;

    @JsonProperty("already_configured")
    @Schema(description = "True when this phone already has an active landing-page deal with a real name (not placeholder TBS)")
    private boolean alreadyConfigured;

    @JsonProperty("requires_details")
    @Schema(description = "When true, call PUT /{dealId}/details with the user's name. When false, skip the details step.")
    private boolean requiresDetails;

    private String message;

    public DealInitResponseDto() {}

    public static DealInitResponseDto fromDeal(Integer dealId, boolean isNewDeal, String userName, String contactNumber) {
        boolean alreadyConfigured = userName != null && !PLACEHOLDER_NAME.equals(userName);
        DealInitResponseDto response = new DealInitResponseDto();
        response.id = dealId;
        response.dealIdSnake = dealId;
        response.dealIdCamel = dealId;
        response.isNewDeal = isNewDeal;
        response.alreadyConfigured = alreadyConfigured;
        response.requiresDetails = !alreadyConfigured;
        if (alreadyConfigured) {
            response.message = "Existing deal reused for contact number: " + contactNumber
                    + ". Details step not required.";
        } else if (isNewDeal) {
            response.message = "Deal initialized successfully with contact number: " + contactNumber;
        } else {
            response.message = "Existing deal reused for contact number: " + contactNumber
                    + ". Submit name via PUT /details.";
        }
        return response;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDealIdSnake() {
        return dealIdSnake;
    }

    public void setDealIdSnake(Integer dealIdSnake) {
        this.dealIdSnake = dealIdSnake;
    }

    public Integer getDealIdCamel() {
        return dealIdCamel;
    }

    public void setDealIdCamel(Integer dealIdCamel) {
        this.dealIdCamel = dealIdCamel;
    }

    public boolean isNewDeal() {
        return isNewDeal;
    }

    public void setNewDeal(boolean newDeal) {
        isNewDeal = newDeal;
    }

    public boolean isAlreadyConfigured() {
        return alreadyConfigured;
    }

    public void setAlreadyConfigured(boolean alreadyConfigured) {
        this.alreadyConfigured = alreadyConfigured;
    }

    public boolean isRequiresDetails() {
        return requiresDetails;
    }

    public void setRequiresDetails(boolean requiresDetails) {
        this.requiresDetails = requiresDetails;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
