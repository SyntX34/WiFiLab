package com.syntx34.wifilab.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syntx34.wifilab.domain.model.ResearchActivityType
import com.syntx34.wifilab.domain.model.ScanOutcome
import com.syntx34.wifilab.domain.model.SignalBand
import com.syntx34.wifilab.domain.model.WifiNetwork
import com.syntx34.wifilab.domain.model.WifiSecurity
import com.syntx34.wifilab.domain.repository.SettingsRepository
import com.syntx34.wifilab.domain.repository.WifiRepository
import com.syntx34.wifilab.domain.usecase.RecordActivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScannerSort(val label: String) {
    SIGNAL("Signal"),
    NAME("Name"),
    CHANNEL("Channel"),
}

enum class SecurityFilter(val label: String) {
    ALL("All security"),
    SECURE("Protected"),
    OPEN("Open or WEP"),
}

data class ScannerUiState(
    val networks: List<WifiNetwork> = emptyList(),
    val visibleNetworks: List<WifiNetwork> = emptyList(),
    val isScanning: Boolean = false,
    val lastScanAt: Long? = null,
    val error: String? = null,
    val notice: String? = null,
    val cooldownRemainingSeconds: Int = 0,
    val query: String = "",
    val bandFilter: SignalBand? = null,
    val securityFilter: SecurityFilter = SecurityFilter.ALL,
    val channelFilter: Int? = null,
    val sort: ScannerSort = ScannerSort.SIGNAL,
)

@HiltViewModel
class WifiScannerViewModel @Inject constructor(
    private val wifiRepository: WifiRepository,
    private val settingsRepository: SettingsRepository,
    private val recordActivity: RecordActivityUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private var configuredCooldownSeconds = 30
    private var cooldownJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.scanCooldownSeconds.collect { configuredCooldownSeconds = it }
        }
    }

    fun startScan() {
        val state = _uiState.value
        if (state.isScanning) return
        if (state.cooldownRemainingSeconds > 0) {
            _uiState.update {
                it.copy(notice = "Android limits Wi Fi scans. Try again in ${state.cooldownRemainingSeconds} seconds.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null, notice = null) }
            when (val outcome = wifiRepository.scan()) {
                is ScanOutcome.Success -> {
                    _uiState.update {
                        it.copy(
                            networks = outcome.networks,
                            isScanning = false,
                            lastScanAt = outcome.scannedAt,
                            error = null,
                            notice = null,
                        )
                    }
                    applyFilters()
                    beginCooldown(configuredCooldownSeconds)
                    recordActivity(
                        type = ResearchActivityType.WIFI_SCAN,
                        title = "Wi Fi scan completed",
                        detail = "${outcome.networks.size} networks observed",
                    )
                }

                is ScanOutcome.PermissionRequired -> {
                    val message = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        "Nearby Wi-Fi and location permissions are required to scan networks."
                    } else {
                        "Location permission is required to scan Wi-Fi networks."
                    }
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            error = message,
                        )
                    }
                }

                is ScanOutcome.Throttled -> {
                    val seconds = ((outcome.retryAfterMillis + 999) / 1000).toInt().coerceAtLeast(1)
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            notice = "Android is throttling scans. A short cooldown is active.",
                        )
                    }
                    beginCooldown(seconds.coerceAtLeast(5))
                }

                is ScanOutcome.Unsupported -> {
                    _uiState.update {
                        it.copy(isScanning = false, error = outcome.reason)
                    }
                }

                is ScanOutcome.Failure -> {
                    _uiState.update {
                        it.copy(isScanning = false, error = outcome.reason)
                    }
                }
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        applyFilters()
    }

    fun setBandFilter(band: SignalBand?) {
        _uiState.update { it.copy(bandFilter = band) }
        applyFilters()
    }

    fun setSecurityFilter(filter: SecurityFilter) {
        _uiState.update { it.copy(securityFilter = filter) }
        applyFilters()
    }

    fun setChannelFilter(channel: Int?) {
        _uiState.update { it.copy(channelFilter = channel) }
        applyFilters()
    }

    fun setSort(sort: ScannerSort) {
        _uiState.update { it.copy(sort = sort) }
        applyFilters()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearNotice() {
        _uiState.update { it.copy(notice = null) }
    }

    private fun applyFilters() {
        _uiState.update { current ->
            val query = current.query.trim().lowercase()
            val filtered = current.networks.filter { network ->
                (query.isEmpty() ||
                    network.displayName.lowercase().contains(query) ||
                    network.bssid?.lowercase()?.contains(query) == true ||
                    network.security.label.lowercase().contains(query)) &&
                    (current.bandFilter == null || network.band == current.bandFilter) &&
                    (current.channelFilter == null || network.channel == current.channelFilter) &&
                    when (current.securityFilter) {
                        SecurityFilter.ALL -> true
                        SecurityFilter.SECURE -> network.security !in setOf(
                            WifiSecurity.OPEN,
                            WifiSecurity.WEP,
                            WifiSecurity.UNKNOWN,
                        )

                        SecurityFilter.OPEN -> network.security in setOf(
                            WifiSecurity.OPEN,
                            WifiSecurity.WEP,
                        )
                    }
            }
            val sorted = when (current.sort) {
                ScannerSort.SIGNAL -> filtered.sortedByDescending { it.rssi }
                ScannerSort.NAME -> filtered.sortedBy { it.displayName.lowercase() }
                ScannerSort.CHANNEL -> filtered.sortedWith(
                    compareBy<WifiNetwork> { it.channel }.thenByDescending { it.rssi },
                )
            }
            current.copy(visibleNetworks = sorted)
        }
    }

    private fun beginCooldown(seconds: Int) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            var remaining = seconds.coerceAtLeast(0)
            while (remaining > 0) {
                _uiState.update { it.copy(cooldownRemainingSeconds = remaining) }
                delay(1_000)
                remaining--
            }
            _uiState.update { it.copy(cooldownRemainingSeconds = 0) }
        }
    }
}
