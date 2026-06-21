package com.lautung.phonecar.backend.vehicle;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lautung.phonecar.backend.auth.AuthService;
import com.lautung.phonecar.backend.support.BackendIntegrationSupport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class VehiclePreferencesIntegrationTest extends BackendIntegrationSupport {
    @Test
    void vehicleState_updatesWithVersionAndRejectsStaleOrInvalidPatches() throws Exception {
        AuthService.AuthResult user = registerUser();
        String bearer = bearer(user);
        mockMvc.perform(get("/api/v1/vehicle-state").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleLocked").value(true))
                .andExpect(jsonPath("$.version").value(0));

        mockMvc.perform(patch("/api/v1/vehicle-state")
                        .header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("version", 0, "vehicleLocked", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleLocked").value(false))
                .andExpect(jsonPath("$.version").value(1));
        mockMvc.perform(patch("/api/v1/vehicle-state")
                        .header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("version", 0, "fanLevel", 3))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STATE_VERSION_CONFLICT"));
        mockMvc.perform(patch("/api/v1/vehicle-state")
                        .header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("version", 1, "fanLevel", 9))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void preferences_areIsolatedPerUser() throws Exception {
        String first = bearer(registerUser());
        String second = bearer(registerUser());
        mockMvc.perform(patch("/api/v1/me/preferences")
                        .header(HttpHeaders.AUTHORIZATION, first)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("locationSharingEnabled", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationSharingEnabled").value(false));
        mockMvc.perform(get("/api/v1/me/preferences").header(HttpHeaders.AUTHORIZATION, second))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationSharingEnabled").value(true));
    }
}
