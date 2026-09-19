package com.hms.authservice.service;

import com.hms.authservice.dto.request.LoginRequest;
import com.hms.authservice.dto.request.LogoutRequest;
import com.hms.authservice.dto.request.RefreshTokenRequest;
import com.hms.authservice.dto.request.RegisterRequest;
import com.hms.authservice.dto.response.AuthResponse;
import com.hms.authservice.dto.response.MessageResponse;
import com.hms.authservice.dto.response.TokenRefreshResponse;
import com.hms.authservice.dto.response.UserResponse;
import com.hms.authservice.entity.RefreshToken;
import com.hms.authservice.entity.Role;
import com.hms.authservice.entity.User;
import com.hms.authservice.event.publisher.SecurityEventPublisher;
import com.hms.authservice.exception.AccountLockedException;
import com.hms.authservice.exception.DuplicateResourceException;
import com.hms.authservice.exception.UnauthorizedException;
import com.hms.authservice.repository.UserRepository;
import com.hms.authservice.security.JwtTokenProvider;
import com.hms.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private SecurityEventPublisher securityEventPublisher;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "maxFailedAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockDurationMinutes", 15);

        sampleUser = User.builder()
                .id(1L)
                .username("testguest")
                .email("testguest@hms.com")
                .password("encoded_pass")
                .role(Role.GUEST)
                .active(true)
                .locked(false)
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    void login_Success() {
        sampleUser.setFailedLoginAttempts(2);
        LoginRequest request = new LoginRequest("testguest", "password123");
        when(userRepository.findByUsernameOrEmail("testguest", "testguest")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "encoded_pass")).thenReturn(true);
        when(tokenProvider.generateToken(any())).thenReturn("jwt_mock_token");
        when(tokenProvider.getExpirationMs()).thenReturn(900000L);
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(RefreshToken.builder()
                .token("mock-refresh-token")
                .build());

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt_mock_token", response.getAccessToken());
        assertEquals("jwt_mock_token", response.getToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
        assertEquals("testguest", response.getUsername());
        assertEquals(Role.GUEST, response.getRole());
        verify(loginAttemptService).resetFailedAttempts(1L);
    }

    @Test
    void login_InvalidPassword_IncrementsFailedAttempts() {
        LoginRequest request = new LoginRequest("testguest", "wrongpass");
        when(userRepository.findByUsernameOrEmail("testguest", "testguest")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongpass", "encoded_pass")).thenReturn(false);
        when(loginAttemptService.recordFailedAttempt(1L, 5)).thenReturn(1);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
        verify(loginAttemptService).recordFailedAttempt(1L, 5);
    }

    @Test
    void login_ExceedsMaxAttempts_LocksAccount() {
        LoginRequest request = new LoginRequest("testguest", "wrongpass");
        when(userRepository.findByUsernameOrEmail("testguest", "testguest")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongpass", "encoded_pass")).thenReturn(false);
        when(loginAttemptService.recordFailedAttempt(1L, 5)).thenReturn(5);

        assertThrows(AccountLockedException.class, () -> authService.login(request));
        verify(loginAttemptService).recordFailedAttempt(1L, 5);
    }

    @Test
    void refreshToken_Success() {
        RefreshTokenRequest request = new RefreshTokenRequest("mock-refresh-token");
        TokenRefreshResponse expectedResponse = TokenRefreshResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(900000L)
                .build();
        when(refreshTokenService.refreshToken(request)).thenReturn(expectedResponse);

        TokenRefreshResponse actualResponse = authService.refreshToken(request);

        assertNotNull(actualResponse);
        assertEquals("new-access-token", actualResponse.getAccessToken());
        assertEquals("mock-refresh-token", actualResponse.getRefreshToken());
        verify(refreshTokenService).refreshToken(request);
    }

    @Test
    void logout_Success() {
        LogoutRequest request = new LogoutRequest("mock-refresh-token");

        MessageResponse response = authService.logout(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(refreshTokenService).revokeRefreshToken("mock-refresh-token");
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("newuser", "newuser@hms.com", "secret123");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@hms.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_secret");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals("newuser@hms.com", response.getEmail());
        assertEquals(Role.GUEST, response.getRole());
    }

    @Test
    void register_DuplicateUsername_ThrowsDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest("existing", "email@hms.com", "secret123");
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }
}
