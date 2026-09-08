package com.hossam.appguardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hossam.appguardian.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android

@Composable
fun AppListItem(
    appName: String,
    packageName: String,
    icon: androidx.compose.ui.graphics.ImageBitmap?,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                androidx.compose.foundation.Image(
                    painter = BitmapPainter(icon),
                    contentDescription = stringResource(R.string.cd_app_icon),
                    modifier = Modifier.size(36.dp)
                )
            } else {
                Icon(Icons.Filled.Android, contentDescription = null)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(appName, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle ?: packageName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        trailing?.invoke()
    }
}

@Composable
fun SelectableAppListItem(
    appName: String,
    packageName: String,
    icon: androidx.compose.ui.graphics.ImageBitmap?,
    selected: Boolean,
    onToggle: () -> Unit,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    AppListItem(
        appName = appName,
        packageName = packageName,
        icon = icon,
        subtitle = subtitle,
        onClick = onToggle,
        trailing = { Checkbox(checked = selected, onCheckedChange = { onToggle() }) },
        modifier = modifier
    )
}
