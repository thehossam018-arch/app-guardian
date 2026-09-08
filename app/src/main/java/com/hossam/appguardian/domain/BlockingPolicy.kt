package com.hossam.appguardian.domain

import com.hossam.appguardian.utils.Constants

/**
 * The single source of truth for "should this foreground package be blocked right now". Kept as
 * plain, framework-free logic on purpose: the accessibility service is the only caller today, but
 * every rule that decides an outcome the user cares about lives here, not scattered across
 * event-handling code.
 */
object BlockingPolicy {

    // Never blockable, no matter what the user adds — protects against accidentally locking
    // yourself out of your own home screen or App Guardian itself.
    private val ALWAYS_ALLOWED = setOf(
        Constants.OWN_PACKAGE_NAME,
        "com.android.systemui"
    )

    fun shouldBlock(
        foregroundPackage: String,
        blockedPackages: Set<String>,
        blockingEnabled: Boolean,
        launcherPackage: String?
    ): Boolean {
        if (!blockingEnabled) return false
        if (foregroundPackage in ALWAYS_ALLOWED) return false
        if (launcherPackage != null && foregroundPackage == launcherPackage) return false
        return foregroundPackage in blockedPackages
    }
}
