package com.hossam.appguardian.utils

import android.content.Context
import android.content.res.Configuration
import com.hossam.appguardian.data.datastore.SettingsDataStore
import java.util.Locale

/**
 * Manual per-app language override, built only from standard platform APIs (Configuration /
 * createConfigurationContext), so it works uniformly all the way down to minSdk 26 without an
 * extra theming/compat library. [tag] is a language tag like "ar" or "en"; null means "follow the
 * device's own language" (we simply return the original context unchanged).
 */
object LocaleUtils {

    fun wrapWithStoredLocale(base: Context): Context {
        val tag = SettingsDataStore.readLanguageTagBlocking(base) ?: return base
        return wrapWithLocale(base, tag)
    }

    fun wrapWithLocale(base: Context, tag: String): Context {
        val locale = Locale(tag)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }
}
