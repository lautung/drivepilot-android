package com.lautung.phonecar.backend.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lautung.phonecar.backend.auth.AuthService;
import com.lautung.phonecar.backend.user.UserRepository;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BackendIntegrationSupport {
    protected static final String ADMIN_USERNAME = "integration_admin";
    protected static final String ADMIN_PASSWORD = "integration-admin-password";
    protected static final String VIEWER_USERNAME = "integration_viewer";
    protected static final String VIEWER_PASSWORD = "integration-viewer-password";
    protected static final String USER_PASSWORD = "integration-user-password";
    private static final String MINIO_ACCESS_KEY = "integration_minio";
    private static final String MINIO_SECRET_KEY = "integration_minio_secret";

    protected static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine");

    protected static final GenericContainer<?> minio = new GenericContainer<>(DockerImageName.parse(
            "quay.io/minio/minio:RELEASE.2025-09-07T16-13-09Z"))
            .withEnv("MINIO_ROOT_USER", MINIO_ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", MINIO_SECRET_KEY)
            .withExposedPorts(9000)
            .withCommand("server", "/data")
            .waitingFor(Wait.forHttp("/minio/health/live").forPort(9000)
                    .withStartupTimeout(Duration.ofMinutes(2)));

    static {
        postgres.start();
        minio.start();
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("phonecar.auth.admin-username", () -> ADMIN_USERNAME);
        registry.add("phonecar.auth.admin-password", () -> ADMIN_PASSWORD);
        registry.add("phonecar.auth.viewer-username", () -> VIEWER_USERNAME);
        registry.add("phonecar.auth.viewer-password", () -> VIEWER_PASSWORD);
        registry.add("phonecar.minio.endpoint", BackendIntegrationSupport::minioEndpoint);
        registry.add("phonecar.minio.public-endpoint", BackendIntegrationSupport::minioEndpoint);
        registry.add("phonecar.minio.access-key", () -> MINIO_ACCESS_KEY);
        registry.add("phonecar.minio.secret-key", () -> MINIO_SECRET_KEY);
        registry.add("phonecar.minio.bucket", () -> "integration-media");
        registry.add("phonecar.minio.initialize-bucket", () -> "true");
        registry.add("phonecar.rate-limit.media-upload.capacity", () -> "1000");
    }

    @Autowired protected MockMvc mockMvc;
    protected final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired protected AuthService auth;
    @Autowired protected UserRepository users;

    protected AuthService.AuthResult registerUser() {
        return auth.register(unique("user"), USER_PASSWORD);
    }

    protected String adminBearer() {
        return bearer(auth.loginAdmin(ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    protected String viewerBearer() {
        return bearer(auth.loginAdmin(VIEWER_USERNAME, VIEWER_PASSWORD));
    }

    protected String bearer(AuthService.AuthResult result) {
        return "Bearer " + result.accessToken();
    }

    protected String unique(String prefix) {
        return prefix + '_' + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private static String minioEndpoint() {
        return "http://" + minio.getHost() + ':' + minio.getMappedPort(9000);
    }
}
