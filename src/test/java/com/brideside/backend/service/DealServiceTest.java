package com.brideside.backend.service;

import com.brideside.backend.dto.DealRequestDto;
import com.brideside.backend.dto.DealResponseDto;
import com.brideside.backend.dto.DealInitRequestDto;
import com.brideside.backend.dto.DealUpdateRequestDto;
import com.brideside.backend.entity.Deal;
import com.brideside.backend.repository.DealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private DealService dealService;

    private DealRequestDto dealRequest;

    @BeforeEach
    void setUp() {
        dealRequest = new DealRequestDto();
        dealRequest.setName("Test User");
        dealRequest.setContactNumber("1234567890");
        
        DealRequestDto.CategoryDto category1 = new DealRequestDto.CategoryDto();
        category1.setName("Photography");
        category1.setEventDate(LocalDate.of(2025, 10, 20));
        category1.setVenue("Test Venue");
        category1.setBudget(new BigDecimal("1000"));
        category1.setExpectedGathering(100);
        
        dealRequest.setCategories(Arrays.asList(category1));
    }

    @Test
    void testCreateDeals_Success() {
        // Given
        when(dealRepository.save(any(Deal.class))).thenReturn(new Deal());

        // When
        DealResponseDto result = dealService.createDeals(dealRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getMessage());
        assertNotNull(result.getCreatedDeals());
        verify(dealRepository, times(1)).save(any(Deal.class));
    }

    @Test
    void testGetAllDeals() {
        // Given
        List<Deal> expectedDeals = Arrays.asList(new Deal(), new Deal());
        when(dealRepository.findAll()).thenReturn(expectedDeals);

        // When
        List<DealResponseDto.DealDto> result = dealService.getAllDeals();

        // Then
        assertNotNull(result);
        assertEquals(expectedDeals.size(), result.size());
        verify(dealRepository, times(1)).findAll();
    }

    @Test
    void testInitializeDeal_NewDeal() {
        // Given
        DealInitRequestDto initRequest = new DealInitRequestDto();
        initRequest.setContactNumber("+1234567890");
        
        Deal savedDeal = new Deal();
        savedDeal.setId(1);
        
        when(dealRepository.findByContactNumber("+1234567890")).thenReturn(Arrays.asList());
        when(dealRepository.save(any(Deal.class))).thenReturn(savedDeal);

        // When
        Integer result = dealService.initializeDeal(initRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result);
        verify(dealRepository, times(1)).findByContactNumber("+1234567890");
        verify(dealRepository, times(1)).save(any(Deal.class));
    }

    @Test
    void testInitializeDeal_ExistingDeal() {
        // Given
        DealInitRequestDto initRequest = new DealInitRequestDto();
        initRequest.setContactNumber("+1234567890");
        
        Deal existingDeal = new Deal();
        existingDeal.setId(1);
        existingDeal.setContactNumber("+1234567890");
        existingDeal.setUserName("Existing User");
        
        Deal updatedDeal = new Deal();
        updatedDeal.setId(1);
        
        when(dealRepository.findByContactNumber("+1234567890")).thenReturn(Arrays.asList(existingDeal));
        when(dealRepository.save(existingDeal)).thenReturn(updatedDeal);

        // When
        Integer result = dealService.initializeDeal(initRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result);
        verify(dealRepository, times(1)).findByContactNumber("+1234567890");
        verify(dealRepository, times(1)).save(existingDeal);
    }

    @Test
    void testUpdateDealWithoutContactNumber_MultipleCategories() {
        // Given
        DealUpdateRequestDto updateRequest = new DealUpdateRequestDto();
        updateRequest.setName("John Doe");
        
        DealUpdateRequestDto.CategoryDto category1 = new DealUpdateRequestDto.CategoryDto();
        category1.setName("Photography");
        category1.setEventDate(LocalDate.of(2024, 6, 15));
        category1.setVenue("Grand Hotel");
        category1.setBudget(new BigDecimal("5000"));
        category1.setExpectedGathering(150);
        
        DealUpdateRequestDto.CategoryDto category2 = new DealUpdateRequestDto.CategoryDto();
        category2.setName("Makeup");
        category2.setEventDate(LocalDate.of(2024, 6, 15));
        category2.setVenue("Grand Hotel");
        category2.setBudget(new BigDecimal("3000"));
        category2.setExpectedGathering(150);
        
        updateRequest.setCategories(Arrays.asList(category1, category2));
        
        Deal existingDeal = new Deal();
        existingDeal.setId(1);
        existingDeal.setUserName("TBD");
        existingDeal.setContactNumber("+1234567890");
        existingDeal.setCategory("TBD");
        
        Deal newDeal1 = new Deal();
        newDeal1.setId(2);
        
        Deal newDeal2 = new Deal();
        newDeal2.setId(3);
        
        when(dealRepository.findById(1)).thenReturn(java.util.Optional.of(existingDeal));
        when(dealRepository.save(any(Deal.class))).thenReturn(newDeal1, newDeal2);

        // When
        DealResponseDto.DealDto result = dealService.updateDealWithoutContactNumber(1, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getId());
        verify(dealRepository, times(1)).findById(1);
        verify(dealRepository, times(2)).save(any(Deal.class)); // Should save 2 new deals
        verify(dealRepository, times(1)).delete(existingDeal); // Should delete original deal
    }
}
