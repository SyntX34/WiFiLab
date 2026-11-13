package com.syntx34.wifilab.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syntx34.wifilab.domain.model.AuditSummary
import com.syntx34.wifilab.domain.model.ConnectionSnapshot
import com.syntx34.wifilab.domain.model.ResearchActivity
import com.syntx34.wifilab.domain.repository.ActivityRepository
import com.syntx34.wifilab.domain.repository.AuditRepository
import com.syntx34.wifilab.domain.repository.WifiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

data class DashboardUiState(
    val connection: ConnectionSnapshot = ConnectionSnapshot.unavailable,
    val lastAudit: AuditSummary? = null,
    val activities: List<ResearchActivity> = emptyList(),
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    wifiRepository: WifiRepository,
    auditRepository: AuditRepository,
    activityRepository: ActivityRepository,
) : ViewModel() {

    private val connectionFlow = wifiRepository.observeConnection()
        .catch { emit(ConnectionSnapshot.unavailable) }

    val uiState = combine(
        connectionFlow,
        auditRepository.observeRecentSummaries(1),
        activityRepository.observeRecent(5),
    ) { connection, audits, activities ->
        DashboardUiState(
            connection = connection,
            lastAudit = audits.firstOrNull(),
            activities = activities,
        )
    }.catch {
        emit(DashboardUiState())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )
}
