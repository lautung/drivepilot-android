package com.lautung.phonecar.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: CredentialsRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: CredentialsRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshRequest)
}

interface PhoneCarApi {
    @GET("vehicle-state")
    suspend fun vehicleState(): VehicleStateDto

    @PATCH("vehicle-state")
    suspend fun updateVehicleState(@Body request: VehicleStatePatch): VehicleStateDto

    @GET("me/preferences")
    suspend fun preferences(): PreferencesDto

    @PATCH("me/preferences")
    suspend fun updatePreferences(@Body request: PreferencesPatch): PreferencesDto

    @POST("vehicle-reservations")
    suspend fun createReservation(@Body request: ReservationRequest)

    @POST("maintenance-bookings")
    suspend fun createMaintenance(@Body request: MaintenanceRequest)

    @GET("subscriptions")
    suspend fun subscriptions(): List<SubscriptionDto>

    @PUT("subscriptions/{plan}")
    suspend fun enableSubscription(@Path("plan") plan: String): SubscriptionDto

    @DELETE("subscriptions/{plan}")
    suspend fun disableSubscription(@Path("plan") plan: String)

    @GET("discovery/contents")
    suspend fun discoveryContents(
        @Query("category") category: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PageDto<DiscoveryContentDto>

    @PUT("discovery/contents/{id}/follow")
    suspend fun followContent(@Path("id") id: String)

    @DELETE("discovery/contents/{id}/follow")
    suspend fun unfollowContent(@Path("id") id: String)
}
