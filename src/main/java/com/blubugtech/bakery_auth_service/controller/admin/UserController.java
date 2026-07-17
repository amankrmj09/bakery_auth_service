package com.blubugtech.bakery_auth_service.controller.admin;

import com.blubugtech.bakery_auth_service.dto.auth.RegisterRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserResponse;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.exception.AuthException;
import com.blubugtech.bakery_auth_service.security.JwtService;
import com.blubugtech.bakery_auth_service.service.dashboard.DashboardStatisticsService;
import com.blubugtech.bakery_auth_service.service.user.UserService;
import com.blubugtech.common.contract.feign.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for managing users and profiles")

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private UserService userService;

    private JwtService jwtService;

    private DashboardStatisticsService dashboardStatisticsService;

    // Get user profile
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserResponse> getUserProfile(HttpServletRequest request) throws AuthException {
        logger.info("Get user profile request received");

        UUID userId = extractUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        UserResponse userResponse = userService.getUserProfile(userId);

        logger.info("User profile retrieved for user ID: {}", userId);
        return ResponseEntity.ok(userResponse);
    }

    // Update user profile
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserResponse> updateUserProfile(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) throws AuthException {

        logger.info("Update user profile request received");

        UUID userId = extractUserIdFromToken(httpRequest);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        UserResponse userResponse = userService.updateUserProfile(userId, request);

        logger.info("User profile updated for user ID: {}", userId);
        return ResponseEntity.ok(userResponse);
    }

    // Get user by ID (Admin or self only)
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable UUID userId,
            HttpServletRequest request) throws AuthException {

        logger.info("Get user by ID request received for user ID: {}", userId);

        UUID requestingUserId = extractUserIdFromToken(request);
        String requestingUserRole = extractRoleFromToken(request);

        // Allow if requesting own profile or if admin
        if (!userId.equals(requestingUserId) && (requestingUserRole == null || !requestingUserRole.equalsIgnoreCase("ADMIN"))) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        UserResponse userResponse = userService.getUserProfile(userId);

        logger.info("User retrieved for user ID: {}", userId);
        return ResponseEntity.ok(userResponse);
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.info("Get all users request received (admin)");

        List<UserResponse> users = userService.getAllUsers();

        logger.info("All users retrieved, count: {}", users.size());
        return ResponseEntity.ok(users);
    }

    // Search users (Admin only)
    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users by query (Admin)")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        logger.info("Search users request received (admin) with query: {}", query);

        List<UserResponse> users = userService.searchUsers(query);

        logger.info("User search completed, results: {}", users.size());
        return ResponseEntity.ok(users);
    }

    // Get users by role (Admin only)
    @GetMapping("/admin/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role (Admin)")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String role) {
        logger.info("Get users by role request received (admin) for role: {}", role);

        User.Role userRole = User.Role.valueOf(role.toUpperCase());
        List<UserResponse> users = userService.getUsersByRole(userRole);

        logger.info("Users by role retrieved, count: {}", users.size());
        return ResponseEntity.ok(users);
    }

    // Update user role (Admin only)
    @PutMapping("/admin/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role (Admin)")
    public ResponseEntity<MessageResponse> updateUserRole(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> request) {

        logger.info("Update user role request received (admin) for user ID: {}", userId);

        String roleStr = request.get("role");
        if (roleStr == null) {
            return ResponseEntity.badRequest().build();
        }

        User.Role role = User.Role.valueOf(roleStr.toUpperCase());
        userService.updateUserRole(userId, role);

        logger.info("User role updated to {} for user ID: {}", role, userId);
        return ResponseEntity.ok(new MessageResponse("User role updated successfully"));
    }

    // Update user status (Admin only)
    @PutMapping("/admin/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status (Admin)")
    public ResponseEntity<MessageResponse> updateUserStatus(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> request) {

        logger.info("Update user status request received (admin) for user ID: {}", userId);

        String statusStr = request.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().build();
        }

        User.UserStatus status = User.UserStatus.valueOf(statusStr.toUpperCase());
        userService.updateUserStatus(userId, status);

        logger.info("User status updated to {} for user ID: {}", status, userId);
        return ResponseEntity.ok(new MessageResponse("User status updated successfully"));
    }

    // Unlock user account (Admin only)
    @PostMapping("/admin/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlock user account (Admin)")
    public ResponseEntity<MessageResponse> unlockUserAccount(@PathVariable UUID userId) throws AuthException {
        logger.info("Unlock user account request received (admin) for user ID: {}", userId);

        userService.unlockAccount(userId);

        logger.info("User account unlocked for user ID: {}", userId);
        return ResponseEntity.ok(new MessageResponse("User account unlocked successfully"));
    }

    // Delete user (Admin only)
    @DeleteMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (Admin)")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable UUID userId) throws AuthException {
        logger.info("Delete user request received (admin) for user ID: {}", userId);

        userService.deleteUser(userId);

        logger.info("User deleted for user ID: {}", userId);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }

    // Get user statistics (Admin only)
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user statistics (Admin)")
    public ResponseEntity<Map<String, Long>> getUserStatistics() {
        logger.info("Get user statistics request received (admin)");

        Map<String, Long> statistics = userService.getUserStatistics();

        logger.info("User statistics retrieved");
        return ResponseEntity.ok(statistics);
    }

    // Get central dashboard statistics (Admin only)
    @GetMapping("/admin/dashboard-stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get dashboard statistics (Admin)")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestParam(defaultValue = "1m") String timeframe) {
        logger.info("Get dashboard statistics request received (admin) for timeframe: {}", timeframe);
        Map<String, Object> stats = dashboardStatisticsService.getStatisticsWithGrowth(timeframe);
        return ResponseEntity.ok(stats);
    }

    // Utility methods
    private UUID extractUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);

        if (token == null || !jwtService.validateToken(token)) {
            return null;
        }

        return jwtService.extractUserId(token);
    }

    private String extractRoleFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);

        if (token == null || !jwtService.validateToken(token)) {
            return null;
        }

        return jwtService.extractRole(token);
    }
}
