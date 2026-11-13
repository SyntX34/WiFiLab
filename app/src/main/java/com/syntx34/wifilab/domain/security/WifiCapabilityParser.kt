package com.syntx34.wifilab.domain.security

import com.syntx34.wifilab.domain.model.SignalBand
import com.syntx34.wifilab.domain.model.SignalQuality
import com.syntx34.wifilab.domain.model.WifiSecurity
import java.util.Locale

object WifiCapabilityParser {

    fun securityFromCapabilities(capabilities: String): WifiSecurity {
        if (capabilities.isBlank()) return WifiSecurity.UNKNOWN
        val caps = capabilities.uppercase(Locale.US)
        val hasSae = caps.contains("SAE")
        val hasOwe = caps.contains("OWE") || caps.contains("OWE-TRANSITION")
        val hasEnterprise = caps.contains("EAP") || caps.contains("802.1X")
        val hasWpa2 = caps.contains("WPA2") || caps.contains("RSN-PSK") ||
            caps.contains("RSN-CCMP") || caps.contains("RSN-TKIP")
        val hasWpa = Regex("WPA-(PSK|TKIP|CCMP)").containsMatchIn(caps) || caps.contains("[WPA]")
        val hasWep = caps.contains("WEP")
        val hasOpen = caps.contains("ESS") || caps.contains("OPEN")

        return when {
            hasEnterprise -> WifiSecurity.ENTERPRISE
            hasSae && hasWpa2 -> WifiSecurity.WPA2_WPA3
            hasSae -> WifiSecurity.WPA3
            hasOwe -> WifiSecurity.OWE
            hasWpa2 -> WifiSecurity.WPA2
            hasWpa -> WifiSecurity.WPA
            hasWep -> WifiSecurity.WEP
            hasOpen -> WifiSecurity.OPEN
            else -> WifiSecurity.UNKNOWN
        }
    }

    fun frequencyToChannel(frequencyMhz: Int): Int = when {
        frequencyMhz == 2484 -> 14
        frequencyMhz in 2412..2483 -> (frequencyMhz - 2407) / 5
        frequencyMhz in 4910..4980 -> (frequencyMhz - 4000) / 5
        frequencyMhz in 5000..5895 -> (frequencyMhz - 5000) / 5
        frequencyMhz in 5925..7125 -> (frequencyMhz - 5950) / 5
        else -> 0
    }

    fun frequencyToBand(frequencyMhz: Int): SignalBand = when {
        frequencyMhz in 5925..7125 -> SignalBand.BAND_6_GHZ
        frequencyMhz in 4900..5895 -> SignalBand.BAND_5_GHZ
        frequencyMhz in 2400..2500 -> SignalBand.BAND_2_4_GHZ
        else -> SignalBand.UNKNOWN
    }

    fun signalQualityFromRssi(rssi: Int): SignalQuality = when {
        rssi >= -50 -> SignalQuality.EXCELLENT
        rssi >= -60 -> SignalQuality.GOOD
        rssi >= -70 -> SignalQuality.FAIR
        rssi >= -80 -> SignalQuality.WEAK
        else -> SignalQuality.POOR
    }

    fun signalLevelFromRssi(rssi: Int): Int = when {
        rssi >= -55 -> 4
        rssi >= -67 -> 3
        rssi >= -75 -> 2
        rssi >= -85 -> 1
        else -> 0
    }

    fun signalPercent(rssi: Int): Int {
        val clamped = rssi.coerceIn(-100, -40)
        return ((clamped + 100) * 100 / 60).coerceIn(0, 100)
    }

    fun channelWidthFromCode(code: Int): Int? = when (code) {
        0 -> 20
        1 -> 40
        2 -> 80
        3 -> 160
        4 -> 80
        5 -> 320
        else -> null
    }

    fun wifiStandardName(standard: Int): String? = when (standard) {
        1 -> "Legacy"
        4 -> "Wi-Fi 4"
        5 -> "Wi-Fi 5"
        6 -> "Wi-Fi 6"
        7 -> "Wi-Fi 6E / 7"
        8 -> "Wi-Fi 7"
        else -> null
    }

    fun isSecure(security: WifiSecurity): Boolean =
        security != WifiSecurity.OPEN && security != WifiSecurity.WEP && security != WifiSecurity.UNKNOWN
}
