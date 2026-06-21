package com.lautung.phonecar.backend.media;

import com.lautung.phonecar.backend.common.ApiException;
import com.lautung.phonecar.backend.content.DiscoveryContentRepository;
import com.lautung.phonecar.backend.user.UserEntity;
import com.lautung.phonecar.backend.user.UserRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Http.Method;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaService {
    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private final MinioClient internalClient;
    private final MinioClient publicClient;
    private final MinioProperties properties;
    private final MediaAssetRepository media;
    private final DiscoveryContentRepository contents;
    private final UserRepository users;

    public MediaService(@Qualifier("internalMinioClient") MinioClient internalClient,
            @Qualifier("publicMinioClient") MinioClient publicClient, MinioProperties properties,
            MediaAssetRepository media, DiscoveryContentRepository contents, UserRepository users) {
        this.internalClient = internalClient; this.publicClient = publicClient; this.properties = properties;
        this.media = media; this.contents = contents; this.users = users;
    }

    @Transactional
    public MediaAssetEntity upload(UUID adminId, MultipartFile file) {
        byte[] bytes = readAndValidate(file);
        DetectedImage detected = detect(bytes);
        UUID id = UUID.randomUUID();
        String objectKey = "discovery/" + id + detected.extension();
        try {
            internalClient.putObject(PutObjectArgs.builder().bucket(properties.bucket()).object(objectKey)
                    .contentType(detected.contentType()).stream(new ByteArrayInputStream(bytes), (long) bytes.length, -1L).build());
            UserEntity admin = users.findById(adminId).orElseThrow(() ->
                    new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "User no longer exists"));
            return media.save(new MediaAssetEntity(id, objectKey, safeFilename(file.getOriginalFilename()),
                    detected.contentType(), bytes.length, sha256(bytes), admin, Instant.now()));
        } catch (ApiException exception) {
            removeQuietly(objectKey); throw exception;
        } catch (Exception exception) {
            removeQuietly(objectKey);
            throw storageUnavailable();
        }
    }

    @Transactional
    public void delete(UUID id) {
        MediaAssetEntity asset = media.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "MEDIA_NOT_FOUND", "Media does not exist"));
        if (contents.existsByMediaId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "MEDIA_IN_USE", "Media is referenced by discovery content");
        }
        try {
            internalClient.removeObject(RemoveObjectArgs.builder().bucket(properties.bucket()).object(asset.getObjectKey()).build());
            media.delete(asset);
        } catch (Exception exception) {
            asset.markDeleteFailed(); media.save(asset); throw storageUnavailable();
        }
    }

    public SignedMedia signed(MediaAssetEntity asset) {
        if (asset == null) return null;
        try {
            int seconds = Math.toIntExact(properties.presignedUrlTtl().toSeconds());
            String url = publicClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET).bucket(properties.bucket()).object(asset.getObjectKey()).expiry(seconds).build());
            return new SignedMedia(asset.getId(), url, Instant.now().plusSeconds(seconds));
        } catch (Exception exception) {
            throw storageUnavailable();
        }
    }

    private byte[] readAndValidate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "EMPTY_MEDIA", "Image file is required");
        if (file.getSize() > MAX_BYTES) throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "MEDIA_TOO_LARGE", "Image must be at most 10 MiB");
        try { return file.getBytes(); } catch (Exception exception) { throw new ApiException(HttpStatus.BAD_REQUEST, "MEDIA_READ_FAILED", "Image could not be read"); }
    }

    private DetectedImage detect(byte[] bytes) {
        if (bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47) return new DetectedImage("image/png", ".png");
        if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) return new DetectedImage("image/jpeg", ".jpg");
        if (bytes.length >= 12 && ascii(bytes, 0, 4).equals("RIFF") && ascii(bytes, 8, 4).equals("WEBP")) return new DetectedImage("image/webp", ".webp");
        throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA", "Only JPEG, PNG, and WebP images are allowed");
    }

    private String ascii(byte[] bytes, int offset, int length) { return new String(bytes, offset, length, StandardCharsets.US_ASCII); }
    private String sha256(byte[] bytes) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); } catch (Exception e) { throw new IllegalStateException(e); } }
    private String safeFilename(String value) { String name = value == null ? "image" : value.replace('\\', '/'); name = name.substring(name.lastIndexOf('/') + 1).trim(); return (name.isBlank() ? "image" : name).substring(0, Math.min(name.length(), 255)); }
    private void removeQuietly(String objectKey) { try { internalClient.removeObject(RemoveObjectArgs.builder().bucket(properties.bucket()).object(objectKey).build()); } catch (Exception ignored) { } }
    private ApiException storageUnavailable() { return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "MEDIA_STORAGE_UNAVAILABLE", "Media storage is unavailable"); }

    private record DetectedImage(String contentType, String extension) {}
    public record SignedMedia(UUID id, String url, Instant expiresAt) {}
}
