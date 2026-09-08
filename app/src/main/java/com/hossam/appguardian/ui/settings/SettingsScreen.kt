package com.hossam.appguardian.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hossam.appguardian.R
import com.hossam.appguardian.ui.components.PinPromptDialog
import com.hossam.appguardian.ui.components.SetPinDialog
import com.hossam.appguardian.utils.PermissionUtils
import kotlinx.coroutines.launch

private enum class PendingPinAction { DISABLE_BLOCKING, CHANGE_PIN, REMOVE_PIN }

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val accessibilityEnabled by viewModel.accessibilityEnabled.collectAsStateWithLifecycle()
    val ignoringBatteryOptimizations by viewModel.ignoringBatteryOptimizations.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshSystemStatus()
    }

    var pendingPinAction by remember { mutableStateOf<PendingPinAction?>(null) }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    val incorrectPinMessage = stringResource(R.string.error_pin_incorrect)

    fun onVerifiedPin(action: PendingPinAction) {
        when (action) {
            PendingPinAction.DISABLE_BLOCKING -> viewModel.setBlockingEnabled(false)
            PendingPinAction.CHANGE_PIN -> showSetPinDialog = true
            PendingPinAction.REMOVE_PIN -> viewModel.clearPin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)

        // Enable blocking
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_enable_blocking), style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(R.string.settings_enable_blocking_desc),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.isBlockingEnabled,
                    onCheckedChange = { turningOn ->
                        if (turningOn) {
                            viewModel.setBlockingEnabled(true)
                        } else if (settings.isPinSet) {
                            pendingPinAction = PendingPinAction.DISABLE_BLOCKING
                            pinError = null
                        } else {
                            viewModel.setBlockingEnabled(false)
                        }
                    }
                )
            }
        }

        // Protection / PIN
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.settings_protection_section), style = MaterialTheme.typography.titleLarge)
                if (!settings.isPinSet) {
                    Button(onClick = { showSetPinDialog = true }) {
                        Text(stringResource(R.string.settings_set_pin))
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            pendingPinAction = PendingPinAction.CHANGE_PIN
                            pinError = null
                        }) { Text(stringResource(R.string.settings_change_pin)) }

                        TextButton(onClick = {
                            pendingPinAction = PendingPinAction.REMOVE_PIN
                            pinError = null
                        }) { Text(stringResource(R.string.settings_remove_pin)) }
                    }
                }
                Text(
                    stringResource(R.string.settings_pin_forgot_note),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Battery optimization
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.settings_battery_section), style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.settings_ignore_battery_optimization), modifier = Modifier.weight(1f))
                    if (!ignoringBatteryOptimizations) {
                        Button(onClick = {
                            context.startActivity(PermissionUtils.requestIgnoreBatteryOptimizationsIntent(context))
                        }) { Text(stringResource(R.string.action_confirm)) }
                    }
                }
                Text(
                    stringResource(R.string.settings_battery_note),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Language
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.settings_language_section), style = MaterialTheme.typography.titleLarge)
                LanguageOption(
                    label = stringResource(R.string.settings_language_system),
                    selected = settings.languageTag == null,
                    onSelect = {
                        viewModel.setLanguageTag(null) { (context as? Activity)?.recreate() }
                    }
                )
                LanguageOption(
                    label = stringResource(R.string.settings_language_arabic),
                    selected = settings.languageTag == "ar",
                    onSelect = {
                        viewModel.setLanguageTag("ar") { (context as? Activity)?.recreate() }
                    }
                )
                LanguageOption(
                    label = stringResource(R.string.settings_language_english),
                    selected = settings.languageTag == "en",
                    onSelect = {
                        viewModel.setLanguageTag("en") { (context as? Activity)?.recreate() }
                    }
                )
            }
        }

        // Accessibility service
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.settings_accessibility_section), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.settings_accessibility_status_label) + ": " +
                        stringResource(
                            if (accessibilityEnabled) R.string.settings_accessibility_enabled
                            else R.string.settings_accessibility_disabled
                        )
                )
                Text(
                    stringResource(R.string.settings_accessibility_instructions),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!accessibilityEnabled) {
                    Button(onClick = { context.startActivity(PermissionUtils.accessibilitySettingsIntent()) }) {
                        Text(stringResource(R.string.home_open_accessibility_settings))
                    }
                }
            }
        }

        // About
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.settings_about_section), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.settings_version_label) + ": 1.0.0")
            }
        }
    }

    val action = pendingPinAction
    if (action != null) {
        PinPromptDialog(
            title = stringResource(R.string.pin_current_prompt),
            errorMessage = pinError,
            onDismiss = { pendingPinAction = null },
            onConfirm = { pin ->
                scope.launch {
                    if (viewModel.verifyPin(pin)) {
                        pendingPinAction = null
                        onVerifiedPin(action)
                    } else {
                        pinError = incorrectPinMessage
                    }
                }
            }
        )
    }

    if (showSetPinDialog) {
        SetPinDialog(
            onDismiss = { showSetPinDialog = false },
            onComplete = { newPin ->
                viewModel.setPin(newPin)
                showSetPinDialog = false
            }
        )
    }
}

@Composable
private fun LanguageOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label)
    }
}
