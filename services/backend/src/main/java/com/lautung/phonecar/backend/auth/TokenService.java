package com.lautung.phonecar.backend.auth;

import com.lautung.phonecar.backend.common.ApiException;
import com.lautung.phonecar.backend.user.UserEntity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokens;
    private final AuthProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenService(JwtEncoder jwtEncoder, RefreshTokenRepository refreshTokens, AuthProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokens = refreshTokens;
        this.properties = properties;
    }

    @Transactional
    public IssuedTokens issue(UserEntity user) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plus(properties.accessTokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("phonecar-backend")
                .issuedAt(now)
                .expiresAt(accessExpiresAt)
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        String refreshToken = newRefreshToken();
        refreshTokens.save(new RefreshTokenEntity(UUID.randomUUID(), user, hash(refreshToken),
                now.plus(properties.refreshTokenTtl()), now));
        return new IssuedTokens(accessToken, refreshToken, accessExpiresAt);
    }

    @Transactional
    public RotatedTokens rotate(String rawRefreshToken) {
        Instant now = Instant.now();
        RefreshTokenEntity existing = refreshTokens.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(this::invalidRefreshToken);
        if (!existing.isUsableAt(now)) throw invalidRefreshToken();
        existing.revoke(now);
        return new RotatedTokens(existing.getUser(), issue(existing.getUser()));
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        refreshTokens.findByTokenHash(hash(rawRefreshToken)).ifPresent(token -> token.revoke(Instant.now()));
    }

    private String newRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        if (token == null || token.isBlank()) throw invalidRefreshToken();
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private ApiException invalidRefreshToken() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired");
    }

    public record IssuedTokens(String accessToken, String refreshToken, Instant accessExpiresAt) {}
    public record RotatedTokens(UserEntity user, IssuedTokens tokens) {}
}
