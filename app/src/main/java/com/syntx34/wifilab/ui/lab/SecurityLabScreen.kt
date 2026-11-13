package com.syntx34.wifilab.ui.lab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.syntx34.wifilab.domain.model.ProtocolExplanation
import com.syntx34.wifilab.ui.components.GlassCard
import com.syntx34.wifilab.ui.components.InfoBanner
import com.syntx34.wifilab.ui.components.SectionHeader
import com.syntx34.wifilab.ui.components.StatusPill
import com.syntx34.wifilab.ui.theme.Blue400
import com.syntx34.wifilab.ui.theme.BorderColor
import com.syntx34.wifilab.ui.theme.Cyan400
import com.syntx34.wifilab.ui.theme.Navy800
import com.syntx34.wifilab.ui.theme.Navy900
import com.syntx34.wifilab.ui.theme.Success
import com.syntx34.wifilab.ui.theme.TextMuted
import com.syntx34.wifilab.ui.theme.TextPrimary
import com.syntx34.wifilab.ui.theme.TextSecondary
import com.syntx34.wifilab.ui.theme.Violet
import com.syntx34.wifilab.ui.theme.Warning

@Composable
fun SecurityLabScreen(
    onOpenPacketAnalyzer: () -> Unit,
    onOpenPasswordLab: () -> Unit,
    onOpenAttackAnatomy: () -> Unit,
    viewModel: SecurityLabViewModel = hiltViewModel(),
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(
                    text = "OFFLINE SECURITY LAB",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cyan400,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Understand the protocol",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Study wireless security with local data and deliberately synthetic examples.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        item {
            LabHero()
        }

        item {
            SectionHeader(
                eyebrow = "LAB BENCH",
                title = "Offline instruments",
                subtitle = "Nothing here sends traffic to an outside service.",
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LabToolCard(
                    icon = Icons.Outlined.FolderOpen,
                    title = "Packet Analyzer",
                    label = "PCAP STUDIO",
                    description = "Import a capture you own or load the synthetic EAPOL demo. Inspect protocols, DNS names and handshake metadata without recovering keys.",
                    accent = Blue400,
                    onClick = onOpenPacketAnalyzer,
                )
                LabToolCard(
                    icon = Icons.Outlined.Lock,
                    title = "Password Lab",
                    label = "ENTROPY",
                    description = "Compare password entropy, pattern penalties and simulated offline guessing using generated test data only.",
                    accent = Warning,
                    onClick = onOpenPasswordLab,
                )
                LabToolCard(
                    icon = Icons.Outlined.Warning,
                    title = "Attack Anatomy",
                    label = "SIMULATION",
                    description = "Step through isolated models of ARP spoofing, DNS spoofing, evil twins, WPA authentication and packet interception.",
                    accent = Violet,
                    onClick = onOpenAttackAnatomy,
                )
            }
        }

        item {
            SectionHeader(
                eyebrow = "PROTOCOL LIBRARY",
                title = "Wireless authentication",
                subtitle = "Expand a protocol for the defensive picture.",
            )
        }

        items(
            items = viewModel.protocols,
            key = { it.id },
        ) { protocol ->
            ProtocolCard(protocol)
        }

        item {
            InfoBanner(
                title = "No live attack tooling",
                message = "WiFiLab does not implement password recovery, deauthentication injection, rogue access points or interception against real devices. Every demonstration is local, synthetic and educational.",
                color = Warning,
                icon = Icons.Outlined.Science,
            )
        }
    }
}

@Composable
private fun LabHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(Navy800, Navy900, Color(0xFF101B38)),
                ),
            )
            .border(1.dp, BorderColor, RoundedCornerShape(26.dp))
            .padding(20.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Cyan400.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Science,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                StatusPill(text = "LOCAL ONLY", color = Success)
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Outlined.Wifi,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Learn by inspecting, not intruding.",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "A safe place to build intuition about handshakes, entropy and local network trust.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun LabToolCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    label: String,
    description: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Navy800.copy(alpha = 0.62f))
            .border(1.dp, BorderColor.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.11f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(21.dp),
            )
        }
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Composable
private fun ProtocolCard(protocol: ProtocolExplanation) {
    var expanded by rememberSaveable(protocol.id) { mutableStateOf(false) }
    GlassCard(
        onClick = { expanded = !expanded },
        contentPadding = 17,
    ) {
        Row(
            modifier = Modifier.animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Cyan400.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = protocol.shortName.take(4),
                    style = MaterialTheme.typography.labelMedium,
                    color = Cyan400,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = protocol.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Text(
                    text = protocol.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = if (expanded) 6 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = if (expanded) {
                    Icons.Outlined.KeyboardArrowUp
                } else {
                    Icons.Outlined.KeyboardArrowDown
                },
                contentDescription = if (expanded) "Collapse protocol" else "Expand protocol",
                tint = TextMuted,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderColor.copy(alpha = 0.7f)),
                )
                Spacer(modifier = Modifier.height(12.dp))
                ProtocolListSection("AUTHENTICATION FLOW", protocol.authenticationFlow, Cyan400)
                Spacer(modifier = Modifier.height(14.dp))
                ProtocolListSection("STRENGTHS", protocol.strengths, Success)
                Spacer(modifier = Modifier.height(14.dp))
                ProtocolListSection("WATCH FOR", protocol.weaknesses, Warning)
                Spacer(modifier = Modifier.height(13.dp))
                InfoBanner(
                    title = "Researcher note",
                    message = protocol.researcherNote,
                    color = Blue400,
                )
            }
        }
    }
}

@Composable
private fun ProtocolListSection(
    title: String,
    items: List<String>,
    color: Color,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = color,
    )
    Spacer(modifier = Modifier.height(6.dp))
    items.forEachIndexed { index, item ->
        Row(
            modifier = Modifier.padding(vertical = 3.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "${index + 1}.",
                style = MaterialTheme.typography.labelMedium,
                color = color,
                modifier = Modifier.width(24.dp),
            )
            Text(
                text = item,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}
