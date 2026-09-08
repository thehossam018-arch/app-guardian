package com.hossam.appguardian.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap

/**
 * Icons are intentionally not cached in our own models — they're loaded straight from
 * PackageManager when a row is actually displayed, and simply return null (caller shows a
 * placeholder) if the app is no longer installed.
 */
@Composable
fun rememberAppIcon(packageName: String): ImageBitmap? {
    val context = LocalContext.current
    val state = produceState<ImageBitmap?>(initialValue = null, packageName) {
        value = runCatching {
            context.packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()
        }.getOrNull()
    }
    return state.value
}
