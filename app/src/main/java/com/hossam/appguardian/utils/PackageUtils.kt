package com.hossam.appguardian.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.hossam.appguardian.model.AppInfo

/**
 * All PackageManager access goes through here so the `<queries>` justification lives in one
 * place: we only ever resolve MAIN/LAUNCHER activities (see AndroidManifest.xml), which is
 * enough to see every user-facing app -- we deliberately never request QUERY_ALL_PACKAGES.
 */
object PackageUtils {

    /** Every app the user could actually open from a launcher, minus App Guardian itself. */
    fun getLaunchableApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfos = pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)

        return resolveInfos
            .asSequence()
            .map { it.activityInfo.applicationInfo }
            .filter { it.packageName != Constants.OWN_PACKAGE_NAME }
            .distinctBy { it.packageName }
            .map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = appInfo.loadLabel(pm).toString(),
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .sortedBy { it.appName.lowercase() }
            .toList()
    }

    /** Best-effort current label for a package, e.g. for showing on the block screen. */
    fun getAppLabel(context: Context, packageName: String): String? = try {
        val pm = context.packageManager
        val appInfo = pm.getApplicationInfo(packageName, 0)
        appInfo.loadLabel(pm).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    fun isAppInstalled(context: Context, packageName: String): Boolean = try {
        context.packageManager.getApplicationInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    /**
     * The device's current default home-screen launcher package. Always excluded from blocking
     * so the app can never lock the user out of their own home screen.
     */
    fun getDefaultLauncherPackage(context: Context): String? {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = context.packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName
    }
}
