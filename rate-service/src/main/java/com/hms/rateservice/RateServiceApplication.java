package com.hms.rateservice;

import com.hms.rateservice.entity.Rate;
import com.hms.rateservice.repository.RateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class RateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RateServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initRateData(RateRepository rateRepository) {
        return args -> {
            if (rateRepository.count() == 0) {
                // SINGLE category (ID 1)
                rateRepository.save(Rate.builder()
                        .categoryId(1L)
                        .resourceType("ROOM")
                        .basePrice(new BigDecimal("100.00"))
                        .firstNightPrice(new BigDecimal("90.00"))
                        .extensionPrice(new BigDecimal("95.00"))
                        .weekendMultiplier(new BigDecimal("1.15"))
                        .holidayMultiplier(new BigDecimal("1.25"))
                        .seasonalMultiplier(new BigDecimal("1.10"))
                        .occupancyThreshold(new BigDecimal("80.00"))
                        .occupancyMultiplier(new BigDecimal("1.20"))
                        .active(true)
                        .build());

                // DOUBLE category (ID 2)
                rateRepository.save(Rate.builder()
                        .categoryId(2L)
                        .resourceType("ROOM")
                        .basePrice(new BigDecimal("160.00"))
                        .firstNightPrice(new BigDecimal("150.00"))
                        .extensionPrice(new BigDecimal("155.00"))
                        .weekendMultiplier(new BigDecimal("1.20"))
                        .holidayMultiplier(new BigDecimal("1.30"))
                        .seasonalMultiplier(new BigDecimal("1.15"))
                        .occupancyThreshold(new BigDecimal("80.00"))
                        .occupancyMultiplier(new BigDecimal("1.25"))
                        .active(true)
                        .build());

                // DELUXE category (ID 3)
                rateRepository.save(Rate.builder()
                        .categoryId(3L)
                        .resourceType("ROOM")
                        .basePrice(new BigDecimal("250.00"))
                        .firstNightPrice(new BigDecimal("230.00"))
                        .extensionPrice(new BigDecimal("240.00"))
                        .weekendMultiplier(new BigDecimal("1.25"))
                        .holidayMultiplier(new BigDecimal("1.35"))
                        .seasonalMultiplier(new BigDecimal("1.20"))
                        .occupancyThreshold(new BigDecimal("75.00"))
                        .occupancyMultiplier(new BigDecimal("1.30"))
                        .active(true)
                        .build());

                // SUITE category (ID 4)
                rateRepository.save(Rate.builder()
                        .categoryId(4L)
                        .resourceType("ROOM")
                        .basePrice(new BigDecimal("450.00"))
                        .firstNightPrice(new BigDecimal("420.00"))
                        .extensionPrice(new BigDecimal("430.00"))
                        .weekendMultiplier(new BigDecimal("1.30"))
                        .holidayMultiplier(new BigDecimal("1.40"))
                        .seasonalMultiplier(new BigDecimal("1.25"))
                        .occupancyThreshold(new BigDecimal("70.00"))
                        .occupancyMultiplier(new BigDecimal("1.35"))
                        .active(true)
                        .build());
            }
        };
    }
}
