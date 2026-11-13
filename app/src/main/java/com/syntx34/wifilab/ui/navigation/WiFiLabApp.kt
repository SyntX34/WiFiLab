package com.syntx34.wifilab.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.syntx34.wifilab.ui.attack.AttackAnatomyScreen
import com.syntx34.wifilab.ui.auditor.NetworkAuditorScreen
import com.syntx34.wifilab.ui.dashboard.DashboardScreen
import com.syntx34.wifilab.ui.lab.SecurityLabScreen
import com.syntx34.wifilab.ui.learning.LearningCenterScreen
import com.syntx34.wifilab.ui.packet.PacketAnalyzerScreen
import com.syntx34.wifilab.ui.password.PasswordLabScreen
import com.syntx34.wifilab.ui.scanner.WifiScannerScreen
import com.syntx34.wifilab.ui.settings.SettingsScreen
import com.syntx34.wifilab.ui.theme.BorderColor
import com.syntx34.wifilab.ui.theme.Cyan400
import com.syntx34.wifilab.ui.theme.Navy900
import com.syntx34.wifilab.ui.theme.TextMuted
import com.syntx34.wifilab.ui.theme.TextPrimary
import com.syntx34.wifilab.ui.theme.TextSecondary

@Composable
fun WiFiLabApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val topLevel = isTopLevelRoute(route)
    val currentDestination = backStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = titleForRoute(route),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (route == Routes.Dashboard) {
                            Text(
                                text = "LOCAL RESEARCH CONSOLE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Cyan400,
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (!topLevel) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Navigate back",
                                tint = TextPrimary,
                            )
                        }
                    }
                },
                actions = {
                    if (route != Routes.Settings) {
                        IconButton(
                            onClick = {
                                if (route != Routes.Settings) {
                                    navController.navigate(Routes.Settings) {
                                        launchSingleTop = true
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Open settings",
                                tint = TextSecondary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Navy900.copy(alpha = 0.95f),
                ),
            )
        },
        bottomBar = {
            if (topLevel) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Navy900.copy(alpha = 0.98f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BorderColor.copy(alpha = 0.7f)),
                    )
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                    ) {
                        topLevelDestinations.forEach { destination ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == destination.route
                            } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) {
                                            destination.selectedIcon
                                        } else {
                                            destination.icon
                                        },
                                        contentDescription = destination.label,
                                        modifier = Modifier.size(22.dp),
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.label,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Cyan400,
                                    selectedTextColor = Cyan400,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                    indicatorColor = Cyan400.copy(alpha = 0.1f),
                                ),
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.Dashboard,
                modifier = Modifier
                    .widthIn(max = 900.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
            ) {
            composable(Routes.Dashboard) {
                DashboardScreen(
                    onOpenScanner = {
                        navController.navigate(Routes.Scanner) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenAuditor = {
                        navController.navigate(Routes.Auditor) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenSecurityLab = {
                        navController.navigate(Routes.SecurityLab) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenPacketAnalyzer = {
                        navController.navigate(Routes.PacketAnalyzer) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Routes.Scanner) {
                WifiScannerScreen()
            }
            composable(Routes.Auditor) {
                NetworkAuditorScreen()
            }
            composable(Routes.SecurityLab) {
                SecurityLabScreen(
                    onOpenPacketAnalyzer = {
                        navController.navigate(Routes.PacketAnalyzer) {
                            launchSingleTop = true
                        }
                    },
                    onOpenPasswordLab = {
                        navController.navigate(Routes.PasswordLab) {
                            launchSingleTop = true
                        }
                    },
                    onOpenAttackAnatomy = {
                        navController.navigate(Routes.AttackAnatomy) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Routes.LearningCenter) {
                LearningCenterScreen()
            }
            composable(Routes.PacketAnalyzer) {
                PacketAnalyzerScreen()
            }
            composable(Routes.PasswordLab) {
                PasswordLabScreen()
            }
            composable(Routes.AttackAnatomy) {
                AttackAnatomyScreen()
            }
            composable(Routes.Settings) {
                SettingsScreen()
            }
            }
        }
    }
}

