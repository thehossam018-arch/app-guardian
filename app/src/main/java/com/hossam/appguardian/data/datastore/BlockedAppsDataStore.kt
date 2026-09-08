package com.hossam.appguardian.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hossam.appguardian.model.BlockedApp
import com.hossam.appguardian.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.blockedAppsDataStoreImpl by preferencesDataStore(
    name = Constants.BLOCKED_APPS_DATASTORE_NAME
)

/**
 * Persists the block list as JSON in Preferences DataStore. DataStore itself is durable local
 * storage (survives process death, app restarts, and device reboots — this is what requirement
 * #7 needs; it is never held only in memory).
 */
class BlockedAppsDataStore(private val context: Context) {

    private object Keys {
        val BLOCKED_APPS_JSON = stringPreferencesKey("blocked_apps_json")
    }

    private val json = Json { ignoreUnknownKeys = true }

    val blockedAppsFlow: Flow<List<BlockedApp>> =
        context.blockedAppsDataStoreImpl.data.map { prefs ->
            val raw = prefs[Keys.BLOCKED_APPS_JSON] ?: return@map emptyList()
            runCatching { json.decodeFromString<List<BlockedApp>>(raw) }.getOrDefault(emptyList())
        }

    suspend fun addApps(newApps: List<BlockedApp>) {
        context.blockedAppsDataStoreImpl.edit { prefs ->
            val current = prefs[Keys.BLOCKED_APPS_JSON]
                ?.let { runCatching { json.decodeFromString<List<BlockedApp>>(it) }.getOrDefault(emptyList()) }
                ?: emptyList()

            val existingPackages = current.map { it.packageName }.toSet()
            val merged = current + newApps.filterNot { it.packageName in existingPackages }
            prefs[Keys.BLOCKED_APPS_JSON] = json.encodeToString(merged)
        }
    }

    suspend fun removeApp(packageName: String) {
        context.blockedAppsDataStoreImpl.edit { prefs ->
            val current = prefs[Keys.BLOCKED_APPS_JSON]
                ?.let { runCatching { json.decodeFromString<List<BlockedApp>>(it) }.getOrDefault(emptyList()) }
                ?: emptyList()
            prefs[Keys.BLOCKED_APPS_JSON] = json.encodeToString(current.filterNot { it.packageName == packageName })
        }
    }
}
