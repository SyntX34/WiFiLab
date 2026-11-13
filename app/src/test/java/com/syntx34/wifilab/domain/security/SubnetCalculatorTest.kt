package com.syntx34.wifilab.domain.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SubnetCalculatorTest {

    @Test
    fun convertsIpv4ToLongAndBack() {
        val value = SubnetCalculator.ipv4ToLong("192.168.1.42")
        assertEquals(3_232_235_818L, value)
        assertEquals("192.168.1.42", SubnetCalculator.longToIpv4(value!!))
        assertNull(SubnetCalculator.ipv4ToLong("192.168.1"))
        assertNull(SubnetCalculator.ipv4ToLong("999.1.1.1"))
    }

    @Test
    fun parsesPrivateCidrScopes() {
        val result = SubnetCalculator.parseCidr("192.168.1.0/24")
        assertTrue(result.isSuccess)
        val network = result.getOrThrow()
        assertEquals("192.168.1.0/24", network.cidr)
        assertEquals(254, SubnetCalculator.hostCount(24))
        assertEquals(2, SubnetCalculator.hostCount(30))
    }

    @Test
    fun rejectsPublicBroadAndMisalignedScopes() {
        assertTrue(SubnetCalculator.parseCidr("8.8.8.0/24").isFailure)
        assertTrue(SubnetCalculator.parseCidr("10.0.0.0/16").isFailure)
        assertTrue(SubnetCalculator.parseCidr("10.0.0.5/24").isFailure)
        assertTrue(SubnetCalculator.parseCidr("not-a-network/24").isFailure)
        assertTrue(SubnetCalculator.parseCidr("192.168.1.0/33").isFailure)
    }

    @Test
    fun createsSafeLocalScopeFromDetectedAddress() {
        val network = SubnetCalculator.fromLocalAddress(
            localIpv4 = "10.20.30.40",
            gatewayIpv4 = "10.20.30.1",
            prefixLength = 16,
            interfaceName = "wlan0",
        )
        assertEquals("10.20.30.0/24", network?.cidr)
        assertEquals("10.20.30.1", network?.gatewayIpv4)
    }

    @Test
    fun exposesHostAndBroadcastAddresses() {
        val hosts = SubnetCalculator.hostAddresses("192.168.1.0", 30)
        assertEquals(listOf("192.168.1.1", "192.168.1.2"), hosts)
        assertEquals("192.168.1.3", SubnetCalculator.broadcastAddress("192.168.1.0", 30))
        assertTrue(SubnetCalculator.isPrivateIpv4("172.16.5.4"))
        assertTrue(SubnetCalculator.isPrivateIpv4("192.168.0.1"))
        assertFalse(SubnetCalculator.isPrivateIpv4("172.32.0.1"))
        assertFalse(SubnetCalculator.isPrivateIpv4("1.1.1.1"))
    }
}
