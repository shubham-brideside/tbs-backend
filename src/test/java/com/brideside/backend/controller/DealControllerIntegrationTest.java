package com.brideside.backend.controller;

import com.brideside.backend.dto.DealRequestDto;
import com.brideside.backend.entity.Deal;
import com.brideside.backend.repository.DealRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.brideside.backend.BridesideBackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class DealControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Clean up any existing data before each test to ensure isolation
        try {
            dealRepository.deleteAll();
            dealRepository.flush();
        } catch (Exception e) {
            // Ignore if table doesn't exist yet - it will be created by Hibernate
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testCreateDeals_Success() throws Exception {
        DealRequestDto request = createSampleDealRequest();

        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.createdDeals").isArray())
                .andExpect(jsonPath("$.createdDeals.length()").value(2));

        // Verify that separate entries were created
        List<Deal> deals = dealRepository.findAll();
        assertEquals(2, deals.size(), "Should have exactly 2 deals");
        assertEquals("Shubham", deals.get(0).getUserName());
        assertEquals("Shubham", deals.get(1).getUserName());
        assertEquals("Photography", deals.get(0).getCategory());
        assertEquals("Makeup", deals.get(1).getCategory());
    }

    @Test
    void testCreateDeals_ValidationError() throws Exception {
        DealRequestDto request = new DealRequestDto();
        request.setName(""); // Invalid empty name
        request.setContactNumber(""); // Invalid empty contact
        request.setCategories(List.of());

        mockMvc.perform(post("/api/deals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllDeals() throws Exception {
        // Create test data
        createTestDeals();

        mockMvc.perform(get("/api/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetDealsByUserName() throws Exception {
        // Create test data
        createTestDeals();

        mockMvc.perform(get("/api/deals/user/Shubham"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userName").value("Shubham"));
    }

    @Test
    void testGetDealsByContactNumber() throws Exception {
        // Create test data
        createTestDeals();

        mockMvc.perform(get("/api/deals/contact/9304683214"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].contactNumber").value("9304683214"));
    }

    @Test
    void testGetDealsByCategory() throws Exception {
        // Create test data
        createTestDeals();

        mockMvc.perform(get("/api/deals/category/Photography"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Photography"));
    }

    private DealRequestDto createSampleDealRequest() {
        DealRequestDto request = new DealRequestDto();
        request.setName("Shubham");
        request.setContactNumber("9304683214");

        DealRequestDto.CategoryDto category1 = new DealRequestDto.CategoryDto();
        category1.setName("Photography");
        category1.setEventDate(LocalDate.of(2025, 10, 20));
        category1.setVenue("The Leela Palace, New Delhi");
        category1.setBudget(new BigDecimal("200000"));
        category1.setExpectedGathering(200);

        DealRequestDto.CategoryDto category2 = new DealRequestDto.CategoryDto();
        category2.setName("Makeup");
        category2.setEventDate(LocalDate.of(2025, 10, 25));
        category2.setVenue("Taj Palace, New Delhi");
        category2.setBudget(new BigDecimal("150000"));
        category2.setExpectedGathering(180);

        request.setCategories(List.of(category1, category2));
        return request;
    }

    private void createTestDeals() {
        Deal deal1 = new Deal("Shubham", "9304683214", "Photography", 
                             LocalDate.of(2025, 10, 20), "The Leela Palace", 
                             new BigDecimal("200000"), 200);
        
        Deal deal2 = new Deal("Shubham", "9304683214", "Makeup", 
                             LocalDate.of(2025, 10, 25), "Taj Palace", 
                             new BigDecimal("150000"), 180);
        
        dealRepository.saveAll(List.of(deal1, deal2));
    }
}
