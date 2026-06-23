package com.shah_s.bakery_auth_service.config;

import com.shah_s.bakery_auth_service.entity.User;
import com.shah_s.bakery_auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminSeederConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.seed-enabled:false}")
    private boolean seedEnabled;

    @Value("${app.admin.first-name:Super}")
    private String adminFirstName;

    @Value("${app.admin.last-name:Admin}")
    private String adminLastName;

    @Value("${app.admin.email:admin@bakery.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;
    
    @Value("${app.admin.phone:1234567890}")
    private String adminPhone;

    @Bean
    public CommandLineRunner seedAdminUser() {
        return args -> {
            if (!seedEnabled || userRepository.existsByEmail(adminEmail)) {
                return;
            }

            User admin = new User(
                    "admin",
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    adminFirstName,
                    adminLastName
            );
            admin.setPhone(adminPhone);
            admin.setRole(User.Role.ADMIN);
            admin.setStatus(User.UserStatus.ACTIVE);
            admin.setEmailVerified(true);

            userRepository.save(admin);
        };
    }
}
