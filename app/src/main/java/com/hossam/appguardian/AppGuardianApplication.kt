package com.hossam.appguardian

import android.app.Application
import android.content.Context
import com.hossam.appguardian.utils.LocaleUtils

/**
 * Wraps the process-wide base Context with the user's stored language choice (see LocaleUtils)
 * so components that use applicationContext directly -- most importantly the accessibility
 * service's uninstall-warning Toast, which has no Activity of its own -- also honor it, not just
 * MainActivity/BlockedActivity's own windows.
 */
class AppGuardianApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtils.wrapWithStoredLocale(base))
    }
}
