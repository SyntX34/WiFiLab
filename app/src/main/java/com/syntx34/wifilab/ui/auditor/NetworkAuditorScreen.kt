package com.syntx34.wifilab.ui.auditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntx34.wifilab.core.util.TimeFormatter
import com.syntx34.wifilab.domain.model.DiscoveredDevice
import com.syntx34.wifilab.domain.model.NetworkAuditReport
import com.syntx34.wifilab.domain.model.RecommendationSeverity
import com.syntx34.wifilab.domain.model.SecurityRecommendation
import com.syntx34.wifilab.ui.components.DetailRow
import com.syntx34.wifilab.ui.components.EmptyState
import com.syntx34.wifilab.ui.components.ErrorBanner
import com.syntx34.wifilab.ui.components.GlassCard
import com.syntx34.wifilab.ui.components.InfoBanner
import com.syntx34.wifilab.ui.components.MetricTile
import com.syntx34.wifilab.ui.components.SectionHeader
import com.syntx34.wifilab.ui.components.StatusPill
import com.syntx34.wifilab.ui.components.TinyBadge
import com.syntx34.wifilab.ui.theme.Blue400
import com.syntx34.wifilab.ui.theme.BorderColor
import com.syntx34.wifilab.ui.theme.Cyan400
import com.syntx34.wifilab.ui.theme.Danger
import com.syntx34.wifilab.ui.theme.Navy900
import com.syntx34.wifilab.ui.theme.Success
import com.syntx34.wifilab.ui.theme.TextMuted
import com.syntx34.wifilab.ui.theme.TextPrimary
import com.syntx34.wifilab.ui.theme.TextSecondary
import com.syntx34.wifilab.ui.theme.Warning

@Composable
fun NetworkAuditorScreen(
    viewModel: NetworkAuditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showBaselineDialog by remember { mutableStateOf(false) }

    if (showBaselineDialog) {
        BaselineConfirmationDialog(
            deviceCount = state.report?.devices?.size ?: 0,
            onConfirm = {
                showBaselineDialog = false
                viewModel.saveBaseline()
            },
            onDismiss = { showBaselineDialog = false },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(
                    text = "AUTHORIZED AUDIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cyan400,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Know your local network",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Conservative discovery with explicit consent and a reviewed baseline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        item {
            InfoBanner(
                title = "Authorization comes first",
                message = "Scan only networks you own or have written permission to test. WiFiLab probes a small set of local TCP services and never attempts to log in, exploit or change a device.",
                color = Warning,
                icon = Icons.Outlined.Lock,
            )
        }

        item {
            ScopeAndConsentCard(
                state = state,
                onSubnetChange = viewModel::updateSubnet,
                onRefreshContext = viewModel::refreshNetworkContext,
                onConsentChange = viewModel::setAuthorizationConfirmed,
                onStart = viewModel::startAudit,
                onCancel = viewModel::cancelAudit,
            )
        }

        if (state.isScanning) {
            item {
                AuditProgressCard(
                    state = state,
                    onCancel = viewModel::cancelAudit,
                )
            }
        }

        state.error?.let { message ->
            item {
                ErrorBanner(
                    message = message,
                    onDismiss = viewModel::dismissError,
                )
            }
        }

        state.notice?.let { message ->
            item {
                InfoBanner(
                    title = "Audit update",
                    message = message,
                    color = Success,
                    icon = Icons.Outlined.CheckCircle,
                )
            }
        }

        item {
            BaselineCard(
                state = state,
                onOpenBaselineDialog = { showBaselineDialog = true },
            )
        }

        val report = state.report
        if (report == null) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Router,
                    title = "No report in this session",
                    message = "Confirm authorization and start an audit. Only responsive hosts in the authorized range are listed.",
                )
            }
        } else {
            item {
                SectionHeader(
                    eyebrow = "RESULTS",
                    title = "Audit snapshot",
                    subtitle = "Scanned ${TimeFormatter.formatTimestamp(report.scannedAt)}",
                )
            }
            item {
                ReportSummary(report = report)
            }
            item {
                SectionHeader(
                    eyebrow = "DEVICES",
                    title = "${report.devices.size} responsive device${if (report.devices.size == 1) "" else "s"}",
                    subtitle = if (report.baselineUsed) {
                        "${report.unexpectedDevices.size} differ from the trusted baseline."
                    } else {
                        "No baseline was available for comparison."
                    },
                )
            }
            if (report.devices.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Devices,
                        title = "No responsive devices found",
                        message = "Guest isolation, client isolation or firewall rules can hide hosts from a non root Android application.",
                        actionLabel = "Run audit again",
                        onAction = viewModel::startAudit,
                    )
                }
            } else {
                items(
                    items = report.sortedDevices(),
                    key = { it.ipAddress },
                ) { device ->
                    DeviceCard(
                        device = device,
                        unexpected = report.unexpectedDevices.any { it.ipAddress == device.ipAddress },
                        baselineUsed = report.baselineUsed,
                    )
                }
            }
            item {
                SectionHeader(
                    eyebrow = "RECOMMENDATIONS",
                    title = "Practical hardening",
                    subtitle = "Apply only changes that fit your network.",
                )
            }
            items(
                items = report.recommendations,
                key = { recommendation -> recommendation.title },
            ) { recommendation ->
                RecommendationCard(recommendation)
            }
            if (state.authorizationConfirmed && report.devices.isNotEmpty()) {
                item {
                    Button(
                        onClick = { showBaselineDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Cyan400,
                            contentColor = Navy900,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save reviewed devices as baseline")
                    }
                }
            }
        }

        item {
            InfoBanner(
                title = "Discovery limits on Android",
                message = "Without root, WiFiLab cannot read the ARP table or guaranteed ICMP. Discovery depends on reachability and a small TCP probe set. Results are clues, not proof of device identity.",
                color = TextMuted,
                icon = Icons.Outlined.Info,
            )
        }
    }
}

@Composable
private fun ScopeAndConsentCard(
    state: AuditorUiState,
    onSubnetChange: (String) -> Unit,
    onRefreshContext: () -> Unit,
    onConsentChange: (Boolean) -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit,
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Audit scope",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Text(
                    text = "Private IPv4 ranges from /24 to /30.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            TextButton(onClick = onRefreshContext) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Cyan400,
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text("Current network", color = Cyan400)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.subnetInput,
            onValueChange = onSubnetChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Authorized range") },
            placeholder = { Text("For example 192.168.1.0/24", color = TextMuted) },
            singleLine = true,
            enabled = !state.isScanning,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cyan400,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Navy900.copy(alpha = 0.5f),
                unfocusedContainerColor = Navy900.copy(alpha = 0.5f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Cyan400,
                focusedLabelColor = Cyan400,
                unfocusedLabelColor = TextMuted,
            ),
        )
        state.detection?.let { context ->
            Spacer(modifier = Modifier.height(10.dp))
            DetailRow("Local address", context.localIpv4, monospace = true)
            DetailRow("Gateway", context.gatewayIpv4 ?: "Not available", monospace = true)
            DetailRow("Interface", context.interfaceName ?: "Not exposed")
        }
        Spacer(modifier = Modifier.height(13.dp))
        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(
                checked = state.authorizationConfirmed,
                onCheckedChange = onConsentChange,
                enabled = !state.isScanning,
                colors = CheckboxDefaults.colors(
                    checkedColor = Cyan400,
                    uncheckedColor = BorderColor,
                    checkmarkColor = Navy900,
                ),
            )
            Text(
                text = "I own this network or have explicit authorization to audit it.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (state.isScanning) {
            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Danger,
                    contentColor = Navy900,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel audit")
            }
        } else {
            Button(
                onClick = onStart,
                enabled = state.authorizationConfirmed,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cyan400,
                    contentColor = Navy900,
                    disabledContainerColor = Navy900,
                    disabledContentColor = TextMuted,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start authorized audit")
            }
        }
    }
}

@Composable
private fun AuditProgressCard(
    state: AuditorUiState,
    onCancel: () -> Unit,
) {
    val progress = state.progress
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Auditing local range",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Text(
                    text = progress?.let {
                        "${it.completedHosts} of ${it.totalHosts} hosts checked"
                    } ?: "Preparing host list",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Text(
                text = "${((progress?.fraction ?: 0f) * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = Cyan400,
            )
        }
        Spacer(modifier = Modifier.height(13.dp))
        LinearProgressIndicator(
            progress = progress?.fraction ?: 0f,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Cyan400,
            trackColor = Navy900,
        )
        Spacer(modifier = Modifier.height(9.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = progress?.currentAddress ?: "Building the host list",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontFamily = FontFamily.Monospace,
            )
            TextButton(onClick = onCancel) {
                Text("Stop", color = Danger)
            }
        }
    }
}

@Composable
private fun BaselineCard(
    state: AuditorUiState,
    onOpenBaselineDialog: () -> Unit,
) {
    val newest = state.baseline.maxOfOrNull { it.approvedAt }
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Blue400.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Blue400,
                    modifier = Modifier.size(19.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (state.baseline.isEmpty()) "No trusted baseline yet" else "Trusted baseline active",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Text(
                    text = if (state.baseline.isEmpty()) {
                        "Save a reviewed audit to enable unexpected device detection."
                    } else {
                        "${state.baseline.size} approved devices, updated ${newest?.let(TimeFormatter::relativeTime) ?: "unknown"}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            StatusPill(
                text = if (state.report?.baselineUsed == true) "Compared" else "Review",
                color = if (state.report?.baselineUsed == true) Success else TextMuted,
            )
        }
        if (state.report?.devices?.isNotEmpty() == true && !state.isScanning) {
            Spacer(modifier = Modifier.height(9.dp))
            TextButton(onClick = onOpenBaselineDialog) {
                Text("Replace baseline with this report", color = Cyan400)
            }
        }
    }
}

@Composable
private fun ReportSummary(report: NetworkAuditReport) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            MetricTile(
                label = "Devices",
                value = report.devices.size.toString(),
                supporting = report.cidr,
                modifier = Modifier.weight(1f),
                accent = Cyan400,
            )
            MetricTile(
                label = "Unexpected",
                value = report.unexpectedDevices.size.toString(),
                supporting = if (report.baselineUsed) "baseline delta" else "no baseline",
                modifier = Modifier.weight(1f),
                accent = if (report.unexpectedDevices.isEmpty()) Success else Warning,
            )
        }
        GlassCard {
            DetailRow("Authorized range", report.cidr, monospace = true)
            DetailRow("Baseline used", if (report.baselineUsed) "Yes" else "No")
            DetailRow("Gateway devices", report.devices.count { it.isGateway }.toString())
            DetailRow("Open service hosts", report.devices.count { it.openPorts.isNotEmpty() }.toString())
        }
    }
}

@Composable
private fun DeviceCard(
    device: DiscoveredDevice,
    unexpected: Boolean,
    baselineUsed: Boolean,
) {
    val accent = when {
        unexpected -> Warning
        device.isGateway -> Blue400
        else -> Cyan400
    }
    val badge = when {
        unexpected -> "UNRECOGNIZED"
        device.isGateway -> "GATEWAY"
        baselineUsed -> "BASELINE"
        else -> "DISCOVERED"
    }
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.11f))
                    .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (device.isGateway) {
                        Icons.Outlined.Router
                    } else {
                        Icons.Outlined.Devices
                    },
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.ipAddress,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = device.hostname ?: "Hostname unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            TinyBadge(text = badge, color = accent)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = when {
                device.serviceNames.isNotEmpty() -> device.serviceNames.joinToString(" · ")
                device.responseKind == com.syntx34.wifilab.domain.model.DeviceResponseKind.PING ->
                    "Responded to reachability probe"

                else -> "Gateway identified from local routing"
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (device.openPorts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                device.openPorts.take(6).forEach { port ->
                    TinyBadge(text = "TCP $port", color = Blue400)
                }
            }
        }
        if (unexpected) {
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = "This address was not part of the saved baseline. Verify it before changing anything.",
                style = MaterialTheme.typography.bodySmall,
                color = Warning,
            )
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: SecurityRecommendation) {
    val color = when (recommendation.severity) {
        RecommendationSeverity.HIGH -> Danger
        RecommendationSeverity.MEDIUM -> Warning
        RecommendationSeverity.LOW -> Blue400
        RecommendationSeverity.INFO -> Cyan400
    }
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TinyBadge(text = recommendation.severity.label, color = color)
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = recommendation.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = recommendation.detail,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

@Composable
private fun BaselineConfirmationDialog(
    deviceCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = com.syntx34.wifilab.ui.theme.Navy850,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        icon = {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = Cyan400,
            )
        },
        title = { Text("Save trusted baseline?") },
        text = {
            Text(
                "This replaces the current baseline with the $deviceCount devices from this audit. Confirm that you recognize each device first.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Save baseline", color = Cyan400)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
    )
}

private fun NetworkAuditReport.sortedDevices(): List<DiscoveredDevice> {
    val unexpectedAddresses = unexpectedDevices.map { it.ipAddress }.toSet()
    return devices.sortedWith(
        compareByDescending<DiscoveredDevice> { it.ipAddress in unexpectedAddresses }
            .thenByDescending { it.isGateway }
            .thenBy {
                it.ipAddress.split('.').mapNotNull { part -> part.toIntOrNull() }
                    .fold(0L) { value, part -> value * 256L + part }
            },
    )
}
