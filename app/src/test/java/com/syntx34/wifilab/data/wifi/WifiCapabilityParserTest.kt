package com.syntx34.wifilab.data.wifi

import com.syntx34.wifilab.domain.model.SignalBand
import com.syntx34.wifilab.domain.model.SignalQuality
import com.syntx34.wifilab.domain.model.WifiSecurity
import com.syntx34.wifilab.domain.security.WifiCapabilityParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiCapabilityParserTest {

    @Test
    fun parsesCommonSecurityCapabilities() {
        assertEquals(
            WifiSecurity.WPA2,
            WifiCapabilityParser.securityFromCapabilities("[WPA2-PSK-CCMP][ESS]"),
        )
        assertEquals(
            WifiSecurity.WPA3,
            WifiCapabilityParser.securityFromCapabilities("[RSN-SAE-CCMP][ESS]"),
        )
        assertEquals(
            WifiSecurity.WPA2_WPA3,
            WifiCapabilityParser.securityFromCapabilities("[WPA2-PSK-CCMP][RSN-SAE-CCMP][ESS]"),
        )
        assertEquals(
            WifiSecurity.ENTERPRISE,
            WifiCapabilityParser.securityFromCapabilities("[WPA2-EAP-CCMP][ESS]"),
        )
        assertEquals(
            WifiSecurity.OWE,
            WifiCapabilityParser.securityFromCapabilities("[RSN-OWE-CCMP][ESS]"),
        )
        assertEquals(
            WifiSecurity.WPA,
            WifiCapabilityParser.securityFromCapabilities("[WPA-PSK-TKIP][ESS]"),
        )
        assertEquals(
            WifiSecurity.WEP,
            WifiCapabilityParser.securityFromCapabilities("[WEP][ESS]"),
        )
        assertEquals(
            WifiSecurity.OPEN,
            WifiCapabilityParser.securityFromCapabilities("[ESS]"),
        )
        assertEquals(
            WifiSecurity.UNKNOWN,
            WifiCapabilityParser.securityFromCapabilities(""),
        )
    }

    @Test
    fun mapsFrequenciesToChannelsAndBands() {
        assertEquals(1, WifiCapabilityParser.frequencyToChannel(2412))
        assertEquals(6, WifiCapabilityParser.frequencyToChannel(2437))
        assertEquals(14, WifiCapabilityParser.frequencyToChannel(2484))
        assertEquals(36, WifiCapabilityParser.frequencyToChannel(5180))
        assertEquals(1, WifiCapabilityParser.frequencyToChannel(5955))
        assertEquals(SignalBand.BAND_2_4_GHZ, WifiCapabilityParser.frequencyToBand(2412))
        assertEquals(SignalBand.BAND_5_GHZ, WifiCapabilityParser.frequencyToBand(5180))
        assertEquals(SignalBand.BAND_6_GHZ, WifiCapabilityParser.frequencyToBand(5955))
        assertEquals(SignalBand.UNKNOWN, WifiCapabilityParser.frequencyToBand(0))
    }

    @Test
    fun mapsSignalToQualityAndLevels() {
        assertEquals(SignalQuality.EXCELLENT, WifiCapabilityParser.signalQualityFromRssi(-42))
        assertEquals(SignalQuality.GOOD, WifiCapabilityParser.signalQualityFromRssi(-58))
        assertEquals(SignalQuality.FAIR, WifiCapabilityParser.signalQualityFromRssi(-68))
        assertEquals(SignalQuality.WEAK, WifiCapabilityParser.signalQualityFromRssi(-78))
        assertEquals(SignalQuality.POOR, WifiCapabilityParser.signalQualityFromRssi(-90))
        assertEquals(4, WifiCapabilityParser.signalLevelFromRssi(-50))
        assertEquals(0, WifiCapabilityParser.signalLevelFromRssi(-95))
        assertTrue(WifiCapabilityParser.signalPercent(-50) > WifiCapabilityParser.signalPercent(-80))
    }

    @Test
    fun recognizesWpa3TransitionAndEnterprise() {
        val mixed = WifiCapabilityParser.securityFromCapabilities(
            "[RSN-PSK-CCMP][RSN-SAE-CCMP][ESS]",
        )
        assertEquals(WifiSecurity.WPA2_WPA3, mixed)
        assertTrue(WifiCapabilityParser.isSecure(mixed))
        assertTrue(!WifiCapabilityParser.isSecure(WifiSecurity.OPEN))
    }
}
