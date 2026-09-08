package com.hossam.appguardian.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hossam.appguardian.data.datastore.AppSettingsState
import com.hossam.appguardian.repository.SettingsRepository
import com.hossam.appguardian.utils.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    val settings: StateFlow<AppSettingsState> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettingsState())

    // Polled on resume, same reasoning as HomeViewModel: no system change-callback exists.
    private val _accessibilityEnabled = MutableStateFlow(
        PermissionUtils.isAccessibilityServiceEnabled(application)
    )
    val accessibilityEnabled: StateFlow<Boolean> = _accessibilityEnabled

    private val _ignoringBatteryOptimizations = MutableStateFlow(
        PermissionUtils.isIgnoringBatteryOptimizations(application)
    )
    val ignoringBatteryOptimizations: StateFlow<Boolean> = _ignoringBatteryOptimizations

    fun refreshSystemStatus() {
        _accessibilityEnabled.value = PermissionUtils.isAccessibilityServiceEnabled(getApplication())
        _ignoringBatteryOptimizations.value = PermissionUtils.isIgnoringBatteryOptimizations(getApplication())
    }

    suspend fun verifyPin(pin: String): Boolean = settingsRepository.verifyPin(pin)

    fun setBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setBlockingEnabled(enabled) }
    }

    fun setPin(pin: String) {
        viewModelScope.launch { settingsRepository.setPin(pin) }
    }

    fun clearPin() {
        viewModelScope.launch { settingsRepository.clearPin() }
    }

    fun setLanguageTag(tag: String?, afterSaved: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setLanguageTag(tag)
            afterSaved()
        }
    }
}
