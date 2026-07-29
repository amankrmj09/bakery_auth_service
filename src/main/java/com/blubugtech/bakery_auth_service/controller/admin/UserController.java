package com.blubugtech.bakery_auth_service.controller.admin;

import com.blubugtech.bakery_auth_service.dto.user.UserProfileUpdateRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserResponse;
import com.blubugtech.bakery_auth_service.dto.user.UpdateRoleRequest;
import com.blubugtech.bakery_auth_service.dto.user.UpdateStatusRequest;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.exception.AuthException;
import com.blubugtech.bakery_auth_service.service.dashboard.DashboardStatisticsService;
import com.blubugtech.bakery_auth_service.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blubakery.common.feign.contract.feign.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for managing users and profiles")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    private final DashboardStatisticsService dashboardStatisticsService;

    // Get user profile
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserResponse> getUserProfile(Authentication authentication) throws AuthException {
        log.info("Get user profile request received");

        UUID userId = UUID.fromString(authentication.getName());

        UserResponse userResponse = userService.getUserProfile(userId);

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

        UserResponse userResponse = userService.updateUserProfile(userId, request);

        log.info("User profile updated for user ID: {}", userId);
        return ResponseEntity.ok(userResponse);
    }

    // Get user by ID (Admin or self only)
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM') or #userId.toString() == authentication.name")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable UUID userId) throws AuthException {

        log.info("Get user by ID request received for user ID: {}", userId);

        UserResponse userResponse = userService.getUserProfile(userId);

        log.info("User retrieved for user ID: {}", userId);
        return ResponseEntity.ok(userResponse);
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin)")
    public ResponseEntity<PagedModel<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Get all users request received (admin)");

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> users = userService.getAllUsers(pageable);

        log.info("All users retrieved, count: {}", users.getContent().size());
        return ResponseEntity.ok(new PagedModel<>(users));
    }

    // Search users (Admin only)
    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users by query (Admin)")
    public ResponseEntity<PagedModel<UserResponse>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Search users request received (admin) with query: {}", query);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> users = userService.searchUsers(query, pageable);

        log.info("User search completed, results: {}", users.getContent().size());
        return ResponseEntity.ok(new PagedModel<>(users));
    }

    // Get users by role (Admin only)
    @GetMapping("/admin/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role (Admin)")
    public ResponseEntity<PagedModel<UserResponse>> getUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Get users by role request received (admin) for role: {}", role);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        User.Role userRole = User.Role.valueOf(role.toUpperCase());
        Page<UserResponse> users = userService.getUsersByRole(userRole, pageable);

        log.info("Users by role retrieved, count: {}", users.getContent().size());
        return ResponseEntity.ok(new PagedModel<>(users));
    }

    // Update user role (Admin only)
    @PutMapping("/admin/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role (Admin)")
    public ResponseEntity<MessageResponse> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateRoleRequest request) {

        log.info("Update user role request received (admin) for user ID: {}", userId);

        userService.updateUserRole(userId, request.role());

        log.info("User role updated to {} for user ID: {}", request.role(), userId);
        return ResponseEntity.ok(new MessageResponse("User role updated successfully"));
    }

    // Update user status (Admin only)
    @PutMapping("/admin/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status (Admin)")
    public ResponseEntity<MessageResponse> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateStatusRequest request) {

        log.info("Update user status request received (admin) for user ID: {}", userId);

        userService.updateUserStatus(userId, request.status());

        log.info("User status updated to {} for user ID: {}", request.status(), userId);
        return ResponseEntity.ok(new MessageResponse("User status updated successfully"));
    }

    // Unlock user account (Admin only)
    @PostMapping("/admin/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlock user account (Admin)")
    public ResponseEntity<MessageResponse> unlockUserAccount(@PathVariable UUID userId) throws AuthException {
        log.info("Unlock user account request received (admin) for user ID: {}", userId);

        userService.unlockAccount(userId);

        log.info("User account unlocked for user ID: {}", userId);
        return ResponseEntity.ok(new MessageResponse("User account unlocked successfully"));
    }

    // Delete user (Admin only)
    @DeleteMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (Admin)")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable UUID userId) throws AuthException {
        log.info("Delete user request received (admin) for user ID: {}", userId);

        userService.deleteUser(userId);

        log.info("User deleted for user ID: {}", userId);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }

    // Get user statistics (Admin only)
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user statistics (Admin)")
    public ResponseEntity<Map<String, Long>> getUserStatistics() {
        log.info("Get user statistics request received (admin)");

        Map<String, Long> statistics = userService.getUserStatistics();

        log.info("User statistics retrieved");
        return ResponseEntity.ok(statistics);
    }

    // Get central dashboard statistics (Admin only)
    @GetMapping("/admin/dashboard-stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get dashboard statistics (Admin)")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestParam(defaultValue = "1m") String timeframe) {
        log.info("Get dashboard statistics request received (admin) for timeframe: {}", timeframe);
        Map<String, Object> stats = dashboardStatisticsService.getStatisticsWithGrowth(timeframe);
        return ResponseEntity.ok(stats);
    }
}
