package com.brideside.backend.controller;

import com.brideside.backend.dto.DealRequestDto;
import com.brideside.backend.dto.DealResponseDto;
import com.brideside.backend.entity.Deal;
import com.brideside.backend.service.DealService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DealControllerUnitTest {

    @Mock
    private DealService dealService;

    @InjectMocks
    private DealController dealController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dealController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testCreateDeals_Success() throws Exception {
        // Given
        DealRequestDto request = createSampleDealRequest();
        DealResponseDto.DealDto dealDto = new DealResponseDto.DealDto();
        DealResponseDto response = new DealResponseDto("Success", Arrays.asList(dealDto));
        when(dealService.createDeals(any(DealRequestDto.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.createdDeals").isArray());
    }

    @Test
    void testCreateDeals_ValidationError() throws Exception {
        // Given
        DealRequestDto request = new DealRequestDto();
        request.setName(""); // Invalid empty name

        // When & Then
        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllDeals() throws Exception {
        // Given
        List<DealResponseDto.DealDto> deals = Arrays.asList(new DealResponseDto.DealDto(), new DealResponseDto.DealDto());
        when(dealService.getAllDeals()).thenReturn(deals);

        // When & Then
        mockMvc.perform(get("/api/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private DealRequestDto createSampleDealRequest() {
        DealRequestDto request = new DealRequestDto();
        request.setName("Test User");
        request.setContactNumber("1234567890");

        DealRequestDto.CategoryDto category = new DealRequestDto.CategoryDto();
        category.setName("Photography");
        category.setEventDate(LocalDate.of(2025, 10, 20));
        category.setVenue("Test Venue");
        category.setBudget(new BigDecimal("1000"));
        category.setExpectedGathering(100);

        request.setCategories(Arrays.asList(category));
        return request;
    }
}
