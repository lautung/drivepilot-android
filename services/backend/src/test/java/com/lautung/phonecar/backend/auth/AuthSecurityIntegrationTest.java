package com.lautung.phonecar.backend.auth;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lautung.phonecar.backend.support.BackendIntegrationSupport;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class AuthSecurityIntegrationTest extends BackendIntegrationSupport {
    @Test
    void mobileAuthContract_registerRefreshAndLogoutRemainsCompatible() throws Exception {
        String username = unique("mobile");
        MvcResult registered = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", USER_PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.accessExpiresAt", matchesPattern(".+Z")))
                .andExpect(jsonPath("$.user.username").value(username))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andReturn();

        String oldRefresh = objectMapper.readTree(registered.getResponse().getContentAsString())
                .get("refreshToken").asText();
        assertNotEquals(USER_PASSWORD, users.findByUsername(username).orElseThrow().getPasswordHash());
        MvcResult refreshed = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", oldRefresh))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn();
        String newRefresh = objectMapper.readTree(refreshed.getResponse().getContentAsString())
                .get("refreshToken").asText();
        assertNotEquals(oldRefresh, newRefresh);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", oldRefresh))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", newRefresh))))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminWebSession_usesHttpOnlyCookieAndRotatesIt() throws Exception {
        MvcResult login = adminLogin(ADMIN_USERNAME, ADMIN_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("HttpOnly"),
                        org.hamcrest.Matchers.containsString("SameSite=Strict"),
                        org.hamcrest.Matchers.containsString("Path=/api/v1/auth/admin"))))
                .andReturn();
        Cookie first = login.getResponse().getCookie("phonecar_admin_refresh");
        assertNotNull(first);

        MvcResult refreshed = mockMvc.perform(post("/api/v1/auth/admin/refresh").cookie(first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();
        Cookie second = refreshed.getResponse().getCookie("phonecar_admin_refresh");
        assertNotNull(second);
        assertNotEquals(first.getValue(), second.getValue());

        mockMvc.perform(post("/api/v1/auth/admin/refresh").cookie(first))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
        mockMvc.perform(post("/api/v1/auth/admin/logout").cookie(second))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("phonecar_admin_refresh", 0));
    }

    @Test
    void adminWebSession_rejectsRegularUserAndMissingCookie() throws Exception {
        String username = unique("regular");
        auth.register(username, USER_PASSWORD);
        adminLogin(username, USER_PASSWORD)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ADMIN_ACCESS_REQUIRED"));
        mockMvc.perform(post("/api/v1/auth/admin/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));

        adminLogin(VIEWER_USERNAME, VIEWER_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("ADMIN_VIEWER"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", ADMIN_USERNAME,
                                "password", ADMIN_PASSWORD))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void adminRoleMatrix_enforcesViewerReadOnlyAndUserIsolation() throws Exception {
        String createBody = objectMapper.writeValueAsString(Map.of(
                "category", "RECOMMENDED",
                "title", "Contract title",
                "summary", "Contract summary",
                "body", "Contract body"));

        mockMvc.perform(get("/api/v1/admin/discovery/contents")
                        .header(HttpHeaders.AUTHORIZATION, viewerBearer()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/discovery/contents")
                        .header(HttpHeaders.AUTHORIZATION, viewerBearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/discovery/contents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(registerUser())))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/discovery/contents")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void openApiAndCurrentUserExposeStableUuidInstantAndRoleContracts() throws Exception {
        AuthService.AuthResult result = registerUser();
        mockMvc.perform(get("/api/v1/me").header(HttpHeaders.AUTHORIZATION, bearer(result)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", matchesPattern("[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.role").value("USER"));
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").isString())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.adminRefreshCookie.in").value("cookie"))
                .andExpect(jsonPath("$.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/auth/admin/refresh'].post.security[0].adminRefreshCookie").isArray());
    }

    @Test
    void concurrentRefresh_consumesTokenExactlyOnce() throws Exception {
        String refreshToken = auth.register(unique("concurrent"), USER_PASSWORD).refreshToken();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> rotate = () -> {
            ready.countDown();
            start.await();
            try {
                auth.refresh(refreshToken);
                return true;
            } catch (com.lautung.phonecar.backend.common.ApiException exception) {
                assertEquals("INVALID_REFRESH_TOKEN", exception.code());
                return false;
            }
        };
        try (var executor = Executors.newFixedThreadPool(2)) {
            List<Future<Boolean>> futures = List.of(executor.submit(rotate), executor.submit(rotate));
            ready.await();
            start.countDown();
            long successes = 0;
            for (Future<Boolean> future : futures) if (future.get()) successes++;
            assertEquals(1, successes);
        }
    }

    private org.springframework.test.web.servlet.ResultActions adminLogin(String username, String password)
            throws Exception {
        return mockMvc.perform(post("/api/v1/auth/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username,
                        "password", password))));
    }
}
