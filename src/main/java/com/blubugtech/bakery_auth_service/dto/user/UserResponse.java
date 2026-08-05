package com.blubugtech.bakery_auth_service.dto.user;

import com.blubugtech.bakery_auth_service.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private User.Role role;
    private User.UserStatus status;
    @Builder.Default
    private Boolean emailVerified = false;
    private LocalDateTime lastLogin;
    @Builder.Default
    private Boolean twoFactorEnabled = false;
    @Builder.Default
    private Boolean loginNotificationsEnabled = false;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

}
