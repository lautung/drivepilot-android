package com.lautung.phonecar.backend.auth;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("phonecar.auth")
public record AuthProperties(
        String jwtSecret,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        String adminUsername,
        String adminPassword,
        String viewerUsername,
        String viewerPassword) {}
