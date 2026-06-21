package com.lautung.phonecar.data.auth

import com.lautung.phonecar.data.remote.AuthApi
import com.lautung.phonecar.data.remote.AuthResponse
import com.lautung.phonecar.data.remote.CredentialsRequest
import com.lautung.phonecar.data.remote.NetworkContainer
import com.lautung.phonecar.data.remote.RefreshRequest
import com.lautung.phonecar.data.remote.UserDto
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {
    @Test
    fun loginAndLogout_persistAndClearRefreshSession() = runBlocking {
        val store = InMemorySessionStore()
        val repository = AuthRepository(FakeAuthApi(), SessionManager(store))

        repository.restore()
        assertEquals(AuthState.SignedOut, repository.state.value)

        repository.login(" Driver_1 ", "password")

        assertTrue(repository.state.value is AuthState.SignedIn)
        assertEquals("refresh-login", store.value?.refreshToken)

        repository.logout()

        assertEquals(AuthState.SignedOut, repository.state.value)
        assertNull(store.value)
    }

    @Test
    fun restore_rotatesStoredRefreshToken() = runBlocking {
        val store = InMemorySessionStore(StoredSession("old", "old", "USER", "stored-refresh"))
        val repository = AuthRepository(FakeAuthApi(), SessionManager(store))

        repository.restore()

        assertEquals(AuthState.SignedIn("user-id", "driver", "USER"), repository.state.value)
        assertEquals("refresh-restored", store.value?.refreshToken)
    }

    @Test
    fun concurrentUnauthorizedRequests_refreshOnlyOnce() = runBlocking {
        val refreshCalls = AtomicInteger()
        val server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path == "/api/v1/auth/refresh" -> {
                    val call = refreshCalls.incrementAndGet()
                    jsonResponse(authJson("access-$call", "refresh-$call"))
                }
                request.path == "/api/v1/vehicle-state" &&
                    request.getHeader("Authorization") == "Bearer access-2" -> jsonResponse(vehicleJson())
                request.path == "/api/v1/vehicle-state" -> MockResponse().setResponseCode(401)
                else -> MockResponse().setResponseCode(404)
            }
        }
        server.start()
        try {
            val store = InMemorySessionStore(StoredSession("user-id", "driver", "USER", "stored-refresh"))
            val container = NetworkContainer(server.url("/api/v1/").toString(), store)
            container.authRepository.restore()

            listOf(
                async { container.phoneCarApi.vehicleState() },
                async { container.phoneCarApi.vehicleState() },
            ).awaitAll()

            assertEquals(2, refreshCalls.get())
            assertEquals("refresh-2", store.value?.refreshToken)
        } finally {
            server.shutdown()
        }
    }

    private fun jsonResponse(body: String) = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    private fun authJson(access: String, refresh: String) = """
        {"accessToken":"$access","refreshToken":"$refresh","accessExpiresAt":"2026-06-20T00:00:00Z","user":{"id":"user-id","username":"driver","role":"USER"}}
    """.trimIndent()

    private fun vehicleJson() = """
        {"vehicleLocked":true,"acEnabled":false,"airPurificationEnabled":false,"cabinTemperature":22.0,"fanLevel":1,"driverSeatHeating":false,"passengerSeatHeating":false,"seatVentilation":false,"autoHeadlights":true,"welcomeLight":true,"windowOpenPercent":0,"mirrorsFolded":true,"trunkOpen":false,"sunshadeOpen":false,"childLock":false,"sentryEnabled":false,"version":0,"updatedAt":"2026-06-20T00:00:00Z"}
    """.trimIndent()
}

private class InMemorySessionStore(initial: StoredSession? = null) : SessionStore {
    var value = initial
    override fun read(): StoredSession? = value
    override fun write(session: StoredSession) { value = session }
    override fun clear() { value = null }
}

private class FakeAuthApi : AuthApi {
    override suspend fun register(request: CredentialsRequest) = response("refresh-register")
    override suspend fun login(request: CredentialsRequest) = response("refresh-login")
    override suspend fun refresh(request: RefreshRequest) = response("refresh-restored")
    override suspend fun logout(request: RefreshRequest) = Unit

    private fun response(refreshToken: String) = AuthResponse(
        accessToken = "access",
        refreshToken = refreshToken,
        accessExpiresAt = "2026-06-20T00:00:00Z",
        user = UserDto("user-id", "driver", "USER"),
    )
}
