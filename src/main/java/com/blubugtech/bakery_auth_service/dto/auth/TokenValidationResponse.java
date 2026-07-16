package com.blubugtech.bakery_auth_service.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    private boolean valid;
    private String message;
    private UUID userId;
    private String username;
    private String email;
    private String role;

    public static TokenValidationResponse valid(UUID userId, String username, String email, String role) {
        return TokenValidationResponse.builder()
                .valid(true)
                .message("Valid")
                .userId(userId)
                .username(username)
                .email(email)
                .role(role)
                .build();
    }

    public static TokenValidationResponse invalid(String message) {
        return TokenValidationResponse.builder()
                .valid(false)
                .message(message)
                .build();
    }
}
