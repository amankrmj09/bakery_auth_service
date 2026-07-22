package com.blubugtech.bakery_auth_service.service.auth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthOtpService {

    private final StringRedisTemplate redisTemplate;
    private final Random random = new Random();
    
    private static final String REG_PREFIX = "reg_otp:";
    private static final String LOGIN_PREFIX = "login_otp:";
    private static final String RESET_PREFIX = "reset_otp:";
    private static final long OTP_VALIDITY_MINUTES = 10;

    public AuthOtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateAndSaveRegisterOtp(String email, String requestJson) {
        String otp = String.format("%06d", random.nextInt(999999));
        // Save request JSON as value, with OTP appended or stored separately
        redisTemplate.opsForValue().set(REG_PREFIX + email, otp + "|" + requestJson, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
        System.out.println("Generated Registration OTP for " + email + ": " + otp);
        return otp;
    }
    
    public String verifyRegisterOtp(String email, String otp) {
        String data = redisTemplate.opsForValue().get(REG_PREFIX + email);
        if (data != null) {
            String storedOtp = data.substring(0, 6);
            if (storedOtp.equals(otp)) {
                redisTemplate.delete(REG_PREFIX + email);
                return data.substring(7); // Return the saved JSON request
            }
        }
        return null;
    }

    public String generateAndSaveLoginOtp(String email) {
        String otp = String.format("%06d", random.nextInt(999999));
        redisTemplate.opsForValue().set(LOGIN_PREFIX + email, otp, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
        System.out.println("Generated Login OTP for " + email + ": " + otp);
        return otp;
    }

    public boolean verifyLoginOtp(String email, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(LOGIN_PREFIX + email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(LOGIN_PREFIX + email);
            return true;
        }
        return false;
    }

    public String generateAndSaveResetOtp(String email) {
        String otp = String.format("%06d", random.nextInt(999999));
        redisTemplate.opsForValue().set(RESET_PREFIX + email, otp, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
        System.out.println("Generated Reset OTP for " + email + ": " + otp);
        return otp;
    }

    public boolean verifyResetOtp(String email, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(RESET_PREFIX + email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(RESET_PREFIX + email);
            return true;
        }
        return false;
    }
}
