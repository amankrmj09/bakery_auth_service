package com.blubugtech.bakery_auth_service.unit;

import com.blubugtech.bakery_auth_service.dto.auth.RegisterRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserProfileUpdateRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserResponse;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.exception.AuthException;
import com.blubugtech.bakery_auth_service.mapper.UserMapper;
import com.blubugtech.bakery_auth_service.repository.UserRepository;
import com.blubugtech.bakery_auth_service.service.dashboard.DashboardStatisticsService;
import com.blubugtech.bakery_auth_service.service.user.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DashboardStatisticsService dashboardStatisticsService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new User("testuser", "test@example.com", "encodedPassword", "Test", "User");
        testUser.setId(testUserId);
        testUser.setLoginAttempts(0);
        
        ReflectionTestUtils.setField(userService, "maxLoginAttempts", 5);
        ReflectionTestUtils.setField(userService, "lockoutDuration", 300000L);
    }

    @Test
    void createUser_Success() throws AuthException {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setUsername("newuser");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(request);

        // Assert
        assertThat(result.getUsername()).isEqualTo("newuser");
        verify(dashboardStatisticsService, times(1)).incrementUsers();
    }

    @Test
    void createUser_ThrowsWhenUsernameExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThrows(AuthException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserProfile_Success() throws AuthException {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        UserResponse response = new UserResponse();
        response.setUsername("testuser");
        when(userMapper.toResponse(testUser)).thenReturn(response);

        // Act
        UserResponse result = userService.getUserProfile(testUserId);

        // Assert
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void recordFailedLogin_LocksAccountWhenMaxAttemptsReached() {
        // Arrange
        testUser.setLoginAttempts(4); // Next attempt will be 5
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));

        // Act
        userService.recordFailedLogin("testuser");

        // Assert
        assertThat(testUser.getLoginAttempts()).isEqualTo(5);
        assertThat(testUser.getLockedUntil()).isNotNull();
        verify(userRepository, times(1)).save(testUser);
    }
    
    @Test
    void recordSuccessfulLogin_UnlocksAccountAndResetsAttempts() throws AuthException {
        // Arrange
        testUser.setLoginAttempts(3);
        testUser.setLockedUntil(LocalDateTime.now().plusDays(1));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        userService.recordSuccessfulLogin(testUserId);

        // Assert
        assertThat(testUser.getLoginAttempts()).isEqualTo(0);
        assertThat(testUser.getLockedUntil()).isNull();
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updatePassword_Success() throws AuthException {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        // Act
        userService.updatePassword(testUserId, "oldPassword", "newPassword");

        // Assert
        assertThat(testUser.getPassword()).isEqualTo("newEncodedPassword");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updatePassword_ThrowsOnInvalidOldPassword() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(AuthException.class, () -> userService.updatePassword(testUserId, "wrongPassword", "newPassword"));
        verify(userRepository, never()).save(any());
    }
}
