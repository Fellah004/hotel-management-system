package com.hms.authservice.service;

import com.hms.authservice.dto.request.LoginRequest;
import com.hms.authservice.dto.request.LogoutRequest;
import com.hms.authservice.dto.request.RefreshTokenRequest;
import com.hms.authservice.dto.request.RegisterRequest;
import com.hms.authservice.dto.response.AuthResponse;
import com.hms.authservice.dto.response.MessageResponse;
import com.hms.authservice.dto.response.TokenRefreshResponse;
import com.hms.authservice.dto.response.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    UserResponse register(RegisterRequest request);
    TokenRefreshResponse refreshToken(RefreshTokenRequest request);
    MessageResponse logout(LogoutRequest request);
}
