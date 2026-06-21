package com.lautung.phonecar.backend.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lautung.phonecar.backend.support.BackendIntegrationSupport;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class ServiceOperationsIntegrationTest extends BackendIntegrationSupport {
    @Test
    void reservationsMaintenanceAndSubscriptions_areUserScopedAndValidated() throws Exception {
        String first = bearer(registerUser());
        String second = bearer(registerUser());
        mockMvc.perform(post("/api/v1/vehicle-reservations")
                        .header(HttpHeaders.AUTHORIZATION, first)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "paint", "AURORA_SILVER", "wheel", "STANDARD_20"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
        mockMvc.perform(get("/api/v1/vehicle-reservations")
                        .header(HttpHeaders.AUTHORIZATION, first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));
        mockMvc.perform(get("/api/v1/vehicle-reservations")
                        .header(HttpHeaders.AUTHORIZATION, second))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));

        mockMvc.perform(post("/api/v1/maintenance-bookings")
                        .header(HttpHeaders.AUTHORIZATION, first)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "service", "REGULAR",
                                "bookingDate", LocalDate.now().minusDays(1).toString()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BOOKING_DATE_IN_PAST"));
        mockMvc.perform(post("/api/v1/maintenance-bookings")
                        .header(HttpHeaders.AUTHORIZATION, first)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "service", "DIAGNOSTIC",
                                "bookingDate", LocalDate.now().plusDays(2).toString()))))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/subscriptions/AUTOPILOT")
                        .header(HttpHeaders.AUTHORIZATION, first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
        mockMvc.perform(put("/api/v1/subscriptions/AUTOPILOT")
                        .header(HttpHeaders.AUTHORIZATION, first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
        mockMvc.perform(delete("/api/v1/subscriptions/AUTOPILOT")
                        .header(HttpHeaders.AUTHORIZATION, first))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/subscriptions")
                        .header(HttpHeaders.AUTHORIZATION, first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(false));

        mockMvc.perform(get("/api/v1/vehicle-reservations?page=-1")
                        .header(HttpHeaders.AUTHORIZATION, first))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGE"));
    }
}
