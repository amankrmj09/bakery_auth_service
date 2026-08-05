package com.blubugtech.bakery_auth_service.dto.user;

import com.blubugtech.bakery_auth_service.entity.User;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull User.UserStatus status
) {
}
