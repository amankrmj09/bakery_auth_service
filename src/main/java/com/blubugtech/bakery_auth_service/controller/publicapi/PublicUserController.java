package com.blubugtech.bakery_auth_service.controller.publicapi;

import com.blubugtech.bakery_auth_service.dto.user.UserProfileUpdateRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserResponse;
import com.blubugtech.bakery_auth_service.exception.AuthException;
import com.blubugtech.bakery_auth_service.service.user.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile", description = "Endpoints for managing user profile")
@RequiredArgsConstructor
@Slf4j
public class PublicUserController {

    private final UserProfileService userProfileService;

    // Get user profile
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserResponse> getUserProfile(Authentication authentication) throws AuthException {
        log.info("Get user profile request received");
        UUID userId = UUID.fromString(authentication.getName());
        UserResponse userResponse = userProfileService.getUserProfile(userId);
        log.info("User profile retrieved for user ID: {}", userId);
        return ResponseEntity.ok(userResponse);
    }

    // Update user profile
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserResponse> updateUserProfile(
            @Valid @RequestBody UserProfileUpdateRequest request,
            Authentication authentication) throws AuthException {
        log.info("Update user profile request received");
        UUID userId = UUID.fromString(authentication.getName());
        UserResponse userResponse = userProfileService.updateUserProfile(userId, request);
        log.info("User profile updated for user ID: {}", userId);
        return ResponseEntity.ok(userResponse);
    }
}
