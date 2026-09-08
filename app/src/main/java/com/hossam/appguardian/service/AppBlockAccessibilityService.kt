package com.hossam.appguardian.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.hossam.appguardian.R
import com.hossam.appguardian.domain.BlockingPolicy
import com.hossam.appguardian.model.BlockedApp
import com.hossam.appguardian.repository.BlockedAppsRepository
import com.hossam.appguardian.repository.SettingsRepository
import com.hossam.appguardian.ui.blockedscreen.BlockedActivity
import com.hossam.appguardian.utils.Constants
import com.hossam.appguardian.utils.PackageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Only ever looks at TWO things: which package just came to the foreground, and -- solely on the
 * system Settings app, solely to compare against this app's own label -- the visible text of the
 * current screen, so it can show a one-time local warning before an uninstall. It never inspects
 * the content of any *blocked* app, never persists anything it observes, and never sends anything
 * off the device. See accessibility_service_config.xml for the matching event-type restriction.
 */
class AppBlockAccessibilityService : AccessibilityService() {

    private lateinit var scope: CoroutineScope
    private lateinit var blockedAppsRepository: BlockedAppsRepository
    private lateinit var settingsRepository: SettingsRepository

    // Keyed by package name so the hot path (policy check) and the occasional need for a
    // display name / reason (when actually launching the block screen) share one source.
    @Volatile private var blockedAppsCache: Map<String, BlockedApp> = emptyMap()
    @Volatile private var blockingEnabled: Boolean = true
    private var launcherPackage: String? = null

    // Debounce so the uninstall-page warning fires once per visit to Settings, not on every
    // window-content refresh while the user stays on that screen.
    private var hasWarnedThisSettingsVisit = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        blockedAppsRepository = BlockedAppsRepository(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)
        launcherPackage = PackageUtils.getDefaultLauncherPackage(applicationContext)

        blockedAppsRepository.blockedApps
            .onEach { list -> blockedAppsCache = list.associateBy { it.packageName } }
            .launchIn(scope)

        settingsRepository.settings
            .onEach { blockingEnabled = it.isBlockingEnabled }
            .launchIn(scope)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val foregroundPackage = event.packageName?.toString() ?: return

        if (foregroundPackage == Constants.SETTINGS_PACKAGE_NAME) {
            maybeWarnAboutUninstallScreen()
            return
        } else {
            hasWarnedThisSettingsVisit = false
        }

        val blocked = BlockingPolicy.shouldBlock(
            foregroundPackage = foregroundPackage,
            blockedPackages = blockedAppsCache.keys,
            blockingEnabled = blockingEnabled,
            launcherPackage = launcherPackage
        )

        if (blocked) {
            launchBlockedScreen(foregroundPackage)
        }
    }

    private fun maybeWarnAboutUninstallScreen() {
        if (hasWarnedThisSettingsVisit) return
        val ownLabel = getString(R.string.app_name)
        val root = rootInActiveWindow ?: return
        val matches = root.findAccessibilityNodeInfosByText(ownLabel)
        if (!matches.isNullOrEmpty()) {
            hasWarnedThisSettingsVisit = true
            Toast.makeText(applicationContext, R.string.uninstall_page_warning, Toast.LENGTH_LONG).show()
        }
    }

    private fun launchBlockedScreen(blockedPackage: String) {
        val cached = blockedAppsCache[blockedPackage]
        val appName = PackageUtils.getAppLabel(applicationContext, blockedPackage)
            ?: cached?.appName
            ?: blockedPackage

        val intent = Intent(applicationContext, BlockedActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(Constants.EXTRA_BLOCKED_PACKAGE, blockedPackage)
            putExtra(Constants.EXTRA_BLOCKED_APP_NAME, appName)
            putExtra(Constants.EXTRA_BLOCKED_REASON, cached?.reason)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // Required override. No held state to tear down here.
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::scope.isInitialized) scope.cancel()
    }
}
