package com.lautung.phonecar.backend.auth;

import com.lautung.phonecar.backend.common.ApiException;
import com.lautung.phonecar.backend.user.UserEntity;
import com.lautung.phonecar.backend.user.AccountDataInitializer;
import com.lautung.phonecar.backend.user.UserRepository;
import com.lautung.phonecar.backend.user.UserRole;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final AccountDataInitializer accountData;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokens, AccountDataInitializer accountData) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.accountData = accountData;
    }

    @Transactional
    public AuthResult register(String rawUsername, String password) {
        String username = normalizeUsername(rawUsername);
        if (users.existsByUsername(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "USERNAME_TAKEN", "Username is already registered");
        }
        Instant now = Instant.now();
        UserEntity user = users.save(new UserEntity(UUID.randomUUID(), username,
                passwordEncoder.encode(password), UserRole.USER, now));
        accountData.initialize(user.getId(), now);
        return result(user, tokens.issue(user));
    }

    @Transactional
    public AuthResult login(String rawUsername, String password) {
        UserEntity user = authenticate(rawUsername, password);
        if (user.getRole() != UserRole.USER) throw invalidCredentials();
        return result(user, tokens.issue(user));
    }

    @Transactional
    public AuthResult loginAdmin(String rawUsername, String password) {
        UserEntity user = authenticate(rawUsername, password);
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.ADMIN_VIEWER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ADMIN_ACCESS_REQUIRED", "Admin access is required");
        }
        return result(user, tokens.issue(user));
    }

    public AuthResult refresh(String refreshToken) {
        TokenService.RotatedTokens rotated = tokens.rotate(refreshToken);
        return result(rotated.user(), rotated.tokens());
    }

    public void logout(String refreshToken) { tokens.revoke(refreshToken); }

    private UserEntity authenticate(String rawUsername, String password) {
        UserEntity user = users.findByUsername(normalizeUsername(rawUsername))
                .filter(UserEntity::isEnabled)
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) throw invalidCredentials();
        return user;
    }

    private String normalizeUsername(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private AuthResult result(UserEntity user, TokenService.IssuedTokens issued) {
        return new AuthResult(issued.accessToken(), issued.refreshToken(), issued.accessExpiresAt(),
                new UserView(user.getId(), user.getUsername(), user.getRole()));
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Username or password is incorrect");
    }

    public record AuthResult(String accessToken, String refreshToken, Instant accessExpiresAt, UserView user) {}
    public record UserView(UUID id, String username, UserRole role) {}
}
