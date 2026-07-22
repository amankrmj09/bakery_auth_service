package com.blubugtech.bakery_auth_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterVerifyRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String otp;
}
