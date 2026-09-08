package com.hossam.appguardian.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hossam.appguardian.repository.BlockedAppsRepository
import com.hossam.appguardian.repository.SettingsRepository
import com.hossam.appguardian.utils.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val blockedCount: Int = 0,
    val blockingEnabled: Boolean = true,
    val accessibilityEnabled: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val blockedAppsRepository = BlockedAppsRepository(application)
    private val settingsRepository = SettingsRepository(application)

    // There is no system callback for "accessibility service enabled state changed" — it must be
    // polled, which we do from the screen whenever it resumes (e.g. returning from Settings).
    private val accessibilityEnabled = MutableStateFlow(
        PermissionUtils.isAccessibilityServiceEnabled(application)
    )

    val uiState: StateFlow<HomeUiState> = combine(
        blockedAppsRepository.blockedApps,
        settingsRepository.settings,
        accessibilityEnabled
    ) { blocked, settings, accessibility ->
        HomeUiState(
            blockedCount = blocked.size,
            blockingEnabled = settings.isBlockingEnabled,
            accessibilityEnabled = accessibility
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun refreshAccessibilityStatus() {
        accessibilityEnabled.value = PermissionUtils.isAccessibilityServiceEnabled(getApplication())
    }
}
