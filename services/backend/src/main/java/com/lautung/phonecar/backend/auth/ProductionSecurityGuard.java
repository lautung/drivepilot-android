package com.lautung.phonecar.backend.auth;

import com.lautung.phonecar.backend.media.MinioProperties;
import java.net.URI;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProductionSecurityGuard implements ApplicationRunner {
    private static final String DEV_JWT_SECRET = "dev-only-change-this-secret-at-least-32-bytes";
    private final AuthProperties auth;
    private final AdminSessionProperties session;
    private final MinioProperties minio;

    public ProductionSecurityGuard(AuthProperties auth, AdminSessionProperties session, MinioProperties minio) {
        this.auth = auth;
        this.session = session;
        this.minio = minio;
    }

    @Override
    public void run(ApplicationArguments args) {
        validate();
    }

    void validate() {
        require(auth.jwtSecret() != null && auth.jwtSecret().length() >= 32
                && !DEV_JWT_SECRET.equals(auth.jwtSecret()), "JWT secret");
        require(auth.adminUsername() != null && !auth.adminUsername().isBlank(), "admin username");
        require(auth.viewerUsername() != null && !auth.viewerUsername().isBlank(), "viewer username");
        require(!auth.adminUsername().trim().equalsIgnoreCase(auth.viewerUsername().trim()),
                "distinct admin and viewer usernames");
        require(strongPassword(auth.adminPassword(), "admin-change-me"), "admin password");
        require(strongPassword(auth.viewerPassword(), "viewer-change-me"), "viewer password");
        require(!auth.adminPassword().equals(auth.viewerPassword()), "distinct admin and viewer passwords");
        require(session.secure(), "admin refresh cookie Secure flag");
        require("Strict".equalsIgnoreCase(session.sameSite()), "admin refresh cookie SameSite policy");
        require(session.cookiePath() != null && session.cookiePath().startsWith("/api/v1/auth/admin"),
                "admin refresh cookie path");
        require("https".equalsIgnoreCase(URI.create(minio.publicEndpoint()).getScheme()),
                "public media HTTPS endpoint");
        require(minio.secretKey() != null && minio.secretKey().length() >= 16
                && !"phonecar_minio_dev".equals(minio.secretKey()), "object storage secret");
    }

    private boolean strongPassword(String value, String developmentDefault) {
        return value != null && value.length() >= 12 && !developmentDefault.equals(value);
    }

    private void require(boolean condition, String field) {
        if (!condition) throw new IllegalStateException("Unsafe production configuration: " + field);
    }
}
