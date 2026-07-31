package com.blubugtech.bakery_auth_service.service.auth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import com.blubugtech.bakery_auth_service.constant.AuthConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthOtpService {

    private final StringRedisTemplate redisTemplate;
    private final Random random = new Random();
    

    private static final long OTP_VALIDITY_MINUTES = 10;

    public AuthOtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateAndSaveRegisterOtp(String email, String requestJson) {
        String otp = String.format("%06d", random.nextInt(999999));
        // Save request JSON as value, with OTP appended or stored separately
        redisTemplate.opsForValue().set(AuthConstants.REG_PREFIX + email, otp + "|" + requestJson, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
        log.info("Generated Registration OTP for {}: {}", email, otp);
        return otp;
    }
    
    public String verifyRegisterOtp(String email, String otp) {
        String data = redisTemplate.opsForValue().get(AuthConstants.REG_PREFIX + email);
        if (data != null) {
            String storedOtp = data.substring(0, 6);
            if (storedOtp.equals(otp)) {
                redisTemplate.delete(AuthConstants.REG_PREFIX + email);
                return data.substring(7); // Return the saved JSON request
            }
        }
        return null;
    }

    public String resendRegisterOtp(String email) {
        String data = redisTemplate.opsForValue().get(AuthConstants.REG_PREFIX + email);
        if (data == null || data.length() <= 7) {
            return null;
        }
        String requestJson = data.substring(7);
        String newOtp = String.format("%06d", random.nextInt(999999));
        redisTemplate.opsForValue().set(AuthConstants.REG_PREFIX + email, newOtp + "|" + requestJson, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
        log.info("Resent Registration OTP for {}: {}", email, newOtp);
        return newOtp;
    }

    public String generateAndSaveLoginOtp(String email) {
        String otp = String.format("%06d", random.nextInt(999999));
        redisTemplate.opsForValue().set(AuthConstants.LOGIN_PREFIX + email, otp, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
        log.info("Generated Login OTP for {}: {}", email, otp);
        return otp;
    }

    public boolean verifyLoginOtp(String email, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(AuthConstants.LOGIN_PREFIX + email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(AuthConstants.LOGIN_PREFIX + email);
            return true;
        }
        return false;
    }

    public String resendLoginOtp(String email) {
        String existingOtp = redisTemplate.opsForValue().get(AuthConstants.LOGIN_PREFIX + email);
        if (existingOtp == null) {
            return null;
        }
        return generateAndSaveLoginOtp(email);
    }

    public String generateAndSaveResetOtp(String email) {
        String otp = String.format("%06d", random.nextInt(999999));
        redisTemplate.opsForValue().set(AuthConstants.RESET_PREFIX + email, otp, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
        log.info("Generated Reset OTP for {}: {}", email, otp);
        return otp;
    }

    public boolean verifyResetOtp(String email, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(AuthConstants.RESET_PREFIX + email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(AuthConstants.RESET_PREFIX + email);
            return true;
        }
        return false;
    }
}
