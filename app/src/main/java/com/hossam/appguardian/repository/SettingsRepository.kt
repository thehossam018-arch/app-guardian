package com.hossam.appguardian.repository

import android.content.Context
import com.hossam.appguardian.data.datastore.AppSettingsState
import com.hossam.appguardian.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow

class SettingsRepository(context: Context) {

    private val dataStore = SettingsDataStore(context.applicationContext)

    val settings: Flow<AppSettingsState> = dataStore.settingsFlow

    suspend fun setBlockingEnabled(enabled: Boolean) = dataStore.setBlockingEnabled(enabled)

    suspend fun setLanguageTag(tag: String?) = dataStore.setLanguageTag(tag)

    suspend fun setPin(pin: String) = dataStore.setPin(pin)

    suspend fun clearPin() = dataStore.clearPin()

    suspend fun verifyPin(candidate: String): Boolean = dataStore.verifyPin(candidate)
}
