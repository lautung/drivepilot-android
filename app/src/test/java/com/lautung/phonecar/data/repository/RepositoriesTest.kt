package com.lautung.phonecar.data.repository

import com.lautung.phonecar.data.local.InMemoryDemoStateStore
import com.lautung.phonecar.data.model.CameraAngle
import com.lautung.phonecar.data.model.DiscoveryTab
import com.lautung.phonecar.data.model.SubscriptionPlan
import com.lautung.phonecar.data.model.WheelOption
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoriesTest {
    @Test
    fun vehicleRepository_updatesSharedPersistentState() = runBlocking {
        val store = InMemoryDemoStateStore()
        val repository: VehicleRepository = DefaultVehicleRepository(store)

        repository.setVehicleLocked(false)
        repository.setCabinTemperature(35f)
        repository.setSentryCamera(CameraAngle.FRONT)

        assertFalse(store.current.vehicleLocked)
        assertEquals(30f, store.current.cabinTemperature)
        assertEquals(CameraAngle.FRONT, store.current.selectedCamera)
    }

    @Test
    fun contentServiceAndProfileRepositories_shareOneStateStore() = runBlocking {
        val store = InMemoryDemoStateStore()
        val content: ContentRepository = DefaultContentRepository(store)
        val service: ServiceRepository = DefaultServiceRepository(store)
        val profile: ProfileRepository = DefaultProfileRepository(store)

        content.selectTab(DiscoveryTab.ACTIVITY)
        service.selectWheel(WheelOption.PERFORMANCE_21)
        service.toggleSubscription(SubscriptionPlan.AUTOPILOT)
        profile.setLocationSharing(false)
        profile.clearDrivingCache()

        assertEquals(DiscoveryTab.ACTIVITY, store.current.discoveryTab)
        assertEquals(437_900, store.current.totalVehiclePrice)
        assertTrue(SubscriptionPlan.AUTOPILOT in store.current.subscriptions)
        assertFalse(store.current.locationSharingEnabled)
        assertTrue(store.current.drivingCacheCleared)
    }
}
