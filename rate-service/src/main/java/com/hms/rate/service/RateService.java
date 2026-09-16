package com.hms.rateservice.service;

import com.hms.rateservice.dto.request.CreateRateRequest;
import com.hms.rateservice.dto.request.QuoteRequest;
import com.hms.rateservice.dto.request.UpdateRateRequest;
import com.hms.rateservice.dto.response.RateQuoteResponse;
import com.hms.rateservice.dto.response.RateResponse;

import java.util.List;

public interface RateService {
    RateResponse createRate(CreateRateRequest request);
    RateResponse getRateById(Long id);
    RateResponse getRateByCategoryId(Long categoryId);
    List<RateResponse> getAllRates();
    RateResponse updateRate(Long id, UpdateRateRequest request);
    void deleteRate(Long id);
    RateQuoteResponse calculateQuote(QuoteRequest request);
}
