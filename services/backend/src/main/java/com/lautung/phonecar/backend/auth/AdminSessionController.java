package com.lautung.phonecar.backend.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/admin")
@SecurityRequirements
public class AdminSessionController {
    private final AuthService auth;
    private final AuthProperties authProperties;
    private final AdminSessionProperties sessionProperties;

    public AdminSessionController(AuthService auth, AuthProperties authProperties,
            AdminSessionProperties sessionProperties) {
        this.auth = auth;
        this.authProperties = authProperties;
        this.sessionProperties = sessionProperties;
    }

    @PostMapping("/login")
    AdminSessionResponse login(@Valid @RequestBody AuthController.CredentialsRequest request,
            HttpServletResponse response) {
        AuthService.AuthResult result = auth.loginAdmin(request.username(), request.password());
        setRefreshCookie(response, result.refreshToken(), authProperties.refreshTokenTtl());
        return AdminSessionResponse.from(result);
    }

    @PostMapping("/refresh")
    @SecurityRequirement(name = "adminRefreshCookie")
    AdminSessionResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        AuthService.AuthResult result = auth.refresh(refreshToken(request));
        setRefreshCookie(response, result.refreshToken(), authProperties.refreshTokenTtl());
        return AdminSessionResponse.from(result);
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "adminRefreshCookie")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = optionalRefreshToken(request);
        if (token != null) auth.logout(token);
        clearRefreshCookie(response);
    }

    private String refreshToken(HttpServletRequest request) {
        String token = optionalRefreshToken(request);
        if (token == null) return "";
        return token;
    }

    private String optionalRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (sessionProperties.cookieName().equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    private void setRefreshCookie(HttpServletResponse response, String token, Duration maxAge) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(token, maxAge).toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString());
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(sessionProperties.cookieName(), value)
                .httpOnly(true)
                .secure(sessionProperties.secure())
                .sameSite(sessionProperties.sameSite())
                .path(sessionProperties.cookiePath())
                .maxAge(maxAge)
                .build();
    }

    public record AdminSessionResponse(
            String accessToken,
            Instant accessExpiresAt,
            AuthService.UserView user) {
        static AdminSessionResponse from(AuthService.AuthResult result) {
            return new AdminSessionResponse(result.accessToken(), result.accessExpiresAt(), result.user());
        }
    }
}
