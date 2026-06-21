package com.lautung.phonecar.data.repository

import com.lautung.phonecar.data.local.DemoStateStore
import com.lautung.phonecar.data.model.CameraAngle
import com.lautung.phonecar.data.model.DemoState
import com.lautung.phonecar.data.model.DiscoveryTab
import com.lautung.phonecar.data.model.MaintenanceService
import com.lautung.phonecar.data.model.PaintOption
import com.lautung.phonecar.data.model.SubscriptionPlan
import com.lautung.phonecar.data.model.WheelOption
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    val state: Flow<DemoState>
    suspend fun setVehicleLocked(enabled: Boolean)
    suspend fun setAcEnabled(enabled: Boolean)
    suspend fun setAirPurification(enabled: Boolean)
    suspend fun setCabinTemperature(value: Float)
    suspend fun setFanLevel(value: Int)
    suspend fun setDriverSeatHeating(enabled: Boolean)
    suspend fun setPassengerSeatHeating(enabled: Boolean)
    suspend fun setSeatVentilation(enabled: Boolean)
    suspend fun setAutoHeadlights(enabled: Boolean)
    suspend fun setWelcomeLight(enabled: Boolean)
    suspend fun setWindowOpenPercent(value: Int)
    suspend fun setMirrorsFolded(enabled: Boolean)
    suspend fun setTrunkOpen(enabled: Boolean)
    suspend fun setSunshadeOpen(enabled: Boolean)
    suspend fun setChildLock(enabled: Boolean)
    suspend fun setSentryEnabled(enabled: Boolean)
    suspend fun setSentryCamera(angle: CameraAngle)
}

class DefaultVehicleRepository(private val store: DemoStateStore) : VehicleRepository {
    override val state = store.state
    override suspend fun setVehicleLocked(enabled: Boolean) = store.update { it.copy(vehicleLocked = enabled) }
    override suspend fun setAcEnabled(enabled: Boolean) = store.update { it.copy(acEnabled = enabled) }
    override suspend fun setAirPurification(enabled: Boolean) = store.update { it.copy(airPurificationEnabled = enabled) }
    override suspend fun setCabinTemperature(value: Float) = store.update { it.withCabinTemperature(value) }
    override suspend fun setFanLevel(value: Int) = store.update { it.withFanLevel(value) }
    override suspend fun setDriverSeatHeating(enabled: Boolean) = store.update { it.copy(driverSeatHeating = enabled) }
    override suspend fun setPassengerSeatHeating(enabled: Boolean) = store.update { it.copy(passengerSeatHeating = enabled) }
    override suspend fun setSeatVentilation(enabled: Boolean) = store.update { it.copy(seatVentilation = enabled) }
    override suspend fun setAutoHeadlights(enabled: Boolean) = store.update { it.copy(autoHeadlights = enabled) }
    override suspend fun setWelcomeLight(enabled: Boolean) = store.update { it.copy(welcomeLight = enabled) }
    override suspend fun setWindowOpenPercent(value: Int) = store.update { it.withWindowOpenPercent(value) }
    override suspend fun setMirrorsFolded(enabled: Boolean) = store.update { it.copy(mirrorsFolded = enabled) }
    override suspend fun setTrunkOpen(enabled: Boolean) = store.update { it.copy(trunkOpen = enabled) }
    override suspend fun setSunshadeOpen(enabled: Boolean) = store.update { it.copy(sunshadeOpen = enabled) }
    override suspend fun setChildLock(enabled: Boolean) = store.update { it.copy(childLock = enabled) }
    override suspend fun setSentryEnabled(enabled: Boolean) = store.update { it.copy(sentryEnabled = enabled) }
    override suspend fun setSentryCamera(angle: CameraAngle) = store.update { it.copy(selectedCamera = angle) }
}

interface ContentRepository {
    val state: Flow<DemoState>
    suspend fun selectTab(tab: DiscoveryTab)
    suspend fun setLiveRoomFollowed(followed: Boolean)
}

class DefaultContentRepository(private val store: DemoStateStore) : ContentRepository {
    override val state = store.state
    override suspend fun selectTab(tab: DiscoveryTab) = store.update { it.copy(discoveryTab = tab) }
    override suspend fun setLiveRoomFollowed(followed: Boolean) = store.update { it.copy(followedLiveRoom = followed) }
}

interface ServiceRepository {
    val state: Flow<DemoState>
    suspend fun selectPaint(option: PaintOption)
    suspend fun selectWheel(option: WheelOption)
    suspend fun selectMaintenanceService(service: MaintenanceService)
    suspend fun selectMaintenanceDay(day: Int)
    suspend fun toggleSubscription(plan: SubscriptionPlan)
    suspend fun confirmReservation()
    suspend fun confirmMaintenanceBooking()
    suspend fun cancelRescue()
}

class DefaultServiceRepository(private val store: DemoStateStore) : ServiceRepository {
    override val state = store.state
    override suspend fun selectPaint(option: PaintOption) = store.update { it.copy(selectedPaint = option) }
    override suspend fun selectWheel(option: WheelOption) = store.update { it.copy(selectedWheel = option) }
    override suspend fun selectMaintenanceService(service: MaintenanceService) = store.update { it.copy(maintenanceService = service) }
    override suspend fun selectMaintenanceDay(day: Int) = store.update { it.copy(maintenanceDay = day.coerceIn(9, 11)) }
    override suspend fun toggleSubscription(plan: SubscriptionPlan) = store.update { it.toggleSubscription(plan) }
    override suspend fun confirmReservation() = store.update { it.copy(reservationConfirmed = true) }
    override suspend fun confirmMaintenanceBooking() = store.update { it.copy(maintenanceBooked = true) }
    override suspend fun cancelRescue() = store.update { it.copy(rescueCancelled = true) }
}

interface ProfileRepository {
    val state: Flow<DemoState>
    suspend fun setLocationSharing(enabled: Boolean)
    suspend fun setCabinCamera(enabled: Boolean)
    suspend fun clearDrivingCache()
}

class DefaultProfileRepository(private val store: DemoStateStore) : ProfileRepository {
    override val state = store.state
    override suspend fun setLocationSharing(enabled: Boolean) = store.update { it.copy(locationSharingEnabled = enabled) }
    override suspend fun setCabinCamera(enabled: Boolean) = store.update { it.copy(cabinCameraEnabled = enabled) }
    override suspend fun clearDrivingCache() = store.update { it.copy(drivingCacheCleared = true) }
}
