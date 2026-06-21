package com.lautung.phonecar

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.lautung.phonecar.data.local.PreferencesDemoStateStore
import com.lautung.phonecar.data.auth.SecureSessionStore
import com.lautung.phonecar.data.remote.NetworkContainer
import com.lautung.phonecar.ui.PhoneCarViewModelFactory

private val Context.phoneCarDataStore by preferencesDataStore(name = "phonecar_demo_state")

class AppContainer(context: Context) {
    private val store = PreferencesDemoStateStore(context.phoneCarDataStore)
    private val network = NetworkContainer(BuildConfig.API_BASE_URL, SecureSessionStore(context))
    val viewModelFactory = PhoneCarViewModelFactory(store, network.authRepository, network.phoneCarApi)
}
