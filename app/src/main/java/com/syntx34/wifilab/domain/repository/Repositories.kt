package com.syntx34.wifilab.domain.repository

import com.syntx34.wifilab.domain.model.AuditProgress
import com.syntx34.wifilab.domain.model.AuthorizedNetwork
import com.syntx34.wifilab.domain.model.BaselineDevice
import com.syntx34.wifilab.domain.model.ConnectionSnapshot
import com.syntx34.wifilab.domain.model.DiscoveredDevice
import com.syntx34.wifilab.domain.model.LocalNetworkContext
import com.syntx34.wifilab.domain.model.AttackScenario
import com.syntx34.wifilab.domain.model.AuditSummary
import com.syntx34.wifilab.domain.model.NetworkAuditReport
import com.syntx34.wifilab.domain.model.ParsedCapture
import com.syntx34.wifilab.domain.model.ProtocolExplanation
import com.syntx34.wifilab.domain.model.ResearchActivity
import com.syntx34.wifilab.domain.model.ResearchActivityType
import com.syntx34.wifilab.domain.model.ResearchEntry
import com.syntx34.wifilab.domain.model.ResearchSection
import com.syntx34.wifilab.domain.model.ScanOutcome
import com.syntx34.wifilab.domain.model.StoredCapture
import com.syntx34.wifilab.domain.model.WifiNetwork
import kotlinx.coroutines.flow.Flow

interface WifiRepository {
    fun observeConnection(): Flow<ConnectionSnapshot>
    suspend fun scan(): ScanOutcome
    fun cachedNetworks(): List<WifiNetwork>
    fun localNetworkContext(): LocalNetworkContext?
}

interface NetworkAuditService {
    suspend fun audit(
        network: AuthorizedNetwork,
        onProgress: (AuditProgress) -> Unit,
    ): List<DiscoveredDevice>
}

interface AuditRepository {
    fun observeRecentSummaries(limit: Int = 10): Flow<List<AuditSummary>>
    fun observeBaseline(): Flow<List<BaselineDevice>>
    suspend fun saveReport(report: NetworkAuditReport): Long
    suspend fun saveBaseline(devices: List<DiscoveredDevice>)
    suspend fun clearReports()
    suspend fun clearBaseline()
}

interface ActivityRepository {
    fun observeRecent(limit: Int): Flow<List<ResearchActivity>>
    suspend fun log(type: ResearchActivityType, title: String, detail: String)
    suspend fun clear()
}

interface PacketCaptureRepository {
    fun observeCaptures(): Flow<List<StoredCapture>>
    suspend fun importCapture(uri: String, displayName: String?): ParsedCapture
    suspend fun loadCapture(id: Long): ParsedCapture?
    suspend fun loadDemoCapture(): ParsedCapture
    suspend fun deleteCapture(id: Long)
    suspend fun clearCaptures()
}

interface SettingsRepository {
    val scanCooldownSeconds: Flow<Int>
    val completedLessonIds: Flow<Set<String>>
    suspend fun setScanCooldownSeconds(seconds: Int)
    suspend fun setLessonCompleted(lessonId: String, completed: Boolean)
    suspend fun clearLearningProgress()
    suspend fun clearAllPreferences()
}

interface ResearchRepository {
    fun entries(section: ResearchSection? = null): List<ResearchEntry>
    fun search(query: String, section: ResearchSection? = null): List<ResearchEntry>
    fun entry(id: String): ResearchEntry?
    fun protocols(): List<ProtocolExplanation>
    fun attacks(): List<AttackScenario>
    fun attack(id: String): AttackScenario?
}
