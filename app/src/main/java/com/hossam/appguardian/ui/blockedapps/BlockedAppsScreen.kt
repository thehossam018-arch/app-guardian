package com.hossam.appguardian.ui.blockedapps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hossam.appguardian.R
import com.hossam.appguardian.ui.components.AppListItem
import com.hossam.appguardian.ui.components.PinPromptDialog
import com.hossam.appguardian.ui.components.rememberAppIcon
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BlockedAppsScreen(
    onAddAppClick: () -> Unit,
    viewModel: BlockedAppsViewModel = viewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val isPinSet by viewModel.isPinSet.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var pendingRemovalPackage by remember { mutableStateOf<String?>(null) }
    var pinError by remember { mutableStateOf<String?>(null) }
    val incorrectPinMessage = stringResource(R.string.error_pin_incorrect)

    fun requestRemoval(packageName: String) {
        if (isPinSet) {
            pendingRemovalPackage = packageName
            pinError = null
        } else {
            viewModel.removeApp(packageName)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.blocked_apps_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAppClick) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.blocked_apps_empty))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(items, key = { it.app.packageName }) { item ->
                    AppListItem(
                        appName = item.app.appName,
                        packageName = item.app.packageName,
                        icon = rememberAppIcon(item.app.packageName),
                        subtitle = stringResource(
                            if (item.isInstalled) R.string.status_installed else R.string.status_not_installed
                        ),
                        trailing = {
                            IconButton(onClick = { requestRemoval(item.app.packageName) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.cd_delete),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    val target = pendingRemovalPackage
    if (target != null) {
        PinPromptDialog(
            title = stringResource(R.string.pin_enter_title),
            errorMessage = pinError,
            onDismiss = { pendingRemovalPackage = null },
            onConfirm = { pin ->
                scope.launch {
                    if (viewModel.verifyPin(pin)) {
                        viewModel.removeApp(target)
                        pendingRemovalPackage = null
                    } else {
                        pinError = incorrectPinMessage
                    }
                }
            }
        )
    }
}
