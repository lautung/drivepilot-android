package com.lautung.phonecar.backend.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lautung.phonecar.backend.media.MinioProperties;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ProductionSecurityGuardTest {
    @Test
    void developmentDefaults_areRejectedForProduction() {
        ProductionSecurityGuard guard = new ProductionSecurityGuard(
                auth("dev-only-change-this-secret-at-least-32-bytes", "admin-change-me", "viewer-change-me"),
                session(false),
                minio("http://localhost:9000", "phonecar_minio_dev"));
        assertThrows(IllegalStateException.class, guard::validate);
    }

    @Test
    void strongHttpsConfiguration_isAccepted() {
        ProductionSecurityGuard guard = new ProductionSecurityGuard(
                auth("a-strong-production-jwt-secret-with-more-than-32-bytes", "strong-admin-password", "strong-viewer-password"),
                session(true),
                minio("https://media.example.test", "strong-object-secret"));
        assertDoesNotThrow(guard::validate);
    }

    private AuthProperties auth(String secret, String adminPassword, String viewerPassword) {
        return new AuthProperties(secret, Duration.ofMinutes(15), Duration.ofDays(30),
                "admin", adminPassword, "viewer", viewerPassword);
    }

    private AdminSessionProperties session(boolean secure) {
        return new AdminSessionProperties("phonecar_admin_refresh", "/api/v1/auth/admin", "Strict", secure);
    }

    private MinioProperties minio(String publicEndpoint, String secret) {
        return new MinioProperties("https://internal.example.test", publicEndpoint, "access", secret,
                "bucket", "us-east-1", Duration.ofMinutes(15));
    }
}
