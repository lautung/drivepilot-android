package com.lautung.phonecar.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoStateTest {
    @Test
    fun defaultState_matchesPrototype() {
        val state = DemoState()

        assertTrue(state.vehicleLocked)
        assertTrue(state.acEnabled)
        assertEquals(24.5f, state.cabinTemperature)
        assertEquals(3, state.fanLevel)
        assertEquals(10, state.windowOpenPercent)
        assertEquals(WheelOption.STANDARD_20, state.selectedWheel)
        assertEquals(429_900, state.totalVehiclePrice)
    }

    @Test
    fun performanceWheel_addsEightThousandToPrice() {
        val state = DemoState(selectedWheel = WheelOption.PERFORMANCE_21)

        assertEquals(437_900, state.totalVehiclePrice)
    }

    @Test
    fun boundedControls_areClampedToPrototypeRanges() {
        val state = DemoState()
            .withFanLevel(9)
            .withWindowOpenPercent(-10)
            .withCabinTemperature(31f)

        assertEquals(5, state.fanLevel)
        assertEquals(0, state.windowOpenPercent)
        assertEquals(30f, state.cabinTemperature)
    }

    @Test
    fun toggleSubscription_addsAndRemovesPlan() {
        val enabled = DemoState().toggleSubscription(SubscriptionPlan.AUTOPILOT)
        val disabled = enabled.toggleSubscription(SubscriptionPlan.AUTOPILOT)

        assertTrue(SubscriptionPlan.AUTOPILOT in enabled.subscriptions)
        assertFalse(SubscriptionPlan.AUTOPILOT in disabled.subscriptions)
    }
}
