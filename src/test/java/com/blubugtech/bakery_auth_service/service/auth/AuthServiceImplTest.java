package com.blubugtech.bakery_auth_service.service.auth;

import com.blubugtech.bakery_auth_service.dto.auth.AuthResponse;
import com.blubugtech.bakery_auth_service.dto.auth.LoginRequest;
import com.blubugtech.bakery_auth_service.dto.auth.RegisterRequest;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.exception.AuthException;
import com.blubugtech.bakery_auth_service.security.CustomUserDetails;
import com.blubugtech.bakery_auth_service.security.JwtService;
import com.blubugtech.bakery_auth_service.service.user.UserAccountSecurityService;
import com.blubugtech.bakery_auth_service.service.user.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private UserAccountSecurityService userAccountSecurityService;

    @Mock
    private JwtService jwtService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthOtpService authOtpService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password", "Test", "User");
        testUser.setId(UUID.randomUUID());
        testUser.setRole(User.Role.USER);

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password");
    }

    @Test
    void register_Success() throws Exception {
        // Arrange
        when(userAccountSecurityService.createUser(any(RegisterRequest.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(testUser)).thenReturn("mockAccessToken");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("mockRefreshToken");
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mockAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("mockRefreshToken");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");

        verify(userAccountSecurityService, times(1)).createUser(any(RegisterRequest.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void register_ThrowsAuthException() throws Exception {
        // Arrange
        when(userAccountSecurityService.createUser(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(AuthException.class, () -> authService.register(registerRequest));
        verify(userAccountSecurityService, times(1)).createUser(any(RegisterRequest.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void login_Success() throws Exception {
        // Arrange
        when(userAccountSecurityService.isAccountLocked(anyString())).thenReturn(false);

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateAccessToken(testUser)).thenReturn("mockAccessToken");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("mockRefreshToken");
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mockAccessToken");
        verify(userAccountSecurityService, times(1)).recordSuccessfulLogin(testUser.getId());
    }
}
