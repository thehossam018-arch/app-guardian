package com.hossam.appguardian.ui.addapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hossam.appguardian.R
import com.hossam.appguardian.ui.components.SelectableAppListItem
import com.hossam.appguardian.ui.components.rememberAppIcon
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppScreen(
    onDone: () -> Unit,
    viewModel: AddAppViewModel = viewModel()
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val browserPresets by viewModel.browserPresets.collectAsStateWithLifecycle()
    val selected by viewModel.selectedPackages.collectAsStateWithLifecycle()
    val selectedCount by viewModel.selectedCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.add_app_title)) }) },
        bottomBar = {
            if (selectedCount > 0) {
                Button(
                    onClick = { viewModel.confirmAdd(onDone) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        stringResource(R.string.action_add_to_block_list) +
                            " (" + stringResource(R.string.add_app_selected_count, selectedCount) + ")"
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::updateQuery,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.cd_search)) },
                label = { Text(stringResource(R.string.add_app_search_hint)) }
            )

            TabRow(selectedTabIndex = tabIndex) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = { Text(stringResource(R.string.tab_installed_apps)) }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = { Text(stringResource(R.string.tab_suggested_browsers)) }
                )
            }

            if (tabIndex == 1) {
                Text(
                    stringResource(R.string.browsers_note),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (tabIndex == 0) {
                    items(installedApps, key = { it.packageName }) { app ->
                        SelectableAppListItem(
                            appName = app.appName,
                            packageName = app.packageName,
                            icon = rememberAppIcon(app.packageName),
                            selected = app.packageName in selected,
                            onToggle = { viewModel.toggleSelection(app.packageName, app.appName) }
                        )
                    }
                } else {
                    items(browserPresets, key = { it.packageName }) { browser ->
                        SelectableAppListItem(
                            appName = browser.displayName,
                            packageName = browser.packageName,
                            icon = rememberAppIcon(browser.packageName),
                            selected = browser.packageName in selected,
                            subtitle = stringResource(
                                if (browser.isInstalled) R.string.status_installed else R.string.status_not_installed
                            ),
                            onToggle = { viewModel.toggleSelection(browser.packageName, browser.displayName) }
                        )
                    }
                }
            }
        }
    }
}
