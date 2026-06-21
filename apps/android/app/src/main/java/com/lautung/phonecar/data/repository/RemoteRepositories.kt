package com.lautung.phonecar.data.repository

import com.lautung.phonecar.data.local.DemoStateStore
import com.lautung.phonecar.data.model.CameraAngle
import com.lautung.phonecar.data.model.DemoState
import com.lautung.phonecar.data.model.DiscoveryContent
import com.lautung.phonecar.data.model.MaintenanceService
import com.lautung.phonecar.data.model.PaintOption
import com.lautung.phonecar.data.model.SubscriptionPlan
import com.lautung.phonecar.data.model.WheelOption
import com.lautung.phonecar.data.remote.MaintenanceRequest
import com.lautung.phonecar.data.remote.PhoneCarApi
import com.lautung.phonecar.data.remote.PreferencesPatch
import com.lautung.phonecar.data.remote.ReservationRequest
import com.lautung.phonecar.data.remote.VehicleStateDto
import com.lautung.phonecar.data.remote.VehicleStatePatch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BackendSyncRepository(
    private val store: DemoStateStore,
    private val api: PhoneCarApi,
) {
    private val writeMutex = Mutex()
    private var vehicleVersion: Long? = null
    private val mutableError = MutableStateFlow<String?>(null)
    val error = mutableError.asStateFlow()
    private val mutableContents = MutableStateFlow<List<DiscoveryContent>>(emptyList())
    val contents = mutableContents.asStateFlow()

    suspend fun refresh() {
        runCatching {
            val vehicle = api.vehicleState()
            val preferences = api.preferences()
            val subscriptions = api.subscriptions()
            val contents = api.discoveryContents(size = 50)
            vehicleVersion = vehicle.version
            store.update { current ->
                current.withRemoteVehicle(vehicle).copy(
                    locationSharingEnabled = preferences.locationSharingEnabled,
                    cabinCameraEnabled = preferences.cabinCameraEnabled,
                    subscriptions = subscriptions.filter { it.active }
                        .mapNotNullTo(mutableSetOf()) { dto ->
                            enumValues<SubscriptionPlan>().firstOrNull { it.name == dto.plan }
                        },
                    reservationConfirmed = false,
                    maintenanceBooked = false,
                )
            }
            mutableContents.value = contents.items.map { content ->
                DiscoveryContent(
                    id = content.id,
                    category = content.category,
                    title = content.title,
                    summary = content.summary,
                    mediaId = content.media?.id,
                    mediaUrl = content.media?.url,
                    followed = content.followed,
                )
            }
        }.onSuccess {
            mutableError.value = null
        }.onFailure {
            mutableError.value = NETWORK_ERROR
        }
    }

    suspend fun updateVehicle(patch: (Long) -> VehicleStatePatch) {
        writeMutex.withLock {
            val version = vehicleVersion
            if (version == null) {
                refresh()
                if (vehicleVersion == null) return
            }
            runCatching { api.updateVehicleState(patch(requireNotNull(vehicleVersion))) }
                .onSuccess { response ->
                    vehicleVersion = response.version
                    store.update { it.withRemoteVehicle(response) }
                    mutableError.value = null
                }
                .onFailure {
                    mutableError.value = NETWORK_ERROR
                    if (it is retrofit2.HttpException && it.code() == 409) refresh()
                }
        }
    }

    suspend fun updatePreferences(patch: PreferencesPatch) {
        runCatching { api.updatePreferences(patch) }
            .onSuccess { response ->
                store.update { current -> current.copy(
                    locationSharingEnabled = response.locationSharingEnabled,
                    cabinCameraEnabled = response.cabinCameraEnabled,
                ) }
                mutableError.value = null
            }
            .onFailure { mutableError.value = NETWORK_ERROR }
    }

    suspend fun clearUserCache() {
        vehicleVersion = null
        store.update { current ->
            DemoState(
                selectedCamera = current.selectedCamera,
                discoveryTab = current.discoveryTab,
                selectedPaint = current.selectedPaint,
                selectedWheel = current.selectedWheel,
                maintenanceService = current.maintenanceService,
                maintenanceDay = current.maintenanceDay,
                rescueCancelled = current.rescueCancelled,
                drivingCacheCleared = current.drivingCacheCleared,
            )
        }
        mutableError.value = null
        mutableContents.value = emptyList()
    }

    fun reportFailure() { mutableError.value = NETWORK_ERROR }
    fun clearError() { mutableError.value = null }

    private fun DemoState.withRemoteVehicle(remote: VehicleStateDto) = copy(
        vehicleLocked = remote.vehicleLocked,
        acEnabled = remote.acEnabled,
        airPurificationEnabled = remote.airPurificationEnabled,
        cabinTemperature = remote.cabinTemperature,
        fanLevel = remote.fanLevel,
        driverSeatHeating = remote.driverSeatHeating,
        passengerSeatHeating = remote.passengerSeatHeating,
        seatVentilation = remote.seatVentilation,
        autoHeadlights = remote.autoHeadlights,
        welcomeLight = remote.welcomeLight,
        windowOpenPercent = remote.windowOpenPercent,
        mirrorsFolded = remote.mirrorsFolded,
        trunkOpen = remote.trunkOpen,
        sunshadeOpen = remote.sunshadeOpen,
        childLock = remote.childLock,
        sentryEnabled = remote.sentryEnabled,
    )

    private companion object {
        const val NETWORK_ERROR = "操作失败，已保留最近同步状态，请检查网络后重试"
    }
}

class RemoteVehicleRepository(
    private val store: DemoStateStore,
    private val sync: BackendSyncRepository,
) : VehicleRepository {
    override val state: Flow<DemoState> = store.state
    override suspend fun setVehicleLocked(enabled: Boolean) = sync.updateVehicle { VehicleStatePatch(it, vehicleLocked = enabled) }
    override suspend fun setAcEnabled(enabled: Boolean) = sync.updateVehicle { VehicleStatePatch(it, acEnabled = enabled) }
    override suspend fun setAirPurification(enabled: Boolean) = sync.updateVehicle { VehicleStatePatch(it, airPurificationEnabled = enabled) }
    override suspend fun setCabinTemperature(value: Float) = sync.updateVehicle { VehicleStatePatch(it, cabinTemperature = value.coerceIn(16f, 30f)) }
    override suspend fun setFanLevel(value: Int) = sync.updateVehicle { VehicleStatePatch(it, fanLevel = value.coerceIn(1, 5)) }
    override suspend fun setDriverSeatHeating(enabled: Boolean) = sync.updateVehicle { VehicleStatePatch(it, driverSeatHeating = enabled) }
    override suspend fun setPassengerSeatHeating(enabled: Boolean) = sync.updateVehicle { VehicleStatePatch(it, passengerSeatHeating = enabled) }
    override suspend fun setSeatVentilation(enabled: Boolean) = sync.updateVehicle { VehicleStatePatch(it, seatVentilation = enabled) }
    override suspend fun setAutoHeadlights(enabled: Boolean) = sync.updateVehicle { VehicleStatePatch(it, autoHeadlights = enabled) }
    override suspend fun setWelcomeLight(enabled: Boolean) = sync.updateVehicle { VehicleStatePatch(it, welcomeLight = enabled) }
    override suspend fun setWindowOpenPercent(value: Int) = sync.updateVehicle { VehicleStatePatch(it, windowOpenPercent = value.coerceIn(0, 100)) }
    override suspend fun setMirrorsFolded(enabled: Boolean) = sync.updateVehicle { VehicleStatePatch(it, mirrorsFolded = enabled) }
    override suspend fun setTrunkOpen(enabled: Boolean) = sync.updateVehicle { VehicleStatePatch(it, trunkOpen = enabled) }
    override suspend fun setSunshadeOpen(enabled: Boolean) = sync.updateVehicle { VehicleStatePatch(it, sunshadeOpen = enabled) }
    override suspend fun setChildLock(enabled: Boolean) = sync.updateVehicle { VehicleStatePatch(it, childLock = enabled) }
    override suspend fun setSentryEnabled(enabled: Boolean) = sync.updateVehicle { VehicleStatePatch(it, sentryEnabled = enabled) }
    override suspend fun setSentryCamera(angle: CameraAngle) = store.update { it.copy(selectedCamera = angle) }
}

class RemoteProfileRepository(
    private val store: DemoStateStore,
    private val sync: BackendSyncRepository,
) : ProfileRepository {
    override val state: Flow<DemoState> = store.state
    override suspend fun setLocationSharing(enabled: Boolean) = sync.updatePreferences(PreferencesPatch(locationSharingEnabled = enabled))
    override suspend fun setCabinCamera(enabled: Boolean) = sync.updatePreferences(PreferencesPatch(cabinCameraEnabled = enabled))
    override suspend fun clearDrivingCache() = store.update { it.copy(drivingCacheCleared = true) }
}

class RemoteServiceRepository(
    private val store: DemoStateStore,
    private val api: PhoneCarApi,
    private val sync: BackendSyncRepository,
) : ServiceRepository {
    override val state: Flow<DemoState> = store.state
    override suspend fun selectPaint(option: PaintOption) = store.update { it.copy(selectedPaint = option) }
    override suspend fun selectWheel(option: WheelOption) = store.update { it.copy(selectedWheel = option) }
    override suspend fun selectMaintenanceService(service: MaintenanceService) = store.update { it.copy(maintenanceService = service) }
    override suspend fun selectMaintenanceDay(day: Int) = store.update { it.copy(maintenanceDay = day.coerceIn(9, 11)) }

    override suspend fun toggleSubscription(plan: SubscriptionPlan) {
        val enabling = plan !in store.state.first().subscriptions
        runCatching {
            if (enabling) api.enableSubscription(plan.name) else api.disableSubscription(plan.name)
        }.onSuccess {
            store.update { it.toggleSubscription(plan) }
            sync.clearError()
        }.onFailure { sync.reportFailure() }
    }

    override suspend fun confirmReservation() {
        val current = store.state.first()
        runCatching { api.createReservation(ReservationRequest(current.selectedPaint.name, current.selectedWheel.name)) }
            .onSuccess { store.update { it.copy(reservationConfirmed = true) }; sync.clearError() }
            .onFailure { sync.reportFailure() }
    }

    override suspend fun confirmMaintenanceBooking() {
        val current = store.state.first()
        val offset = (current.maintenanceDay - 8).coerceAtLeast(1)
        val bookingDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, offset) }.time
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(bookingDate)
        runCatching { api.createMaintenance(MaintenanceRequest(current.maintenanceService.name, formattedDate)) }
            .onSuccess { store.update { it.copy(maintenanceBooked = true) }; sync.clearError() }
            .onFailure { sync.reportFailure() }
    }

    override suspend fun cancelRescue() = store.update { it.copy(rescueCancelled = true) }
}
