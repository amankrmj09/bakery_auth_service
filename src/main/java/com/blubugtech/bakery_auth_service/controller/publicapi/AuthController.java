package com.blubugtech.bakery_auth_service.controller.publicapi;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_auth_service.dto.auth.*;
import com.blubugtech.bakery_auth_service.exception.AuthException;
import com.blubugtech.bakery_auth_service.service.auth.AuthService;
import com.blubugtech.bakery_auth_service.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import org.blubakery.bakery_common_libs.contract.feign.MessageResponse;
import org.blubakery.bakery_common_libs.exception.security.UnauthenticatedException;
import org.blubakery.bakery_common_libs.exception.security.InvalidTokenException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and authorization")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    // User Registration - Step 1: Initiate
    @PostMapping("/register")
    @Operation(summary = "Initiate registration for a new user")
    public ResponseEntity<MessageResponse> initiateRegister(@Valid @RequestBody RegisterRequest request) throws AuthException {
        log.info("Registration initiation request received for username: {}", request.getUsername());
        String otp = authService.initiateRegister(request);
        // Only return OTP in dev environments for learning purposes
        return ResponseEntity.ok(new MessageResponse("OTP Sent. Mock OTP: " + otp));
    }

    // User Registration - Step 2: Verify
    @PostMapping("/register/verify")
    @Operation(summary = "Verify OTP to complete registration")
    public ResponseEntity<AuthResponse> verifyRegister(@Valid @RequestBody RegisterVerifyRequest request) throws AuthException {
        log.info("Registration verification request received for email: {}", request.getEmail());
        AuthResponse response = authService.verifyRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // User Registration - Resend OTP
    @PostMapping("/register/resend")
    @Operation(summary = "Resend OTP for registration")
    public ResponseEntity<MessageResponse> resendRegisterOtp(@Valid @RequestBody ResendOtpRequest request) throws AuthException {
        log.info("Resend registration OTP request received for email: {}", request.getEmail());
        String otp = authService.resendRegisterOtp(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("OTP Sent. Mock OTP: " + otp));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and initiate 2FA")
    public ResponseEntity<com.blubugtech.bakery_auth_service.dto.auth.LoginInitResponse> login(@Valid @RequestBody LoginRequest request) throws AuthException {
        log.info("Login request received for user: {}", request.getUsernameOrEmail());
        com.blubugtech.bakery_auth_service.dto.auth.LoginInitResponse response = authService.initiateLogin(request);
        return ResponseEntity.ok(response);
    }

    // Admin Login (1-step, no OTP for now)
    @PostMapping("/admin/login")
    @Operation(summary = "Direct login for admin without OTP")
    public ResponseEntity<AuthResponse> adminLogin(@Valid @RequestBody LoginRequest request) throws AuthException {
        log.info("Admin login request received for user: {}", request.getUsernameOrEmail());
        AuthResponse response = authService.login(request);
        
        if (response.getUser().getRole() != com.blubugtech.bakery_auth_service.entity.User.Role.ADMIN) {
            throw new com.blubugtech.common.exception.security.UnauthorizedAccessException("Access denied. Admin role required.");
        }
        return ResponseEntity.ok(response);
    }

    // User Login - Step 2: Verify
    @PostMapping("/login/verify")
    @Operation(summary = "Verify OTP to complete login")
    public ResponseEntity<AuthResponse> verifyLogin(@Valid @RequestBody LoginVerifyRequest request) throws AuthException {
        log.info("Login verification request received for: {}", request.getUsernameOrEmail());
        AuthResponse response = authService.verifyLogin(request);
        return ResponseEntity.ok(response);
    }

    // User Login - Resend OTP
    @PostMapping("/login/resend")
    @Operation(summary = "Resend OTP for login")
    public ResponseEntity<MessageResponse> resendLoginOtp(@Valid @RequestBody ResendOtpRequest request) throws AuthException {
        log.info("Resend login OTP request received for: {}", request.getEmail());
        String otp = authService.resendLoginOtp(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("OTP Sent. Mock OTP: " + otp));
    }

    // Forgot Password - Step 1: Initiate
    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate password reset")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) throws AuthException {
        log.info("Forgot password request received for email: {}", request.getEmail());
        String otp = authService.initiateForgotPassword(request);
        return ResponseEntity.ok(new MessageResponse("OTP Sent. Mock OTP: " + otp));
    }

    // Forgot Password - Step 2: Reset
    @PostMapping("/forgot-password/reset")
    @Operation(summary = "Reset password using OTP")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) throws AuthException {
        log.info("Reset password request received for email: {}", request.getEmail());
        authService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
    }

    // Refresh Token
    @PostMapping("/refresh")
    @Operation(summary = "Refresh authentication token from header")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) throws AuthException {
        log.info("Token refresh request received");

        String authHeader = request.getHeader("Authorization");
        String refreshToken = jwtService.extractTokenFromHeader(authHeader);

        if (refreshToken == null) {
            log.warn("No refresh token provided in Authorization header");
            return ResponseEntity.badRequest().build();
        }

        AuthResponse response = authService.refreshToken(refreshToken);

        log.info("Token refresh successful");
        return ResponseEntity.ok(response);
    }

    // Validate Token (for other microservices)
    @PostMapping("/validate")
    @Operation(summary = "Validate token from header (for internal microservice use)")
    public ResponseEntity<TokenValidationResponse> validateToken(HttpServletRequest request) {
        log.debug("Token validation request received");

        String authHeader = request.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);

        if (token == null) {
            log.warn("No token provided for validation");
            throw new UnauthenticatedException("No token provided");
        }

        TokenValidationResponse validation = authService.validateToken(token);

        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(validation.isValid())
                .message(validation.getMessage())
                .build();

        if (validation.isValid()) {
            response.setUserId(validation.getUserId());
            response.setUsername(validation.getUsername());
            response.setEmail(validation.getEmail());
            response.setRole(validation.getRole());
        }

        return ResponseEntity.ok(response);
    }

    // Logout
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout user")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        log.info("Logout request received");

        String authHeader = request.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);

        if (token != null) {
            authService.logout(token);
        }

        log.info("Logout processed successfully");
        return ResponseEntity.ok(new MessageResponse("Logout successful"));
    }

    // Change Password
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change user password")
    public ResponseEntity<MessageResponse> changePassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) throws AuthException {

        log.info("Password change request received");

        String authHeader = httpRequest.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);

        if (token == null) {
            throw new UnauthenticatedException("Authentication required");
        }

        UUID userId = jwtService.extractUserId(token);
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            throw new IllegalArgumentException("Current password and new password are required");
        }

        authService.changePassword(userId, currentPassword, newPassword);

        log.info("Password change successful for user ID: {}", userId);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    // Verify Email
    @PostMapping("/verify-email/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Verify user email")
    public ResponseEntity<MessageResponse> verifyEmail(@PathVariable UUID userId) throws AuthException {
        log.info("Email verification request received for user ID: {}", userId);
        authService.verifyEmail(userId);
        log.info("Email verification successful for user ID: {}", userId);
        return ResponseEntity.ok(new MessageResponse("Email verified successfully"));
    }

    // Get current user info (from token)
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current authenticated user info")
    public ResponseEntity<TokenValidationResponse> getCurrentUser(HttpServletRequest request) {
        log.debug("Current user info request received");

        String authHeader = request.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);

        if (token == null) {
            throw new UnauthenticatedException("Authentication required");
        }

        TokenValidationResponse validation = authService.validateToken(token);

        if (!validation.isValid()) {
            throw new InvalidTokenException("Invalid token");
        }

        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(validation.isValid())
                .message(validation.getMessage())
                .userId(validation.getUserId())
                .username(validation.getUsername())
                .email(validation.getEmail())
                .role(validation.getRole())
                .build();

        return ResponseEntity.ok(response);
    }
}
