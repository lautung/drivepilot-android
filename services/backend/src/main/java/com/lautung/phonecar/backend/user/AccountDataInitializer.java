package com.lautung.phonecar.backend.user;

import com.lautung.phonecar.backend.vehicle.VehicleStateEntity;
import com.lautung.phonecar.backend.vehicle.VehicleStateRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AccountDataInitializer {
    private final VehicleStateRepository vehicleStates;
    private final UserPreferencesRepository preferences;

    public AccountDataInitializer(VehicleStateRepository vehicleStates, UserPreferencesRepository preferences) {
        this.vehicleStates = vehicleStates;
        this.preferences = preferences;
    }

    public void initialize(UUID userId, Instant now) {
        vehicleStates.save(VehicleStateEntity.defaults(userId, now));
        preferences.save(new UserPreferencesEntity(userId, now));
    }
}
