package com.lautung.phonecar.backend.content;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "content_follows")
class ContentFollowEntity {
    @EmbeddedId private ContentFollowId id;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected ContentFollowEntity() {}
    ContentFollowEntity(UUID userId, UUID contentId, Instant now) { id = new ContentFollowId(userId, contentId); createdAt = now; }
}

@jakarta.persistence.Embeddable
record ContentFollowId(@Column(name = "user_id") UUID userId,
                       @Column(name = "content_id") UUID contentId) implements Serializable {}
