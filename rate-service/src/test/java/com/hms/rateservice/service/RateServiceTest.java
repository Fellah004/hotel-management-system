package com.hms.rateservice.service;

import com.hms.rateservice.dto.request.CreateRateRequest;
import com.hms.rateservice.dto.request.QuoteRequest;
import com.hms.rateservice.dto.response.RateQuoteResponse;
import com.hms.rateservice.dto.response.RateResponse;
import com.hms.rateservice.entity.Rate;
import com.hms.rateservice.exception.BusinessRuleException;
import com.hms.rateservice.exception.DuplicateResourceException;
import com.hms.rateservice.repository.RateRepository;
import com.hms.rateservice.service.impl.RateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateServiceTest {

    @Mock
    private RateRepository rateRepository;

    @InjectMocks
    private RateServiceImpl rateService;

    private Rate sampleRate;

    @BeforeEach
    void setUp() {
        sampleRate = Rate.builder()
                .id(1L)
                .categoryId(1L)
                .resourceType("ROOM")
                .basePrice(new BigDecimal("100.00"))
                .firstNightPrice(new BigDecimal("90.00"))
                .extensionPrice(new BigDecimal("95.00"))
                .weekendMultiplier(new BigDecimal("1.20"))
                .holidayMultiplier(new BigDecimal("1.30"))
                .seasonalMultiplier(new BigDecimal("1.10"))
                .occupancyThreshold(new BigDecimal("80.00"))
                .occupancyMultiplier(new BigDecimal("1.25"))
                .active(true)
                .build();
    }

    @Test
    void createRate_Success() {
        CreateRateRequest request = CreateRateRequest.builder()
                .categoryId(2L)
                .basePrice(new BigDecimal("160.00"))
                .build();

        when(rateRepository.findByCategoryIdAndActiveTrue(2L)).thenReturn(Optional.empty());
        when(rateRepository.save(any(Rate.class))).thenAnswer(i -> {
            Rate r = i.getArgument(0);
            r.setId(2L);
            return r;
        });

        RateResponse response = rateService.createRate(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("160.00"), response.getBasePrice());
    }

    @Test
    void createRate_DuplicateActiveCategory_ThrowsDuplicateResourceException() {
        CreateRateRequest request = CreateRateRequest.builder()
                .categoryId(1L)
                .basePrice(new BigDecimal("100.00"))
                .build();

        when(rateRepository.findByCategoryIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleRate));

        assertThrows(DuplicateResourceException.class, () -> rateService.createRate(request));
    }

    @Test
    void calculateQuote_Success() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn.plusDays(3); // 3 nights

        QuoteRequest request = QuoteRequest.builder()
                .categoryId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .guestsCount(2)
                .currentOccupancyPercentage(new BigDecimal("60.00"))
                .build();

        when(rateRepository.findByCategoryIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleRate));

        RateQuoteResponse quote = rateService.calculateQuote(request);

        assertNotNull(quote);
        assertEquals(3, quote.getNumberOfNights());
        assertNotNull(quote.getTotalQuotedAmount());
        assertEquals(3, quote.getDailyBreakdown().size());
    }

    @Test
    void calculateQuote_InvalidDates_ThrowsBusinessRuleException() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn.minusDays(1); // Invalid checkOut before checkIn

        QuoteRequest request = QuoteRequest.builder()
                .categoryId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .build();

        assertThrows(BusinessRuleException.class, () -> rateService.calculateQuote(request));
    }

    @Test
    void calculateQuote_FridayIsWeekday_SaturdayIsWeekend() {
        // Find next Friday (e.g. 2026-09-18)
        LocalDate friday = LocalDate.of(2026, 9, 18); // Friday
        LocalDate sunday = LocalDate.of(2026, 9, 20); // 2 nights: Friday night and Saturday night

        QuoteRequest request = QuoteRequest.builder()
                .categoryId(1L)
                .checkInDate(friday)
                .checkOutDate(sunday)
                .guestsCount(1)
                .currentOccupancyPercentage(new BigDecimal("50.00"))
                .build();

        when(rateRepository.findByCategoryIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleRate));

        RateQuoteResponse quote = rateService.calculateQuote(request);

        assertNotNull(quote);
        assertEquals(2, quote.getNumberOfNights());
        // Friday night (day 0) should not receive weekend multiplier (1.00)
        // Saturday night (day 1) should receive weekend multiplier (1.20)
        assertEquals(2, quote.getDailyBreakdown().size());
        assertEquals(BigDecimal.ONE, quote.getDailyBreakdown().get(0).getWeekendMultiplier());
        assertEquals(new BigDecimal("1.20"), quote.getDailyBreakdown().get(1).getWeekendMultiplier());
    }
}
