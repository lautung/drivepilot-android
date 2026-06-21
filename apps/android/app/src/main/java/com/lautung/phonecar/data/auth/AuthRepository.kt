package com.lautung.phonecar.data.auth

import com.lautung.phonecar.data.remote.AuthApi
import com.lautung.phonecar.data.remote.AuthResponse
import com.lautung.phonecar.data.remote.CredentialsRequest
import com.lautung.phonecar.data.remote.RefreshRequest
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthState {
    data object Restoring : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val userId: String, val username: String, val role: String) : AuthState
    data class Error(val message: String) : AuthState
}

class SessionManager(private val store: SessionStore) {
    private val accessToken = AtomicReference<String?>(null)
    fun accessToken(): String? = accessToken.get()
    fun storedSession(): StoredSession? = store.read()
    fun update(response: AuthResponse) {
        accessToken.set(response.accessToken)
        store.write(StoredSession(response.user.id, response.user.username, response.user.role, response.refreshToken))
    }
    fun clear() { accessToken.set(null); store.clear() }
}

class AuthRepository(
    private val api: AuthApi,
    private val session: SessionManager,
) {
    private val mutableState = MutableStateFlow<AuthState>(AuthState.Restoring)
    val state: StateFlow<AuthState> = mutableState.asStateFlow()

    suspend fun restore() {
        val stored = session.storedSession()
        if (stored == null) {
            mutableState.value = AuthState.SignedOut
            return
        }
        runCatching { api.refresh(RefreshRequest(stored.refreshToken)) }
            .onSuccess(::accept)
            .onFailure {
                session.clear()
                mutableState.value = AuthState.SignedOut
            }
    }

    suspend fun login(username: String, password: String) = authenticate {
        api.login(CredentialsRequest(username.trim(), password))
    }

    suspend fun register(username: String, password: String) = authenticate {
        api.register(CredentialsRequest(username.trim(), password))
    }

    suspend fun logout() {
        session.storedSession()?.let { stored ->
            runCatching { api.logout(RefreshRequest(stored.refreshToken)) }
        }
        session.clear()
        mutableState.value = AuthState.SignedOut
    }

    fun markSignedOut() {
        session.clear()
        mutableState.value = AuthState.SignedOut
    }

    private suspend fun authenticate(call: suspend () -> AuthResponse) {
        mutableState.value = AuthState.Restoring
        runCatching { call() }
            .onSuccess(::accept)
            .onFailure { mutableState.value = AuthState.Error("登录失败，请检查账号、密码和网络后重试") }
    }

    private fun accept(response: AuthResponse) {
        session.update(response)
        mutableState.value = AuthState.SignedIn(response.user.id, response.user.username, response.user.role)
    }
}
