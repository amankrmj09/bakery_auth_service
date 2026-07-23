package com.blubugtech.bakery_auth_service.controller.publicapi;

import com.blubugtech.bakery_auth_service.dto.user.UserAddressRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserAddressResponse;
import com.blubugtech.bakery_auth_service.service.user.UserAddressService;
import com.blubugtech.bakery_auth_service.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/addresses")
@Tag(name = "User Addresses", description = "Endpoints for managing user delivery addresses")
public class UserAddressController {

    private final UserAddressService userAddressService;
    private final JwtService jwtService;

    public UserAddressController(UserAddressService userAddressService, JwtService jwtService) {
        this.userAddressService = userAddressService;
        this.jwtService = jwtService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all addresses for current user")
    public ResponseEntity<List<UserAddressResponse>> getUserAddresses(HttpServletRequest request) {
        UUID userId = extractUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userAddressService.getUserAddresses(userId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add a new address for current user")
    public ResponseEntity<UserAddressResponse> addAddress(
            @Valid @RequestBody UserAddressRequest addressRequest,
            HttpServletRequest request) {
        UUID userId = extractUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userAddressService.addAddress(userId, addressRequest));
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update an existing address")
    public ResponseEntity<UserAddressResponse> updateAddress(
            @PathVariable UUID addressId,
            @Valid @RequestBody UserAddressRequest addressRequest,
            HttpServletRequest request) {
        UUID userId = extractUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userAddressService.updateAddress(userId, addressId, addressRequest));
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete an address")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable UUID addressId,
            HttpServletRequest request) {
        UUID userId = extractUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        userAddressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{addressId}/default")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Set an address as default")
    public ResponseEntity<Void> setDefaultAddress(
            @PathVariable UUID addressId,
            HttpServletRequest request) {
        UUID userId = extractUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        userAddressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }

    private UUID extractUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);
        if (token == null || !jwtService.validateToken(token)) {
            return null;
        }
        return jwtService.extractUserId(token);
    }
}
