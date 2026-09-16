package com.hms.authservice.service;

import com.hms.authservice.dto.request.RefreshTokenRequest;
import com.hms.authservice.dto.response.TokenRefreshResponse;
import com.hms.authservice.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(Long userId);

    TokenRefreshResponse refreshToken(RefreshTokenRequest request);

    void revokeRefreshToken(String token);

    void revokeAllUserTokens(Long userId);
}
