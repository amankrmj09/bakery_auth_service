package com.blubugtech.bakery_auth_service.config;

import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.repository.UserRepository;
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

    @Value("${app.admin.first-name}")
    private String adminFirstName;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.last-name}")
    private String adminLastName;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Bean
    public CommandLineRunner seedAdminUser() {
        return args -> {
            if (!seedEnabled || userRepository.existsByEmail(adminEmail)) {
                return;
            }

            User admin = new User(
                    adminUsername,
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
