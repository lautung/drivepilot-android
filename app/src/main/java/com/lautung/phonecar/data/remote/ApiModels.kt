package com.lautung.phonecar.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CredentialsRequest(val username: String, val password: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class UserDto(val id: String, val username: String, val role: String)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresAt: String,
    val user: UserDto,
)

@Serializable
data class VehicleStateDto(
    val vehicleLocked: Boolean,
    val acEnabled: Boolean,
    val airPurificationEnabled: Boolean,
    val cabinTemperature: Float,
    val fanLevel: Int,
    val driverSeatHeating: Boolean,
    val passengerSeatHeating: Boolean,
    val seatVentilation: Boolean,
    val autoHeadlights: Boolean,
    val welcomeLight: Boolean,
    val windowOpenPercent: Int,
    val mirrorsFolded: Boolean,
    val trunkOpen: Boolean,
    val sunshadeOpen: Boolean,
    val childLock: Boolean,
    val sentryEnabled: Boolean,
    val version: Long,
    val updatedAt: String,
)

@Serializable
data class VehicleStatePatch(
    val version: Long,
    val vehicleLocked: Boolean? = null,
    val acEnabled: Boolean? = null,
    val airPurificationEnabled: Boolean? = null,
    val cabinTemperature: Float? = null,
    val fanLevel: Int? = null,
    val driverSeatHeating: Boolean? = null,
    val passengerSeatHeating: Boolean? = null,
    val seatVentilation: Boolean? = null,
    val autoHeadlights: Boolean? = null,
    val welcomeLight: Boolean? = null,
    val windowOpenPercent: Int? = null,
    val mirrorsFolded: Boolean? = null,
    val trunkOpen: Boolean? = null,
    val sunshadeOpen: Boolean? = null,
    val childLock: Boolean? = null,
    val sentryEnabled: Boolean? = null,
)

@Serializable
data class PreferencesDto(
    val locationSharingEnabled: Boolean,
    val cabinCameraEnabled: Boolean,
    val updatedAt: String,
)

@Serializable
data class PreferencesPatch(
    val locationSharingEnabled: Boolean? = null,
    val cabinCameraEnabled: Boolean? = null,
)

@Serializable
data class ReservationRequest(val paint: String, val wheel: String)

@Serializable
data class MaintenanceRequest(val service: String, val bookingDate: String)

@Serializable
data class SubscriptionDto(
    val id: String,
    val plan: String,
    val active: Boolean,
    val activatedAt: String? = null,
    val deactivatedAt: String? = null,
    val updatedAt: String,
)

@Serializable
data class MediaDto(val id: String, val url: String, val expiresAt: String)

@Serializable
data class DiscoveryContentDto(
    val id: String,
    val category: String,
    val title: String,
    val summary: String,
    val body: String,
    val media: MediaDto? = null,
    val status: String,
    val publishedAt: String? = null,
    val followed: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PageDto<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
