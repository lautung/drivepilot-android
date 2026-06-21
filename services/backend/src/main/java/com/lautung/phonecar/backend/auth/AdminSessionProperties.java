package com.lautung.phonecar.backend.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("phonecar.admin-session")
public record AdminSessionProperties(
        String cookieName,
        String cookiePath,
        String sameSite,
        boolean secure) {}
