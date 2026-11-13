package com.syntx34.wifilab.ui.auditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syntx34.wifilab.domain.model.AuditProgress
import com.syntx34.wifilab.domain.model.AuditResult
import com.syntx34.wifilab.domain.model.BaselineDevice
import com.syntx34.wifilab.domain.model.LocalNetworkContext
import com.syntx34.wifilab.domain.model.NetworkAuditReport
import com.syntx34.wifilab.domain.model.ResearchActivityType
import com.syntx34.wifilab.domain.repository.AuditRepository
import com.syntx34.wifilab.domain.repository.WifiRepository
import com.syntx34.wifilab.domain.security.SubnetCalculator
import com.syntx34.wifilab.domain.usecase.NetworkAuditUseCase
import com.syntx34.wifilab.domain.usecase.RecordActivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuditorUiState(
    val subnetInput: String = "",
    val detection: LocalNetworkContext? = null,
    val authorizationConfirmed: Boolean = false,
    val isScanning: Boolean = false,
    val progress: AuditProgress? = null,
    val report: NetworkAuditReport? = null,
    val baseline: List<BaselineDevice> = emptyList(),
    val error: String? = null,
    val notice: String? = null,
)

@HiltViewModel
class NetworkAuditorViewModel @Inject constructor(
    private val wifiRepository: WifiRepository,
    private val auditRepository: AuditRepository,
    private val networkAudit: NetworkAuditUseCase,
    private val recordActivity: RecordActivityUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditorUiState())
    val uiState: StateFlow<AuditorUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        refreshNetworkContext()
        viewModelScope.launch {
            auditRepository.observeBaseline().collect { baseline ->
                _uiState.update { it.copy(baseline = baseline) }
            }
        }
    }

    fun refreshNetworkContext() {
        val context = wifiRepository.localNetworkContext()
        val detected = context?.let {
            SubnetCalculator.fromLocalAddress(
                localIpv4 = it.localIpv4,
                gatewayIpv4 = it.gatewayIpv4,
                prefixLength = it.prefixLength,
                interfaceName = it.interfaceName,
            )
        }
        _uiState.update { state ->
            state.copy(
                detection = context,
                subnetInput = if (detected != null) detected.cidr else state.subnetInput,
            )
        }
    }

    fun updateSubnet(value: String) {
        _uiState.update { it.copy(subnetInput = value, error = null) }
    }

    fun setAuthorizationConfirmed(confirmed: Boolean) {
        _uiState.update { it.copy(authorizationConfirmed = confirmed) }
    }

    fun startAudit() {
        if (scanJob?.isActive == true) return
        val state = _uiState.value
        if (!state.authorizationConfirmed) {
            _uiState.update {
                it.copy(error = "Confirm that you own or are authorized to audit this network first.")
            }
            return
        }
        val parsed = SubnetCalculator.parseCidr(state.subnetInput).getOrElse { error ->
            _uiState.update { it.copy(error = error.message) }
            return
        }
        val detection = state.detection
        val gateway = detection?.gatewayIpv4?.takeIf {
            SubnetCalculator.networkAddress(detection.localIpv4, parsed.prefixLength) ==
                parsed.networkAddress
        }
        val network = parsed.copy(
            localIpv4 = detection?.localIpv4 ?: parsed.localIpv4,
            gatewayIpv4 = gateway,
            interfaceName = detection?.interfaceName,
        )
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScanning = true,
                    progress = AuditProgress(0, SubnetCalculator.hostCount(network.prefixLength), null),
                    report = null,
                    error = null,
                    notice = null,
                )
            }
            when (
                val result = networkAudit(
                    network = network,
                    authorizationConfirmed = true,
                    onProgress = { progress ->
                        _uiState.update { it.copy(progress = progress) }
                    },
                )
            ) {
                is AuditResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            progress = null,
                            report = result.report,
                        )
                    }
                    recordActivity(
                        type = ResearchActivityType.NETWORK_AUDIT,
                        title = "Local network audit",
                        detail = "${result.report.devices.size} devices on ${result.report.cidr}, ${result.report.unexpectedDevices.size} unexpected",
                    )
                }

                is AuditResult.AuthorizationRequired -> {
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            progress = null,
                            error = "Authorization confirmation is required.",
                        )
                    }
                }

                is AuditResult.InvalidScope -> {
                    _uiState.update {
                        it.copy(isScanning = false, progress = null, error = result.reason)
                    }
                }

                is AuditResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            progress = null,
                            error = result.reason,
                        )
                    }
                }
            }
        }
    }

    fun cancelAudit() {
        scanJob?.cancel()
        scanJob = null
        _uiState.update {
            it.copy(
                isScanning = false,
                progress = null,
                notice = "The local audit was cancelled. No report was saved.",
            )
        }
    }

    fun saveBaseline() {
        val devices = _uiState.value.report?.devices ?: return
        if (devices.isEmpty()) {
            _uiState.update { it.copy(error = "There are no discovered devices to save as a baseline.") }
            return
        }
        viewModelScope.launch {
            auditRepository.saveBaseline(devices)
            _uiState.update {
                it.copy(
                    notice = "${devices.size} devices saved as the trusted local baseline. Future audits will flag additions.",
                )
            }
            recordActivity(
                type = ResearchActivityType.NETWORK_AUDIT,
                title = "Trusted baseline updated",
                detail = "${devices.size} devices approved locally",
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun dismissNotice() {
        _uiState.update { it.copy(notice = null) }
    }
}
