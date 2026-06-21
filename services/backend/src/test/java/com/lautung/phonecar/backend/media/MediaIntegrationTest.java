package com.lautung.phonecar.backend.media;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lautung.phonecar.backend.support.BackendIntegrationSupport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

class MediaIntegrationTest extends BackendIntegrationSupport {
    private static final byte[] PNG = new byte[] {
        (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
    };

    @Test
    void mediaLifecycle_usesPrivateObjectStorageAndPreventsReferencedDelete() throws Exception {
        String admin = adminBearer();
        MvcResult uploaded = upload(admin, "image/png", PNG)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.sha256").isString())
                .andReturn();
        String mediaId = objectMapper.readTree(uploaded.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/admin/media")
                        .header(HttpHeaders.AUTHORIZATION, viewerBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '%s')]", mediaId).exists());
        MvcResult content = mockMvc.perform(post("/api/v1/admin/discovery/contents")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "category", "RECOMMENDED",
                                "title", "Media content",
                                "summary", "Media summary",
                                "body", "Media body",
                                "mediaId", mediaId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.media.id").value(mediaId))
                .andExpect(jsonPath("$.media.url", containsString("integration-media")))
                .andReturn();
        String contentId = objectMapper.readTree(content.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(delete("/api/v1/admin/media/{id}", mediaId)
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEDIA_IN_USE"));
        mockMvc.perform(delete("/api/v1/admin/discovery/contents/{id}", contentId)
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/admin/media/{id}", mediaId)
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void mediaUpload_rejectsViewerInvalidSignatureMimeMismatchAndOversize() throws Exception {
        upload(viewerBearer(), "image/png", PNG).andExpect(status().isForbidden());
        upload(adminBearer(), "image/png", new byte[] {1, 2, 3, 4})
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA"));
        upload(adminBearer(), "text/plain", PNG)
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("MEDIA_TYPE_MISMATCH"));
        upload(adminBearer(), "image/png", new byte[10 * 1024 * 1024 + 1])
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value("MEDIA_TOO_LARGE"));
    }

    @Test
    void mediaUpload_acceptsJpegAndWebpMagic() throws Exception {
        String admin = adminBearer();
        MvcResult jpeg = upload(admin, "image/jpeg", new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff})
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentType").value("image/jpeg"))
                .andReturn();
        MvcResult webp = upload(admin, "image/webp", new byte[] {
                'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P',
            })
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentType").value("image/webp"))
                .andReturn();
        for (MvcResult uploaded : new MvcResult[] {jpeg, webp}) {
            String id = objectMapper.readTree(uploaded.getResponse().getContentAsString()).get("id").asText();
            mockMvc.perform(delete("/api/v1/admin/media/{id}", id)
                            .header(HttpHeaders.AUTHORIZATION, admin))
                    .andExpect(status().isNoContent());
        }
    }

    private org.springframework.test.web.servlet.ResultActions upload(
            String bearer, String contentType, byte[] bytes) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", contentType, bytes);
        return mockMvc.perform(multipart("/api/v1/admin/media")
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, bearer));
    }
}
