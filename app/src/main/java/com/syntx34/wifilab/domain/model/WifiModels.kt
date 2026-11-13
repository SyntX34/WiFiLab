package com.syntx34.wifilab.domain.model

enum class WifiSecurity(val label: String) {
    OPEN("Open"),
    WEP("WEP"),
    WPA("WPA"),
    WPA2("WPA2"),
    WPA2_WPA3("WPA2 / WPA3"),
    WPA3("WPA3"),
    OWE("Enhanced Open"),
    ENTERPRISE("Enterprise"),
    UNKNOWN("Unknown"),
}

enum class SignalBand(val label: String) {
    BAND_2_4_GHZ("2.4 GHz"),
    BAND_5_GHZ("5 GHz"),
    BAND_6_GHZ("6 GHz"),
    UNKNOWN("Unknown"),
}

enum class SignalQuality(val label: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair"),
    WEAK("Weak"),
    POOR("Poor"),
}

enum class ConnectionState {
    CONNECTED,
    DISCONNECTED,
    UNAVAILABLE,
}

data class WifiNetwork(
    val ssid: String,
    val bssid: String?,
    val frequencyMhz: Int,
    val channel: Int,
    val channelWidthMhz: Int?,
    val rssi: Int,
    val signalLevel: Int,
    val signalQuality: SignalQuality,
    val band: SignalBand,
    val security: WifiSecurity,
    val capabilities: String,
    val wifiStandard: String?,
    val isHidden: Boolean,
) {
    val displayName: String
        get() = if (ssid.isBlank()) "Hidden network" else ssid
}

data class ConnectionSnapshot(
    val state: ConnectionState,
    val ssid: String?,
    val bssid: String?,
    val frequencyMhz: Int?,
    val rssi: Int?,
    val signalLevel: Int?,
    val linkSpeedMbps: Int?,
    val localIpv4: String?,
    val gatewayIpv4: String?,
    val dnsServers: List<String>,
    val security: WifiSecurity?,
    val isWifi: Boolean,
    val isMetered: Boolean?,
    val interfaceName: String?,
) {
    companion object {
        val unavailable = ConnectionSnapshot(
            state = ConnectionState.UNAVAILABLE,
            ssid = null,
            bssid = null,
            frequencyMhz = null,
            rssi = null,
            signalLevel = null,
            linkSpeedMbps = null,
            localIpv4 = null,
            gatewayIpv4 = null,
            dnsServers = emptyList(),
            security = null,
            isWifi = false,
            isMetered = null,
            interfaceName = null,
        )
    }
}

data class LocalNetworkContext(
    val localIpv4: String,
    val gatewayIpv4: String?,
    val prefixLength: Int?,
    val interfaceName: String?,
)

sealed interface ScanOutcome {
    data class Success(
        val networks: List<WifiNetwork>,
        val scannedAt: Long,
    ) : ScanOutcome

    data class PermissionRequired(
        val permissions: List<String>,
    ) : ScanOutcome

    data class Throttled(
        val retryAfterMillis: Long,
    ) : ScanOutcome

    data class Unsupported(
        val reason: String,
    ) : ScanOutcome

    data class Failure(
        val reason: String,
    ) : ScanOutcome
}
