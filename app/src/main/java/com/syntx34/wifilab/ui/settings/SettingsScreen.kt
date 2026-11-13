package com.syntx34.wifilab.ui.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntx34.wifilab.BuildConfig
import com.syntx34.wifilab.ui.components.DetailRow
import com.syntx34.wifilab.ui.components.GlassCard
import com.syntx34.wifilab.ui.components.InfoBanner
import com.syntx34.wifilab.ui.components.MetricTile
import com.syntx34.wifilab.ui.components.SectionHeader
import com.syntx34.wifilab.ui.components.StatusPill
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

private enum class ClearTarget(val title: String, val description: String) {
    AUDITS("Clear audit history?", "All saved audit summaries and device records will be removed."),
    BASELINE("Remove trusted baseline?", "Future audits will no longer flag new devices until a baseline is saved again."),
    CAPTURES("Delete imported captures?", "Every imported PCAP and PCAPNG file will be removed from private app storage."),
    ACTIVITY("Clear research activity?", "The recent activity trail on the dashboard will be emptied."),
    LEARNING("Reset learning progress?", "Completed terminology, lessons and exercises will be marked incomplete."),
    ALL(
        "Clear all local data?",
        "Audits, baseline, imported captures, activity and learning progress will be removed. Settings return to defaults. This cannot be undone.",
    ),
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var clearTarget by remember { mutableStateOf<ClearTarget?>(null) }

    clearTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { clearTarget = null },
            containerColor = com.syntx34.wifilab.ui.theme.Navy850,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            icon = {
                Icon(
                    imageVector = if (target == ClearTarget.ALL) {
                        Icons.Outlined.Warning
                    } else {
                        Icons.Outlined.Delete
                    },
                    contentDescription = null,
                    tint = Danger,
                )
            },
            title = { Text(target.title) },
            text = { Text(target.description) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = target
                        clearTarget = null
                        when (selected) {
                            ClearTarget.AUDITS -> viewModel.clearAudits()
                            ClearTarget.BASELINE -> viewModel.clearBaseline()
                            ClearTarget.CAPTURES -> viewModel.clearCaptures()
                            ClearTarget.ACTIVITY -> viewModel.clearActivity()
                            ClearTarget.LEARNING -> viewModel.clearLearningProgress()
                            ClearTarget.ALL -> viewModel.clearAll()
                        }
                    },
                ) {
                    Text(if (target == ClearTarget.ALL) "Clear everything" else "Delete", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { clearTarget = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
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
                    text = "APPLICATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cyan400,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Settings and local data",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Control scan pacing, inspect storage and remove research data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        if (state.busy) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Cyan400,
                    trackColor = Navy900,
                )
            }
        }

        state.message?.let { message ->
            item {
                InfoBanner(
                    title = "Done",
                    message = message,
                    color = Success,
                    icon = Icons.Outlined.CheckCircle,
                )
            }
        }

        state.error?.let { message ->
            item {
                InfoBanner(
                    title = "Could not complete the action",
                    message = message,
                    color = Danger,
                    icon = Icons.Outlined.Warning,
                )
            }
        }

        item {
            SectionHeader(
                eyebrow = "SCANNING",
                title = "Scan pacing",
                subtitle = "Longer cooldowns are gentler on Android scan throttling.",
            )
        }

        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Cyan400.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Wifi,
                            contentDescription = null,
                            tint = Cyan400,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cooldown between scans",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                        )
                        Text(
                            text = "Android may still throttle independent of this setting.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    StatusPill(text = "${state.scanCooldownSeconds}s", color = Cyan400)
                }
                Spacer(modifier = Modifier.height(13.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 30, 60, 120).forEach { seconds ->
                        CooldownChip(
                            seconds = seconds,
                            selected = state.scanCooldownSeconds == seconds,
                            onClick = { viewModel.setScanCooldown(seconds) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                eyebrow = "LOCAL STORAGE",
                title = "Research inventory",
                subtitle = "Counts come from the on device database and private file storage.",
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                MetricTile(
                    label = "Audits",
                    value = state.auditCount.toString(),
                    supporting = "sessions",
                    modifier = Modifier.weight(1f),
                    accent = Blue400,
                )
                MetricTile(
                    label = "Baseline",
                    value = state.baselineCount.toString(),
                    supporting = "trusted",
                    modifier = Modifier.weight(1f),
                    accent = Success,
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                MetricTile(
                    label = "Captures",
                    value = state.captureCount.toString(),
                    supporting = "local files",
                    modifier = Modifier.weight(1f),
                    accent = Cyan400,
                )
                MetricTile(
                    label = "Activity",
                    value = state.activityCount.toString(),
                    supporting = "events",
                    modifier = Modifier.weight(1f),
                    accent = Warning,
                )
                MetricTile(
                    label = "Lessons",
                    value = state.completedLessons.toString(),
                    supporting = "complete",
                    modifier = Modifier.weight(1f),
                    accent = Success,
                )
            }
        }

        item {
            GlassCard {
                DataActionRow(
                    icon = Icons.Outlined.Delete,
                    title = "Clear audit history",
                    description = "Remove saved audit sessions",
                    onClick = { clearTarget = ClearTarget.AUDITS },
                    enabled = !state.busy && state.auditCount > 0,
                )
                DataActionRow(
                    icon = Icons.Outlined.Lock,
                    title = "Remove trusted baseline",
                    description = "Disable unexpected device comparison",
                    onClick = { clearTarget = ClearTarget.BASELINE },
                    enabled = !state.busy && state.baselineCount > 0,
                )
                DataActionRow(
                    icon = Icons.Outlined.Analytics,
                    title = "Delete imported captures",
                    description = "Remove PCAP files from app private storage",
                    onClick = { clearTarget = ClearTarget.CAPTURES },
                    enabled = !state.busy && state.captureCount > 0,
                )
                DataActionRow(
                    icon = Icons.Outlined.Refresh,
                    title = "Clear activity trail",
                    description = "Empty the dashboard research log",
                    onClick = { clearTarget = ClearTarget.ACTIVITY },
                    enabled = !state.busy && state.activityCount > 0,
                )
                DataActionRow(
                    icon = Icons.Outlined.MenuBook,
                    title = "Reset learning progress",
                    description = "Mark every lesson and exercise incomplete",
                    onClick = { clearTarget = ClearTarget.LEARNING },
                    enabled = !state.busy && state.completedLessons > 0,
                )
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = { clearTarget = ClearTarget.ALL },
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = Danger,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text("Clear all local research data", color = Danger)
                }
            }
        }

        item {
            SectionHeader(
                eyebrow = "PRIVACY",
                title = "Built for local research",
            )
        }

        item {
            GlassCard {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "No accounts. No telemetry. No capture uploads.",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = BuildConfig.PRIVACY_STATEMENT + " Scan results, imported files, audit summaries and learning progress stay in app private storage. Android cloud backup is disabled.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                eyebrow = "ABOUT",
                title = "WiFiLab",
            )
        }

        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Cyan400.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Wifi,
                            contentDescription = null,
                            tint = Cyan400,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WiFiLab",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                        Text(
                            text = "Version ${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    StatusPill(text = "GPL 3.0", color = Blue400)
                }
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow("Author", "SyntX34")
                DetailRow("License", "GNU General Public License v3.0")
                DetailRow("Minimum Android", "8.0, API 26")
                DetailRow("Privacy", "Local only by default", valueColor = Success)
            }
        }

        item {
            InfoBanner(
                title = "Responsible use",
                message = "WiFiLab is an educational tool. Only audit networks you own or are authorized to test, and never use research findings against third party devices.",
                color = Warning,
                icon = Icons.Outlined.Info,
            )
        }
    }
}

@Composable
private fun CooldownChip(
    seconds: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Cyan400.copy(alpha = 0.16f) else Navy900.copy(alpha = 0.55f))
            .border(
                1.dp,
                if (selected) Cyan400.copy(alpha = 0.6f) else BorderColor,
                RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${seconds}s",
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Cyan400 else TextSecondary,
        )
    }
}

@Composable
private fun DataActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) TextMuted else BorderColor,
            modifier = Modifier.size(19.dp),
        )
        Spacer(modifier = Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (enabled) TextPrimary else TextMuted,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) TextSecondary else BorderColor,
            )
        }
        Text(
            text = "Clear",
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) Danger else BorderColor,
        )
    }
}
