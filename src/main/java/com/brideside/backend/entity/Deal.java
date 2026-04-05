package com.brideside.backend.entity;

import com.brideside.backend.converter.JsonListDateConverter;
import com.brideside.backend.enums.CreatedBy;
import com.brideside.backend.enums.DealStatus;
import com.brideside.backend.enums.DealSubSource;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deals")
public class Deal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "User name is required")
    private String userName;
    
    /**
     * Maps to {@code phone_number}. Whenever a phone value exists, it is also written to {@link #contactNumberLegacy}
     * ({@code contact_number}) before persist/update.
     */
    @Column(name = "phone_number", nullable = false, length = 255)
    @NotBlank(message = "Contact number is required")
    private String contactNumber;

    /**
     * Legacy {@code contact_number} column. Same value as {@link #contactNumber} whenever either is non-null.
     */
    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumberLegacy;
    
    @Column(name = "event_date")
    private LocalDate eventDate;
    
    @Column(name = "event_dates", columnDefinition = "JSON")
    @Convert(converter = JsonListDateConverter.class)
    private List<LocalDate> eventDates;
    
    @Column(name = "venue", length = 255)
    private String venue;
    
    @Column(name = "budget", precision = 10, scale = 2)
    @PositiveOrZero(message = "Budget must be positive or zero")
    private BigDecimal budget;
    
    @Column(name = "\"value\"", nullable = false, precision = 12, scale = 2)
    private BigDecimal value = BigDecimal.ZERO;
    
    @Column(name = "expected_gathering", length = 64)
    private String expectedGathering;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        syncContactNumberColumns();
        // Explicitly set timestamps in IST timezone to ensure correct storage
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        syncContactNumberColumns();
        // Explicitly set updated timestamp in IST timezone
        updatedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }

    /**
     * If either {@code phone_number} or {@code contact_number} is non-null/non-blank, set both to that same value.
     */
    private void syncContactNumberColumns() {
        String phone = firstNonBlank(contactNumber, contactNumberLegacy);
        if (phone == null) {
            return;
        }
        contactNumber = phone;
        contactNumberLegacy = phone;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }
    
    @Column(name = "pipedrive_deal_id", length = 100)
    private String pipedriveDealId;
    
    @Column(name = "person_id")
    private Long personId;
    
    @Column(name = "pipeline_id")
    private Long pipelineId;

    /** Stored as MySQL JSON (e.g. {@code [67]}) — must not map as a bare Long. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pipeline_history", columnDefinition = "json")
    private List<Long> pipelineHistory;

    @Column(name = "source_pipeline_id")
    private Long sourcePipelineId;

    @Column(name = "contacted_to")
    private Long contactedTo;
    
    @Column(name = "organization_id")
    private Long organizationId;

    /** FK to {@code users.id}; copied from {@link Organization#getOwnerId()} when {@link #organizationId} is set. */
    @Column(name = "owner_id")
    private Long ownerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DealStatus status;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "deal_source", length = 50)
    private String dealSource;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "deal_sub_source")
    private DealSubSource dealSubSource;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "created_by")
    private CreatedBy createdBy;
    
    @Column(name = "created_by_name", length = 255)
    private String createdByName;
    
    @Column(name = "created_by_user_id")
    private Long createdByUserId;
    
    @Column(name = "stage_id")
    private Long stageId;
    
    // Default constructor
    public Deal() {}
    
    // Constructor with all fields
    public Deal(String userName, String contactNumber, LocalDate eventDate,
                String venue, BigDecimal budget, String expectedGathering) {
        this.userName = userName;
        this.contactNumber = contactNumber;
        this.contactNumberLegacy = contactNumber;
        this.eventDate = eventDate;
        this.venue = venue;
        this.budget = budget;
        this.expectedGathering = expectedGathering;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getContactNumber() {
        return contactNumber != null ? contactNumber : contactNumberLegacy;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
        this.contactNumberLegacy = contactNumber;
    }
    
    public LocalDate getEventDate() {
        return eventDate;
    }
    
    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }
    
    public List<LocalDate> getEventDates() {
        return eventDates;
    }
    
    public void setEventDates(List<LocalDate> eventDates) {
        this.eventDates = eventDates;
    }
    
    public String getVenue() {
        return venue;
    }
    
    public void setVenue(String venue) {
        this.venue = venue;
    }
    
    public BigDecimal getBudget() {
        return budget;
    }
    
    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
    
    public BigDecimal getValue() {
        return value;
    }
    
    public void setValue(BigDecimal value) {
        this.value = value;
    }
    
    public String getExpectedGathering() {
        return expectedGathering;
    }
    
    public void setExpectedGathering(String expectedGathering) {
        this.expectedGathering = expectedGathering;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getPipedriveDealId() {
        return pipedriveDealId;
    }
    
    public void setPipedriveDealId(String pipedriveDealId) {
        this.pipedriveDealId = pipedriveDealId;
    }
    
    public Long getPersonId() {
        return personId;
    }
    
    public void setPersonId(Long personId) {
        this.personId = personId;
    }
    
    public Long getPipelineId() {
        return pipelineId;
    }
    
    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public List<Long> getPipelineHistory() {
        return pipelineHistory;
    }

    public void setPipelineHistory(List<Long> pipelineHistory) {
        this.pipelineHistory = pipelineHistory;
    }

    public Long getSourcePipelineId() {
        return sourcePipelineId;
    }

    public void setSourcePipelineId(Long sourcePipelineId) {
        this.sourcePipelineId = sourcePipelineId;
    }

    public Long getContactedTo() {
        return contactedTo;
    }

    public void setContactedTo(Long contactedTo) {
        this.contactedTo = contactedTo;
    }
    
    public Long getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
    
    public DealStatus getStatus() {
        return status;
    }
    
    public void setStatus(DealStatus status) {
        this.status = status;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getDealSource() {
        return dealSource;
    }
    
    public void setDealSource(String dealSource) {
        this.dealSource = dealSource;
    }
    
    public DealSubSource getDealSubSource() {
        return dealSubSource;
    }
    
    public void setDealSubSource(DealSubSource dealSubSource) {
        this.dealSubSource = dealSubSource;
    }
    
    public CreatedBy getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    public Long getCreatedByUserId() {
        return createdByUserId;
    }
    
    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
    
    public Long getStageId() {
        return stageId;
    }
    
    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }
    
    @Override
    public String toString() {
        return "Deal{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", categoryId=" + categoryId +
                ", eventDate=" + eventDate +
                ", venue='" + venue + '\'' +
                ", budget=" + budget +
                ", expectedGathering=" + expectedGathering +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", pipedriveDealId='" + pipedriveDealId + '\'' +
                ", personId=" + personId +
                '}';
    }
}
