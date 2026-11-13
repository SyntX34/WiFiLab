package com.syntx34.wifilab.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syntx34.wifilab.domain.repository.ActivityRepository
import com.syntx34.wifilab.domain.repository.AuditRepository
import com.syntx34.wifilab.domain.repository.PacketCaptureRepository
import com.syntx34.wifilab.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val scanCooldownSeconds: Int = 30,
    val baselineCount: Int = 0,
    val captureCount: Int = 0,
    val auditCount: Int = 0,
    val activityCount: Int = 0,
    val completedLessons: Int = 0,
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val auditRepository: AuditRepository,
    private val packetCaptureRepository: PacketCaptureRepository,
    private val activityRepository: ActivityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.scanCooldownSeconds.collect { seconds ->
                _uiState.update { it.copy(scanCooldownSeconds = seconds) }
            }
        }
        viewModelScope.launch {
            auditRepository.observeBaseline().catch { emit(emptyList()) }.collect { baseline ->
                _uiState.update { it.copy(baselineCount = baseline.size) }
            }
        }
        viewModelScope.launch {
            auditRepository.observeRecentSummaries(100).catch { emit(emptyList()) }.map { it.size }
                .collect { count -> _uiState.update { it.copy(auditCount = count) } }
        }
        viewModelScope.launch {
            packetCaptureRepository.observeCaptures().catch { emit(emptyList()) }.collect { captures ->
                _uiState.update { it.copy(captureCount = captures.size) }
            }
        }
        viewModelScope.launch {
            activityRepository.observeRecent(200).catch { emit(emptyList()) }.collect { activities ->
                _uiState.update { it.copy(activityCount = activities.size) }
            }
        }
        viewModelScope.launch {
            settingsRepository.completedLessonIds.collect { completed ->
                _uiState.update { it.copy(completedLessons = completed.size) }
            }
        }
    }

    fun setScanCooldown(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.setScanCooldownSeconds(seconds)
            _uiState.update {
                it.copy(message = "Scan cooldown set to ${seconds} seconds.", error = null)
            }
        }
    }

    fun clearAudits() = runClear("Audit history removed") {
        auditRepository.clearReports()
    }

    fun clearBaseline() = runClear("Trusted baseline removed") {
        auditRepository.clearBaseline()
    }

    fun clearCaptures() = runClear("Imported captures deleted") {
        packetCaptureRepository.clearCaptures()
    }

    fun clearActivity() = runClear("Research activity cleared") {
        activityRepository.clear()
    }

    fun clearLearningProgress() = runClear("Learning progress reset") {
        settingsRepository.clearLearningProgress()
    }

    fun clearAll() = runClear("All local research data cleared") {
        auditRepository.clearReports()
        auditRepository.clearBaseline()
        packetCaptureRepository.clearCaptures()
        activityRepository.clear()
        settingsRepository.clearAllPreferences()
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun runClear(successMessage: String, action: suspend () -> Unit) {
        if (_uiState.value.busy) return
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, message = null, error = null) }
            try {
                action()
                _uiState.update { it.copy(busy = false, message = successMessage) }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        busy = false,
                        error = error.message ?: "The local data could not be cleared",
                    )
                }
            }
        }
    }
}
