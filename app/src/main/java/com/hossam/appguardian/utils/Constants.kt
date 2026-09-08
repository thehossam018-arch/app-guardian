package com.hossam.appguardian.utils

object Constants {
    const val OWN_PACKAGE_NAME = "com.hossam.appguardian"
    const val SETTINGS_PACKAGE_NAME = "com.android.settings"

    const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
    const val EXTRA_BLOCKED_APP_NAME = "extra_blocked_app_name"
    const val EXTRA_BLOCKED_REASON = "extra_blocked_reason"

    const val BLOCKED_APPS_DATASTORE_NAME = "blocked_apps"
    const val SETTINGS_DATASTORE_NAME = "app_settings"

    const val PIN_MIN_LENGTH = 4
    const val PIN_MAX_LENGTH = 6
    const val PBKDF2_ITERATIONS = 120_000
    const val PBKDF2_KEY_LENGTH_BITS = 256
    const val SALT_LENGTH_BYTES = 16
}
