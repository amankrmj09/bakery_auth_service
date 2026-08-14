package com.blubugtech.bakery_auth_service.method;

import com.blubugtech.bakery_auth_service.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class UserEntityTest {

    @Test
    @DisplayName("getFullName should return first and last name when both are present")
    void testGetFullName_WithFirstAndLastName() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");

        assertThat(user.getFullName()).isEqualTo("John Doe");
    }

    @ParameterizedTest
    @DisplayName("getFullName should handle missing names gracefully")
    @CsvSource({
            "John, , John",
            " , Doe, Doe",
            " , , testuser" // Falls back to username if both are missing
    })
    void testGetFullName_WithMissingNames(String firstName, String lastName, String expectedFullName) {
        User user = new User();
        user.setUsername("testuser");
        user.setFirstName(firstName);
        user.setLastName(lastName);

        assertThat(user.getFullName()).isEqualTo(expectedFullName);
    }

    @ParameterizedTest
    @DisplayName("isActive should correctly compute status based on UserStatus and lock expiration")
    @CsvSource({
            "ACTIVE, false, true",
            "INACTIVE, false, false",
            "LOCKED, false, false",
            "ACTIVE, true, false" // Active but account is locked via lockedUntil date
    })
    void testIsActive_BoundaryConditions(User.UserStatus status, boolean isLocked, boolean expectedIsActive) {
        User user = new User();
        user.setStatus(status);

        if (isLocked) {
            user.setLockedUntil(java.time.LocalDateTime.now().plusDays(1));
        } else {
            user.setLockedUntil(java.time.LocalDateTime.now().minusDays(1)); // lock expired
        }

        assertThat(user.isActive()).isEqualTo(expectedIsActive);
    }
}
