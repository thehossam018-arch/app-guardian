package com.hossam.appguardian.ui.addapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hossam.appguardian.model.AppInfo
import com.hossam.appguardian.model.BrowserPreset
import com.hossam.appguardian.repository.BlockedAppsRepository
import com.hossam.appguardian.repository.InstalledAppsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddAppViewModel(application: Application) : AndroidViewModel(application) {

    private val blockedAppsRepository = BlockedAppsRepository(application)
    private val installedAppsRepository = InstalledAppsRepository(application)

    private val blockedPackages: StateFlow<Set<String>> = blockedAppsRepository.blockedPackageNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // packageName -> display name, kept together so a not-yet-installed browser preset can still
    // be added with a sensible cached name (PackageManager has nothing to look up for it yet).
    private val _selectedPackages = MutableStateFlow<Map<String, String>>(emptyMap())
    val selectedPackages: StateFlow<Map<String, String>> = _selectedPackages.asStateFlow()
    val selectedCount: StateFlow<Int> = _selectedPackages.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val installedApps: StateFlow<List<AppInfo>> = combine(blockedPackages, _searchQuery) { blocked, query ->
        installedAppsRepository.getLaunchableApps()
            .filter { it.packageName !in blocked }
            .filter { query.isBlank() || it.appName.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val browserPresets: StateFlow<List<BrowserPreset>> = combine(blockedPackages, _searchQuery) { blocked, query ->
        installedAppsRepository.getBrowserPresets()
            .filter { it.packageName !in blocked }
            .filter { query.isBlank() || it.displayName.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSelection(packageName: String, appName: String) {
        _selectedPackages.update { current ->
            if (packageName in current) current - packageName else current + (packageName to appName)
        }
    }

    fun confirmAdd(onDone: () -> Unit) {
        val toAdd = _selectedPackages.value.map { (packageName, appName) -> packageName to appName }
        if (toAdd.isEmpty()) {
            onDone()
            return
        }
        viewModelScope.launch {
            blockedAppsRepository.addPackages(toAdd)
            _selectedPackages.value = emptyMap()
            onDone()
        }
    }
}
