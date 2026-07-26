package com.blubugtech.bakery_auth_service.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendOtpRequest {
    @NotBlank(message = "Email or username is required")
    private String email;
}
