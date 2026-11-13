package com.syntx34.wifilab.domain.security

import com.syntx34.wifilab.domain.model.BaselineDevice
import com.syntx34.wifilab.domain.model.DeviceResponseKind
import com.syntx34.wifilab.domain.model.DiscoveredDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BaselineComparatorTest {

    private fun device(ip: String, hostname: String? = null) = DiscoveredDevice(
        ipAddress = ip,
        hostname = hostname,
        serviceNames = emptyList(),
        openPorts = emptyList(),
        isGateway = false,
        responseKind = DeviceResponseKind.PING,
        firstSeenAt = 1L,
    )

    @Test
    fun noBaselineReturnsAllDevicesAsKnown() {
        val comparison = BaselineComparator.compare(
            discovered = listOf(device("192.168.1.2"), device("192.168.1.3")),
            baseline = emptyList(),
        )
        assertFalse(comparison.baselineUsed)
        assertTrue(comparison.unexpectedDevices.isEmpty())
        assertEquals(2, comparison.knownDevices.size)
    }

    @Test
    fun newAddressIsUnexpected() {
        val baseline = listOf(
            BaselineDevice("192.168.1.2", null, emptyList(), 10L),
            BaselineDevice("192.168.1.3", null, emptyList(), 10L),
        )
        val comparison = BaselineComparator.compare(
            discovered = listOf(
                device("192.168.1.2"),
                device("192.168.1.50"),
            ),
            baseline = baseline,
        )
        assertTrue(comparison.baselineUsed)
        assertEquals(1, comparison.unexpectedDevices.size)
        assertEquals("192.168.1.50", comparison.unexpectedDevices.first().ipAddress)
        assertEquals(1, comparison.knownDevices.size)
    }

    @Test
    fun matchingIpAndHostnameIsTrusted() {
        val baseline = listOf(BaselineDevice("192.168.1.10", "router", listOf("HTTP"), 10L))
        val comparison = BaselineComparator.compare(
            discovered = listOf(device("192.168.1.10", "router")),
            baseline = baseline,
        )
        assertTrue(comparison.unexpectedDevices.isEmpty())
        assertEquals(1, comparison.knownDevices.size)
    }
}
