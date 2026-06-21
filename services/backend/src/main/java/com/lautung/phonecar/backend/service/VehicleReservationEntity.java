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
@Table(name = "vehicle_reservations")
class VehicleReservationEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PaintOption paint;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private WheelOption wheel;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RecordStatus status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected VehicleReservationEntity() {}
    VehicleReservationEntity(UUID id, UUID userId, PaintOption paint, WheelOption wheel, Instant now) {
        this.id = id; this.userId = userId; this.paint = paint; this.wheel = wheel;
        this.status = RecordStatus.SUBMITTED; this.createdAt = now;
    }
    UUID getId() { return id; }
    PaintOption getPaint() { return paint; }
    WheelOption getWheel() { return wheel; }
    RecordStatus getStatus() { return status; }
    Instant getCreatedAt() { return createdAt; }
}

enum PaintOption { AURORA_SILVER, DEEP_BLUE, OBSIDIAN_BLACK, PEARL_WHITE }
enum WheelOption { STANDARD_20, PERFORMANCE_21 }
enum RecordStatus { SUBMITTED, CANCELLED }
