package com.blubugtech.bakery_auth_service.dto.user;

import com.blubugtech.bakery_auth_service.entity.User;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull User.Role role
) {}
