package com.hossam.appguardian.repository

import android.content.Context
import com.hossam.appguardian.data.datastore.BlockedAppsDataStore
import com.hossam.appguardian.model.AppInfo
import com.hossam.appguardian.model.BlockedApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BlockedAppsRepository(context: Context) {

    private val dataStore = BlockedAppsDataStore(context.applicationContext)

    val blockedApps: Flow<List<BlockedApp>> = dataStore.blockedAppsFlow

    /** Fast-lookup form used by the accessibility service's hot path. */
    val blockedPackageNames: Flow<Set<String>> = blockedApps.map { list ->
        list.map { it.packageName }.toSet()
    }

    suspend fun addApps(apps: List<AppInfo>, reason: String? = null) {
        val now = System.currentTimeMillis()
        val entries = apps.map { app ->
            BlockedApp(
                packageName = app.packageName,
                appName = app.appName,
                addedAtEpochMillis = now,
                reason = reason
            )
        }
        dataStore.addApps(entries)
    }

    suspend fun addPackages(packages: List<Pair<String, String>>, reason: String? = null) {
        val now = System.currentTimeMillis()
        val entries = packages.map { (packageName, appName) ->
            BlockedApp(packageName, appName, now, reason)
        }
        dataStore.addApps(entries)
    }

    suspend fun removeApp(packageName: String) {
        dataStore.removeApp(packageName)
    }
}
