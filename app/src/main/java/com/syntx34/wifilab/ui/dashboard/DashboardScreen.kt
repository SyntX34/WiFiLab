package com.syntx34.wifilab.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntx34.wifilab.core.util.TimeFormatter
import com.syntx34.wifilab.domain.model.ConnectionSnapshot
import com.syntx34.wifilab.domain.model.ConnectionState
import com.syntx34.wifilab.domain.model.ResearchActivity
import com.syntx34.wifilab.domain.model.ResearchActivityType
import com.syntx34.wifilab.domain.model.WifiSecurity
import com.syntx34.wifilab.domain.security.WifiCapabilityParser
import com.syntx34.wifilab.ui.components.EmptyState
import com.syntx34.wifilab.ui.components.GlassCard
import com.syntx34.wifilab.ui.components.MetricTile
import com.syntx34.wifilab.ui.components.SectionHeader
import com.syntx34.wifilab.ui.components.SignalBars
import com.syntx34.wifilab.ui.components.StatusPill
import com.syntx34.wifilab.ui.components.TinyBadge
import com.syntx34.wifilab.ui.permissions.WifiPermissionSupport
import com.syntx34.wifilab.ui.permissions.rememberWifiPermissionGranted
import com.syntx34.wifilab.ui.theme.Blue400
import com.syntx34.wifilab.ui.theme.BorderColor
import com.syntx34.wifilab.ui.theme.Cyan400
import com.syntx34.wifilab.ui.theme.Danger
import com.syntx34.wifilab.ui.theme.Navy700
import com.syntx34.wifilab.ui.theme.Navy800
import com.syntx34.wifilab.ui.theme.Navy900
import com.syntx34.wifilab.ui.theme.Success
import com.syntx34.wifilab.ui.theme.TextMuted
import com.syntx34.wifilab.ui.theme.TextPrimary
import com.syntx34.wifilab.ui.theme.TextSecondary
import com.syntx34.wifilab.ui.theme.Warning

@Composable
fun DashboardScreen(
    onOpenScanner: () -> Unit,
    onOpenAuditor: () -> Unit,
    onOpenSecurityLab: () -> Unit,
    onOpenPacketAnalyzer: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hasWifiPermission = rememberWifiPermissionGranted()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 12.dp,
            bottom = 36.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column {
                Text(
                    text = "WIRELESS OVERVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cyan400,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Your network, in focus",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Discovery, auditing and offline research with a local first design.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        item {
            ConnectionHero(
                connection = state.connection,
                hasWifiPermission = hasWifiPermission,
                onOpenScanner = onOpenScanner,
            )
        }

        item {
            SectionHeader(
                eyebrow = "AUDIT STATUS",
                title = "Local network oversight",
                subtitle = "Findings never leave this device.",
                actionLabel = "Open auditor",
                onAction = onOpenAuditor,
            )
        }

        item {
            AuditStatusCard(
                state = state,
                onOpenAuditor = onOpenAuditor,
            )
        }

        if (!hasWifiPermission) {
            item {
                PermissionHint(
                    onOpenScanner = onOpenScanner,
                    locationRequired = WifiPermissionSupport.requiredPermissions().any {
                        it == android.Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    context = context,
                )
            }
        }

        item {
            SectionHeader(
                eyebrow = "QUICK TOOLS",
                title = "Continue your research",
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardActionCard(
                    icon = Icons.Outlined.Wifi,
                    title = "Wi Fi Scanner",
                    caption = "Survey nearby networks and channel load",
                    accent = Cyan400,
                    onClick = onOpenScanner,
                )
                DashboardActionCard(
                    icon = Icons.Outlined.FolderOpen,
                    title = "Packet Analyzer",
                    caption = "Study EAPOL, DNS and transport metadata offline",
                    accent = Blue400,
                    onClick = onOpenPacketAnalyzer,
                )
                DashboardActionCard(
                    icon = Icons.Outlined.Science,
                    title = "Security Lab",
                    caption = "Protocol lessons, password entropy and attack anatomy",
                    accent = Success,
                    onClick = onOpenSecurityLab,
                )
            }
        }

        item {
            SectionHeader(
                eyebrow = "RECENT ACTIVITY",
                title = "Research trail",
                subtitle = "The latest five local events.",
            )
        }

        item {
            if (state.activities.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.MenuBook,
                    title = "No research activity yet",
                    message = "Run a scan, complete an audit or open the offline labs to build a local history.",
                    actionLabel = "Start with a scan",
                    onAction = onOpenScanner,
                )
            } else {
                GlassCard(contentPadding = 8) {
                    state.activities.forEachIndexed { index, activity ->
                        ActivityRow(activity = activity)
                        if (index != state.activities.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(BorderColor.copy(alpha = 0.6f)),
                            )
                        }
                    }
                }
            }
        }

        item {
            GlassCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Cyan400.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = Cyan400,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Local by design",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "No analytics, accounts or capture uploads. Imported files stay in app private storage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionHero(
    connection: ConnectionSnapshot,
    hasWifiPermission: Boolean,
    onOpenScanner: () -> Unit,
) {
    val connected = connection.state == ConnectionState.CONNECTED
    val security = connection.security
    val securityColor = when (security) {
        WifiSecurity.OPEN, WifiSecurity.WEP -> Danger
        WifiSecurity.UNKNOWN, null -> Warning
        else -> Success
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Navy700, Navy800, Navy900),
                ),
            )
            .border(1.dp, BorderColor.copy(alpha = 0.9f), RoundedCornerShape(26.dp)),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(180.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Cyan400.copy(alpha = 0.13f), Color.Transparent),
                    ),
                ),
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Wifi,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "CURRENT CONNECTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
                StatusPill(
                    text = when {
                        connected && connection.isWifi -> "Wi Fi online"
                        connected -> "Network online"
                        else -> "Offline"
                    },
                    color = if (connected) Success else TextMuted,
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = connection.ssid ?: when {
                    connected && connection.isWifi -> "Network name unavailable"
                    connected -> "Non Wi Fi connection"
                    else -> "No active connection"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TinyBadge(
                    text = security?.label ?: "Security not exposed",
                    color = securityColor,
                )
                if (connection.isMetered == true) {
                    TinyBadge(text = "Metered", color = Warning)
                }
            }
            if (connected) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Navy900.copy(alpha = 0.66f))
                            .border(1.dp, BorderColor.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                    ) {
                        Text(
                            text = "SIGNAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                        Spacer(modifier = Modifier.height(7.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SignalBars(level = connection.signalLevel ?: 0)
                            Text(
                                text = connection.rssi?.let { "$it dBm" } ?: "Unavailable",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                            )
                        }
                    }
                    ConnectionInfoTile(
                        label = "LOCAL IP",
                        value = connection.localIpv4 ?: "Unavailable",
                        modifier = Modifier.weight(1f),
                    )
                    ConnectionInfoTile(
                        label = "GATEWAY",
                        value = connection.gatewayIpv4 ?: "Unavailable",
                        modifier = Modifier.weight(1f),
                    )
                }
                val channel = connection.frequencyMhz
                    ?.takeIf { it > 0 }
                    ?.let(WifiCapabilityParser::frequencyToChannel)
                val band = connection.frequencyMhz
                    ?.takeIf { it > 0 }
                    ?.let(WifiCapabilityParser::frequencyToBand)
                if (channel != null && channel > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Channel $channel",
                            style = MaterialTheme.typography.labelMedium,
                            color = Cyan400,
                        )
                        Text(
                            text = band?.label ?: "Unknown band",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted,
                        )
                        if (connection.linkSpeedMbps != null) {
                            Text(
                                text = "${connection.linkSpeedMbps} Mbps link",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted,
                            )
                        }
                    }
                }
                if (!hasWifiPermission) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Grant nearby Wi Fi access to reveal network identity and security details.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Warning,
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Connect to a network or open the scanner to survey the wireless environment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onOpenScanner,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan400,
                        contentColor = Navy900,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Open scanner", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ConnectionInfoTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Navy900.copy(alpha = 0.66f))
            .border(1.dp, BorderColor.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AuditStatusCard(
    state: DashboardUiState,
    onOpenAuditor: () -> Unit,
) {
    val audit = state.lastAudit
    GlassCard(onClick = onOpenAuditor) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Blue400.copy(alpha = 0.1f))
                    .border(1.dp, Blue400.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Router,
                    contentDescription = null,
                    tint = Blue400,
                    modifier = Modifier.size(21.dp),
                )
            }
            Spacer(modifier = Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (audit == null) "No audits recorded" else "Last audit complete",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (audit == null) {
                        "Authorize a local range and inspect devices you own."
                    } else {
                        "${audit.deviceCount} devices on ${audit.cidr}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
            )
        }
        if (audit != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                MetricTile(
                    label = "Devices",
                    value = audit.deviceCount.toString(),
                    modifier = Modifier.weight(1f),
                    accent = Blue400,
                )
                MetricTile(
                    label = "Unexpected",
                    value = audit.unexpectedCount.toString(),
                    modifier = Modifier.weight(1f),
                    accent = if (audit.unexpectedCount > 0) Warning else Success,
                )
                MetricTile(
                    label = "Last run",
                    value = TimeFormatter.relativeTime(audit.scannedAt),
                    modifier = Modifier.weight(1.2f),
                    accent = Cyan400,
                )
            }
        }
    }
}

@Composable
private fun PermissionHint(
    onOpenScanner: () -> Unit,
    locationRequired: Boolean,
    context: android.content.Context,
) {
    GlassCard(onClick = onOpenScanner) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Wifi,
                contentDescription = null,
                tint = Warning,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Wi Fi permission needed",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Text(
                    text = if (locationRequired) {
                        "Android requires location access for scan results. Open the scanner to grant it."
                    } else {
                        "Nearby Wi Fi access lets Android return network details. Open the scanner to grant it."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
            )
        }
    }
}

@Composable
private fun DashboardActionCard(
    icon: ImageVector,
    title: String,
    caption: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Navy800.copy(alpha = 0.62f))
            .border(1.dp, BorderColor.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.11f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
        )
    }
}

@Composable
private fun ActivityRow(activity: ResearchActivity) {
    val (icon, color) = when (activity.type) {
        ResearchActivityType.WIFI_SCAN -> Icons.Outlined.Wifi to Cyan400
        ResearchActivityType.NETWORK_AUDIT -> Icons.Outlined.Router to Blue400
        ResearchActivityType.PACKET_ANALYSIS -> Icons.Outlined.Analytics to Blue400
        ResearchActivityType.PASSWORD_STUDY -> Icons.Outlined.Lock to Warning
        ResearchActivityType.LEARNING -> Icons.Outlined.MenuBook to Success
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(17.dp),
            )
        }
        Spacer(modifier = Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = activity.detail,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = TimeFormatter.relativeTime(activity.occurredAt),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }
}
