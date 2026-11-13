package com.syntx34.wifilab.ui.attack

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntx34.wifilab.domain.model.AttackScenario
import com.syntx34.wifilab.domain.model.SimulationActor
import com.syntx34.wifilab.domain.model.SimulationStage
import com.syntx34.wifilab.ui.components.GlassCard
import com.syntx34.wifilab.ui.components.InfoBanner
import com.syntx34.wifilab.ui.components.SectionHeader
import com.syntx34.wifilab.ui.components.TinyBadge
import com.syntx34.wifilab.ui.theme.BorderColor
import com.syntx34.wifilab.ui.theme.Cyan400
import com.syntx34.wifilab.ui.theme.Danger
import com.syntx34.wifilab.ui.theme.Navy800
import com.syntx34.wifilab.ui.theme.Navy900
import com.syntx34.wifilab.ui.theme.Success
import com.syntx34.wifilab.ui.theme.TextMuted
import com.syntx34.wifilab.ui.theme.TextPrimary
import com.syntx34.wifilab.ui.theme.TextSecondary
import com.syntx34.wifilab.ui.theme.Violet
import com.syntx34.wifilab.ui.theme.Warning
import kotlinx.coroutines.delay

@Composable
fun AttackAnatomyScreen(
    viewModel: AttackAnatomyViewModel = hiltViewModel(),
) {
    val selectedId by viewModel.selectedId.collectAsStateWithLifecycle()
    val selected = viewModel.scenarios.firstOrNull { it.id == selectedId }
        ?: viewModel.scenarios.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(
                    text = "ISOLATED SIMULATIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Violet,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Attack anatomy",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Step through attacker models with fixed actors and synthetic packets. No real network is touched.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        item {
            InfoBanner(
                title = "Simulation only",
                message = "There is no packet injection, scanning, rogue access point or interception code in this module. The timeline reveals educational descriptions only.",
                color = Warning,
                icon = Icons.Outlined.Warning,
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                viewModel.scenarios.forEach { scenario ->
                    ScenarioChip(
                        title = scenario.title,
                        selected = scenario.id == selected?.id,
                        onClick = { viewModel.selectScenario(scenario.id) },
                    )
                }
            }
        }

        if (selected != null) {
            item {
                ScenarioOverview(selected)
            }
            item {
                SimulationWorkspace(selected = selected)
            }
            item {
                SectionHeader(
                    eyebrow = "DEFENSES",
                    title = "What actually helps",
                    subtitle = "Protective controls for a home or small office network.",
                )
            }
            item {
                GlassCard {
                    selected.defenses.forEachIndexed { index, defense ->
                        Row(
                            modifier = Modifier.padding(vertical = 5.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Success.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Success,
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = defense,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }
            item {
                InfoBanner(
                    title = "Key takeaway",
                    message = selected.takeaway,
                    color = Cyan400,
                )
            }
        }
    }
}

@Composable
private fun ScenarioOverview(scenario: AttackScenario) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Violet.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Violet,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
                Text(
                    text = scenario.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            TinyBadge(text = scenario.riskLevel, color = Warning)
        }
        Spacer(modifier = Modifier.height(13.dp))
        Text(
            text = scenario.concept,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}

@Composable
private fun AttackTopology(
    selected: AttackScenario,
    activeActor: SimulationActor,
) {
    val pulse = rememberInfiniteTransition(label = "topologyPulse")
    val glow by pulse.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "topologyGlow",
    )
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "SIMULATED ENVIRONMENT",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            TinyBadge(text = "NO TRAFFIC", color = Success)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActorNode(
                icon = Icons.Outlined.Computer,
                title = "Client",
                active = activeActor == SimulationActor.CLIENT,
                glow = glow,
                modifier = Modifier.weight(1f),
            )
            Connector()
            ActorNode(
                icon = Icons.Outlined.Router,
                title = "Network",
                active = activeActor in setOf(
                    SimulationActor.NETWORK,
                    SimulationActor.ACCESS_POINT,
                    SimulationActor.DNS,
                ),
                glow = glow,
                modifier = Modifier.weight(1f),
            )
            Connector()
            ActorNode(
                icon = Icons.Outlined.Warning,
                title = "Attacker",
                active = activeActor == SimulationActor.ATTACKER,
                glow = glow,
                accent = Danger,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Active stage focus: ${activeActor.label}",
            style = MaterialTheme.typography.bodySmall,
            color = Cyan400,
        )
    }
}

@Composable
private fun ActorNode(
    icon: ImageVector,
    title: String,
    active: Boolean,
    glow: Float,
    modifier: Modifier = Modifier,
    accent: Color = Cyan400,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) accent.copy(alpha = 0.12f) else Navy900.copy(alpha = 0.6f))
            .border(
                1.dp,
                if (active) accent.copy(alpha = glow) else BorderColor.copy(alpha = 0.6f),
                RoundedCornerShape(16.dp),
            )
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) accent else TextMuted,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) TextPrimary else TextSecondary,
            maxLines = 1,
        )
    }
}

@Composable
private fun Connector() {
    Text(
        text = "···",
        style = MaterialTheme.typography.titleLarge,
        color = TextMuted,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun SimulationWorkspace(selected: AttackScenario) {
    var stepCount by rememberSaveable(selected.id) { mutableIntStateOf(0) }
    var running by remember(selected.id) { mutableStateOf(false) }

    LaunchedEffect(running, selected.id) {
        if (running) {
            while (running && stepCount < selected.stages.size) {
                delay(900)
                stepCount += 1
                if (stepCount >= selected.stages.size) {
                    running = false
                }
            }
        }
    }

    val activeActor = selected.stages
        .getOrNull((stepCount - 1).coerceAtLeast(0))
        ?.actor
        ?: SimulationActor.NETWORK

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AttackTopology(
            selected = selected,
            activeActor = activeActor,
        )
        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Simulation timeline",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = if (running) {
                            "Revealing stage ${stepCount.coerceAtMost(selected.stages.size)} of ${selected.stages.size}"
                        } else if (stepCount == 0) {
                            "Run the model to reveal each stage."
                        } else {
                            "${stepCount} of ${selected.stages.size} stages revealed"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                Button(
                    onClick = {
                        if (stepCount >= selected.stages.size) {
                            stepCount = 0
                            running = true
                        } else {
                            running = true
                        }
                    },
                    enabled = !running,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan400,
                        contentColor = Navy900,
                        disabledContainerColor = Navy800,
                        disabledContentColor = TextMuted,
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    if (running) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            color = TextMuted,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = if (stepCount == 0) {
                                Icons.Outlined.PlayArrow
                            } else {
                                Icons.Outlined.Refresh
                            },
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(if (stepCount == 0) "Run" else "Replay")
                    }
                }
            }
        }
        if (stepCount > 0) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                selected.stages.take(stepCount).forEachIndexed { index, stage ->
                    AnimatedVisibility(visible = true) {
                        StageCard(
                            index = index,
                            stage = stage,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StageCard(
    index: Int,
    stage: SimulationStage,
) {
    GlassCard(contentPadding = 14) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Cyan400.copy(alpha = 0.11f))
                    .border(1.dp, Cyan400.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Cyan400,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Spacer(modifier = Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stage.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TinyBadge(text = stage.actor.label, color = Violet)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stage.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun ScenarioChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Violet.copy(alpha = 0.16f) else Navy900.copy(alpha = 0.6f))
            .border(
                1.dp,
                if (selected) Violet.copy(alpha = 0.65f) else BorderColor,
                RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Violet else TextSecondary,
        )
    }
}
