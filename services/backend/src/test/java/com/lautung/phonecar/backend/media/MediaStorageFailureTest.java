package com.lautung.phonecar.backend.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.lautung.phonecar.backend.common.ApiException;
import com.lautung.phonecar.backend.content.DiscoveryContentRepository;
import com.lautung.phonecar.backend.user.UserRepository;
import io.minio.MinioClient;
import java.time.Duration;
import java.util.UUID;
import java.time.Instant;
import java.util.Optional;
import com.lautung.phonecar.backend.user.UserEntity;
import com.lautung.phonecar.backend.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class MediaStorageFailureTest {
    @Test
    void unavailableObjectStorage_mapsToStableServiceUnavailableError() {
        MinioClient unavailable = MinioClient.builder()
                .endpoint("http://127.0.0.1:1")
                .credentials("access", "secret-secret")
                .build();
        MediaAssetRepository media = mock(MediaAssetRepository.class);
        MediaService service = new MediaService(
                unavailable,
                unavailable,
                new MinioProperties("http://127.0.0.1:1", "http://127.0.0.1:1", "access",
                        "secret-secret", "test", "us-east-1", Duration.ofMinutes(15)),
                media,
                mock(DiscoveryContentRepository.class),
                mock(UserRepository.class));

        ApiException exception = assertThrows(ApiException.class, () -> service.upload(
                UUID.randomUUID(),
                new MockMultipartFile("file", "image.png", "image/png", new byte[] {
                    (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
                })));

        assertEquals(503, exception.status().value());
        assertEquals("MEDIA_STORAGE_UNAVAILABLE", exception.code());
        verifyNoInteractions(media);
    }

    @Test
    void failedObjectDelete_marksMediaForRecovery() {
        MinioClient unavailable = MinioClient.builder()
                .endpoint("http://127.0.0.1:1")
                .credentials("access", "secret-secret")
                .build();
        MediaAssetRepository media = mock(MediaAssetRepository.class);
        DiscoveryContentRepository contents = mock(DiscoveryContentRepository.class);
        UUID id = UUID.randomUUID();
        UserEntity admin = new UserEntity(UUID.randomUUID(), "admin", "hash", UserRole.ADMIN, Instant.now());
        MediaAssetEntity asset = new MediaAssetEntity(id, "discovery/asset.png", "asset.png", "image/png",
                8, "0".repeat(64), admin, Instant.now());
        when(media.findById(id)).thenReturn(Optional.of(asset));
        when(contents.existsByMediaId(id)).thenReturn(false);
        MediaService service = new MediaService(
                unavailable,
                unavailable,
                new MinioProperties("http://127.0.0.1:1", "http://127.0.0.1:1", "access",
                        "secret-secret", "test", "us-east-1", Duration.ofMinutes(15)),
                media,
                contents,
                mock(UserRepository.class));

        ApiException exception = assertThrows(ApiException.class, () -> service.delete(id));

        assertEquals("MEDIA_STORAGE_UNAVAILABLE", exception.code());
        assertEquals(MediaStatus.DELETE_FAILED, asset.getStatus());
        verify(media).save(asset);
    }
}
