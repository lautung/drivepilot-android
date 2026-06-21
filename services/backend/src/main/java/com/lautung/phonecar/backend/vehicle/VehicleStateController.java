package com.lautung.phonecar.backend.vehicle;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicle-state")
public class VehicleStateController {
    private final VehicleStateService service;

    public VehicleStateController(VehicleStateService service) { this.service = service; }

    @GetMapping
    VehicleStateResponse get(@AuthenticationPrincipal Jwt jwt) { return service.get(userId(jwt)); }

    @PatchMapping
    VehicleStateResponse patch(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody VehicleStatePatch patch) {
        return service.update(userId(jwt), patch);
    }

    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }

    public record VehicleStatePatch(
            @NotNull Long version,
            Boolean vehicleLocked,
            Boolean acEnabled,
            Boolean airPurificationEnabled,
            @DecimalMin("16.0") @DecimalMax("30.0") Float cabinTemperature,
            @Min(1) @Max(5) Integer fanLevel,
            Boolean driverSeatHeating,
            Boolean passengerSeatHeating,
            Boolean seatVentilation,
            Boolean autoHeadlights,
            Boolean welcomeLight,
            @Min(0) @Max(100) Integer windowOpenPercent,
            Boolean mirrorsFolded,
            Boolean trunkOpen,
            Boolean sunshadeOpen,
            Boolean childLock,
            Boolean sentryEnabled) {}

    public record VehicleStateResponse(
            boolean vehicleLocked,
            boolean acEnabled,
            boolean airPurificationEnabled,
            float cabinTemperature,
            int fanLevel,
            boolean driverSeatHeating,
            boolean passengerSeatHeating,
            boolean seatVentilation,
            boolean autoHeadlights,
            boolean welcomeLight,
            int windowOpenPercent,
            boolean mirrorsFolded,
            boolean trunkOpen,
            boolean sunshadeOpen,
            boolean childLock,
            boolean sentryEnabled,
            long version,
            Instant updatedAt) {}
}
