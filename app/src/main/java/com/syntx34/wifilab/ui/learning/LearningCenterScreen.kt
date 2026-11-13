package com.syntx34.wifilab.ui.learning

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
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntx34.wifilab.domain.model.ResearchEntry
import com.syntx34.wifilab.domain.model.ResearchSection
import com.syntx34.wifilab.ui.components.DetailRow
import com.syntx34.wifilab.ui.components.EmptyState
import com.syntx34.wifilab.ui.components.GlassCard
import com.syntx34.wifilab.ui.components.InfoBanner
import com.syntx34.wifilab.ui.components.MiniProgress
import com.syntx34.wifilab.ui.components.SectionHeader
import com.syntx34.wifilab.ui.components.StatusPill
import com.syntx34.wifilab.ui.components.TinyBadge
import com.syntx34.wifilab.ui.theme.Blue400
import com.syntx34.wifilab.ui.theme.BorderColor
import com.syntx34.wifilab.ui.theme.Cyan400
import com.syntx34.wifilab.ui.theme.Navy850
import com.syntx34.wifilab.ui.theme.Navy900
import com.syntx34.wifilab.ui.theme.Success
import com.syntx34.wifilab.ui.theme.TextMuted
import com.syntx34.wifilab.ui.theme.TextPrimary
import com.syntx34.wifilab.ui.theme.TextSecondary
import com.syntx34.wifilab.ui.theme.Violet
import com.syntx34.wifilab.ui.theme.Warning

@Composable
fun LearningCenterScreen(
    viewModel: LearningCenterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedEntry = selectedId?.let(viewModel::entry)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (selectedEntry != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedId = null },
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
            LearningDetail(
                entry = selectedEntry,
                completed = selectedEntry.id in state.completedIds,
                onToggleCompleted = { completed ->
                    viewModel.setCompleted(selectedEntry, completed)
                },
                onClose = { selectedId = null },
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
                    text = "RESEARCH CENTER",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cyan400,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Understand the packet",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Terminology, protocol notes, guided exercises and practical defenses.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        item {
            LearningProgressCard(state)
        }

        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::updateQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search terms, protocols or lessons", color = TextMuted)
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
            SectionFilters(
                selected = state.section,
                onSelect = viewModel::setSection,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${state.results.size} resource${if (state.results.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                if (state.section != null || state.query.isNotBlank()) {
                    TextButton(
                        onClick = {
                            viewModel.updateQuery("")
                            viewModel.setSection(null)
                        },
                    ) {
                        Text("Clear filters", color = Cyan400)
                    }
                }
            }
        }

        if (state.results.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Search,
                    title = "No matching resources",
                    message = "Try a shorter search term or choose a different section.",
                    actionLabel = "Clear search",
                    onAction = {
                        viewModel.updateQuery("")
                        viewModel.setSection(null)
                    },
                )
            }
        } else {
            items(
                items = state.results,
                key = { it.id },
            ) { entry ->
                ResearchEntryCard(
                    entry = entry,
                    completed = entry.id in state.completedIds,
                    onClick = { selectedId = entry.id },
                )
            }
        }

        item {
            InfoBanner(
                title = "Everything is available offline",
                message = "This library is bundled with the application. Progress is stored in local preferences and can be removed from Settings.",
                color = Blue400,
            )
        }
    }
}

@Composable
private fun LearningProgressCard(state: LearningCenterUiState) {
    val completed = state.completedIds.size.coerceAtMost(state.totalEntries)
    val fraction = if (state.totalEntries == 0) 0f else completed.toFloat() / state.totalEntries
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Success.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(19.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Self paced learning path",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Text(
                    text = "$completed of ${state.totalEntries} resources completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            StatusPill(text = "${(fraction * 100).toInt()}%", color = Success)
        }
        Spacer(modifier = Modifier.height(11.dp))
        MiniProgress(fraction = fraction, color = Success)
    }
}

@Composable
private fun SectionFilters(
    selected: ResearchSection?,
    onSelect: (ResearchSection?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LearningChip(
            label = "All",
            selected = selected == null,
            onClick = { onSelect(null) },
        )
        ResearchSection.entries.forEach { section ->
            LearningChip(
                label = when (section) {
                    ResearchSection.TERMINOLOGY -> "Terms"
                    ResearchSection.PROTOCOL -> "Protocols"
                    ResearchSection.PACKET_LESSON -> "Lessons"
                    ResearchSection.EXERCISE -> "Exercises"
                    ResearchSection.RECOMMENDATION -> "Tips"
                },
                selected = selected == section,
                onClick = { onSelect(section) },
            )
        }
    }
}

@Composable
private fun LearningChip(
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
private fun ResearchEntryCard(
    entry: ResearchEntry,
    completed: Boolean,
    onClick: () -> Unit,
) {
    val sectionColor = sectionColor(entry.section)
    val sectionIcon = sectionIcon(entry.section)
    GlassCard(onClick = onClick, contentPadding = 15) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(sectionColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = sectionIcon,
                    contentDescription = null,
                    tint = sectionColor,
                    modifier = Modifier.size(19.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (completed) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Completed",
                            tint = Success,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Text(
                    text = entry.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    TinyBadge(
                        text = sectionShortLabel(entry.section),
                        color = sectionColor,
                    )
                    TinyBadge(
                        text = entry.difficulty.label,
                        color = TextMuted,
                    )
                    Text(
                        text = "${entry.estimatedMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LearningDetail(
    entry: ResearchEntry,
    completed: Boolean,
    onToggleCompleted: (Boolean) -> Unit,
    onClose: () -> Unit,
) {
    val sectionColor = sectionColor(entry.section)
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
            StatusPill(
                text = sectionShortLabel(entry.section),
                color = sectionColor,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close learning detail",
                    tint = TextMuted,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = entry.title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Cyan400,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = entry.overview,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(14.dp))
        GlassCard {
            DetailRow("Difficulty", entry.difficulty.label)
            DetailRow("Estimated time", "${entry.estimatedMinutes} minutes")
            DetailRow("Status", if (completed) "Completed" else "In progress", valueColor = if (completed) Success else TextSecondary)
        }
        if (entry.details.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            LearningListSection(
                title = "WHAT TO KNOW",
                items = entry.details,
                color = sectionColor,
                numbered = false,
            )
        }
        if (entry.keyPoints.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            LearningListSection(
                title = "KEY POINTS",
                items = entry.keyPoints,
                color = Success,
                numbered = false,
            )
        }
        if (entry.steps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            LearningListSection(
                title = "GUIDED STEPS",
                items = entry.steps,
                color = Cyan400,
                numbered = true,
            )
        }
        if (entry.reflectionQuestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            LearningListSection(
                title = "REFLECT",
                items = entry.reflectionQuestions,
                color = Warning,
                numbered = true,
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = { onToggleCompleted(!completed) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (completed) Navy900 else Cyan400,
                contentColor = if (completed) Cyan400 else Navy900,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (completed) "Mark incomplete" else "Mark completed")
        }
    }
}

@Composable
private fun LearningListSection(
    title: String,
    items: List<String>,
    color: Color,
    numbered: Boolean,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = color,
    )
    Spacer(modifier = Modifier.height(7.dp))
    items.forEachIndexed { index, item ->
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = if (numbered) "${index + 1}." else "·",
                style = MaterialTheme.typography.labelMedium,
                color = color,
                modifier = Modifier.width(22.dp),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = item,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

private fun sectionColor(section: ResearchSection): Color = when (section) {
    ResearchSection.TERMINOLOGY -> Cyan400
    ResearchSection.PROTOCOL -> Blue400
    ResearchSection.PACKET_LESSON -> Violet
    ResearchSection.EXERCISE -> Warning
    ResearchSection.RECOMMENDATION -> Success
}

private fun sectionIcon(section: ResearchSection): ImageVector = when (section) {
    ResearchSection.TERMINOLOGY -> Icons.Outlined.MenuBook
    ResearchSection.PROTOCOL -> Icons.Outlined.Lock
    ResearchSection.PACKET_LESSON -> Icons.Outlined.Analytics
    ResearchSection.EXERCISE -> Icons.Outlined.PlayArrow
    ResearchSection.RECOMMENDATION -> Icons.Outlined.CheckCircle
}

private fun sectionShortLabel(section: ResearchSection): String = when (section) {
    ResearchSection.TERMINOLOGY -> "Term"
    ResearchSection.PROTOCOL -> "Protocol"
    ResearchSection.PACKET_LESSON -> "Lesson"
    ResearchSection.EXERCISE -> "Exercise"
    ResearchSection.RECOMMENDATION -> "Tip"
}
