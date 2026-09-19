package com.hms.authservice.dto.response;

import com.hms.authservice.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String token; // Alias for accessToken for backward compatibility
    @Builder.Default
    private String type = "Bearer";
    private String refreshToken;
    private Long userId;
    private String username;
    private String email;
    private Role role;
    private long expiresIn;
}
