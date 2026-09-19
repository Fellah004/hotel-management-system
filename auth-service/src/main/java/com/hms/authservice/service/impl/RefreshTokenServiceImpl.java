package com.hms.authservice.service.impl;

import com.hms.authservice.dto.request.RefreshTokenRequest;
import com.hms.authservice.dto.response.TokenRefreshResponse;
import com.hms.authservice.entity.RefreshToken;
import com.hms.authservice.entity.User;
import com.hms.authservice.exception.ResourceNotFoundException;
import com.hms.authservice.exception.UnauthorizedException;
import com.hms.authservice.repository.RefreshTokenRepository;
import com.hms.authservice.repository.UserRepository;
import com.hms.authservice.security.JwtTokenProvider;
import com.hms.authservice.security.UserPrincipal;
import com.hms.authservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Generate cryptographically unique refresh token
        String tokenString = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenString)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for userId: {}, expires at: {}", userId, saved.getExpiryDate());
        return saved;
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token. Token does not exist."));

        if (refreshToken.isRevoked()) {
            log.warn("Attempt to use revoked refresh token: {}", requestToken);
            throw new UnauthorizedException("Refresh token has been revoked. Please login again.");
        }

        if (refreshToken.isExpired()) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            log.warn("Attempt to use expired refresh token: {}", requestToken);
            throw new UnauthorizedException("Refresh token has expired. Please login again.");
        }

        User user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new UnauthorizedException("User account is inactive. Please contact administrator.");
        }

        if (user.isLocked()) {
            throw new UnauthorizedException("User account is currently locked.");
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String newAccessToken = jwtTokenProvider.generateToken(principal);

        log.info("Successfully refreshed access token for user: {}", user.getUsername());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .refreshToken(requestToken)
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .build();
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token. Token does not exist."));

        if (!refreshToken.isRevoked()) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            log.info("Revoked refresh token for userId: {}", refreshToken.getUser().getId());
        }
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        refreshTokenRepository.revokeAllUserTokens(user);
        log.info("Revoked all active refresh tokens for userId: {}", userId);
    }
}
