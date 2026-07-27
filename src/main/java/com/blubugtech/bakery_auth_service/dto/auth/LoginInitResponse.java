package com.blubugtech.bakery_auth_service.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginInitResponse {
    private boolean requiresOtp;
    private String message;
    private AuthResponse authResponse;
}
