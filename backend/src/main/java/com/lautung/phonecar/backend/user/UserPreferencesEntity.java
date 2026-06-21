package com.lautung.phonecar.backend.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_preferences")
public class UserPreferencesEntity {
    @Id
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "location_sharing_enabled", nullable = false)
    private boolean locationSharingEnabled;
    @Column(name = "cabin_camera_enabled", nullable = false)
    private boolean cabinCameraEnabled;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserPreferencesEntity() {}

    public UserPreferencesEntity(UUID userId, Instant now) {
        this.userId = userId;
        this.locationSharingEnabled = true;
        this.cabinCameraEnabled = true;
        this.updatedAt = now;
    }

    void apply(UserPreferencesController.PreferencesPatch patch, Instant now) {
        if (patch.locationSharingEnabled() != null) locationSharingEnabled = patch.locationSharingEnabled();
        if (patch.cabinCameraEnabled() != null) cabinCameraEnabled = patch.cabinCameraEnabled();
        updatedAt = now;
    }

    public boolean isLocationSharingEnabled() { return locationSharingEnabled; }
    public boolean isCabinCameraEnabled() { return cabinCameraEnabled; }
    public Instant getUpdatedAt() { return updatedAt; }
}
