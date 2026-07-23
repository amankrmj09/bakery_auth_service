package com.blubugtech.bakery_auth_service.dto.auth;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginVerifyRequest {
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    @NotBlank
    private String otp;
}
