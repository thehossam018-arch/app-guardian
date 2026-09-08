package com.hossam.appguardian.ui.blockedscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hossam.appguardian.R
import com.hossam.appguardian.ui.theme.AppGuardianTheme
import com.hossam.appguardian.ui.theme.GuardRed
import com.hossam.appguardian.utils.Constants
import com.hossam.appguardian.utils.LocaleUtils

/**
 * Launched by AppBlockAccessibilityService on top of any app on the block list. Pressing system
 * back is deliberately intercepted to go Home instead of the default back-stack behavior --
 * otherwise back could reveal the blocked app underneath, which would defeat "very strict"
 * blocking with no bypass.
 */
class BlockedActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleUtils.wrapWithStoredLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appName = intent.getStringExtra(Constants.EXTRA_BLOCKED_APP_NAME)
            ?: intent.getStringExtra(Constants.EXTRA_BLOCKED_PACKAGE)
            ?: ""
        val reason = intent.getStringExtra(Constants.EXTRA_BLOCKED_REASON)

        setContent {
            AppGuardianTheme {
                BlockedScreenContent(
                    appName = appName,
                    reason = reason,
                    onGoHome = { goHome() }
                )
            }
        }
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(homeIntent)
        finish()
    }

    // New blocked-app launches reuse this singleTask instance; make sure each one shows its own
    // app name/reason instead of the first one that ever created the Activity.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val appName = intent.getStringExtra(Constants.EXTRA_BLOCKED_APP_NAME)
            ?: intent.getStringExtra(Constants.EXTRA_BLOCKED_PACKAGE)
            ?: ""
        val reason = intent.getStringExtra(Constants.EXTRA_BLOCKED_REASON)
        setContent {
            AppGuardianTheme {
                BlockedScreenContent(appName = appName, reason = reason, onGoHome = { goHome() })
            }
        }
    }
}

@Composable
private fun BlockedScreenContent(appName: String, reason: String?, onGoHome: () -> Unit) {
    BackHandler { onGoHome() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = GuardRed,
                modifier = Modifier.size(96.dp)
            )

            Column(
                modifier = Modifier.padding(top = 24.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.blocked_screen_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.blocked_screen_app_label) + ": " + appName,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (!reason.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.blocked_screen_reason_label) + ": " + reason,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(onClick = onGoHome) {
                Text(stringResource(R.string.action_go_home))
            }
        }
    }
}
