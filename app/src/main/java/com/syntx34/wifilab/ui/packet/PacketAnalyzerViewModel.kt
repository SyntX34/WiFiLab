package com.syntx34.wifilab.ui.packet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syntx34.wifilab.domain.model.PacketInfo
import com.syntx34.wifilab.domain.model.PacketProtocol
import com.syntx34.wifilab.domain.model.ParsedCapture
import com.syntx34.wifilab.domain.model.ResearchActivityType
import com.syntx34.wifilab.domain.model.StoredCapture
import com.syntx34.wifilab.domain.repository.PacketCaptureRepository
import com.syntx34.wifilab.domain.usecase.RecordActivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PacketAnalyzerUiState(
    val storedCaptures: List<StoredCapture> = emptyList(),
    val capture: ParsedCapture? = null,
    val selectedCaptureId: Long? = null,
    val isDemo: Boolean = false,
    val importing: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val protocolFilter: PacketProtocol? = null,
    val visiblePackets: List<PacketInfo> = emptyList(),
    val matchingPackets: Int = 0,
    val selectedPacket: PacketInfo? = null,
)

@HiltViewModel
class PacketAnalyzerViewModel @Inject constructor(
    private val repository: PacketCaptureRepository,
    private val recordActivity: RecordActivityUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PacketAnalyzerUiState())
    val uiState: StateFlow<PacketAnalyzerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeCaptures()
                .catch { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Stored captures could not be read")
                    }
                }
                .collect { captures ->
                    _uiState.update { it.copy(storedCaptures = captures) }
                }
        }
    }

    fun importCapture(uri: String) {
        if (_uiState.value.importing) return
        viewModelScope.launch {
            _uiState.update { it.copy(importing = true, error = null) }
            try {
                val capture = repository.importCapture(uri, null)
                _uiState.update {
                    it.copy(
                        importing = false,
                        capture = capture,
                        selectedCaptureId = null,
                        isDemo = false,
                        query = "",
                        protocolFilter = null,
                    )
                }
                applyFilters()
                recordActivity(
                    type = ResearchActivityType.PACKET_ANALYSIS,
                    title = "Capture imported",
                    detail = "${capture.fileName}: ${capture.summary.packetCount} packets, ${capture.summary.eapolCount} EAPOL",
                )
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        importing = false,
                        error = error.message ?: "The capture could not be parsed",
                    )
                }
            }
        }
    }

    fun openCapture(id: Long) {
        if (_uiState.value.loading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                val capture = repository.loadCapture(id)
                if (capture == null) {
                    _uiState.update {
                        it.copy(loading = false, error = "That capture is no longer available on this device.")
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            capture = capture,
                            selectedCaptureId = id,
                            isDemo = false,
                            query = "",
                            protocolFilter = null,
                            selectedPacket = null,
                        )
                    }
                    applyFilters()
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = error.message ?: "The capture could not be opened",
                    )
                }
            }
        }
    }

    fun loadDemo() {
        if (_uiState.value.loading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                val capture = repository.loadDemoCapture()
                _uiState.update {
                    it.copy(
                        loading = false,
                        capture = capture,
                        selectedCaptureId = null,
                        isDemo = true,
                        query = "",
                        protocolFilter = null,
                        selectedPacket = null,
                    )
                }
                applyFilters()
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(loading = false, error = error.message ?: "The demo could not be generated")
                }
            }
        }
    }

    fun closeCapture() {
        _uiState.update {
            it.copy(
                capture = null,
                selectedCaptureId = null,
                isDemo = false,
                query = "",
                protocolFilter = null,
                visiblePackets = emptyList(),
                matchingPackets = 0,
                selectedPacket = null,
            )
        }
    }

    fun deleteCapture(id: Long) {
        viewModelScope.launch {
            repository.deleteCapture(id)
            if (_uiState.value.selectedCaptureId == id) {
                closeCapture()
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        applyFilters()
    }

    fun setProtocolFilter(protocol: PacketProtocol?) {
        _uiState.update { it.copy(protocolFilter = protocol) }
        applyFilters()
    }

    fun selectPacket(packet: PacketInfo?) {
        _uiState.update { it.copy(selectedPacket = packet) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun applyFilters() {
        _uiState.update { current ->
            val capture = current.capture ?: return@update current
            val query = current.query.trim().lowercase()
            val matched = capture.packets.filter { packet ->
                (current.protocolFilter == null || packet.protocol == current.protocolFilter) &&
                    (query.isEmpty() ||
                        packet.source.lowercase().contains(query) ||
                        packet.destination.lowercase().contains(query) ||
                        packet.summary.lowercase().contains(query) ||
                        packet.protocol.label.lowercase().contains(query) ||
                        packet.eapol?.messageLabel?.lowercase()?.contains(query) == true)
            }
            current.copy(
                visiblePackets = matched.take(MAX_VISIBLE_PACKETS),
                matchingPackets = matched.size,
            )
        }
    }

    companion object {
        const val MAX_VISIBLE_PACKETS = 2_000
    }
}
