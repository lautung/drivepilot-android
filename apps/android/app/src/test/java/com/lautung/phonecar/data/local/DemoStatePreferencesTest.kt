package com.lautung.phonecar.data.local

import androidx.datastore.preferences.core.mutablePreferencesOf
import com.lautung.phonecar.data.model.CameraAngle
import com.lautung.phonecar.data.model.DemoState
import com.lautung.phonecar.data.model.DiscoveryTab
import com.lautung.phonecar.data.model.MaintenanceService
import com.lautung.phonecar.data.model.PaintOption
import com.lautung.phonecar.data.model.SubscriptionPlan
import com.lautung.phonecar.data.model.WheelOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoStatePreferencesTest {
    @Test
    fun preferencesRoundTrip_restoresInteractiveState() {
        val original = DemoState(
            vehicleLocked = false,
            acEnabled = false,
            airPurificationEnabled = true,
            cabinTemperature = 21.5f,
            fanLevel = 5,
            driverSeatHeating = true,
            passengerSeatHeating = true,
            seatVentilation = true,
            autoHeadlights = false,
            welcomeLight = true,
            windowOpenPercent = 65,
            mirrorsFolded = true,
            trunkOpen = true,
            sunshadeOpen = true,
            childLock = true,
            sentryEnabled = false,
            selectedCamera = CameraAngle.FRONT,
            discoveryTab = DiscoveryTab.ACTIVITY,
            followedLiveRoom = true,
            selectedPaint = PaintOption.DEEP_BLUE,
            selectedWheel = WheelOption.PERFORMANCE_21,
            maintenanceService = MaintenanceService.DIAGNOSTIC,
            maintenanceDay = 11,
            subscriptions = setOf(SubscriptionPlan.AUTOPILOT, SubscriptionPlan.ENTERTAINMENT),
            locationSharingEnabled = false,
            cabinCameraEnabled = true,
            reservationConfirmed = true,
            maintenanceBooked = true,
            rescueCancelled = true,
            drivingCacheCleared = true,
        )

        val restored = original.toPreferences().toDemoState()

        assertEquals(original, restored)
    }

    @Test
    fun emptyPreferences_usePrototypeDefaults() {
        val restored = mutablePreferencesOf().toDemoState()

        assertTrue(restored.vehicleLocked)
        assertTrue(restored.acEnabled)
        assertFalse(restored.airPurificationEnabled)
        assertEquals(CameraAngle.RIGHT, restored.selectedCamera)
        assertEquals(9, restored.maintenanceDay)
    }

    @Test
    fun invalidEnumValues_fallBackToPrototypeDefaults() {
        val preferences = mutablePreferencesOf(PreferenceKeys.SELECTED_CAMERA to "INVALID")

        val restored = preferences.toDemoState()

        assertEquals(CameraAngle.RIGHT, restored.selectedCamera)
    }
}
