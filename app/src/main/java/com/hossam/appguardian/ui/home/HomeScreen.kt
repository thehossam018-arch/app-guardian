package com.hossam.appguardian.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hossam.appguardian.R
import com.hossam.appguardian.ui.navigation.Screen
import com.hossam.appguardian.utils.PermissionUtils

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshAccessibilityStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.home_blocked_count, uiState.blockedCount),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.home_service_status_label), style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.accessibilityEnabled) Color(0xFF2E7D32) else Color(0xFFB3261E)
                            )
                    )
                    Text(
                        stringResource(
                            if (uiState.accessibilityEnabled) R.string.home_service_active
                            else R.string.home_service_inactive
                        )
                    )
                }
                if (!uiState.accessibilityEnabled) {
                    Button(onClick = { context.startActivity(PermissionUtils.accessibilitySettingsIntent()) }) {
                        Text(stringResource(R.string.home_open_accessibility_settings))
                    }
                }
            }
        }

        Button(
            onClick = { onNavigate(Screen.BlockedApps.route) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.home_manage_blocked_apps)) }

        Button(
            onClick = { onNavigate(Screen.AddApp.route) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.home_add_app)) }

        OutlinedButton(
            onClick = { onNavigate(Screen.Settings.route) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.home_protection_settings)) }
    }
}
