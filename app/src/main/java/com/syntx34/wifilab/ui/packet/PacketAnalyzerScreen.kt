package com.syntx34.wifilab.ui.packet

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntx34.wifilab.core.util.TimeFormatter
import com.syntx34.wifilab.domain.model.PacketInfo
import com.syntx34.wifilab.domain.model.PacketProtocol
import com.syntx34.wifilab.domain.model.ParsedCapture
import com.syntx34.wifilab.domain.model.StoredCapture
import com.syntx34.wifilab.ui.components.DetailRow
import com.syntx34.wifilab.ui.components.EmptyState
import com.syntx34.wifilab.ui.components.ErrorBanner
import com.syntx34.wifilab.ui.components.GlassCard
import com.syntx34.wifilab.ui.components.InfoBanner
import com.syntx34.wifilab.ui.components.LoadingState
import com.syntx34.wifilab.ui.components.MetricTile
import com.syntx34.wifilab.ui.components.ProtocolBreakdown
import com.syntx34.wifilab.ui.components.SectionHeader
import com.syntx34.wifilab.ui.components.StatusPill
import com.syntx34.wifilab.ui.components.TinyBadge
import com.syntx34.wifilab.ui.components.protocolColor
import com.syntx34.wifilab.ui.theme.Blue400
import com.syntx34.wifilab.ui.theme.BorderColor
import com.syntx34.wifilab.ui.theme.Cyan400
import com.syntx34.wifilab.ui.theme.Danger
import com.syntx34.wifilab.ui.theme.Navy800
import com.syntx34.wifilab.ui.theme.Navy850
import com.syntx34.wifilab.ui.theme.Navy900
import com.syntx34.wifilab.ui.theme.Success
import com.syntx34.wifilab.ui.theme.TextMuted
import com.syntx34.wifilab.ui.theme.TextPrimary
import com.syntx34.wifilab.ui.theme.TextSecondary
import com.syntx34.wifilab.ui.theme.Warning
import java.util.Locale

@Composable
fun PacketAnalyzerScreen(
    viewModel: PacketAnalyzerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<StoredCapture?>(null) }
    val openDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.importCapture(uri.toString())
        }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedPacket = state.selectedPacket

    pendingDelete?.let { capture ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = Navy850,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Delete local capture?") },
            text = {
                Text("${capture.fileName} will be removed from private app storage. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCapture(capture.id)
                        pendingDelete = null
                    },
                ) {
                    Text("Delete", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
        )
    }

    if (selectedPacket != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectPacket(null) },
            sheetState = sheetState,
            containerColor = Navy850,
            contentColor = TextPrimary,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(38.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BorderColor),
                )
            },
        ) {
            PacketDetailSheet(
                packet = selectedPacket,
                onClose = { viewModel.selectPacket(null) },
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        item {
            Column {
                Text(
                    text = "PACKET STUDIO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cyan400,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Offline capture analysis",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Inspect EAPOL, DNS, ARP and transport metadata from files you select.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { openDocument.launch(arrayOf("*/*")) },
                    enabled = !state.importing && !state.loading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan400,
                        contentColor = Navy900,
                        disabledContainerColor = Navy800,
                        disabledContentColor = TextMuted,
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 11.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import capture", maxLines = 1)
                }
                OutlinedButton(
                    onClick = viewModel::loadDemo,
                    enabled = !state.importing && !state.loading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(13.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 11.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = Cyan400,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Synthetic demo", color = Cyan400, maxLines = 1)
                }
            }
        }

        if (state.importing) {
            item {
                LoadingState("Copying the capture into private app storage")
            }
        }
        if (state.loading) {
            item {
                LoadingState("Reading local packet metadata")
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

        val capture = state.capture
        if (capture == null) {
            item {
                InfoBanner(
                    title = "Private by design",
                    message = "Imported files are copied into app private storage and parsed on this device. WiFiLab never uploads captures or contacts the addresses inside them.",
                    color = Blue400,
                    icon = Icons.Outlined.Info,
                )
            }
            item {
                SectionHeader(
                    eyebrow = "RECENT FILES",
                    title = "Stored captures",
                    subtitle = "${state.storedCaptures.size} local file${if (state.storedCaptures.size == 1) "" else "s"}",
                )
            }
            if (state.storedCaptures.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.FolderOpen,
                        title = "No captures imported",
                        message = "Choose a PCAP or PCAPNG file you are authorized to analyze, or open the built in synthetic EAPOL demonstration.",
                        actionLabel = "Load synthetic demo",
                        onAction = viewModel::loadDemo,
                        accent = Cyan400,
                    )
                }
            } else {
                items(
                    items = state.storedCaptures,
                    key = { it.id },
                ) { stored ->
                    StoredCaptureCard(
                        capture = stored,
                        onOpen = { viewModel.openCapture(stored.id) },
                        onDelete = { pendingDelete = stored },
                    )
                }
            }
            item {
                EvidenceGuide()
            }
        } else {
            item {
                CaptureHeader(
                    capture = capture,
                    isDemo = state.isDemo,
                    onClose = viewModel::closeCapture,
                )
            }
            item {
                CaptureMetrics(capture)
            }
            item {
                SectionHeader(
                    eyebrow = "COMPOSITION",
                    title = "Protocol mix",
                    subtitle = "Parsed from ${capture.summary.packetCount} packets.",
                )
            }
            item {
                GlassCard {
                    ProtocolBreakdown(capture.summary.protocolCounts)
                }
            }
            if (capture.summary.eapolCount > 0) {
                item {
                    InfoBanner(
                        title = "EAPOL key metadata found",
                        message = "${capture.summary.eapolCount} key frame${if (capture.summary.eapolCount == 1) "" else "s"}. Open a packet to read message numbers, control flags, replay counters and PMKID presence. No password or key is recovered.",
                        color = Cyan400,
                        icon = Icons.Outlined.Analytics,
                    )
                }
            }
            if (capture.summary.warnings.isNotEmpty()) {
                item {
                    InfoBanner(
                        title = "Parser notes",
                        message = capture.summary.warnings.joinToString(" "),
                        color = Warning,
                        icon = Icons.Outlined.Warning,
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::updateQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Search address, protocol or summary", color = TextMuted)
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
                ProtocolFilters(
                    capture = capture,
                    selected = state.protocolFilter,
                    onSelect = viewModel::setProtocolFilter,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${state.matchingPackets} matching packets",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    if (state.matchingPackets > state.visiblePackets.size) {
                        Text(
                            text = "Showing first ${state.visiblePackets.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                }
            }
            if (state.visiblePackets.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Search,
                        title = "No matching packets",
                        message = "Clear the query or protocol filter to see the full capture.",
                        actionLabel = "Reset filters",
                        onAction = {
                            viewModel.updateQuery("")
                            viewModel.setProtocolFilter(null)
                        },
                    )
                }
            } else {
                items(
                    items = state.visiblePackets,
                    key = { it.number },
                ) { packet ->
                    PacketRow(
                        packet = packet,
                        onClick = { viewModel.selectPacket(packet) },
                    )
                }
            }
            item {
                InfoBanner(
                    title = "Educational analysis",
                    message = "Packet details are metadata. WiFiLab does not decrypt traffic or attempt credential recovery, and it never opens network connections for a captured address.",
                    color = TextMuted,
                    icon = Icons.Outlined.Info,
                )
            }
        }
    }
}

@Composable
private fun CaptureHeader(
    capture: ParsedCapture,
    isDemo: Boolean,
    onClose: () -> Unit,
) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Cyan400.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    tint = Cyan400,
                    modifier = Modifier.size(21.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = capture.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${capture.summary.format.label} · ${capture.summary.packetCount} packets",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close capture",
                    tint = TextMuted,
                )
            }
        }
        if (isDemo) {
            Spacer(modifier = Modifier.height(11.dp))
            StatusPill(text = "SYNTHETIC DEMO", color = Warning)
        }
    }
}

@Composable
private fun CaptureMetrics(capture: ParsedCapture) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            MetricTile(
                label = "Packets",
                value = capture.summary.packetCount.toString(),
                supporting = "captured",
                modifier = Modifier.weight(1f),
                accent = Cyan400,
            )
            MetricTile(
                label = "EAPOL",
                value = capture.summary.eapolCount.toString(),
                supporting = "key frames",
                modifier = Modifier.weight(1f),
                accent = Blue400,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            MetricTile(
                label = "Protocols",
                value = capture.summary.protocolCounts.size.toString(),
                supporting = "observed",
                modifier = Modifier.weight(1f),
                accent = Blue400,
            )
            MetricTile(
                label = "Duration",
                value = TimeFormatter.formatDurationMs(capture.summary.durationMillis),
                supporting = "capture span",
                modifier = Modifier.weight(1f),
                accent = Cyan400,
            )
        }
    }
}

@Composable
private fun StoredCaptureCard(
    capture: StoredCapture,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    GlassCard(
        onClick = onOpen,
        contentPadding = 15,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Blue400.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Analytics,
                    contentDescription = null,
                    tint = Blue400,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = capture.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${capture.packetCount} packets · ${capture.eapolCount} EAPOL · ${TimeFormatter.formatBytes(capture.sizeBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Text(
                    text = TimeFormatter.formatShortTimestamp(capture.importedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete capture",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun EvidenceGuide() {
    GlassCard {
        Text(
            text = "What this parser reads",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(7.dp))
        listOf(
            "Classic PCAP in microsecond and nanosecond variants",
            "PCAPNG section, interface and enhanced packet blocks",
            "Ethernet, VLAN, raw IP, Linux cooked and IPv6 headers",
            "EAPOL key flags, replay counters, nonces and PMKID presence",
            "ARP bindings, DNS questions and answers, TCP and UDP ports",
        ).forEach { item ->
            Row(
                modifier = Modifier.padding(vertical = 3.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Cyan400),
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = "Radio tap headers and encrypted 802.11 payloads are not decoded. Encrypted key data remains unreadable by design.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
    }
}

@Composable
private fun ProtocolFilters(
    capture: ParsedCapture,
    selected: PacketProtocol?,
    onSelect: (PacketProtocol?) -> Unit,
) {
    val protocols = capture.summary.protocolCounts.entries.sortedByDescending { it.value }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        FilterChip(
            label = "All",
            count = capture.summary.packetCount,
            selected = selected == null,
            color = Cyan400,
            onClick = { onSelect(null) },
        )
        protocols.forEach { (protocol, count) ->
            FilterChip(
                label = protocol.label,
                count = count,
                selected = selected == protocol,
                color = protocolColor(protocol),
                onClick = { onSelect(protocol) },
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) color.copy(alpha = 0.15f) else Navy900.copy(alpha = 0.55f))
            .border(
                1.dp,
                if (selected) color.copy(alpha = 0.55f) else BorderColor,
                RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) color else TextSecondary,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) color.copy(alpha = 0.8f) else TextMuted,
        )
    }
}

@Composable
private fun PacketRow(
    packet: PacketInfo,
    onClick: () -> Unit,
) {
    val color = protocolColor(packet.protocol)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Navy800.copy(alpha = 0.56f))
            .border(1.dp, BorderColor.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = packet.number.toString().padStart(3, '0'),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(30.dp),
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Spacer(modifier = Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = packet.protocol.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    modifier = Modifier.width(45.dp),
                )
                Text(
                    text = if (packet.source == packet.destination) {
                        packet.source
                    } else {
                        "${packet.source} → ${packet.destination}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = packet.summary,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${packet.capturedLength} B",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }
}

@Composable
private fun PacketDetailSheet(
    packet: PacketInfo,
    onClose: () -> Unit,
) {
    val color = protocolColor(packet.protocol)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PACKET ${packet.number}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = packet.protocol.label,
                    style = MaterialTheme.typography.headlineSmall,
                    color = color,
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close packet details",
                    tint = TextMuted,
                )
            }
        }
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = packet.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(14.dp))
        GlassCard {
            DetailRow("Timestamp", if (packet.timestampMillis > 0) {
                TimeFormatter.formatTimestamp(packet.timestampMillis)
            } else {
                "Not provided"
            })
            DetailRow("Source", packet.source, monospace = true)
            DetailRow("Destination", packet.destination, monospace = true)
            DetailRow("Original length", "${packet.originalLength} bytes")
            DetailRow("Captured length", "${packet.capturedLength} bytes")
        }
        val eapol = packet.eapol
        if (eapol != null) {
            Spacer(modifier = Modifier.height(13.dp))
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = eapol.messageLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = Cyan400,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TinyBadge(
                        text = if (eapol.pmkidPresent) "PMKID PRESENT" else "KEY FRAME",
                        color = if (eapol.pmkidPresent) Warning else Cyan400,
                    )
                }
                Spacer(modifier = Modifier.height(9.dp))
                DetailRow("Descriptor type", eapol.descriptorType.toString())
                DetailRow(
                    "Key information",
                    String.format(Locale.US, "0x%04X", eapol.keyInfo),
                    monospace = true,
                )
                DetailRow("Key length", "${eapol.keyLength} bytes")
                DetailRow(
                    "Replay counter",
                    java.lang.Long.toUnsignedString(eapol.replayCounter),
                    monospace = true,
                )
                DetailRow("Nonce present", if (eapol.noncePresent) "Yes" else "No")
                DetailRow("Key data length", "${eapol.keyDataLength} bytes")
                DetailRow("Encrypted key data", if (eapol.encryptedKeyData) "Yes" else "No")
                DetailRow("PMKID present", if (eapol.pmkidPresent) "Yes" else "No")
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (eapol.ack) TinyBadge("ACK", Cyan400)
                    if (eapol.mic) TinyBadge("MIC", Blue400)
                    if (eapol.install) TinyBadge("INSTALL", Success)
                    if (eapol.secure) TinyBadge("SECURE", Success)
                }
                Spacer(modifier = Modifier.height(9.dp))
                Text(
                    text = "The key information field is metadata. WiFiLab does not derive or display a passphrase, PMK or PTK.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
        }
        val dns = packet.dns
        if (dns != null) {
            Spacer(modifier = Modifier.height(13.dp))
            GlassCard {
                Text(
                    text = if (dns.isResponse) "DNS response" else "DNS query",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(9.dp))
                DetailRow(
                    "Transaction ID",
                    String.format(Locale.US, "0x%04X", dns.transactionId),
                    monospace = true,
                )
                DetailRow("Response code", dns.responseCode.toString())
                if (dns.questions.isNotEmpty()) {
                    DetailRow("Questions", dns.questions.joinToString("\n"))
                }
                if (dns.answers.isNotEmpty()) {
                    DetailRow("Answers", dns.answers.joinToString("\n"))
                }
            }
        }
        val arp = packet.arp
        if (arp != null) {
            Spacer(modifier = Modifier.height(13.dp))
            GlassCard {
                Text(
                    text = "ARP ${arp.operationLabel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(9.dp))
                DetailRow("Sender", "${arp.senderIp} / ${arp.senderMac}", monospace = true)
                DetailRow("Target", "${arp.targetIp} / ${arp.targetMac}", monospace = true)
            }
        }
        packet.payloadPreview?.takeIf { it.isNotBlank() }?.let { preview ->
            Spacer(modifier = Modifier.height(13.dp))
            Text(
                text = "PAYLOAD PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = preview,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Navy900)
                    .padding(12.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        InfoBanner(
            title = "Local analysis only",
            message = "Packet contents remain on this device. Sensitive addresses and names should be removed before sharing a capture.",
            color = TextMuted,
            icon = Icons.Outlined.Info,
        )
    }
}
