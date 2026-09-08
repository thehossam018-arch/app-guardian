package com.hossam.appguardian.model

import kotlinx.serialization.Serializable

/**
 * A single entry in the persisted block list. The [packageName] is the only real identifier —
 * everything else is just cached display information so the "Blocked Apps" screen can still show
 * something meaningful for an app that isn't currently installed.
 */
@Serializable
data class BlockedApp(
    val packageName: String,
    val appName: String,
    val addedAtEpochMillis: Long,
    val reason: String? = null
)
