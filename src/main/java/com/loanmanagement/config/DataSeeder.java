package com.loanmanagement.config;

import com.loanmanagement.entity.User;
import com.loanmanagement.enums.AuthProvider;
import com.loanmanagement.enums.Role;
import com.loanmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.name}")
    private String adminName;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {

        if (!userRepository.existsByEmail(adminEmail)) {
            User manager = User.builder()
                    .name(adminName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.MANAGER)
                    .authProvider(AuthProvider.LOCAL)
                    .creditScore(900)
                    .isActive(true)
                    .build();
            userRepository.save(manager);
            log.info("✅ Default Manager created!");
            log.info("Email : {}", adminEmail);
        }

    }
}
