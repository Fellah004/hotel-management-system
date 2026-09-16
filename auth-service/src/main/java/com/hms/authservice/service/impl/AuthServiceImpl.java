package com.hms.authservice.service.impl;

import com.hms.authservice.dto.request.LoginRequest;
import com.hms.authservice.dto.request.RegisterRequest;
import com.hms.authservice.dto.request.LogoutRequest;
import com.hms.authservice.dto.request.RefreshTokenRequest;
import com.hms.authservice.dto.response.AuthResponse;
import com.hms.authservice.dto.response.MessageResponse;
import com.hms.authservice.dto.response.TokenRefreshResponse;
import com.hms.authservice.dto.response.UserResponse;
import com.hms.authservice.entity.RefreshToken;
import com.hms.authservice.entity.Role;
import com.hms.authservice.entity.User;
import com.hms.authservice.event.publisher.SecurityEventPublisher;
import com.hms.authservice.exception.AccountLockedException;
import com.hms.authservice.exception.BusinessRuleException;
import com.hms.authservice.exception.DuplicateResourceException;
import com.hms.authservice.exception.UnauthorizedException;
import com.hms.authservice.repository.UserRepository;
import com.hms.authservice.security.JwtTokenProvider;
import com.hms.authservice.security.UserPrincipal;
import com.hms.authservice.service.AuthService;
import com.hms.authservice.service.LoginAttemptService;
import com.hms.authservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final SecurityEventPublisher securityEventPublisher;
    private final LoginAttemptService loginAttemptService;
    private final RefreshTokenService refreshTokenService;

    @Value("${security.lockout.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.lockout.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));

        // Check if account is locked
        if (user.isLocked()) {
            if (user.getLockTime() != null && user.getLockTime().plusMinutes(lockDurationMinutes).isBefore(LocalDateTime.now())) {
                // Auto unlock after 15 minutes
                loginAttemptService.unlockAccount(user.getId());
                user.setLocked(false);
                user.setFailedLoginAttempts(0);
                user.setLockTime(null);
            } else {
                long minutesRemaining = user.getLockTime() != null
                        ? Math.max(1, lockDurationMinutes - java.time.Duration.between(user.getLockTime(), LocalDateTime.now()).toMinutes())
                        : lockDurationMinutes;
                throw new AccountLockedException("Account is locked due to too many failed login attempts. Please try again after " + minutesRemaining + " minute(s).");
            }
        }

        if (!user.isActive()) {
            throw new BusinessRuleException("User account is inactive. Please contact administrator.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int attempts = loginAttemptService.recordFailedAttempt(user.getId(), maxFailedAttempts);
            if (attempts >= maxFailedAttempts) {
                throw new AccountLockedException("Account has been locked due to " + maxFailedAttempts + " consecutive failed login attempts. Please try again after " + lockDurationMinutes + " minutes.");
            }
            throw new UnauthorizedException("Invalid username/email or password");
        }

        // Reset failed login attempts on successful login
        if (user.getFailedLoginAttempts() > 0 || user.isLocked() || user.getLockTime() != null) {
            loginAttemptService.resetFailedAttempts(user.getId());
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateToken(principal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        log.info("User {} logged in successfully with role {}", user.getUsername(), user.getRole());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .token(accessToken)
                .type("Bearer")
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(tokenProvider.getExpirationMs())
                .build();
    }

    @Override
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.refreshToken(request);
    }

    @Override
    public MessageResponse logout(LogoutRequest request) {
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        return MessageResponse.builder()
                .message("User logged out successfully. Refresh token has been revoked.")
                .success(true)
                .build();
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.GUEST)
                .active(true)
                .locked(false)
                .failedLoginAttempts(0)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new GUEST user: {}", savedUser.getUsername());

        return mapToResponse(savedUser);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .locked(user.isLocked())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .lockTime(user.getLockTime())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
