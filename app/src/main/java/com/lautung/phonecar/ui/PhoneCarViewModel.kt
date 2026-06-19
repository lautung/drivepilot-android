package com.lautung.phonecar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lautung.phonecar.data.local.DemoStateStore
import com.lautung.phonecar.data.model.CameraAngle
import com.lautung.phonecar.data.model.DemoState
import com.lautung.phonecar.data.model.DiscoveryTab
import com.lautung.phonecar.data.model.MaintenanceService
import com.lautung.phonecar.data.model.PaintOption
import com.lautung.phonecar.data.model.SubscriptionPlan
import com.lautung.phonecar.data.model.WheelOption
import com.lautung.phonecar.data.repository.DefaultContentRepository
import com.lautung.phonecar.data.repository.DefaultProfileRepository
import com.lautung.phonecar.data.repository.DefaultServiceRepository
import com.lautung.phonecar.data.repository.DefaultVehicleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PhoneCarViewModel(store: DemoStateStore) : ViewModel() {
    private val vehicle = DefaultVehicleRepository(store)
    private val content = DefaultContentRepository(store)
    private val service = DefaultServiceRepository(store)
    private val profile = DefaultProfileRepository(store)

    val state: StateFlow<DemoState> = store.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DemoState(),
    )

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
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PhoneCarViewModel(store) as T
}
