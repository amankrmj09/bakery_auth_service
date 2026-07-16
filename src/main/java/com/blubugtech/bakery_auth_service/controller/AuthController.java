package com.blubugtech.bakery_auth_service.controller;

import com.blubugtech.bakery_auth_service.dto.AuthResponseDto;
import com.blubugtech.bakery_auth_service.dto.LoginRequestDto;
import com.blubugtech.bakery_auth_service.dto.RegisterRequestDto;
import com.blubugtech.bakery_auth_service.exception.AuthException;
import com.blubugtech.bakery_auth_service.service.AuthService;
import com.blubugtech.bakery_auth_service.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import com.blubugtech.common.dto.MessageResponseDto;
import com.blubugtech.common.dto.HealthResponseDto;
import com.blubugtech.bakery_auth_service.dto.TokenValidationResponseDto;
import com.blubugtech.common.exception.UnauthenticatedException;
import com.blubugtech.common.exception.InvalidTokenException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and authorization")

public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    final private AuthService authService;

    final private JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    // User Registration
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) throws AuthException {
        logger.info("Registration request received for username: {}", request.getUsername());

        AuthResponseDto response = authService.register(request);

        logger.info("Registration successful for username: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // User Login
    @PostMapping("/login")
    @Operation(summary = "Login and get tokens")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) throws AuthException {
        logger.info("Login request received for user: {}", request.getUsernameOrEmail());

        AuthResponseDto response = authService.login(request);

        logger.info("Login successful for user: {}", request.getUsernameOrEmail());
        return ResponseEntity.ok(response);
    }

    // Refresh Token
    @PostMapping("/refresh")
    @Operation(summary = "Refresh authentication token from header")
    public ResponseEntity<AuthResponseDto> refreshToken(HttpServletRequest request) throws AuthException {
        logger.info("Token refresh request received");

        String authHeader = request.getHeader("Authorization");
        String refreshToken = jwtService.extractTokenFromHeader(authHeader);

        if (refreshToken == null) {
            logger.warn("No refresh token provided in Authorization header");
            return ResponseEntity.badRequest().build();
        }

        AuthResponseDto response = authService.refreshToken(refreshToken);

        logger.info("Token refresh successful");
        return ResponseEntity.ok(response);
    }

    // Alternative refresh token endpoint (from request body)
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh authentication token from request body")
    public ResponseEntity<AuthResponseDto> refreshTokenFromBody(@RequestBody Map<String, String> request) throws AuthException {
        logger.info("Token refresh request received (from body)");

        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            logger.warn("No refresh token provided in request body");
            return ResponseEntity.badRequest().build();
        }

        AuthResponseDto response = authService.refreshToken(refreshToken);

        logger.info("Token refresh successful (from body)");
        return ResponseEntity.ok(response);
    }

    // Validate Token (for other microservices)
    @PostMapping("/validate")
    @Operation(summary = "Validate token from header (for internal microservice use)")
    public ResponseEntity<TokenValidationResponseDto> validateToken(HttpServletRequest request) {
        logger.debug("Token validation request received");

        String authHeader = request.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);

        if (token == null) {
            logger.warn("No token provided for validation");
            throw new UnauthenticatedException("No token provided");
        }

        AuthService.TokenValidationResponse validation = authService.validateToken(token);

        TokenValidationResponseDto response = TokenValidationResponseDto.builder()
                .valid(validation.isValid())
                .message(validation.getMessage())
                .build();

        if (validation.isValid()) {
            response.setUserId(validation.getUserId());
            response.setUsername(validation.getUsername());
            response.setEmail(validation.getEmail());
            response.setRole(validation.getRole());
            logger.debug("Token validation successful for user: {}", validation.getUsername());
        } else {
            logger.debug("Token validation failed: {}", validation.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // Alternative validate token endpoint (from request body)
    @PostMapping("/validate-token")
    @Operation(summary = "Validate token from body (for internal microservice use)")
    public ResponseEntity<TokenValidationResponseDto> validateTokenFromBody(@RequestBody Map<String, String> request) {
        logger.debug("Token validation request received (from body)");

        String token = request.get("token");
        if (token == null || token.trim().isEmpty()) {
            logger.warn("No token provided for validation");
            throw new UnauthenticatedException("No token provided");
        }

        AuthService.TokenValidationResponse validation = authService.validateToken(token);

        TokenValidationResponseDto response = TokenValidationResponseDto.builder()
                .valid(validation.isValid())
                .message(validation.getMessage())
                .build();

        if (validation.isValid()) {
            response.setUserId(validation.getUserId());
            response.setUsername(validation.getUsername());
            response.setEmail(validation.getEmail());
            response.setRole(validation.getRole());
            logger.debug("Token validation successful for user: {}", validation.getUsername());
        } else {
            logger.debug("Token validation failed: {}", validation.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // Logout
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout user")
    public ResponseEntity<MessageResponseDto> logout(HttpServletRequest request) {
        logger.info("Logout request received");

        String authHeader = request.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);

        if (token != null) {
            authService.logout(token);
        }

        logger.info("Logout processed successfully");
        return ResponseEntity.ok(new MessageResponseDto("Logout successful"));
    }

    // Change Password
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change user password")
    public ResponseEntity<MessageResponseDto> changePassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) throws AuthException {

        logger.info("Password change request received");

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

        logger.info("Password change successful for user ID: {}", userId);
        return ResponseEntity.ok(new MessageResponseDto("Password changed successfully"));
    }

    // Verify Email
    @PostMapping("/verify-email/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Verify user email")
    public ResponseEntity<MessageResponseDto> verifyEmail(@PathVariable UUID userId) throws AuthException {
        logger.info("Email verification request received for user ID: {}", userId);

        authService.verifyEmail(userId);

        logger.info("Email verification successful for user ID: {}", userId);
        return ResponseEntity.ok(new MessageResponseDto("Email verified successfully"));
    }

    // Health check endpoint
    @GetMapping("/health")
    @Operation(summary = "Check service health")
    public ResponseEntity<HealthResponseDto> health() {
        return ResponseEntity.ok(new HealthResponseDto("UP", "bakery-auth-service"));
    }

    // Get current user info (from token)
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current authenticated user info")
    public ResponseEntity<TokenValidationResponseDto> getCurrentUser(HttpServletRequest request) {
        logger.debug("Current user info request received");

        String authHeader = request.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);

        if (token == null) {
            throw new UnauthenticatedException("Authentication required");
        }

        AuthService.TokenValidationResponse validation = authService.validateToken(token);

        if (!validation.isValid()) {
            throw new InvalidTokenException("Invalid token");
        }

        TokenValidationResponseDto response = TokenValidationResponseDto.builder()
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
