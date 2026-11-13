package com.syntx34.wifilab.ui.password

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntx34.wifilab.domain.model.EntropyAnalysis
import com.syntx34.wifilab.domain.model.PasswordComparison
import com.syntx34.wifilab.domain.model.PasswordStrength
import com.syntx34.wifilab.domain.model.SyntheticRecovery
import com.syntx34.wifilab.ui.components.DetailRow
import com.syntx34.wifilab.ui.components.EntropyGauge
import com.syntx34.wifilab.ui.components.GlassCard
import com.syntx34.wifilab.ui.components.InfoBanner
import com.syntx34.wifilab.ui.components.MiniProgress
import com.syntx34.wifilab.ui.components.SectionHeader
import com.syntx34.wifilab.ui.components.StatusPill
import com.syntx34.wifilab.ui.components.TinyBadge
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
fun PasswordLabScreen(
    viewModel: PasswordLabViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(
                    text = "PASSWORD ENTROPY LAB",
                    style = MaterialTheme.typography.labelSmall,
                    color = Warning,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Measure the unknown",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Explore length, randomness and dictionary patterns with synthetic data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        item {
            InfoBanner(
                title = "Use a sample, not a real password",
                message = "The field is evaluated in memory only, but this is an educational estimate and not a security audit. Close the screen to discard it.",
                color = Warning,
                icon = Icons.Outlined.Info,
            )
        }

        item {
            GlassCard {
                Text(
                    text = "Entropy estimator",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(11.dp))
                OutlinedTextField(
                    value = state.input,
                    onValueChange = viewModel::updateInput,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Sample passphrase") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = if (passwordVisible) "Hide text" else "Show text",
                                tint = TextMuted,
                            )
                        }
                    },
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
                Spacer(modifier = Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    TextButton(onClick = viewModel::useExample) {
                        Text("Use synthetic example", color = Cyan400)
                    }
                    TextButton(onClick = viewModel::clearInput) {
                        Text("Clear", color = TextSecondary)
                    }
                }
            }
        }

        state.analysis?.let { analysis ->
            item {
                EntropyResultCard(analysis)
            }
            if (analysis.patternWarnings.isNotEmpty()) {
                item {
                    GlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = Warning,
                                modifier = Modifier.size(19.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pattern observations",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        analysis.patternWarnings.forEach { warning ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Text(
                                    text = "·",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Warning,
                                    modifier = Modifier.width(13.dp),
                                )
                                Text(
                                    text = warning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                )
                            }
                        }
                        if (analysis.dictionaryMatch != null) {
                            Spacer(modifier = Modifier.height(7.dp))
                            Text(
                                text = "Dictionary match: ${analysis.dictionaryMatch}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Warning,
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                eyebrow = "COMPARISON",
                title = "Why length and randomness win",
                subtitle = "At an illustrative offline rate of 10 billion guesses per second.",
            )
        }

        state.comparisons.forEach { comparison ->
            item(key = comparison.label) {
                ComparisonCard(comparison)
            }
        }

        item {
            SectionHeader(
                eyebrow = "SYNTHETIC RECOVERY",
                title = "Offline guessing drill",
                subtitle = "Generated locally. No captured or user secret is used.",
                actionLabel = "Regenerate",
                onAction = viewModel::regenerateSimulations,
            )
        }

        item {
            InfoBanner(
                title = "What the simulation means",
                message = "Time estimates assume uniform random guesses and an attacker who can test 10 billion guesses per second against an offline representation. Real hashing, hardware and rate limits change this substantially.",
                color = Blue400,
                icon = Icons.Outlined.Info,
            )
        }

        state.simulations.forEach { simulation ->
            item(key = simulation.label) {
                SyntheticRecoveryCard(simulation)
            }
        }

        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Defensive summary",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(9.dp))
                listOf(
                    "Prefer long randomly generated secrets stored in a password manager.",
                    "Never reuse a password between services.",
                    "Enable multi factor authentication where available.",
                    "Length is usually more valuable than forced symbol substitutions.",
                    "Treat this estimate as a teaching aid, not a guarantee.",
                ).forEach { point ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text("·", color = Cyan400, modifier = Modifier.width(13.dp))
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = viewModel::regenerateSimulations,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy800,
                        contentColor = Cyan400,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text("New generated examples")
                }
            }
        }
    }
}

@Composable
private fun EntropyResultCard(analysis: EntropyAnalysis) {
    val color = strengthColor(analysis.strength)
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EntropyGauge(
                bits = analysis.effectiveBits,
                color = color,
                centerLabel = "%.1f".format(analysis.effectiveBits),
                centerCaption = "effective bits",
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                StatusPill(text = analysis.strength.label, color = color)
                Spacer(modifier = Modifier.height(10.dp))
                DetailRow("Length", analysis.passwordLength.toString())
                DetailRow("Character pool", analysis.poolSize.toString())
                DetailRow("Unique characters", analysis.uniqueCharacters.toString())
                DetailRow("Raw entropy", "%.1f bits".format(analysis.rawBits))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        GlassCard(contentPadding = 12) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SIMULATED OFFLINE TIME",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = analysis.crackTime.display,
                        style = MaterialTheme.typography.titleMedium,
                        color = color,
                    )
                }
                TinyBadge(text = "10B guesses/s", color = Blue400)
            }
            Spacer(modifier = Modifier.height(8.dp))
            MiniProgress(
                fraction = (analysis.effectiveBits / 128.0).toFloat(),
                color = color,
            )
        }
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = if (analysis.dictionaryMatch != null) {
                "The raw formula overstates this password because it contains a common pattern."
            } else {
                "Effective entropy accounts for repeated blocks and common patterns detected by this laboratory."
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
    }
}

@Composable
private fun ComparisonCard(comparison: PasswordComparison) {
    GlassCard(contentPadding = 14) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comparison.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = comparison.example,
                    style = MaterialTheme.typography.labelMedium,
                    color = Cyan400,
                    fontFamily = FontFamily.Monospace,
                )
            }
            TinyBadge(text = "%.0f bits".format(comparison.bits), color = Cyan400)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = comparison.note,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = comparison.crackTimeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Warning,
            )
        }
        Spacer(modifier = Modifier.height(7.dp))
        MiniProgress(
            fraction = (comparison.bits / 128.0).toFloat(),
            color = Blue400,
        )
    }
}

@Composable
private fun SyntheticRecoveryCard(simulation: SyntheticRecovery) {
    GlassCard(contentPadding = 14) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Warning.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "SIM",
                    style = MaterialTheme.typography.labelSmall,
                    color = Warning,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = simulation.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Text(
                    text = simulation.patternDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Navy900)
                .padding(11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Generated target",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = simulation.generatedPassword,
                style = MaterialTheme.typography.labelMedium,
                color = Cyan400,
                fontFamily = FontFamily.Monospace,
            )
        }
        Spacer(modifier = Modifier.height(9.dp))
        DetailRow("Search space", "%.1f bits".format(simulation.searchSpaceBits))
        DetailRow("Simulated exhaustion", simulation.simulatedTimeLabel, valueColor = Warning)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = simulation.explanation,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
    }
}

private fun strengthColor(strength: PasswordStrength): Color = when (strength) {
    PasswordStrength.VERY_WEAK -> Danger
    PasswordStrength.WEAK -> Warning
    PasswordStrength.MODERATE -> Warning
    PasswordStrength.STRONG -> Success
    PasswordStrength.VERY_STRONG -> Cyan400
}
