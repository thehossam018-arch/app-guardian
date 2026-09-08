package com.hossam.appguardian.ui.blockedapps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hossam.appguardian.model.BlockedApp
import com.hossam.appguardian.repository.BlockedAppsRepository
import com.hossam.appguardian.repository.InstalledAppsRepository
import com.hossam.appguardian.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BlockedAppUiItem(
    val app: BlockedApp,
    val isInstalled: Boolean
)

class BlockedAppsViewModel(application: Application) : AndroidViewModel(application) {

    private val blockedAppsRepository = BlockedAppsRepository(application)
    private val installedAppsRepository = InstalledAppsRepository(application)
    private val settingsRepository = SettingsRepository(application)

    val items: StateFlow<List<BlockedAppUiItem>> = blockedAppsRepository.blockedApps
        .map { list ->
            list.map { app -> BlockedAppUiItem(app, installedAppsRepository.isInstalled(app.packageName)) }
                .sortedBy { it.app.appName.lowercase() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isPinSet: StateFlow<Boolean> = settingsRepository.settings
        .map { it.isPinSet }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    suspend fun verifyPin(pin: String): Boolean = settingsRepository.verifyPin(pin)

    fun removeApp(packageName: String) {
        viewModelScope.launch { blockedAppsRepository.removeApp(packageName) }
    }
}
