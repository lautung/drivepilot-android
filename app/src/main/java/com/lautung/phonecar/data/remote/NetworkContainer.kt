package com.lautung.phonecar.data.remote

import com.lautung.phonecar.data.auth.AuthRepository
import com.lautung.phonecar.data.auth.SessionManager
import com.lautung.phonecar.data.auth.SessionStore
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class NetworkContainer(baseUrl: String, sessionStore: SessionStore) {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    private val converterFactory = json.asConverterFactory("application/json".toMediaType())
    private val publicRetrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(OkHttpClient())
        .addConverterFactory(converterFactory)
        .build()
    private val authApi = publicRetrofit.create(AuthApi::class.java)
    private val session = SessionManager(sessionStore)
    val authRepository = AuthRepository(authApi, session)
    private val tokenAuthenticator = RefreshTokenAuthenticator(authApi, session, authRepository)
    private val authorizedClient = OkHttpClient.Builder()
        .addInterceptor(AccessTokenInterceptor(session))
        .authenticator(tokenAuthenticator)
        .build()
    val phoneCarApi: PhoneCarApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(authorizedClient)
        .addConverterFactory(converterFactory)
        .build()
        .create(PhoneCarApi::class.java)
}

private class AccessTokenInterceptor(private val session: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = session.accessToken()
        val request = if (token == null) chain.request() else chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}

private class RefreshTokenAuthenticator(
    private val api: AuthApi,
    private val session: SessionManager,
    private val authRepository: AuthRepository,
) : Authenticator {
    private val refreshLock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        synchronized(refreshLock) {
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            session.accessToken()?.takeIf { it != requestToken }?.let { current ->
                return response.request.newBuilder().header("Authorization", "Bearer $current").build()
            }
            val stored = session.storedSession() ?: return null
            val refreshed = runCatching {
                runBlocking { api.refresh(RefreshRequest(stored.refreshToken)) }
            }.getOrElse {
                authRepository.markSignedOut()
                return null
            }
            session.update(refreshed)
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${refreshed.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
