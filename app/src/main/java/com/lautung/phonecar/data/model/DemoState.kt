package com.lautung.phonecar.data.model

enum class WheelOption(val surcharge: Int) {
    STANDARD_20(0),
    PERFORMANCE_21(8_000),
}

enum class PaintOption {
    AURORA_SILVER,
    DEEP_BLUE,
    OBSIDIAN_BLACK,
    PEARL_WHITE,
}

enum class CameraAngle {
    FRONT,
    REAR,
    LEFT,
    RIGHT,
}

enum class DiscoveryTab {
    RECOMMENDED,
    LOCAL,
    ACTIVITY,
    STORE,
}

enum class MaintenanceService {
    REGULAR,
    DIAGNOSTIC,
}

enum class SubscriptionPlan {
    AUTOPILOT,
    ENTERTAINMENT,
}

data class DemoState(
    val vehicleLocked: Boolean = true,
    val acEnabled: Boolean = true,
    val airPurificationEnabled: Boolean = false,
    val cabinTemperature: Float = 24.5f,
    val fanLevel: Int = 3,
    val driverSeatHeating: Boolean = false,
    val passengerSeatHeating: Boolean = false,
    val seatVentilation: Boolean = false,
    val autoHeadlights: Boolean = true,
    val welcomeLight: Boolean = false,
    val windowOpenPercent: Int = 10,
    val mirrorsFolded: Boolean = false,
    val trunkOpen: Boolean = false,
    val sunshadeOpen: Boolean = false,
    val childLock: Boolean = false,
    val sentryEnabled: Boolean = true,
    val selectedCamera: CameraAngle = CameraAngle.RIGHT,
    val discoveryTab: DiscoveryTab = DiscoveryTab.RECOMMENDED,
    val followedLiveRoom: Boolean = false,
    val selectedPaint: PaintOption = PaintOption.AURORA_SILVER,
    val selectedWheel: WheelOption = WheelOption.STANDARD_20,
    val maintenanceService: MaintenanceService = MaintenanceService.REGULAR,
    val maintenanceDay: Int = 9,
    val subscriptions: Set<SubscriptionPlan> = emptySet(),
    val locationSharingEnabled: Boolean = true,
    val cabinCameraEnabled: Boolean = true,
    val reservationConfirmed: Boolean = false,
    val maintenanceBooked: Boolean = false,
    val rescueCancelled: Boolean = false,
    val drivingCacheCleared: Boolean = false,
) {
    val totalVehiclePrice: Int
        get() = BASE_VEHICLE_PRICE + selectedWheel.surcharge

    fun withFanLevel(level: Int): DemoState = copy(fanLevel = level.coerceIn(1, 5))

    fun withWindowOpenPercent(percent: Int): DemoState =
        copy(windowOpenPercent = percent.coerceIn(0, 100))

    fun withCabinTemperature(temperature: Float): DemoState =
        copy(cabinTemperature = temperature.coerceIn(16f, 30f))

    fun toggleSubscription(plan: SubscriptionPlan): DemoState = copy(
        subscriptions = if (plan in subscriptions) subscriptions - plan else subscriptions + plan,
    )

    private companion object {
        const val BASE_VEHICLE_PRICE = 429_900
    }
}
