package com.lautung.phonecar.backend.auth;

import com.lautung.phonecar.backend.user.UserEntity;
import com.lautung.phonecar.backend.user.UserRepository;
import com.lautung.phonecar.backend.user.UserRole;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties properties;

    public AdminBootstrap(UserRepository users, PasswordEncoder passwordEncoder, AuthProperties properties) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createIfMissing(properties.adminUsername(), properties.adminPassword(), UserRole.ADMIN);
        createIfMissing(properties.viewerUsername(), properties.viewerPassword(), UserRole.ADMIN_VIEWER);
    }

    private void createIfMissing(String rawUsername, String password, UserRole role) {
        String username = rawUsername == null ? "" : rawUsername.trim().toLowerCase(Locale.ROOT);
        if (username.isBlank() || password == null || password.isBlank() || users.existsByUsername(username)) return;
        Instant now = Instant.now();
        users.save(new UserEntity(UUID.randomUUID(), username, passwordEncoder.encode(password), role, now));
    }
}
