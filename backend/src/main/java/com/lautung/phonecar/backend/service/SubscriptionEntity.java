package com.lautung.phonecar.backend.service;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
class SubscriptionEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private SubscriptionPlan plan;
    @Column(nullable = false) private boolean active;
    @Column(name = "activated_at") private Instant activatedAt;
    @Column(name = "deactivated_at") private Instant deactivatedAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected SubscriptionEntity() {}
    SubscriptionEntity(UUID id, UUID userId, SubscriptionPlan plan, Instant now) {
        this.id = id; this.userId = userId; this.plan = plan; activate(now);
    }
    void activate(Instant now) { active = true; activatedAt = now; deactivatedAt = null; updatedAt = now; }
    void deactivate(Instant now) { active = false; deactivatedAt = now; updatedAt = now; }
    UUID getId() { return id; }
    SubscriptionPlan getPlan() { return plan; }
    boolean isActive() { return active; }
    Instant getActivatedAt() { return activatedAt; }
    Instant getDeactivatedAt() { return deactivatedAt; }
    Instant getUpdatedAt() { return updatedAt; }
}

enum SubscriptionPlan { AUTOPILOT, ENTERTAINMENT }
