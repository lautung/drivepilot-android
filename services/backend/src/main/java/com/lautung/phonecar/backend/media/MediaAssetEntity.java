package com.lautung.phonecar.backend.media;

import com.lautung.phonecar.backend.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_assets")
public class MediaAssetEntity {
    @Id private UUID id;
    @Column(name = "object_key", nullable = false, unique = true) private String objectKey;
    @Column(name = "original_filename", nullable = false) private String originalFilename;
    @Column(name = "content_type", nullable = false) private String contentType;
    @Column(name = "size_bytes", nullable = false) private long sizeBytes;
    @Column(nullable = false, length = 64) private String sha256;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private MediaStatus status;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false) private UserEntity uploadedBy;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected MediaAssetEntity() {}
    public MediaAssetEntity(UUID id, String objectKey, String originalFilename, String contentType,
            long sizeBytes, String sha256, UserEntity uploadedBy, Instant now) {
        this.id = id; this.objectKey = objectKey; this.originalFilename = originalFilename;
        this.contentType = contentType; this.sizeBytes = sizeBytes; this.sha256 = sha256;
        this.status = MediaStatus.ACTIVE; this.uploadedBy = uploadedBy; this.createdAt = now;
    }
    public UUID getId() { return id; }
    public String getObjectKey() { return objectKey; }
    public String getOriginalFilename() { return originalFilename; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public String getSha256() { return sha256; }
    public MediaStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void markDeleteFailed() { status = MediaStatus.DELETE_FAILED; }
}

enum MediaStatus { ACTIVE, DELETE_FAILED }
