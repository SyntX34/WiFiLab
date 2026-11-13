package com.syntx34.wifilab.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val Dashboard = "dashboard"
    const val Scanner = "scanner"
    const val Auditor = "auditor"
    const val SecurityLab = "security_lab"
    const val PacketAnalyzer = "packet_analyzer"
    const val PasswordLab = "password_lab"
    const val AttackAnatomy = "attack_anatomy"
    const val LearningCenter = "learning_center"
    const val Settings = "settings"
}

data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

val topLevelDestinations = listOf(
    TopLevelDestination(
        route = Routes.Dashboard,
        label = "Dashboard",
        icon = Icons.Outlined.Dashboard,
        selectedIcon = Icons.Filled.Dashboard,
    ),
    TopLevelDestination(
        route = Routes.Scanner,
        label = "Scanner",
        icon = Icons.Outlined.Wifi,
        selectedIcon = Icons.Filled.Wifi,
    ),
    TopLevelDestination(
        route = Routes.Auditor,
        label = "Auditor",
        icon = Icons.Outlined.Router,
        selectedIcon = Icons.Filled.Router,
    ),
    TopLevelDestination(
        route = Routes.SecurityLab,
        label = "Lab",
        icon = Icons.Outlined.Science,
        selectedIcon = Icons.Filled.Science,
    ),
    TopLevelDestination(
        route = Routes.LearningCenter,
        label = "Learn",
        icon = Icons.Outlined.MenuBook,
        selectedIcon = Icons.Filled.MenuBook,
    ),
)

fun titleForRoute(route: String?): String = when (route) {
    Routes.Dashboard -> "WiFiLab"
    Routes.Scanner -> "Wi Fi Scanner"
    Routes.Auditor -> "Network Auditor"
    Routes.SecurityLab -> "Security Lab"
    Routes.PacketAnalyzer -> "Packet Analyzer"
    Routes.PasswordLab -> "Password Lab"
    Routes.AttackAnatomy -> "Attack Anatomy"
    Routes.LearningCenter -> "Learning Center"
    Routes.Settings -> "Settings"
    else -> "WiFiLab"
}

fun isTopLevelRoute(route: String?): Boolean =
    topLevelDestinations.any { it.route == route }
