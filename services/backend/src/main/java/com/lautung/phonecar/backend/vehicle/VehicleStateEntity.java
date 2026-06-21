package com.lautung.phonecar.backend.vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicle_states")
public class VehicleStateEntity {
    @Id
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "vehicle_locked", nullable = false)
    private boolean vehicleLocked;
    @Column(name = "ac_enabled", nullable = false)
    private boolean acEnabled;
    @Column(name = "air_purification_enabled", nullable = false)
    private boolean airPurificationEnabled;
    @Column(name = "cabin_temperature", nullable = false)
    private float cabinTemperature;
    @Column(name = "fan_level", nullable = false)
    private int fanLevel;
    @Column(name = "driver_seat_heating", nullable = false)
    private boolean driverSeatHeating;
    @Column(name = "passenger_seat_heating", nullable = false)
    private boolean passengerSeatHeating;
    @Column(name = "seat_ventilation", nullable = false)
    private boolean seatVentilation;
    @Column(name = "auto_headlights", nullable = false)
    private boolean autoHeadlights;
    @Column(name = "welcome_light", nullable = false)
    private boolean welcomeLight;
    @Column(name = "window_open_percent", nullable = false)
    private int windowOpenPercent;
    @Column(name = "mirrors_folded", nullable = false)
    private boolean mirrorsFolded;
    @Column(name = "trunk_open", nullable = false)
    private boolean trunkOpen;
    @Column(name = "sunshade_open", nullable = false)
    private boolean sunshadeOpen;
    @Column(name = "child_lock", nullable = false)
    private boolean childLock;
    @Column(name = "sentry_enabled", nullable = false)
    private boolean sentryEnabled;
    @Version
    @Column(nullable = false)
    private long version;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected VehicleStateEntity() {}

    public static VehicleStateEntity defaults(UUID userId, Instant now) {
        VehicleStateEntity state = new VehicleStateEntity();
        state.userId = userId;
        state.vehicleLocked = true;
        state.acEnabled = true;
        state.cabinTemperature = 24.5f;
        state.fanLevel = 3;
        state.autoHeadlights = true;
        state.sentryEnabled = true;
        state.updatedAt = now;
        return state;
    }

    public void apply(VehicleStateController.VehicleStatePatch patch, Instant now) {
        if (patch.vehicleLocked() != null) vehicleLocked = patch.vehicleLocked();
        if (patch.acEnabled() != null) acEnabled = patch.acEnabled();
        if (patch.airPurificationEnabled() != null) airPurificationEnabled = patch.airPurificationEnabled();
        if (patch.cabinTemperature() != null) cabinTemperature = patch.cabinTemperature();
        if (patch.fanLevel() != null) fanLevel = patch.fanLevel();
        if (patch.driverSeatHeating() != null) driverSeatHeating = patch.driverSeatHeating();
        if (patch.passengerSeatHeating() != null) passengerSeatHeating = patch.passengerSeatHeating();
        if (patch.seatVentilation() != null) seatVentilation = patch.seatVentilation();
        if (patch.autoHeadlights() != null) autoHeadlights = patch.autoHeadlights();
        if (patch.welcomeLight() != null) welcomeLight = patch.welcomeLight();
        if (patch.windowOpenPercent() != null) windowOpenPercent = patch.windowOpenPercent();
        if (patch.mirrorsFolded() != null) mirrorsFolded = patch.mirrorsFolded();
        if (patch.trunkOpen() != null) trunkOpen = patch.trunkOpen();
        if (patch.sunshadeOpen() != null) sunshadeOpen = patch.sunshadeOpen();
        if (patch.childLock() != null) childLock = patch.childLock();
        if (patch.sentryEnabled() != null) sentryEnabled = patch.sentryEnabled();
        updatedAt = now;
    }

    public UUID getUserId() { return userId; }
    public boolean isVehicleLocked() { return vehicleLocked; }
    public boolean isAcEnabled() { return acEnabled; }
    public boolean isAirPurificationEnabled() { return airPurificationEnabled; }
    public float getCabinTemperature() { return cabinTemperature; }
    public int getFanLevel() { return fanLevel; }
    public boolean isDriverSeatHeating() { return driverSeatHeating; }
    public boolean isPassengerSeatHeating() { return passengerSeatHeating; }
    public boolean isSeatVentilation() { return seatVentilation; }
    public boolean isAutoHeadlights() { return autoHeadlights; }
    public boolean isWelcomeLight() { return welcomeLight; }
    public int getWindowOpenPercent() { return windowOpenPercent; }
    public boolean isMirrorsFolded() { return mirrorsFolded; }
    public boolean isTrunkOpen() { return trunkOpen; }
    public boolean isSunshadeOpen() { return sunshadeOpen; }
    public boolean isChildLock() { return childLock; }
    public boolean isSentryEnabled() { return sentryEnabled; }
    public long getVersion() { return version; }
    public Instant getUpdatedAt() { return updatedAt; }
}
