package com.syntx34.wifilab.data.wifi

import android.net.wifi.ScanResult
import android.os.Build
import com.syntx34.wifilab.domain.model.WifiNetwork
import com.syntx34.wifilab.domain.security.WifiCapabilityParser

object ScanResultMapper {

    fun map(result: ScanResult): WifiNetwork {
        val capabilities = result.capabilities.orEmpty()
        val rawSsid = result.SSID.orEmpty()
        val hidden = rawSsid.isBlank()
        val standard = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WifiCapabilityParser.wifiStandardName(result.wifiStandard)
        } else {
            null
        }
        return WifiNetwork(
            ssid = if (hidden) "" else rawSsid,
            bssid = result.BSSID?.takeIf { it.isNotBlank() },
            frequencyMhz = result.frequency,
            channel = WifiCapabilityParser.frequencyToChannel(result.frequency),
            channelWidthMhz = WifiCapabilityParser.channelWidthFromCode(result.channelWidth),
            rssi = result.level,
            signalLevel = WifiCapabilityParser.signalLevelFromRssi(result.level),
            signalQuality = WifiCapabilityParser.signalQualityFromRssi(result.level),
            band = WifiCapabilityParser.frequencyToBand(result.frequency),
            security = WifiCapabilityParser.securityFromCapabilities(capabilities),
            capabilities = capabilities,
            wifiStandard = standard,
            isHidden = hidden,
        )
    }
}
