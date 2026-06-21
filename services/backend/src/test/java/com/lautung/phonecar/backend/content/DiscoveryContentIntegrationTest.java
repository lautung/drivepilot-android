package com.lautung.phonecar.backend.content;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import com.lautung.phonecar.backend.support.BackendIntegrationSupport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class DiscoveryContentIntegrationTest extends BackendIntegrationSupport {
    @Test
    void adminContent_supportsServerSideStatusAndCategoryFilters() throws Exception {
        String admin = adminBearer();
        String viewer = viewerBearer();
        String suffix = java.util.UUID.randomUUID().toString();
        String draftTitle = "Local draft " + suffix;
        String publishedTitle = "Store published " + suffix;

        createAdminContent(admin, "LOCAL", draftTitle);
        String publishedId = createAdminContent(admin, "STORE", publishedTitle);
        mockMvc.perform(post("/api/v1/admin/discovery/contents/{id}/publish", publishedId)
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/discovery/contents")
                        .header(HttpHeaders.AUTHORIZATION, viewer)
                        .param("status", "DRAFT")
                        .param("category", "LOCAL")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].title", hasItem(draftTitle)))
                .andExpect(jsonPath("$.items[*].title", not(hasItem(publishedTitle))));

        mockMvc.perform(get("/api/v1/admin/discovery/contents")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .param("status", "PUBLISHED")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].title", hasItem(publishedTitle)))
                .andExpect(jsonPath("$.items[*].title", not(hasItem(draftTitle))));

        mockMvc.perform(get("/api/v1/admin/discovery/contents")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .param("category", "LOCAL")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].title", hasItem(draftTitle)))
                .andExpect(jsonPath("$.items[*].title", not(hasItem(publishedTitle))));

        mockMvc.perform(get("/api/v1/admin/discovery/contents")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void publishedContent_isVisibleAndFollowedPerUser() throws Exception {
        String admin = adminBearer();
        String first = bearer(registerUser());
        String second = bearer(registerUser());
        MvcResult created = mockMvc.perform(post("/api/v1/admin/discovery/contents")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "category", "ACTIVITY",
                                "title", "Integration activity",
                                "summary", "Integration summary",
                                "body", "Integration body"))))
                .andExpect(status().isCreated())
                .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/discovery/contents/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, first))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CONTENT_NOT_FOUND"));
        mockMvc.perform(put("/api/v1/admin/discovery/contents/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "category", "ACTIVITY",
                                "title", "Updated activity",
                                "summary", "Updated summary",
                                "body", "Updated body"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated activity"));
        mockMvc.perform(post("/api/v1/admin/discovery/contents/{id}/publish", id)
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
        mockMvc.perform(put("/api/v1/discovery/contents/{id}/follow", id)
                        .header(HttpHeaders.AUTHORIZATION, first))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/discovery/contents/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followed").value(true));
        mockMvc.perform(get("/api/v1/discovery/contents/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, second))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followed").value(false));
        mockMvc.perform(delete("/api/v1/discovery/contents/{id}/follow", id)
                        .header(HttpHeaders.AUTHORIZATION, first))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/admin/discovery/contents/{id}/unpublish", id)
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNPUBLISHED"));
        mockMvc.perform(delete("/api/v1/admin/discovery/contents/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
    }

    private String createAdminContent(String admin, String category, String title) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/admin/discovery/contents")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "category", category,
                                "title", title,
                                "summary", "Filter test summary",
                                "body", "Filter test body"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
    }
}
