package com.hms.roomservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**", "/api/room-categories/**", "/api/amenities/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/rooms/**", "/api/room-categories/**", "/api/amenities/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/*/status").hasAnyRole("ADMIN", "OWNER", "MANAGER", "RECEPTIONIST", "HOUSEKEEPER")
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**", "/api/room-categories/**", "/api/amenities/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**", "/api/room-categories/**", "/api/amenities/**").hasAnyRole("ADMIN", "OWNER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
