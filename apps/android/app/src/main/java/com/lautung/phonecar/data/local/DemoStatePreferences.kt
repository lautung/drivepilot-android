package com.lautung.phonecar.data.local

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.lautung.phonecar.data.model.CameraAngle
import com.lautung.phonecar.data.model.DemoState
import com.lautung.phonecar.data.model.DiscoveryTab
import com.lautung.phonecar.data.model.MaintenanceService
import com.lautung.phonecar.data.model.PaintOption
import com.lautung.phonecar.data.model.SubscriptionPlan
import com.lautung.phonecar.data.model.WheelOption

object PreferenceKeys {
    val VEHICLE_LOCKED = booleanPreferencesKey("vehicle_locked")
    val AC_ENABLED = booleanPreferencesKey("ac_enabled")
    val AIR_PURIFICATION = booleanPreferencesKey("air_purification")
    val CABIN_TEMPERATURE = floatPreferencesKey("cabin_temperature")
    val FAN_LEVEL = intPreferencesKey("fan_level")
    val DRIVER_SEAT_HEATING = booleanPreferencesKey("driver_seat_heating")
    val PASSENGER_SEAT_HEATING = booleanPreferencesKey("passenger_seat_heating")
    val SEAT_VENTILATION = booleanPreferencesKey("seat_ventilation")
    val AUTO_HEADLIGHTS = booleanPreferencesKey("auto_headlights")
    val WELCOME_LIGHT = booleanPreferencesKey("welcome_light")
    val WINDOW_PERCENT = intPreferencesKey("window_percent")
    val MIRRORS_FOLDED = booleanPreferencesKey("mirrors_folded")
    val TRUNK_OPEN = booleanPreferencesKey("trunk_open")
    val SUNSHADE_OPEN = booleanPreferencesKey("sunshade_open")
    val CHILD_LOCK = booleanPreferencesKey("child_lock")
    val SENTRY_ENABLED = booleanPreferencesKey("sentry_enabled")
    val SELECTED_CAMERA = stringPreferencesKey("selected_camera")
    val DISCOVERY_TAB = stringPreferencesKey("discovery_tab")
    val FOLLOWED_LIVE_ROOM = booleanPreferencesKey("followed_live_room")
    val SELECTED_PAINT = stringPreferencesKey("selected_paint")
    val SELECTED_WHEEL = stringPreferencesKey("selected_wheel")
    val MAINTENANCE_SERVICE = stringPreferencesKey("maintenance_service")
    val MAINTENANCE_DAY = intPreferencesKey("maintenance_day")
    val SUBSCRIPTIONS = stringSetPreferencesKey("subscriptions")
    val LOCATION_SHARING = booleanPreferencesKey("location_sharing")
    val CABIN_CAMERA = booleanPreferencesKey("cabin_camera")
    val RESERVATION_CONFIRMED = booleanPreferencesKey("reservation_confirmed")
    val MAINTENANCE_BOOKED = booleanPreferencesKey("maintenance_booked")
    val RESCUE_CANCELLED = booleanPreferencesKey("rescue_cancelled")
    val DRIVING_CACHE_CLEARED = booleanPreferencesKey("driving_cache_cleared")
}

fun DemoState.toPreferences(): Preferences = mutablePreferencesOf().also { writeTo(it) }

fun DemoState.writeTo(target: MutablePreferences) {
    target[PreferenceKeys.VEHICLE_LOCKED] = vehicleLocked
    target[PreferenceKeys.AC_ENABLED] = acEnabled
    target[PreferenceKeys.AIR_PURIFICATION] = airPurificationEnabled
    target[PreferenceKeys.CABIN_TEMPERATURE] = cabinTemperature
    target[PreferenceKeys.FAN_LEVEL] = fanLevel
    target[PreferenceKeys.DRIVER_SEAT_HEATING] = driverSeatHeating
    target[PreferenceKeys.PASSENGER_SEAT_HEATING] = passengerSeatHeating
    target[PreferenceKeys.SEAT_VENTILATION] = seatVentilation
    target[PreferenceKeys.AUTO_HEADLIGHTS] = autoHeadlights
    target[PreferenceKeys.WELCOME_LIGHT] = welcomeLight
    target[PreferenceKeys.WINDOW_PERCENT] = windowOpenPercent
    target[PreferenceKeys.MIRRORS_FOLDED] = mirrorsFolded
    target[PreferenceKeys.TRUNK_OPEN] = trunkOpen
    target[PreferenceKeys.SUNSHADE_OPEN] = sunshadeOpen
    target[PreferenceKeys.CHILD_LOCK] = childLock
    target[PreferenceKeys.SENTRY_ENABLED] = sentryEnabled
    target[PreferenceKeys.SELECTED_CAMERA] = selectedCamera.name
    target[PreferenceKeys.DISCOVERY_TAB] = discoveryTab.name
    target[PreferenceKeys.FOLLOWED_LIVE_ROOM] = followedLiveRoom
    target[PreferenceKeys.SELECTED_PAINT] = selectedPaint.name
    target[PreferenceKeys.SELECTED_WHEEL] = selectedWheel.name
    target[PreferenceKeys.MAINTENANCE_SERVICE] = maintenanceService.name
    target[PreferenceKeys.MAINTENANCE_DAY] = maintenanceDay
    target[PreferenceKeys.SUBSCRIPTIONS] = subscriptions.mapTo(mutableSetOf()) { it.name }
    target[PreferenceKeys.LOCATION_SHARING] = locationSharingEnabled
    target[PreferenceKeys.CABIN_CAMERA] = cabinCameraEnabled
    target[PreferenceKeys.RESERVATION_CONFIRMED] = reservationConfirmed
    target[PreferenceKeys.MAINTENANCE_BOOKED] = maintenanceBooked
    target[PreferenceKeys.RESCUE_CANCELLED] = rescueCancelled
    target[PreferenceKeys.DRIVING_CACHE_CLEARED] = drivingCacheCleared
}

fun Preferences.toDemoState(): DemoState {
    val defaults = DemoState()
    return DemoState(
        vehicleLocked = this[PreferenceKeys.VEHICLE_LOCKED] ?: defaults.vehicleLocked,
        acEnabled = this[PreferenceKeys.AC_ENABLED] ?: defaults.acEnabled,
        airPurificationEnabled = this[PreferenceKeys.AIR_PURIFICATION] ?: defaults.airPurificationEnabled,
        cabinTemperature = this[PreferenceKeys.CABIN_TEMPERATURE] ?: defaults.cabinTemperature,
        fanLevel = this[PreferenceKeys.FAN_LEVEL] ?: defaults.fanLevel,
        driverSeatHeating = this[PreferenceKeys.DRIVER_SEAT_HEATING] ?: defaults.driverSeatHeating,
        passengerSeatHeating = this[PreferenceKeys.PASSENGER_SEAT_HEATING] ?: defaults.passengerSeatHeating,
        seatVentilation = this[PreferenceKeys.SEAT_VENTILATION] ?: defaults.seatVentilation,
        autoHeadlights = this[PreferenceKeys.AUTO_HEADLIGHTS] ?: defaults.autoHeadlights,
        welcomeLight = this[PreferenceKeys.WELCOME_LIGHT] ?: defaults.welcomeLight,
        windowOpenPercent = this[PreferenceKeys.WINDOW_PERCENT] ?: defaults.windowOpenPercent,
        mirrorsFolded = this[PreferenceKeys.MIRRORS_FOLDED] ?: defaults.mirrorsFolded,
        trunkOpen = this[PreferenceKeys.TRUNK_OPEN] ?: defaults.trunkOpen,
        sunshadeOpen = this[PreferenceKeys.SUNSHADE_OPEN] ?: defaults.sunshadeOpen,
        childLock = this[PreferenceKeys.CHILD_LOCK] ?: defaults.childLock,
        sentryEnabled = this[PreferenceKeys.SENTRY_ENABLED] ?: defaults.sentryEnabled,
        selectedCamera = enumOrDefault(this[PreferenceKeys.SELECTED_CAMERA], defaults.selectedCamera),
        discoveryTab = enumOrDefault(this[PreferenceKeys.DISCOVERY_TAB], defaults.discoveryTab),
        followedLiveRoom = this[PreferenceKeys.FOLLOWED_LIVE_ROOM] ?: defaults.followedLiveRoom,
        selectedPaint = enumOrDefault(this[PreferenceKeys.SELECTED_PAINT], defaults.selectedPaint),
        selectedWheel = enumOrDefault(this[PreferenceKeys.SELECTED_WHEEL], defaults.selectedWheel),
        maintenanceService = enumOrDefault(this[PreferenceKeys.MAINTENANCE_SERVICE], defaults.maintenanceService),
        maintenanceDay = this[PreferenceKeys.MAINTENANCE_DAY] ?: defaults.maintenanceDay,
        subscriptions = this[PreferenceKeys.SUBSCRIPTIONS]
            ?.mapNotNullTo(mutableSetOf()) { name -> enumOrNull<SubscriptionPlan>(name) }
            ?: defaults.subscriptions,
        locationSharingEnabled = this[PreferenceKeys.LOCATION_SHARING] ?: defaults.locationSharingEnabled,
        cabinCameraEnabled = this[PreferenceKeys.CABIN_CAMERA] ?: defaults.cabinCameraEnabled,
        reservationConfirmed = this[PreferenceKeys.RESERVATION_CONFIRMED] ?: defaults.reservationConfirmed,
        maintenanceBooked = this[PreferenceKeys.MAINTENANCE_BOOKED] ?: defaults.maintenanceBooked,
        rescueCancelled = this[PreferenceKeys.RESCUE_CANCELLED] ?: defaults.rescueCancelled,
        drivingCacheCleared = this[PreferenceKeys.DRIVING_CACHE_CLEARED] ?: defaults.drivingCacheCleared,
    )
}

private inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T =
    enumOrNull<T>(value) ?: default

private inline fun <reified T : Enum<T>> enumOrNull(value: String?): T? =
    enumValues<T>().firstOrNull { it.name == value }
