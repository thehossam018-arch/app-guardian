package com.hossam.appguardian.repository

import android.content.Context
import com.hossam.appguardian.model.AppInfo
import com.hossam.appguardian.model.BrowserPreset
import com.hossam.appguardian.utils.BrowserPresets
import com.hossam.appguardian.utils.PackageUtils

class InstalledAppsRepository(private val context: Context) {

    fun getLaunchableApps(): List<AppInfo> = PackageUtils.getLaunchableApps(context.applicationContext)

    fun getAppLabel(packageName: String): String? =
        PackageUtils.getAppLabel(context.applicationContext, packageName)

    fun isInstalled(packageName: String): Boolean =
        PackageUtils.isAppInstalled(context.applicationContext, packageName)

    fun getBrowserPresets(): List<BrowserPreset> = BrowserPresets.ALL.map { entry ->
        BrowserPreset(
            packageName = entry.packageName,
            displayName = entry.displayName,
            isInstalled = isInstalled(entry.packageName)
        )
    }
}
