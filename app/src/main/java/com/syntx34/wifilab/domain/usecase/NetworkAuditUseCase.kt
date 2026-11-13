package com.syntx34.wifilab.domain.usecase

import com.syntx34.wifilab.domain.model.AuditProgress
import com.syntx34.wifilab.domain.model.AuditResult
import com.syntx34.wifilab.domain.model.AuthorizedNetwork
import com.syntx34.wifilab.domain.model.NetworkAuditReport
import com.syntx34.wifilab.domain.repository.AuditRepository
import com.syntx34.wifilab.domain.repository.NetworkAuditService
import com.syntx34.wifilab.domain.security.AuditRecommendations
import com.syntx34.wifilab.domain.security.BaselineComparator
import com.syntx34.wifilab.domain.security.SubnetCalculator
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class NetworkAuditUseCase @Inject constructor(
    private val auditRepository: AuditRepository,
    private val auditService: NetworkAuditService,
) {
    suspend operator fun invoke(
        network: AuthorizedNetwork,
        authorizationConfirmed: Boolean,
        onProgress: (AuditProgress) -> Unit,
    ): AuditResult {
        if (!authorizationConfirmed) return AuditResult.AuthorizationRequired
        if (!SubnetCalculator.isPrivateIpv4(network.networkAddress) ||
            network.prefixLength !in SubnetCalculator.MIN_PREFIX..SubnetCalculator.MAX_PREFIX
        ) {
            return AuditResult.InvalidScope("WiFiLab audits only small private IPv4 ranges")
        }
        return try {
            val baseline = auditRepository.observeBaseline().first()
            val discovered = auditService.audit(network, onProgress)
            val comparison = BaselineComparator.compare(discovered, baseline)
            val recommendations = AuditRecommendations.build(
                devices = discovered,
                unexpectedDevices = comparison.unexpectedDevices,
                baselineUsed = comparison.baselineUsed,
                gatewayIpv4 = network.gatewayIpv4,
            )
            val report = NetworkAuditReport(
                scannedAt = System.currentTimeMillis(),
                cidr = network.cidr,
                devices = discovered,
                unexpectedDevices = comparison.unexpectedDevices,
                recommendations = recommendations,
                baselineUsed = comparison.baselineUsed,
            )
            auditRepository.saveReport(report)
            AuditResult.Success(report)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            AuditResult.Failure(error.message ?: "The network audit could not be completed")
        }
    }
}
