package com.lautung.phonecar.backend.service;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "maintenance_bookings")
class MaintenanceBookingEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private MaintenanceService service;
    @Column(name = "booking_date", nullable = false) private LocalDate bookingDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RecordStatus status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected MaintenanceBookingEntity() {}
    MaintenanceBookingEntity(UUID id, UUID userId, MaintenanceService service, LocalDate bookingDate, Instant now) {
        this.id = id; this.userId = userId; this.service = service; this.bookingDate = bookingDate;
        this.status = RecordStatus.SUBMITTED; this.createdAt = now;
    }
    UUID getId() { return id; }
    MaintenanceService getService() { return service; }
    LocalDate getBookingDate() { return bookingDate; }
    RecordStatus getStatus() { return status; }
    Instant getCreatedAt() { return createdAt; }
}

enum MaintenanceService { REGULAR, DIAGNOSTIC }
