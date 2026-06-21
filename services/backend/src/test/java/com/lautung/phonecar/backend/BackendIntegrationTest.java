package com.lautung.phonecar.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lautung.phonecar.backend.auth.AuthService;
import com.lautung.phonecar.backend.common.ApiException;
import com.lautung.phonecar.backend.user.UserRepository;
import com.lautung.phonecar.backend.vehicle.VehicleStateController;
import com.lautung.phonecar.backend.vehicle.VehicleStateService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class BackendIntegrationTest {
    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("phonecar.auth.admin-username", () -> "integration_admin");
        registry.add("phonecar.auth.admin-password", () -> "integration-admin-password");
        registry.add("phonecar.minio.initialize-bucket", () -> "false");
    }

    @Autowired AuthService auth;
    @Autowired UserRepository users;
    @Autowired VehicleStateService vehicleStates;
    @Autowired MockMvc mockMvc;

    @Test
    void register_createsHashedCredentialsAndIsolatedDefaultState() {
        String firstName = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String secondName = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        AuthService.AuthResult first = auth.register(firstName, "password-one");
        AuthService.AuthResult second = auth.register(secondName, "password-two");

        assertNotEquals("password-one", users.findByUsername(firstName).orElseThrow().getPasswordHash());
        assertTrue(vehicleStates.get(first.user().id()).vehicleLocked());
        assertTrue(vehicleStates.get(second.user().id()).vehicleLocked());

        VehicleStateController.VehicleStateResponse changed = vehicleStates.update(first.user().id(), patch(0L, false));

        assertEquals(false, changed.vehicleLocked());
        assertTrue(vehicleStates.get(second.user().id()).vehicleLocked());
    }

    @Test
    void refresh_rotatesTokenAndRejectsReusedToken() {
        String username = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        AuthService.AuthResult original = auth.register(username, "password-three");

        AuthService.AuthResult rotated = auth.refresh(original.refreshToken());

        assertNotEquals(original.refreshToken(), rotated.refreshToken());
        ApiException exception = assertThrows(ApiException.class, () -> auth.refresh(original.refreshToken()));
        assertEquals("INVALID_REFRESH_TOKEN", exception.code());
    }

    @Test
    void httpContracts_exposeProblemDetailsUuidInstantEnumPaginationAndOpenApi() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"x\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors").isArray());

        String username = "contract_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String token = auth.register(username, "contract-password").accessToken();
        String bearer = "Bearer " + token;

        mockMvc.perform(get("/api/v1/me").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", matchesPattern("[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("USER"));

        mockMvc.perform(get("/api/v1/vehicle-state").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").isNumber())
                .andExpect(jsonPath("$.updatedAt", matchesPattern(".+Z")));

        mockMvc.perform(get("/api/v1/discovery/contents").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").isNumber())
                .andExpect(jsonPath("$.size").isNumber())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());

        mockMvc.perform(get("/api/v1/admin/media").header("Authorization", bearer))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").isString());
    }

    private VehicleStateController.VehicleStatePatch patch(Long version, Boolean locked) {
        return new VehicleStateController.VehicleStatePatch(version, locked, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);
    }
}
