package com.blubugtech.bakery_auth_service.unit;

import com.blubugtech.bakery_auth_service.constant.AuthConstants;
import com.blubugtech.bakery_auth_service.service.auth.AuthOtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthOtpServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthOtpService authOtpService;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_JSON = "{\"username\":\"test\"}";

    @BeforeEach
    void setUp() {
        // leniency to avoid UnnecessaryStubbingException if a test doesn't use it
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void generateAndSaveRegisterOtp_Success() {
        // Act
        String otp = authOtpService.generateAndSaveRegisterOtp(TEST_EMAIL, TEST_JSON);

        // Assert
        assertThat(otp).isNotNull();
        assertThat(otp).hasSize(6);
        assertThat(otp).matches("\\d{6}");

        verify(valueOperations, times(1)).set(
                eq(AuthConstants.REG_PREFIX + TEST_EMAIL),
                eq(otp + "|" + TEST_JSON),
                eq(10L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void verifyRegisterOtp_Success() {
        // Arrange
        String validOtp = "123456";
        String storedData = validOtp + "|" + TEST_JSON;
        when(valueOperations.get(AuthConstants.REG_PREFIX + TEST_EMAIL)).thenReturn(storedData);

        // Act
        String result = authOtpService.verifyRegisterOtp(TEST_EMAIL, validOtp);

        // Assert
        assertThat(result).isEqualTo(TEST_JSON);
        verify(redisTemplate, times(1)).delete(AuthConstants.REG_PREFIX + TEST_EMAIL);
    }

    @Test
    void verifyRegisterOtp_FailsOnWrongOtp() {
        // Arrange
        String validOtp = "123456";
        String wrongOtp = "654321";
        String storedData = validOtp + "|" + TEST_JSON;
        when(valueOperations.get(AuthConstants.REG_PREFIX + TEST_EMAIL)).thenReturn(storedData);

        // Act
        String result = authOtpService.verifyRegisterOtp(TEST_EMAIL, wrongOtp);

        // Assert
        assertThat(result).isNull();
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void resendRegisterOtp_Success() {
        // Arrange
        String oldOtp = "123456";
        String storedData = oldOtp + "|" + TEST_JSON;
        when(valueOperations.get(AuthConstants.REG_PREFIX + TEST_EMAIL)).thenReturn(storedData);

        // Act
        String newOtp = authOtpService.resendRegisterOtp(TEST_EMAIL);

        // Assert
        assertThat(newOtp).isNotNull();
        assertThat(newOtp).hasSize(6);
        assertThat(newOtp).isNotEqualTo(oldOtp); // Technically could be same by random chance, but 1 in a million

        verify(valueOperations, times(1)).set(
                eq(AuthConstants.REG_PREFIX + TEST_EMAIL),
                eq(newOtp + "|" + TEST_JSON),
                eq(10L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void generateAndSaveLoginOtp_Success() {
        // Act
        String otp = authOtpService.generateAndSaveLoginOtp(TEST_EMAIL);

        // Assert
        assertThat(otp).hasSize(6);
        verify(valueOperations, times(1)).set(
                eq(AuthConstants.LOGIN_PREFIX + TEST_EMAIL),
                eq(otp),
                eq(10L),
                eq(TimeUnit.MINUTES)
        );
    }
}
