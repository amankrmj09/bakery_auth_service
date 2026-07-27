package com.blubugtech.bakery_auth_service.service.auth;

import com.blubugtech.bakery_auth_service.dto.auth.AuthResponse;
import com.blubugtech.bakery_auth_service.dto.auth.LoginRequest;
import com.blubugtech.bakery_auth_service.dto.auth.RegisterRequest;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.dto.auth.TokenValidationResponse;
import com.blubugtech.bakery_auth_service.service.user.UserService;
import com.blubugtech.bakery_auth_service.security.JwtService;
import com.blubugtech.bakery_auth_service.exception.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.blubugtech.common.event.UserEvent;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.blubugtech.bakery_auth_service.dto.auth.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    final private UserService userService;

    final private JwtService jwtService;
    
    final private KafkaTemplate<String, Object> kafkaTemplate;

    private final org.springframework.security.authentication.AuthenticationManager authenticationManager;

    private final AuthOtpService authOtpService;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${kafka.topic.user-events:user-events}")
    private String userEventsTopic;
    public AuthServiceImpl(UserService userService, JwtService jwtService, KafkaTemplate<String, Object> kafkaTemplate, org.springframework.security.authentication.AuthenticationManager authenticationManager, AuthOtpService authOtpService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.kafkaTemplate = kafkaTemplate;
        this.authenticationManager = authenticationManager;
        this.authOtpService = authOtpService;
        this.objectMapper = objectMapper;
    }

    // User registration
    public AuthResponse register(RegisterRequest request) throws AuthException {
        logger.info("Processing registration for username: {}", request.getUsername());

        try {
            // Create user through UserService
            User user = userService.createUser(request);

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            Long expiresIn = jwtService.getExpirationTime();

            logger.info("Registration successful for user: {}", user.getUsername());
            
            // Send welcome notification via Kafka
            try {
                com.blubugtech.common.contract.messaging.UserPayload payload = com.blubugtech.common.contract.messaging.UserPayload.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .action("REGISTERED")
                        .phoneNumber(user.getPhone())
                        .timestamp(java.time.LocalDateTime.now())
                        .build();
                UserEvent event = new UserEvent();
                event.setEventId(java.util.UUID.randomUUID().toString());
                event.setEventType("USER_REGISTERED");
                event.setTimestamp(java.time.Instant.now());
                event.setPayload(payload);
                kafkaTemplate.send(userEventsTopic, user.getId().toString(), event);
                logger.info("Published UserEvent for registered user: {}", user.getId());
            } catch (Exception ex) {
                logger.error("Failed to publish UserEvent: {}", ex.getMessage());
            }

            return AuthResponse.of(accessToken, refreshToken, expiresIn, user);

        } catch (Exception e) {
            logger.error("Registration failed for username: {} - {}", request.getUsername(), e.getMessage());
            throw new AuthException("Registration failed: " + e.getMessage());
        }
    }

    // User login
    public AuthResponse login(LoginRequest request) throws AuthException {
        logger.info("Processing login for user: {}", request.getUsernameOrEmail());

        try {
            // Check if account is locked
            if (userService.isAccountLocked(request.getUsernameOrEmail())) {
                throw new AccountLockedException("Account is locked due to too many failed login attempts. Please try again later.");
            }

            // Authenticate using AuthenticationManager
            org.springframework.security.core.Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(
                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                request.getUsernameOrEmail(),
                                request.getPassword()
                        )
                );
            } catch (org.springframework.security.core.AuthenticationException e) {
                // Record failed login attempt
                userService.recordFailedLogin(request.getUsernameOrEmail());
                throw new InvalidCredentialsException("Invalid credentials");
            }

            // Get user from authentication context
            com.blubugtech.bakery_auth_service.security.CustomUserDetails userDetails = 
                (com.blubugtech.bakery_auth_service.security.CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Record successful login
            userService.recordSuccessfulLogin(user.getId());

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            Long expiresIn = jwtService.getExpirationTime();

            logger.info("Login successful for user: {}", user.getUsername());

            return AuthResponse.of(accessToken, refreshToken, expiresIn, user);

        } catch (AuthException e) {
            logger.warn("Login failed for user: {} - {}", request.getUsernameOrEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for user: {} - {}", request.getUsernameOrEmail(), e.getMessage());
            throw new AuthException("Login failed due to an unexpected error");
        }
    }

    public String initiateRegister(RegisterRequest request) throws AuthException {
        logger.info("Initiating OTP registration for: {}", request.getEmail());
        try {
            if (userService.findByUsername(request.getUsername()).isPresent() || userService.findByEmail(request.getEmail()).isPresent()) {
                throw new AuthException("User already exists");
            }
            String requestJson = objectMapper.writeValueAsString(request);
            String otp = authOtpService.generateAndSaveRegisterOtp(request.getEmail(), requestJson);
            sendOtpEvent(null, request.getEmail(), request.getFirstName(), request.getLastName(), request.getPhone(), otp);
            return otp;
        } catch (Exception e) {
            throw new AuthException("Failed to initiate registration");
        }
    }

    public AuthResponse verifyRegister(RegisterVerifyRequest request) throws AuthException {
        try {
            String requestJson = authOtpService.verifyRegisterOtp(request.getEmail(), request.getOtp());
            if (requestJson == null) {
                throw new InvalidTokenException("Invalid or expired OTP");
            }
            RegisterRequest registerRequest = objectMapper.readValue(requestJson, RegisterRequest.class);
            return register(registerRequest); // Re-use existing register flow
        } catch (Exception e) {
            throw new AuthException("OTP Verification failed");
        }
    }

    public String resendRegisterOtp(String email) throws AuthException {
        logger.info("Resending OTP registration for: {}", email);
        try {
            String otp = authOtpService.resendRegisterOtp(email);
            if (otp == null) {
                throw new InvalidTokenException("Registration session expired or not found. Please register again.");
            }
            sendOtpEvent(null, email, null, null, null, otp);
            return otp;
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException("Failed to resend registration OTP");
        }
    }

    public String initiateLogin(LoginRequest request) throws AuthException {
        try {
            if (userService.isAccountLocked(request.getUsernameOrEmail())) {
                throw new AccountLockedException("Account locked");
            }
            // Verify password for 2FA
            org.springframework.security.core.Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(
                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                request.getUsernameOrEmail(), request.getPassword()
                        )
                );
            } catch (org.springframework.security.core.AuthenticationException e) {
                userService.recordFailedLogin(request.getUsernameOrEmail());
                throw new InvalidCredentialsException("Invalid credentials");
            }
            com.blubugtech.bakery_auth_service.security.CustomUserDetails userDetails = 
                (com.blubugtech.bakery_auth_service.security.CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            String otp = authOtpService.generateAndSaveLoginOtp(user.getEmail());
            sendOtpEvent(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhone(), otp);
            return otp;
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException("Failed to initiate login");
        }
    }

    public AuthResponse verifyLogin(LoginVerifyRequest request) throws AuthException {
        Optional<User> userOptional = userService.findByUsername(request.getUsernameOrEmail());
        if (userOptional.isEmpty()) {
            userOptional = userService.findByEmail(request.getUsernameOrEmail());
        }
        if (userOptional.isEmpty()) throw new UserNotFoundException("User not found");
        User user = userOptional.get();

        if (!authOtpService.verifyLoginOtp(user.getEmail(), request.getOtp())) {
            throw new InvalidTokenException("Invalid or expired OTP");
        }

        userService.recordSuccessfulLogin(user.getId());
        
        try {
            String ipAddress = "Unknown IP";
            org.springframework.web.context.request.RequestAttributes requestAttributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                jakarta.servlet.http.HttpServletRequest httpRequest = ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes).getRequest();
                ipAddress = httpRequest.getHeader("X-Forwarded-For");
                if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = httpRequest.getRemoteAddr();
                }
            }

            com.blubugtech.common.contract.messaging.UserPayload payload = com.blubugtech.common.contract.messaging.UserPayload.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .action("NEW_SIGN_IN")
                    .ipAddress(ipAddress)
                    .location("Unknown Location") // Typically you'd use a GeoIP service here
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
            com.blubugtech.common.event.UserEvent event = new com.blubugtech.common.event.UserEvent();
            event.setEventId(java.util.UUID.randomUUID().toString());
            event.setEventType("NEW_SIGN_IN");
            event.setTimestamp(java.time.Instant.now());
            event.setPayload(payload);
            kafkaTemplate.send(userEventsTopic, user.getId().toString(), event);
            logger.info("Published NEW_SIGN_IN event for user: {}", user.getUsername());
        } catch (Exception ex) {
            logger.error("Failed to publish NEW_SIGN_IN event for user: {}", user.getUsername(), ex);
        }
        
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthResponse.of(accessToken, refreshToken, jwtService.getExpirationTime(), user);
    }

    public String resendLoginOtp(String usernameOrEmail) throws AuthException {
        logger.info("Resending login OTP for: {}", usernameOrEmail);
        try {
            Optional<User> userOptional = userService.findByUsername(usernameOrEmail);
            if (userOptional.isEmpty()) {
                userOptional = userService.findByEmail(usernameOrEmail);
            }
            if (userOptional.isEmpty()) {
                throw new UserNotFoundException("User not found");
            }
            User user = userOptional.get();
            String otp = authOtpService.resendLoginOtp(user.getEmail());
            if (otp == null) {
                throw new InvalidTokenException("Login session expired or not found. Please login again.");
            }
            sendOtpEvent(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhone(), otp);
            return otp;
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException("Failed to resend login OTP");
        }
    }

    public String initiateForgotPassword(ForgotPasswordRequest request) throws AuthException {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) throw new UserNotFoundException("User not found");
        User user = userOpt.get();
        String otp = authOtpService.generateAndSaveResetOtp(request.getEmail());
        sendOtpEvent(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhone(), otp);
        return otp;
    }

    public void resetPassword(ResetPasswordRequest request) throws AuthException {
        if (!authOtpService.verifyResetOtp(request.getEmail(), request.getOtp())) {
            throw new InvalidTokenException("Invalid or expired OTP");
        }
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) throw new UserNotFoundException("User not found");
        // For reset, we update without requiring current password. We need a method in userService for direct password update or use existing if it doesn't strictly check old password. 
        // We will call the repo directly or add a direct update method to userService.
        User user = userOpt.get();
        // Since we are inside auth service, we can use userService.resetPassword (assuming we create it) or just update it via another means.
        userService.resetPassword(user.getId(), request.getNewPassword());
    }

    // Refresh token
    public AuthResponse refreshToken(String refreshToken) throws AuthException {
        logger.info("Processing token refresh");

        try {
            // Validate refresh token format
            if (!jwtService.validateToken(refreshToken)) {
                throw new InvalidTokenException("Invalid refresh token");
            }

            // Check if it's actually a refresh token
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new InvalidTokenException("Token is not a refresh token");
            }

            // Check if refresh token is expired
            if (jwtService.isTokenExpired(refreshToken)) {
                throw new TokenExpiredException("Refresh token is expired");
            }

            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshToken);

            // Find user
            Optional<User> userOptional = userService.findByUsername(username);
            if (userOptional.isEmpty()) {
                throw new UserNotFoundException("User not found");
            }

            User user = userOptional.get();

            // Check if user is still active
            if (!user.isActive()) {
                throw new AccountLockedException("Account is not active");
            }

            // Generate new tokens
            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            Long expiresIn = jwtService.getExpirationTime();

            logger.info("Token refresh successful for user: {}", user.getUsername());

            return AuthResponse.of(newAccessToken, newRefreshToken, expiresIn, user);

        } catch (AuthException e) {
            logger.warn("Token refresh failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during token refresh: {}", e.getMessage());
            throw new AuthException("Token refresh failed");
        }
    }

    // Validate token (for other microservices)
    public TokenValidationResponse validateToken(String token) {
        logger.debug("Validating token");

        try {
            // Basic token validation
            if (!jwtService.validateToken(token)) {
                return TokenValidationResponse.invalid("Invalid token");
            }

            // Check if it's an access token
            if (!jwtService.isAccessToken(token)) {
                return TokenValidationResponse.invalid("Not an access token");
            }

            // Extract user information
            String username = jwtService.extractUsername(token);
            UUID userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);
            String email = jwtService.extractEmail(token);

            // Verify user still exists and is active
            Optional<User> userOptional = userService.findById(userId);
            if (userOptional.isEmpty()) {
                return TokenValidationResponse.invalid("User not found");
            }

            User user = userOptional.get();
            if (!user.isActive()) {
                return TokenValidationResponse.invalid("User account is not active");
            }

            logger.debug("Token validation successful for user: {}", username);

            return TokenValidationResponse.valid(userId, username, email, role);

        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            return TokenValidationResponse.invalid("Token validation failed");
        }
    }

    // Logout (optional - for token blacklisting if needed)
    public void logout(String token) {
        logger.info("Processing logout");

        try {
            String username = jwtService.extractUsername(token);
            logger.info("Logout successful for user: {}", username);

            // In a production system, you might want to blacklist the token here
            // For now, we'll just log the logout

        } catch (Exception e) {
            logger.warn("Logout processing failed: {}", e.getMessage());
        }
    }

    // Change password (authenticated user)
    public void changePassword(UUID userId, String currentPassword, String newPassword) throws AuthException {
        logger.info("Processing password change for user ID: {}", userId);

        try {
            userService.updatePassword(userId, currentPassword, newPassword);
            logger.info("Password change successful for user ID: {}", userId);
            
            // Send password change notification
            try {
                Optional<User> userOpt = userService.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    Map<String, Object> notificationReq = new HashMap<>();
                    notificationReq.put("type", "EMAIL");
                    notificationReq.put("recipientEmail", user.getEmail());
                    com.blubugtech.common.contract.messaging.UserPayload payload = com.blubugtech.common.contract.messaging.UserPayload.builder()
                            .userId(user.getId())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .action("PASSWORD_CHANGED")
                            .phoneNumber(user.getPhone())
                            .timestamp(java.time.LocalDateTime.now())
                            .build();
                    UserEvent event = new UserEvent();
                    event.setEventId(java.util.UUID.randomUUID().toString());
                    event.setEventType("PASSWORD_CHANGED");
                    event.setTimestamp(java.time.Instant.now());
                    event.setPayload(payload);
                    kafkaTemplate.send(userEventsTopic, user.getId().toString(), event);
                    logger.info("Published UserEvent for password change: {}", user.getId());
                }
            } catch (Exception ex) {
                logger.error("Failed to send password change notification: {}", ex.getMessage());
            }

        } catch (Exception e) {
            logger.error("Password change failed for user ID: {} - {}", userId, e.getMessage());
            throw new AuthException("Password change failed: " + e.getMessage());
        }
    }

    // Verify email (if you implement email verification)
    public void verifyEmail(UUID userId) throws AuthException {
        logger.info("Processing email verification for user ID: {}", userId);

        try {
            userService.verifyEmail(userId);
            logger.info("Email verification successful for user ID: {}", userId);

        } catch (Exception e) {
            logger.error("Email verification failed for user ID: {} - {}", userId, e.getMessage());
            throw new AuthException("Email verification failed");
        }
    }


    private void sendOtpEvent(UUID userId, String email, String firstName, String lastName, String phone, String otp) {
        try {
            com.blubugtech.common.contract.messaging.UserPayload payload = com.blubugtech.common.contract.messaging.UserPayload.builder()
                    .userId(userId)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .action("OTP_REQUESTED")
                    .phoneNumber(phone)
                    .otpCode(otp)
                    .expiryMinutes(10) // Fixed matching AuthOtpService validity
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
            UserEvent event = new UserEvent();
            event.setEventId(java.util.UUID.randomUUID().toString());
            event.setEventType("OTP_REQUESTED");
            event.setTimestamp(java.time.Instant.now());
            event.setPayload(payload);
            
            String key = userId != null ? userId.toString() : email;
            kafkaTemplate.send(userEventsTopic, key, event);
            logger.info("Published UserEvent for OTP request to email: {}", email);
        } catch (Exception ex) {
            logger.error("Failed to publish OTP event: {}", ex.getMessage());
        }
    }
}
