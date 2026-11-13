package com.syntx34.wifilab.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syntx34.wifilab.domain.model.PacketProtocol
import com.syntx34.wifilab.domain.model.WifiNetwork
import com.syntx34.wifilab.ui.theme.BorderColor
import com.syntx34.wifilab.ui.theme.Blue300
import com.syntx34.wifilab.ui.theme.Blue400
import com.syntx34.wifilab.ui.theme.Cyan400
import com.syntx34.wifilab.ui.theme.Danger
import com.syntx34.wifilab.ui.theme.Navy900
import com.syntx34.wifilab.ui.theme.Success
import com.syntx34.wifilab.ui.theme.Teal300
import com.syntx34.wifilab.ui.theme.TextMuted
import com.syntx34.wifilab.ui.theme.TextPrimary
import com.syntx34.wifilab.ui.theme.TextSecondary
import com.syntx34.wifilab.ui.theme.Violet
import com.syntx34.wifilab.ui.theme.Warning

@Composable
fun SignalBars(
    level: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Cyan400,
) {
    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        val active = level.coerceIn(0, 4)
        repeat(4) { index ->
            val height = (6 + index * 4).dp
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (index < active) activeColor else BorderColor),
            )
        }
    }
}

@Composable
fun ChannelDistributionChart(
    networks: List<WifiNetwork>,
    selectedChannel: Int?,
    onSelectChannel: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grouped = networks
        .filter { it.channel > 0 }
        .groupBy { it.channel }
        .map { (channel, entries) -> ChannelGroup(channel, entries.first().band.label, entries.size) }
        .sortedWith(compareByDescending<ChannelGroup> { it.count }.thenBy { it.channel })
        .take(12)
    val maxCount = grouped.maxOfOrNull { it.count } ?: 1

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (grouped.isEmpty()) {
            Text(
                text = "Channel data will appear after a scan.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
        grouped.forEach { group ->
            val selected = selectedChannel == group.channel
            val fraction by animateFloatAsState(
                targetValue = group.count.toFloat() / maxCount.toFloat(),
                animationSpec = tween(500),
                label = "channelBar",
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) Cyan400.copy(alpha = 0.08f) else Color.Transparent)
                    .clickable {
                        onSelectChannel(if (selected) null else group.channel)
                    }
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.width(82.dp)) {
                    Text(
                        text = "Channel ${group.channel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Cyan400 else TextPrimary,
                    )
                    Text(
                        text = group.band,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Navy900),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction.coerceAtLeast(0.03f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (selected) Cyan400 else Blue400.copy(alpha = 0.75f),
                            ),
                    )
                }
                Text(
                    text = group.count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.width(18.dp),
                )
            }
        }
    }
}

@Composable
fun ProtocolBreakdown(
    counts: Map<PacketProtocol, Int>,
    modifier: Modifier = Modifier,
) {
    val total = counts.values.sum().coerceAtLeast(1)
    val sorted = counts.entries.sortedByDescending { it.value }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        sorted.forEach { (protocol, count) ->
            val color = protocolColor(protocol)
            val fraction by animateFloatAsState(
                targetValue = count.toFloat() / total.toFloat(),
                animationSpec = tween(600),
                label = "protocolBar",
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color),
                )
                Text(
                    text = protocol.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    modifier = Modifier.width(54.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Navy900),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction.coerceAtLeast(0.02f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(color),
                    )
                }
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.width(28.dp),
                )
            }
        }
    }
}

@Composable
fun EntropyGauge(
    bits: Double,
    color: Color,
    modifier: Modifier = Modifier,
    centerLabel: String,
    centerCaption: String,
) {
    Box(
        modifier = modifier.size(156.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 11.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = Navy900,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            val fraction = (bits / 128.0).coerceIn(0.0, 1.0).toFloat()
            drawArc(
                color = color,
                startAngle = 140f,
                sweepAngle = 260f * fraction,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = centerCaption,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
fun MiniProgress(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = Cyan400,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(450),
        label = "miniProgress",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Navy900),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
    }
}

fun protocolColor(protocol: PacketProtocol): Color = when (protocol) {
    PacketProtocol.EAPOL -> Cyan400
    PacketProtocol.WLAN -> Blue300
    PacketProtocol.ARP -> Warning
    PacketProtocol.DNS -> Violet
    PacketProtocol.DHCP -> Success
    PacketProtocol.ICMP -> Danger
    PacketProtocol.TCP -> Blue400
    PacketProtocol.UDP -> Teal300
    PacketProtocol.IPV6 -> Blue300
    PacketProtocol.OTHER -> TextMuted
}

private data class ChannelGroup(
    val channel: Int,
    val band: String,
    val count: Int,
)
