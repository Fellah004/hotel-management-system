package com.hms.operationsservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && !requestTemplate.headers().containsKey("Authorization")) {
                    requestTemplate.header("Authorization", authHeader);
                }
                String correlationId = request.getHeader("X-Correlation-ID");
                if (correlationId != null && !requestTemplate.headers().containsKey("X-Correlation-ID")) {
                    requestTemplate.header("X-Correlation-ID", correlationId);
                }
            }
        };
    }
}
