package com.hms.authservice.controller;

import com.hms.authservice.dto.request.LoginRequest;
import com.hms.authservice.dto.request.LogoutRequest;
import com.hms.authservice.dto.request.RefreshTokenRequest;
import com.hms.authservice.dto.request.RegisterRequest;
import com.hms.authservice.dto.response.AuthResponse;
import com.hms.authservice.dto.response.MessageResponse;
import com.hms.authservice.dto.response.TokenRefreshResponse;
import com.hms.authservice.dto.response.UserResponse;
import com.hms.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication, token refresh, logout, and guest registration")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user credentials and returns Access Token, Refresh Token, and user claims")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register guest user", description = "Registers a new GUEST user account")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Validates refresh token against database and generates a new access token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revokes refresh token in database to prevent future token refresh")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody LogoutRequest request) {
        MessageResponse response = authService.logout(request);
        return ResponseEntity.ok(response);
    }
}
