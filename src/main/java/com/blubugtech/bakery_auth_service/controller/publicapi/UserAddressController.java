package com.blubugtech.bakery_auth_service.controller.publicapi;

import com.blubugtech.bakery_auth_service.dto.user.UserAddressRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserAddressResponse;
import com.blubugtech.bakery_auth_service.service.user.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/addresses")
@Tag(name = "User Addresses", description = "Endpoints for managing user delivery addresses")
public class UserAddressController {

    private final UserAddressService userAddressService;

    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all addresses for current user")
    public ResponseEntity<List<UserAddressResponse>> getUserAddresses(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(userAddressService.getUserAddresses(userId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add a new address for current user")
    public ResponseEntity<UserAddressResponse> addAddress(
            @Valid @RequestBody UserAddressRequest addressRequest,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(userAddressService.addAddress(userId, addressRequest));
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update an existing address")
    public ResponseEntity<UserAddressResponse> updateAddress(
            @PathVariable UUID addressId,
            @Valid @RequestBody UserAddressRequest addressRequest,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(userAddressService.updateAddress(userId, addressId, addressRequest));
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete an address")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable UUID addressId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        userAddressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{addressId}/default")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Set an address as default")
    public ResponseEntity<Void> setDefaultAddress(
            @PathVariable UUID addressId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        userAddressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }
}
