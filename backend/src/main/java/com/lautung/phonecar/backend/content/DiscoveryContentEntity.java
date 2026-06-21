package com.lautung.phonecar.backend.content;

import com.lautung.phonecar.backend.media.MediaAssetEntity;
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
@Table(name = "discovery_contents")
public class DiscoveryContentEntity {
    @Id private UUID id;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ContentCategory category;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 500) private String summary;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "media_id") private MediaAssetEntity media;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ContentStatus status;
    @Column(name = "published_at") private Instant publishedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected DiscoveryContentEntity() {}
    DiscoveryContentEntity(UUID id, ContentCategory category, String title, String summary, String body,
            MediaAssetEntity media, Instant now) {
        this.id = id; update(category, title, summary, body, media, now);
        this.status = ContentStatus.DRAFT; this.createdAt = now;
    }
    void update(ContentCategory category, String title, String summary, String body, MediaAssetEntity media, Instant now) {
        this.category = category; this.title = title; this.summary = summary; this.body = body;
        this.media = media; this.updatedAt = now;
    }
    void publish(Instant now) { status = ContentStatus.PUBLISHED; publishedAt = now; updatedAt = now; }
    void unpublish(Instant now) { status = ContentStatus.UNPUBLISHED; updatedAt = now; }
    public UUID getId() { return id; }
    public ContentCategory getCategory() { return category; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getBody() { return body; }
    public MediaAssetEntity getMedia() { return media; }
    public ContentStatus getStatus() { return status; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

enum ContentCategory { RECOMMENDED, LOCAL, ACTIVITY, STORE }
enum ContentStatus { DRAFT, PUBLISHED, UNPUBLISHED }
