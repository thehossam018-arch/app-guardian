package com.hossam.appguardian.model

/**
 * A lightweight, non-persisted description of an app currently installed on the device.
 * Used only in-memory to populate the "Add App" picker; icons are loaded on demand from
 * PackageManager by the UI layer rather than being cached here.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false
)

/**
 * One entry in the curated "suggested browsers" preset (see [com.hossam.appguardian.utils.BrowserPresets]).
 * [isInstalled] is resolved at query time against the device's actual installed apps.
 */
data class BrowserPreset(
    val packageName: String,
    val displayName: String,
    val isInstalled: Boolean
)
