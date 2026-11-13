package com.syntx34.wifilab.data.repository

import com.syntx34.wifilab.data.local.dao.AuditDao
import com.syntx34.wifilab.data.local.dao.BaselineDao
import com.syntx34.wifilab.data.local.entity.AuditDeviceEntity
import com.syntx34.wifilab.data.local.entity.AuditSessionEntity
import com.syntx34.wifilab.data.local.entity.BaselineDeviceEntity
import com.syntx34.wifilab.domain.model.AuditSummary
import com.syntx34.wifilab.domain.model.BaselineDevice
import com.syntx34.wifilab.domain.model.DiscoveredDevice
import com.syntx34.wifilab.domain.model.NetworkAuditReport
import com.syntx34.wifilab.domain.repository.AuditRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AuditRepositoryImpl @Inject constructor(
    private val auditDao: AuditDao,
    private val baselineDao: BaselineDao,
) : AuditRepository {

    override fun observeRecentSummaries(limit: Int): Flow<List<AuditSummary>> =
        auditDao.observeRecentSessions(limit).map { sessions ->
            sessions.map { session ->
                AuditSummary(
                    id = session.id,
                    scannedAt = session.scannedAt,
                    cidr = session.cidr,
                    deviceCount = session.deviceCount,
                    unexpectedCount = session.unexpectedCount,
                    baselineUsed = session.baselineUsed,
                )
            }
        }

    override fun observeBaseline(): Flow<List<BaselineDevice>> =
        baselineDao.observeAll().map { devices ->
            devices.map { entity ->
                BaselineDevice(
                    ipAddress = entity.ipAddress,
                    hostname = entity.hostname,
                    serviceNames = decodeList(entity.serviceNames),
                    approvedAt = entity.approvedAt,
                )
            }
        }

    override suspend fun saveReport(report: NetworkAuditReport): Long {
        val unexpectedAddresses = report.unexpectedDevices.map { it.ipAddress }.toSet()
        val session = AuditSessionEntity(
            scannedAt = report.scannedAt,
            cidr = report.cidr,
            deviceCount = report.devices.size,
            unexpectedCount = report.unexpectedDevices.size,
            baselineUsed = report.baselineUsed,
        )
        val devices = report.devices.map { device ->
            AuditDeviceEntity(
                sessionId = 0,
                ipAddress = device.ipAddress,
                hostname = device.hostname,
                serviceNames = encodeList(device.serviceNames),
                openPorts = device.openPorts.joinToString(","),
                isGateway = device.isGateway,
                unexpected = device.ipAddress in unexpectedAddresses,
                responseKind = device.responseKind.name,
                firstSeenAt = device.firstSeenAt,
            )
        }
        return auditDao.saveSession(session, devices)
    }

    override suspend fun saveBaseline(devices: List<DiscoveredDevice>) {
        val now = System.currentTimeMillis()
        val entities = devices
            .distinctBy { it.identity }
            .map { device ->
                BaselineDeviceEntity(
                    identity = device.identity,
                    ipAddress = device.ipAddress,
                    hostname = device.hostname,
                    serviceNames = encodeList(device.serviceNames),
                    approvedAt = now,
                )
            }
        baselineDao.replaceAll(entities)
    }

    override suspend fun clearReports() {
        auditDao.deleteAllSessions()
    }

    override suspend fun clearBaseline() {
        baselineDao.deleteAll()
    }

    private fun encodeList(values: List<String>): String = values.joinToString("|")

    private fun decodeList(value: String): List<String> =
        value.split("|").map { it.trim() }.filter { it.isNotEmpty() }
}
