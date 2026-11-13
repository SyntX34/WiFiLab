package com.syntx34.wifilab.domain.model

enum class RecommendationSeverity(val label: String) {
    INFO("Information"),
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
}

data class SecurityRecommendation(
    val title: String,
    val detail: String,
    val severity: RecommendationSeverity,
)

enum class DeviceResponseKind {
    GATEWAY,
    PING,
    SERVICE,
    UNKNOWN,
}

data class DiscoveredDevice(
    val ipAddress: String,
    val hostname: String?,
    val serviceNames: List<String>,
    val openPorts: List<Int>,
    val isGateway: Boolean,
    val responseKind: DeviceResponseKind,
    val firstSeenAt: Long,
) {
    val identity: String
        get() = "$ipAddress|${hostname.orEmpty()}"
}

data class AuthorizedNetwork(
    val localIpv4: String,
    val gatewayIpv4: String?,
    val networkAddress: String,
    val prefixLength: Int,
    val interfaceName: String?,
) {
    val cidr: String
        get() = "$networkAddress/$prefixLength"
}

data class BaselineDevice(
    val ipAddress: String,
    val hostname: String?,
    val serviceNames: List<String>,
    val approvedAt: Long,
) {
    val identity: String
        get() = "$ipAddress|${hostname.orEmpty()}"
}

data class AuditProgress(
    val completedHosts: Int,
    val totalHosts: Int,
    val currentAddress: String?,
) {
    val fraction: Float
        get() = if (totalHosts <= 0) 0f else completedHosts.toFloat() / totalHosts.toFloat()
}

data class NetworkAuditReport(
    val scannedAt: Long,
    val cidr: String,
    val devices: List<DiscoveredDevice>,
    val unexpectedDevices: List<DiscoveredDevice>,
    val recommendations: List<SecurityRecommendation>,
    val baselineUsed: Boolean,
)

data class AuditSummary(
    val id: Long,
    val scannedAt: Long,
    val cidr: String,
    val deviceCount: Int,
    val unexpectedCount: Int,
    val baselineUsed: Boolean,
)

sealed interface AuditResult {
    data class Success(val report: NetworkAuditReport) : AuditResult
    data object AuthorizationRequired : AuditResult
    data class InvalidScope(val reason: String) : AuditResult
    data class Failure(val reason: String) : AuditResult
}
