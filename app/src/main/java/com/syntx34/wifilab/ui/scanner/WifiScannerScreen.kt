package com.syntx34.wifilab.ui.scanner

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntx34.wifilab.core.util.TimeFormatter
import com.syntx34.wifilab.domain.model.SignalBand
import com.syntx34.wifilab.domain.model.SignalQuality
import com.syntx34.wifilab.domain.model.WifiNetwork
import com.syntx34.wifilab.domain.model.WifiSecurity
import com.syntx34.wifilab.ui.components.ChannelDistributionChart
import com.syntx34.wifilab.ui.components.DetailRow
import com.syntx34.wifilab.ui.components.EmptyState
import com.syntx34.wifilab.ui.components.ErrorBanner
import com.syntx34.wifilab.ui.components.GlassCard
import com.syntx34.wifilab.ui.components.InfoBanner
import com.syntx34.wifilab.ui.components.MetricTile
import com.syntx34.wifilab.ui.components.SectionHeader
import com.syntx34.wifilab.ui.components.SignalBars
import com.syntx34.wifilab.ui.components.StatusPill
import com.syntx34.wifilab.ui.permissions.WifiPermissionSupport
import com.syntx34.wifilab.ui.permissions.rememberLocationServicesEnabled
import com.syntx34.wifilab.ui.permissions.rememberWifiPermissionGranted
import com.syntx34.wifilab.ui.permissions.rememberWifiPermissionLauncher
import com.syntx34.wifilab.ui.theme.Blue400
import com.syntx34.wifilab.ui.theme.BorderColor
import com.syntx34.wifilab.ui.theme.Cyan400
import com.syntx34.wifilab.ui.theme.Danger
import com.syntx34.wifilab.ui.theme.Navy800
import com.syntx34.wifilab.ui.theme.Navy900
import com.syntx34.wifilab.ui.theme.Success
import com.syntx34.wifilab.ui.theme.TextMuted
import com.syntx34.wifilab.ui.theme.TextPrimary
import com.syntx34.wifilab.ui.theme.TextSecondary
import com.syntx34.wifilab.ui.theme.Warning

@Composable
fun WifiScannerScreen(
    viewModel: WifiScannerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionGranted = rememberWifiPermissionGranted()
    val locationEnabled = rememberLocationServicesEnabled()
    val needsLocation = WifiPermissionSupport.requiredPermissions()
        .contains(Manifest.permission.ACCESS_FINE_LOCATION)
    var permissionDenied by rememberSaveable { mutableStateOf(false) }

    val requestPermission = rememberWifiPermissionLauncher { granted ->
        permissionDenied = !granted
        if (granted) viewModel.startScan()
    }

    LaunchedEffect(permissionGranted, locationEnabled) {
        if (permissionGranted &&
            (!needsLocation || locationEnabled) &&
            state.lastScanAt == null &&
            !state.isScanning
        ) {
            viewModel.startScan()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(
                    text = "WIRELESS SURVEY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cyan400,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Nearby networks",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A permission aware view of what Android exposes to this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        if (!permissionGranted) {
            item {
                PermissionGate(
                    permissionDenied = permissionDenied,
                    needsLocation = needsLocation,
                    onRequest = requestPermission,
                    onOpenSettings = { WifiPermissionSupport.openAppSettings(context) },
                )
            }
            item {
                InfoBanner(
                    title = "Why Android asks",
                    message = "Scan results can reveal nearby locations, so Android gates Wi Fi scans behind location or nearby Wi Fi permission. WiFiLab only uses results inside the app.",
                    color = Blue400,
                )
            }
            return@LazyColumn
        }

        if (needsLocation && !locationEnabled) {
            item {
                GlassCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(22.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Location services are off",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                            )
                            Text(
                                text = "This Android version requires location services for scan results. WiFiLab does not record or share your location.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                    }
                    TextButton(
                        onClick = { WifiPermissionSupport.openLocationSettings(context) },
                    ) {
                        Text("Open location settings", color = Cyan400)
                    }
                }
            }
        }

        item {
            ScannerControls(
                state = state,
                scanningBlockedByLocation = needsLocation && !locationEnabled,
                onScan = viewModel::startScan,
            )
        }

        if (state.error != null) {
            item {
                ErrorBanner(
                    message = state.error.orEmpty(),
                    onDismiss = viewModel::clearError,
                )
            }
        }

        if (state.notice != null) {
            item {
                ErrorBanner(
                    message = state.notice.orEmpty(),
                    onDismiss = viewModel::clearNotice,
                )
            }
        }

        item {
            ScanSummary(state = state)
        }

        if (state.networks.isNotEmpty()) {
            item {
                SectionHeader(
                    eyebrow = "SPECTRUM",
                    title = "Channel distribution",
                    subtitle = "Tap a channel to filter the visible networks.",
                    actionLabel = if (state.channelFilter != null) "Clear" else null,
                    onAction = if (state.channelFilter != null) {
                        { viewModel.setChannelFilter(null) }
                    } else {
                        null
                    },
                )
            }
            item {
                GlassCard {
                    ChannelDistributionChart(
                        networks = state.networks,
                        selectedChannel = state.channelFilter,
                        onSelectChannel = viewModel::setChannelFilter,
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::updateQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Search SSID, BSSID or security",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = TextMuted,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan400,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = Navy900.copy(alpha = 0.5f),
                    unfocusedContainerColor = Navy900.copy(alpha = 0.5f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Cyan400,
                ),
            )
        }

        item {
            BandFilters(
                selectedBand = state.bandFilter,
                securityFilter = state.securityFilter,
                sort = state.sort,
                onBandChange = viewModel::setBandFilter,
                onSecurityChange = viewModel::setSecurityFilter,
                onSortChange = viewModel::setSort,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${state.visibleNetworks.size} result${if (state.visibleNetworks.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                if (state.lastScanAt != null) {
                    Text(
                        text = "Scanned ${TimeFormatter.relativeTime(state.lastScanAt!!)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }
            }
        }

        if (state.isScanning && state.networks.isEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Cyan400,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Requesting a scan from Android",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        } else if (state.visibleNetworks.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Wifi,
                    title = if (state.networks.isEmpty()) "No scan results yet" else "No matching networks",
                    message = if (state.networks.isEmpty()) {
                        "Start a scan to see nearby access points and their advertised capabilities."
                    } else {
                        "Clear the search or filters to see the full scan result."
                    },
                    actionLabel = if (state.networks.isEmpty()) "Start scan" else "Reset filters",
                    onAction = {
                        if (state.networks.isEmpty()) {
                            viewModel.startScan()
                        } else {
                            viewModel.updateQuery("")
                            viewModel.setBandFilter(null)
                            viewModel.setSecurityFilter(SecurityFilter.ALL)
                            viewModel.setChannelFilter(null)
                        }
                    },
                )
            }
        }

        items(
            items = state.visibleNetworks,
            key = { network -> "${network.bssid}-${network.ssid}-${network.frequencyMhz}" },
        ) { network ->
            var expanded by rememberSaveable(network.bssid, network.ssid) {
                mutableStateOf(false)
            }
            NetworkCard(
                network = network,
                expanded = expanded,
                onToggle = { expanded = !expanded },
            )
        }

        item {
            InfoBanner(
                title = "Scan responsibly",
                message = "Wi Fi scan results describe advertisements, not trust. Android may throttle repeated scans, and some devices hide BSSID or current network details.",
                color = TextMuted,
            )
        }
    }
}

@Composable
private fun ScannerControls(
    state: ScannerUiState,
    scanningBlockedByLocation: Boolean,
    onScan: () -> Unit,
) {
    val canScan = !state.isScanning &&
        state.cooldownRemainingSeconds == 0 &&
        !scanningBlockedByLocation
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (state.isScanning) "Scanning nearby networks" else "Ready to survey",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = when {
                        state.cooldownRemainingSeconds > 0 ->
                            "Cooldown ${state.cooldownRemainingSeconds}s to respect Android throttling"

                        state.isScanning -> "Waiting for Android to return results"
                        else -> "Foreground scans are intentionally rate limited"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onScan,
                enabled = canScan,
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cyan400,
                    contentColor = Navy900,
                    disabledContainerColor = Navy800,
                    disabledContentColor = TextMuted,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
            ) {
                if (state.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Navy900,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = if (state.cooldownRemainingSeconds > 0) {
                            "${state.cooldownRemainingSeconds}s"
                        } else {
                            "Scan"
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanSummary(state: ScannerUiState) {
    val protected = state.networks.count {
        it.security !in setOf(WifiSecurity.OPEN, WifiSecurity.WEP, WifiSecurity.UNKNOWN)
    }
    val open = state.networks.count { it.security == WifiSecurity.OPEN || it.security == WifiSecurity.WEP }
    val newest = state.networks.maxOfOrNull { it.rssi }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        MetricTile(
            label = "Networks",
            value = state.networks.size.toString(),
            supporting = "visible",
            modifier = Modifier.weight(1f),
            accent = Cyan400,
        )
        MetricTile(
            label = "Protected",
            value = protected.toString(),
            supporting = "WPA or OWE",
            modifier = Modifier.weight(1f),
            accent = Success,
        )
        MetricTile(
            label = "Open",
            value = open.toString(),
            supporting = "review",
            modifier = Modifier.weight(1f),
            accent = if (open > 0) Warning else TextMuted,
        )
        MetricTile(
            label = "Strongest",
            value = newest?.let { "$it dBm" } ?: "None",
            supporting = "signal",
            modifier = Modifier.weight(1.25f),
            accent = Blue400,
        )
    }
}

@Composable
private fun BandFilters(
    selectedBand: SignalBand?,
    securityFilter: SecurityFilter,
    sort: ScannerSort,
    onBandChange: (SignalBand?) -> Unit,
    onSecurityChange: (SecurityFilter) -> Unit,
    onSortChange: (ScannerSort) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SelectChip(
                label = "All bands",
                selected = selectedBand == null,
                onClick = { onBandChange(null) },
            )
            SelectChip(
                label = "2.4 GHz",
                selected = selectedBand == SignalBand.BAND_2_4_GHZ,
                onClick = { onBandChange(SignalBand.BAND_2_4_GHZ) },
            )
            SelectChip(
                label = "5 GHz",
                selected = selectedBand == SignalBand.BAND_5_GHZ,
                onClick = { onBandChange(SignalBand.BAND_5_GHZ) },
            )
            SelectChip(
                label = "6 GHz",
                selected = selectedBand == SignalBand.BAND_6_GHZ,
                onClick = { onBandChange(SignalBand.BAND_6_GHZ) },
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "FILTER",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(end = 2.dp),
            )
            SecurityFilter.entries.forEach { filter ->
                SelectChip(
                    label = filter.label,
                    selected = securityFilter == filter,
                    onClick = { onSecurityChange(filter) },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "SORT",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(end = 2.dp),
            )
            ScannerSort.entries.forEach { option ->
                SelectChip(
                    label = option.label,
                    selected = sort == option,
                    onClick = { onSortChange(option) },
                )
            }
        }
    }
}

@Composable
private fun SelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Cyan400.copy(alpha = 0.16f) else Navy900.copy(alpha = 0.55f))
            .border(
                1.dp,
                if (selected) Cyan400.copy(alpha = 0.6f) else BorderColor,
                RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Cyan400 else TextSecondary,
        )
    }
}

@Composable
private fun NetworkCard(
    network: WifiNetwork,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val securityColor = when (network.security) {
        WifiSecurity.OPEN, WifiSecurity.WEP -> Danger
        WifiSecurity.UNKNOWN -> Warning
        else -> Success
    }
    val signalColor = when (network.signalQuality) {
        SignalQuality.EXCELLENT, SignalQuality.GOOD -> Success
        SignalQuality.FAIR -> Cyan400
        SignalQuality.WEAK -> Warning
        SignalQuality.POOR -> Danger
    }
    GlassCard(
        onClick = onToggle,
        contentPadding = 15,
    ) {
        Row(
            modifier = Modifier.animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Cyan400.copy(alpha = 0.09f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Wifi,
                    contentDescription = null,
                    tint = if (network.isHidden) TextMuted else Cyan400,
                    modifier = Modifier.size(21.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = network.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = network.bssid ?: "BSSID hidden",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                SignalBars(level = network.signalLevel, activeColor = signalColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${network.rssi} dBm",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
        Spacer(modifier = Modifier.height(11.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            StatusPill(text = network.security.label, color = securityColor)
            StatusPill(text = network.band.label, color = Blue400)
            Text(
                text = "CH ${network.channel}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) {
                    Icons.Outlined.KeyboardArrowUp
                } else {
                    Icons.Outlined.KeyboardArrowDown
                },
                contentDescription = if (expanded) "Collapse details" else "Expand details",
                tint = TextMuted,
                modifier = Modifier.size(20.dp),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(modifier = Modifier.height(13.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderColor.copy(alpha = 0.7f)),
                )
                Spacer(modifier = Modifier.height(9.dp))
                DetailRow("Frequency", "${network.frequencyMhz} MHz")
                DetailRow("Channel width", network.channelWidthMhz?.let { "$it MHz" } ?: "Not exposed")
                DetailRow("Wi Fi standard", network.wifiStandard ?: "Not exposed")
                DetailRow("Signal quality", network.signalQuality.label)
                DetailRow("Capabilities", network.capabilities.ifBlank { "Not advertised" })
                if (network.security == WifiSecurity.OPEN) {
                    Spacer(modifier = Modifier.height(9.dp))
                    InfoBanner(
                        title = "Open network",
                        message = "Traffic is not protected at the wireless link. Avoid sensitive activity unless you trust the network and use encrypted services.",
                        color = Danger,
                        icon = Icons.Outlined.Warning,
                    )
                } else if (network.security == WifiSecurity.WEP) {
                    Spacer(modifier = Modifier.height(9.dp))
                    InfoBanner(
                        title = "Legacy security",
                        message = "WEP is obsolete and should be replaced with WPA2 or WPA3.",
                        color = Danger,
                        icon = Icons.Outlined.Warning,
                    )
                }
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = "WiFiLab never attempts to join or capture credentials from a scanned network.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
        }
    }
}

@Composable
private fun PermissionGate(
    permissionDenied: Boolean,
    needsLocation: Boolean,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    GlassCard {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Cyan400.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Wifi,
                contentDescription = null,
                tint = Cyan400,
                modifier = Modifier.size(23.dp),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Nearby Wi Fi access is required",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = if (needsLocation) {
                "Android uses location permission to reveal scan results. WiFiLab does not store your location or send scan data anywhere."
            } else {
                "Android requires the nearby Wi Fi devices permission to return scan results. WiFiLab does not record your location."
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cyan400,
                    contentColor = Navy900,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(if (permissionDenied) "Request again" else "Grant permission")
            }
            TextButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text("App settings", color = TextSecondary)
            }
        }
        if (permissionDenied) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "If Android no longer shows the prompt, enable the permission in system settings.",
                style = MaterialTheme.typography.bodySmall,
                color = Warning,
            )
        }
    }
}
