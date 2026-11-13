package com.syntx34.wifilab.ui.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syntx34.wifilab.domain.model.ResearchActivityType
import com.syntx34.wifilab.domain.model.ResearchEntry
import com.syntx34.wifilab.domain.model.ResearchSection
import com.syntx34.wifilab.domain.repository.ResearchRepository
import com.syntx34.wifilab.domain.repository.SettingsRepository
import com.syntx34.wifilab.domain.usecase.RecordActivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LearningCenterUiState(
    val query: String = "",
    val section: ResearchSection? = null,
    val results: List<ResearchEntry> = emptyList(),
    val completedIds: Set<String> = emptySet(),
    val totalEntries: Int = 0,
)

@HiltViewModel
class LearningCenterViewModel @Inject constructor(
    private val repository: ResearchRepository,
    private val settingsRepository: SettingsRepository,
    private val recordActivity: RecordActivityUseCase,
) : ViewModel() {

    private val allEntries = repository.entries()
    private var completedIds: Set<String> = emptySet()
    private var query: String = ""
    private var section: ResearchSection? = null

    private val _uiState = MutableStateFlow(
        LearningCenterUiState(
            results = allEntries,
            totalEntries = allEntries.size,
        ),
    )
    val uiState: StateFlow<LearningCenterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.completedLessonIds.collect { completed ->
                completedIds = completed
                refresh()
            }
        }
    }

    fun updateQuery(value: String) {
        query = value
        refresh()
    }

    fun setSection(value: ResearchSection?) {
        section = value
        refresh()
    }

    fun entry(id: String): ResearchEntry? = allEntries.firstOrNull { it.id == id }

    fun setCompleted(entry: ResearchEntry, completed: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLessonCompleted(entry.id, completed)
            if (completed) {
                recordActivity(
                    type = ResearchActivityType.LEARNING,
                    title = "Learning milestone",
                    detail = entry.title,
                )
            }
        }
    }

    private fun refresh() {
        val results = repository.search(query, section)
        _uiState.update {
            it.copy(
                query = query,
                section = section,
                results = results,
                completedIds = completedIds,
                totalEntries = allEntries.size,
            )
        }
    }
}
