package com.lautung.phonecar.backend.vehicle;

import com.lautung.phonecar.backend.common.ApiException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleStateService {
    private final VehicleStateRepository states;

    public VehicleStateService(VehicleStateRepository states) { this.states = states; }

    @Transactional(readOnly = true)
    public VehicleStateController.VehicleStateResponse get(UUID userId) {
        return response(requireState(userId));
    }

    @Transactional
    public VehicleStateController.VehicleStateResponse update(UUID userId, VehicleStateController.VehicleStatePatch patch) {
        VehicleStateEntity state = requireState(userId);
        if (state.getVersion() != patch.version()) throw versionConflict();
        state.apply(patch, Instant.now());
        try {
            return response(states.saveAndFlush(state));
        } catch (OptimisticLockingFailureException exception) {
            throw versionConflict();
        }
    }

    private VehicleStateEntity requireState(UUID userId) {
        return states.findById(userId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "VEHICLE_STATE_NOT_FOUND", "Vehicle state does not exist"));
    }

    private ApiException versionConflict() {
        return new ApiException(HttpStatus.CONFLICT, "STATE_VERSION_CONFLICT", "Vehicle state has changed; refresh and retry");
    }

    private VehicleStateController.VehicleStateResponse response(VehicleStateEntity state) {
        return new VehicleStateController.VehicleStateResponse(
                state.isVehicleLocked(), state.isAcEnabled(), state.isAirPurificationEnabled(),
                state.getCabinTemperature(), state.getFanLevel(), state.isDriverSeatHeating(),
                state.isPassengerSeatHeating(), state.isSeatVentilation(), state.isAutoHeadlights(),
                state.isWelcomeLight(), state.getWindowOpenPercent(), state.isMirrorsFolded(),
                state.isTrunkOpen(), state.isSunshadeOpen(), state.isChildLock(), state.isSentryEnabled(),
                state.getVersion(), state.getUpdatedAt());
    }
}
