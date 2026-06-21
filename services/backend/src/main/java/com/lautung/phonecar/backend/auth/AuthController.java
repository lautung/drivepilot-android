package com.lautung.phonecar.backend.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@SecurityRequirements
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    AuthService.AuthResult register(@Valid @RequestBody CredentialsRequest request) {
        return auth.register(request.username(), request.password());
    }

    @PostMapping("/login")
    AuthService.AuthResult login(@Valid @RequestBody CredentialsRequest request) {
        return auth.login(request.username(), request.password());
    }

    @PostMapping("/refresh")
    AuthService.AuthResult refresh(@Valid @RequestBody RefreshRequest request) {
        return auth.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@Valid @RequestBody RefreshRequest request) { auth.logout(request.refreshToken()); }

    public record CredentialsRequest(
            @NotBlank @Size(min = 3, max = 64)
            @Pattern(regexp = "[A-Za-z0-9_]+", message = "must contain only letters, numbers, or underscore")
            String username,
            @NotBlank @Size(min = 8, max = 72) String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
}
