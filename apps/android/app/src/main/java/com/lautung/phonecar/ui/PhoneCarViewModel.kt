package com.lautung.phonecar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lautung.phonecar.data.local.DemoStateStore
import com.lautung.phonecar.data.auth.AuthRepository
import com.lautung.phonecar.data.auth.AuthState
import com.lautung.phonecar.data.model.CameraAngle
import com.lautung.phonecar.data.model.DemoState
import com.lautung.phonecar.data.model.DiscoveryTab
import com.lautung.phonecar.data.model.DiscoveryContent
import com.lautung.phonecar.data.model.MaintenanceService
import com.lautung.phonecar.data.model.PaintOption
import com.lautung.phonecar.data.model.SubscriptionPlan
import com.lautung.phonecar.data.model.WheelOption
import com.lautung.phonecar.data.repository.DefaultContentRepository
import com.lautung.phonecar.data.repository.DefaultProfileRepository
import com.lautung.phonecar.data.repository.DefaultServiceRepository
import com.lautung.phonecar.data.repository.DefaultVehicleRepository
import com.lautung.phonecar.data.repository.BackendSyncRepository
import com.lautung.phonecar.data.repository.ContentRepository
import com.lautung.phonecar.data.repository.ProfileRepository
import com.lautung.phonecar.data.repository.RemoteProfileRepository
import com.lautung.phonecar.data.repository.RemoteServiceRepository
import com.lautung.phonecar.data.repository.RemoteVehicleRepository
import com.lautung.phonecar.data.repository.ServiceRepository
import com.lautung.phonecar.data.repository.VehicleRepository
import com.lautung.phonecar.data.remote.PhoneCarApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PhoneCarViewModel(store: DemoStateStore) : ViewModel() {
    constructor(
        store: DemoStateStore,
        authRepository: AuthRepository,
        phoneCarApi: PhoneCarApi,
    ) : this(store) {
        this.authRepository = authRepository
        this.phoneCarApi = phoneCarApi
        val remoteSync = BackendSyncRepository(store, phoneCarApi)
        backendSync = remoteSync
        vehicle = RemoteVehicleRepository(store, remoteSync)
        service = RemoteServiceRepository(store, phoneCarApi, remoteSync)
        profile = RemoteProfileRepository(store, remoteSync)
        syncError = remoteSync.error
        discoveryContents = remoteSync.contents
        authState = authRepository.state
        viewModelScope.launch { authRepository.restore() }
        viewModelScope.launch {
            authRepository.state.collect { auth ->
                when (auth) {
                    is AuthState.SignedIn -> remoteSync.refresh()
                    AuthState.SignedOut -> remoteSync.clearUserCache()
                    else -> Unit
                }
            }
        }
    }

    private var authRepository: AuthRepository? = null
    private var phoneCarApi: PhoneCarApi? = null
    private var backendSync: BackendSyncRepository? = null
    private var vehicle: VehicleRepository = DefaultVehicleRepository(store)
    private var content: ContentRepository = DefaultContentRepository(store)
    private var service: ServiceRepository = DefaultServiceRepository(store)
    private var profile: ProfileRepository = DefaultProfileRepository(store)

    val state: StateFlow<DemoState> = store.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DemoState(),
    )

    var authState: StateFlow<AuthState> = kotlinx.coroutines.flow.MutableStateFlow(
        AuthState.SignedIn("offline-demo", "智能驾驶官", "USER"),
    )
        private set

    var syncError: StateFlow<String?> = kotlinx.coroutines.flow.MutableStateFlow(null)
        private set

    var discoveryContents: StateFlow<List<DiscoveryContent>> = kotlinx.coroutines.flow.MutableStateFlow(emptyList())
        private set

    fun login(username: String, password: String) = launch { authRepository?.login(username, password) }
    fun register(username: String, password: String) = launch { authRepository?.register(username, password) }
    fun logout() = launch { authRepository?.logout() }

    fun setVehicleLocked(value: Boolean) = launch { vehicle.setVehicleLocked(value) }
    fun setAcEnabled(value: Boolean) = launch { vehicle.setAcEnabled(value) }
    fun setAirPurification(value: Boolean) = launch { vehicle.setAirPurification(value) }
    fun setCabinTemperature(value: Float) = launch { vehicle.setCabinTemperature(value) }
    fun setFanLevel(value: Int) = launch { vehicle.setFanLevel(value) }
    fun setDriverSeatHeating(value: Boolean) = launch { vehicle.setDriverSeatHeating(value) }
    fun setPassengerSeatHeating(value: Boolean) = launch { vehicle.setPassengerSeatHeating(value) }
    fun setSeatVentilation(value: Boolean) = launch { vehicle.setSeatVentilation(value) }
    fun setAutoHeadlights(value: Boolean) = launch { vehicle.setAutoHeadlights(value) }
    fun setWelcomeLight(value: Boolean) = launch { vehicle.setWelcomeLight(value) }
    fun setWindowOpenPercent(value: Int) = launch { vehicle.setWindowOpenPercent(value) }
    fun setMirrorsFolded(value: Boolean) = launch { vehicle.setMirrorsFolded(value) }
    fun setTrunkOpen(value: Boolean) = launch { vehicle.setTrunkOpen(value) }
    fun setSunshadeOpen(value: Boolean) = launch { vehicle.setSunshadeOpen(value) }
    fun setChildLock(value: Boolean) = launch { vehicle.setChildLock(value) }
    fun setSentryEnabled(value: Boolean) = launch { vehicle.setSentryEnabled(value) }
    fun setSentryCamera(value: CameraAngle) = launch { vehicle.setSentryCamera(value) }
    fun selectDiscoveryTab(value: DiscoveryTab) = launch { content.selectTab(value) }
    fun setLiveRoomFollowed(value: Boolean) = launch { content.setLiveRoomFollowed(value) }
    fun selectPaint(value: PaintOption) = launch { service.selectPaint(value) }
    fun selectWheel(value: WheelOption) = launch { service.selectWheel(value) }
    fun selectMaintenanceService(value: MaintenanceService) = launch { service.selectMaintenanceService(value) }
    fun selectMaintenanceDay(value: Int) = launch { service.selectMaintenanceDay(value) }
    fun toggleSubscription(value: SubscriptionPlan) = launch { service.toggleSubscription(value) }
    fun confirmReservation() = launch { service.confirmReservation() }
    fun confirmMaintenanceBooking() = launch { service.confirmMaintenanceBooking() }
    fun cancelRescue() = launch { service.cancelRescue() }
    fun setLocationSharing(value: Boolean) = launch { profile.setLocationSharing(value) }
    fun setCabinCamera(value: Boolean) = launch { profile.setCabinCamera(value) }
    fun clearDrivingCache() = launch { profile.clearDrivingCache() }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}

class PhoneCarViewModelFactory(
    private val store: DemoStateStore,
    private val authRepository: AuthRepository,
    private val phoneCarApi: PhoneCarApi,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PhoneCarViewModel(store, authRepository, phoneCarApi) as T
}
