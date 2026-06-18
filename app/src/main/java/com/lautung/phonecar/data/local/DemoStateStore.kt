package com.lautung.phonecar.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.lautung.phonecar.data.model.DemoState
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface DemoStateStore {
    val state: Flow<DemoState>
    suspend fun update(transform: (DemoState) -> DemoState)
}

class InMemoryDemoStateStore(initial: DemoState = DemoState()) : DemoStateStore {
    private val mutableState = MutableStateFlow(initial)
    override val state: Flow<DemoState> = mutableState
    val current: DemoState get() = mutableState.value

    override suspend fun update(transform: (DemoState) -> DemoState) {
        mutableState.value = transform(mutableState.value)
    }
}

class PreferencesDemoStateStore(
    private val dataStore: DataStore<Preferences>,
) : DemoStateStore {
    private val writeMutex = Mutex()

    override val state: Flow<DemoState> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map(Preferences::toDemoState)

    override suspend fun update(transform: (DemoState) -> DemoState) {
        writeMutex.withLock {
            val next = transform(state.first())
            dataStore.edit { preferences ->
                preferences.clear()
                next.writeTo(preferences)
            }
        }
    }
}
