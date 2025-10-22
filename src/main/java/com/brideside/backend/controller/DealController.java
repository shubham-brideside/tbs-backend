package com.brideside.backend.controller;

import com.brideside.backend.dto.DealRequestDto;
import com.brideside.backend.dto.DealResponseDto;
import com.brideside.backend.dto.DealInitRequestDto;
import com.brideside.backend.dto.DealUpdateRequestDto;
import com.brideside.backend.service.DealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deals")
@CrossOrigin(origins = "*")
@Tag(name = "Deal Management", description = "APIs for managing deals and deal-related operations")
public class DealController {
    
    @Autowired
    private DealService dealService;
    
    /**
     * Create multiple deals from a single request
     * Each category in the request will create a separate deal entry
     */
    @Operation(summary = "Create deals", description = "Create multiple deals from a single request. Each category in the request will create a separate deal entry.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Deals created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<DealResponseDto> createDeals(@Valid @RequestBody DealRequestDto dealRequest) {
        try {
            DealResponseDto response = dealService.createDeals(dealRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            DealResponseDto errorResponse = new DealResponseDto(
                "Error creating deals: " + e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Initialize a deal with just contact number
     * If a deal with the same contact number already exists, it updates the updated_at timestamp
     * Otherwise, it creates a new basic deal entry that can be updated later with full details
     */
    @Operation(
        summary = "Initialize Deal", 
        description = "Initialize a deal with contact number. If a deal with the same contact number already exists, it updates the updated_at timestamp. Otherwise, it creates a new basic deal entry. This is the first step in the two-step deal creation process. Returns a deal ID that can be used later to update the deal with full details using the PUT /api/deals/{id} endpoint.",
        tags = {"Deal Initialization"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "201", 
                description = "Deal initialized successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class),
                    examples = @ExampleObject(
                        value = "{\"deal_id\": 123, \"message\": \"Deal initialized successfully with contact number: +1234567890\"}"
                    )
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data - contact number is required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initializeDeal(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Contact number for the deal",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DealInitRequestDto.class),
                examples = @ExampleObject(
                    value = "{\"contact_number\": \"+1234567890\"}"
                )
            )
        )
        @Valid @RequestBody DealInitRequestDto dealInitRequest) {
        try {
            Integer dealId = dealService.initializeDeal(dealInitRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("deal_id", dealId);
            response.put("message", "Deal processed successfully with contact number: " + dealInitRequest.getContactNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error initializing deal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Update a deal with full details (name and categories) without contact number
     * This is the second step in the two-step deal creation process
     * Updates the original deal with the first category and creates additional deals for remaining categories
     */
    @Operation(
        summary = "Update Deal Details", 
        description = "Update a deal with name and categories. This is the second step in the two-step deal creation process. Updates the original deal with the first category and creates additional deals for any remaining categories. The original deal ID and Pipedrive deal ID are preserved. Contact number is not required as it was already set during initialization.",
        tags = {"Deal Update"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "Deal updated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DealResponseDto.DealDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 123, \"user_name\": \"John Doe\", \"contact_number\": \"+1234567890\", \"category\": \"Wedding Photography\", \"event_date\": \"2024-06-15\", \"venue\": \"Grand Hotel\", \"budget\": \"5000.00\", \"expected_gathering\": 150, \"created_at\": \"2024-01-15 10:30:00\", \"updated_at\": \"2024-01-15 10:35:00\"}"
                    )
                )
            ),
            @ApiResponse(responseCode = "404", description = "Deal not found"),
            @ApiResponse(responseCode = "400", description = "Deal already fully configured or invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/details")
    public ResponseEntity<DealResponseDto.DealDto> updateDealWithDetails(
        @io.swagger.v3.oas.annotations.Parameter(description = "Deal ID to update", required = true)
        @PathVariable Integer id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Deal details including name and categories (contact number not required)",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DealUpdateRequestDto.class),
                examples = @ExampleObject(
                    value = "{\"name\": \"John Doe\", \"categories\": [{\"name\": \"Wedding Photography\", \"event_date\": \"2024-06-15\", \"venue\": \"Grand Hotel\", \"budget\": 5000, \"expected_gathering\": 150}, {\"name\": \"Makeup\", \"event_date\": \"2024-06-15\", \"venue\": \"Grand Hotel\", \"budget\": 3000, \"expected_gathering\": 150}]}"
                )
            )
        )
        @Valid @RequestBody DealUpdateRequestDto dealUpdateRequest) {
        try {
            DealResponseDto.DealDto response = dealService.updateDealWithoutContactNumber(id, dealUpdateRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("already been fully configured")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all deals
     */
    @Operation(summary = "Get all deals", description = "Retrieve all deals from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all deals"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<DealResponseDto.DealDto>> getAllDeals() {
        try {
            List<DealResponseDto.DealDto> deals = dealService.getAllDeals();
            return ResponseEntity.ok(deals);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get deals by user name
     */
    @GetMapping("/user/{userName}")
    public ResponseEntity<List<DealResponseDto.DealDto>> getDealsByUserName(@PathVariable String userName) {
        try {
            List<DealResponseDto.DealDto> deals = dealService.getDealsByUserName(userName);
            return ResponseEntity.ok(deals);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get deals by contact number
     */
    @GetMapping("/contact/{contactNumber}")
    public ResponseEntity<List<DealResponseDto.DealDto>> getDealsByContactNumber(@PathVariable String contactNumber) {
        try {
            List<DealResponseDto.DealDto> deals = dealService.getDealsByContactNumber(contactNumber);
            return ResponseEntity.ok(deals);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get deals by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<DealResponseDto.DealDto>> getDealsByCategory(@PathVariable String category) {
        try {
            List<DealResponseDto.DealDto> deals = dealService.getDealsByCategory(category);
            return ResponseEntity.ok(deals);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get a single deal by ID
     */
    @Operation(summary = "Get deal by ID", description = "Retrieve a specific deal by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the deal"),
            @ApiResponse(responseCode = "404", description = "Deal not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DealResponseDto.DealDto> getDealById(@PathVariable Integer id) {
        try {
            DealResponseDto.DealDto deal = dealService.getDealById(id);
            if (deal != null) {
                return ResponseEntity.ok(deal);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update a deal by ID
     */
    @Operation(summary = "Update deal", description = "Update an existing deal by its ID with new details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deal updated successfully"),
            @ApiResponse(responseCode = "404", description = "Deal not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DealResponseDto.DealDto> updateDeal(@PathVariable Integer id, 
                                                             @Valid @RequestBody DealRequestDto dealRequest) {
        try {
            DealResponseDto.DealDto updatedDeal = dealService.updateDeal(id, dealRequest);
            return ResponseEntity.ok(updatedDeal);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete a deal by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDeal(@PathVariable Integer id) {
        try {
            boolean deleted = dealService.deleteDeal(id);
            if (deleted) {
                return ResponseEntity.ok("Deal deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete all deals for a specific user
     */
    @DeleteMapping("/user/{userName}")
    public ResponseEntity<String> deleteDealsByUserName(@PathVariable String userName) {
        try {
            int deletedCount = dealService.deleteDealsByUserName(userName);
            return ResponseEntity.ok("Deleted " + deletedCount + " deal(s) for user " + userName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}