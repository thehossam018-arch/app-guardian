package com.hossam.appguardian.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hossam.appguardian.R
import com.hossam.appguardian.ui.addapp.AddAppScreen
import com.hossam.appguardian.ui.blockedapps.BlockedAppsScreen
import com.hossam.appguardian.ui.home.HomeScreen
import com.hossam.appguardian.ui.settings.SettingsScreen

sealed class Screen(val route: String, val titleRes: Int, val icon: ImageVector) {
    data object Home : Screen("home", R.string.nav_home, Icons.Filled.Home)
    data object BlockedApps : Screen("blocked_apps", R.string.nav_blocked_apps, Icons.Filled.Block)
    data object AddApp : Screen("add_app", R.string.nav_add_app, Icons.Filled.Add)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings)
}

val bottomNavItems = listOf(Screen.Home, Screen.BlockedApps, Screen.AddApp, Screen.Settings)

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) {
            HomeScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable(Screen.BlockedApps.route) {
            BlockedAppsScreen(onAddAppClick = { navController.navigate(Screen.AddApp.route) })
        }
        composable(Screen.AddApp.route) {
            AddAppScreen(onDone = { navController.navigate(Screen.BlockedApps.route) })
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
