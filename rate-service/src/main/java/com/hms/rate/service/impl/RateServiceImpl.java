package com.hms.rateservice.service.impl;

import com.hms.rateservice.dto.request.CreateRateRequest;
import com.hms.rateservice.dto.request.QuoteRequest;
import com.hms.rateservice.dto.request.UpdateRateRequest;
import com.hms.rateservice.dto.response.DailyRateDetail;
import com.hms.rateservice.dto.response.RateQuoteResponse;
import com.hms.rateservice.dto.response.RateResponse;
import com.hms.rateservice.entity.Rate;
import com.hms.rateservice.exception.BusinessRuleException;
import com.hms.rateservice.exception.DuplicateResourceException;
import com.hms.rateservice.exception.ResourceNotFoundException;
import com.hms.rateservice.repository.RateRepository;
import com.hms.rateservice.service.RateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateServiceImpl implements RateService {

    private final RateRepository rateRepository;

    @Override
    @Transactional
    public RateResponse createRate(CreateRateRequest request) {
        if (rateRepository.findByCategoryIdAndActiveTrue(request.getCategoryId()).isPresent()) {
            throw new DuplicateResourceException("Active rate configuration already exists for category id: " + request.getCategoryId());
        }

        Rate rate = Rate.builder()
                .categoryId(request.getCategoryId())
                .resourceType(request.getResourceType() != null ? request.getResourceType() : "ROOM")
                .basePrice(request.getBasePrice())
                .firstNightPrice(request.getFirstNightPrice())
                .extensionPrice(request.getExtensionPrice())
                .weekendMultiplier(request.getWeekendMultiplier() != null ? request.getWeekendMultiplier() : new BigDecimal("1.20"))
                .holidayMultiplier(request.getHolidayMultiplier() != null ? request.getHolidayMultiplier() : new BigDecimal("1.30"))
                .seasonalMultiplier(request.getSeasonalMultiplier() != null ? request.getSeasonalMultiplier() : new BigDecimal("1.15"))
                .occupancyThreshold(request.getOccupancyThreshold() != null ? request.getOccupancyThreshold() : new BigDecimal("80.00"))
                .occupancyMultiplier(request.getOccupancyMultiplier() != null ? request.getOccupancyMultiplier() : new BigDecimal("1.25"))
                .lastMinuteDays(request.getLastMinuteDays() != null ? request.getLastMinuteDays() : 2)
                .lastMinuteMultiplier(request.getLastMinuteMultiplier() != null ? request.getLastMinuteMultiplier() : new BigDecimal("1.10"))
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(true)
                .build();

        Rate saved = rateRepository.save(rate);
        log.info("Created rate config for category id: {}", saved.getCategoryId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RateResponse getRateById(Long id) {
        Rate rate = rateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate not found with id: " + id));
        return mapToResponse(rate);
    }

    @Override
    @Transactional(readOnly = true)
    public RateResponse getRateByCategoryId(Long categoryId) {
        Rate rate = rateRepository.findByCategoryIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No active rate found for category id: " + categoryId));
        return mapToResponse(rate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RateResponse> getAllRates() {
        return rateRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RateResponse updateRate(Long id, UpdateRateRequest request) {
        Rate rate = rateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate not found with id: " + id));

        if (request.getBasePrice() != null) rate.setBasePrice(request.getBasePrice());
        if (request.getFirstNightPrice() != null) rate.setFirstNightPrice(request.getFirstNightPrice());
        if (request.getExtensionPrice() != null) rate.setExtensionPrice(request.getExtensionPrice());
        if (request.getWeekendMultiplier() != null) rate.setWeekendMultiplier(request.getWeekendMultiplier());
        if (request.getHolidayMultiplier() != null) rate.setHolidayMultiplier(request.getHolidayMultiplier());
        if (request.getSeasonalMultiplier() != null) rate.setSeasonalMultiplier(request.getSeasonalMultiplier());
        if (request.getOccupancyThreshold() != null) rate.setOccupancyThreshold(request.getOccupancyThreshold());
        if (request.getOccupancyMultiplier() != null) rate.setOccupancyMultiplier(request.getOccupancyMultiplier());
        if (request.getLastMinuteDays() != null) rate.setLastMinuteDays(request.getLastMinuteDays());
        if (request.getLastMinuteMultiplier() != null) rate.setLastMinuteMultiplier(request.getLastMinuteMultiplier());
        if (request.getEffectiveFrom() != null) rate.setEffectiveFrom(request.getEffectiveFrom());
        if (request.getEffectiveTo() != null) rate.setEffectiveTo(request.getEffectiveTo());
        if (request.getActive() != null) rate.setActive(request.getActive());

        Rate updated = rateRepository.save(rate);
        log.info("Updated rate id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRate(Long id) {
        if (!rateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rate not found with id: " + id);
        }
        rateRepository.deleteById(id);
        log.info("Deleted rate id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public RateQuoteResponse calculateQuote(QuoteRequest request) {
        if (request.getCheckOutDate().isBefore(request.getCheckInDate()) || request.getCheckOutDate().isEqual(request.getCheckInDate())) {
            throw new BusinessRuleException("Check-out date must be strictly after check-in date");
        }

        Rate rate = rateRepository.findByCategoryIdAndActiveTrue(request.getCategoryId())
                .orElse(Rate.builder()
                        .categoryId(request.getCategoryId())
                        .basePrice(new BigDecimal("150.00"))
                        .weekendMultiplier(new BigDecimal("1.20"))
                        .holidayMultiplier(new BigDecimal("1.30"))
                        .seasonalMultiplier(new BigDecimal("1.10"))
                        .occupancyThreshold(new BigDecimal("80.00"))
                        .occupancyMultiplier(new BigDecimal("1.25"))
                        .lastMinuteDays(2)
                        .lastMinuteMultiplier(new BigDecimal("1.10"))
                        .build());

        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        List<DailyRateDetail> dailyBreakdowns = new ArrayList<>();
        BigDecimal totalCalculated = BigDecimal.ZERO;
        BigDecimal totalBase = BigDecimal.ZERO;

        for (int i = 0; i < nights; i++) {
            LocalDate currentDate = request.getCheckInDate().plusDays(i);
            DayOfWeek day = currentDate.getDayOfWeek();
            BigDecimal dailyBase = (i == 0 && rate.getFirstNightPrice() != null)
                    ? rate.getFirstNightPrice()
                    : rate.getBasePrice();

            totalBase = totalBase.add(dailyBase);

            BigDecimal weekendMult = (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY)
                    ? rate.getWeekendMultiplier()
                    : BigDecimal.ONE;

            // Seasonal check: e.g. July/August or December (summer & holiday peaks)
            int month = currentDate.getMonthValue();
            BigDecimal seasonMult = (month == 7 || month == 8 || month == 12)
                    ? rate.getSeasonalMultiplier()
                    : BigDecimal.ONE;

            // Occupancy multiplier
            BigDecimal occMult = BigDecimal.ONE;
            if (request.getCurrentOccupancyPercentage() != null
                    && request.getCurrentOccupancyPercentage().compareTo(rate.getOccupancyThreshold()) >= 0) {
                occMult = rate.getOccupancyMultiplier();
            }

            BigDecimal holidayMult = BigDecimal.ONE; // Can be configured with holiday calendars

            BigDecimal dailyMultiplier = weekendMult.multiply(seasonMult).multiply(occMult).multiply(holidayMult);
            BigDecimal finalDailyPrice = dailyBase.multiply(dailyMultiplier).setScale(2, RoundingMode.HALF_UP);

            dailyBreakdowns.add(DailyRateDetail.builder()
                    .date(currentDate)
                    .dayOfWeek(day.name())
                    .basePrice(dailyBase)
                    .weekendMultiplier(weekendMult)
                    .holidayMultiplier(holidayMult)
                    .seasonalMultiplier(seasonMult)
                    .occupancyMultiplier(occMult)
                    .finalDailyPrice(finalDailyPrice)
                    .build());

            totalCalculated = totalCalculated.add(finalDailyPrice);
        }

        BigDecimal totalDynamicAdjustment = totalCalculated.subtract(totalBase);

        log.info("Calculated rate quote for category {}: nights={}, base={}, total={}",
                request.getCategoryId(), nights, totalBase, totalCalculated);

        return RateQuoteResponse.builder()
                .categoryId(request.getCategoryId())
                .roomId(request.getRoomId())
                .resourceType(request.getResourceType() != null ? request.getResourceType() : "ROOM")
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .numberOfNights((int) nights)
                .guestsCount(request.getGuestsCount())
                .basePricePerNight(rate.getBasePrice())
                .totalBasePrice(totalBase)
                .totalDynamicAdjustment(totalDynamicAdjustment)
                .totalQuotedAmount(totalCalculated)
                .dailyBreakdown(dailyBreakdowns)
                .build();
    }

    private RateResponse mapToResponse(Rate rate) {
        return RateResponse.builder()
                .id(rate.getId())
                .categoryId(rate.getCategoryId())
                .resourceType(rate.getResourceType())
                .basePrice(rate.getBasePrice())
                .firstNightPrice(rate.getFirstNightPrice())
                .extensionPrice(rate.getExtensionPrice())
                .weekendMultiplier(rate.getWeekendMultiplier())
                .holidayMultiplier(rate.getHolidayMultiplier())
                .seasonalMultiplier(rate.getSeasonalMultiplier())
                .occupancyThreshold(rate.getOccupancyThreshold())
                .occupancyMultiplier(rate.getOccupancyMultiplier())
                .lastMinuteDays(rate.getLastMinuteDays())
                .lastMinuteMultiplier(rate.getLastMinuteMultiplier())
                .effectiveFrom(rate.getEffectiveFrom())
                .effectiveTo(rate.getEffectiveTo())
                .active(rate.isActive())
                .createdAt(rate.getCreatedAt())
                .updatedAt(rate.getUpdatedAt())
                .build();
    }
}
