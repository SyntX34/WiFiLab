package com.syntx34.wifilab.data.packet

import com.syntx34.wifilab.domain.model.CaptureFormat
import com.syntx34.wifilab.domain.model.PacketProtocol
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PcapParserTest {

    private val parser = PcapParser()

    @Test
    fun parsesSyntheticClassicPcap() {
        val capture = parser.parse(DemoCaptureFactory.createPcapBytes(), "demo.pcap")
        assertEquals(CaptureFormat.PCAP, capture.summary.format)
        assertEquals(8, capture.summary.packetCount)
        assertEquals(4, capture.summary.eapolCount)
        assertEquals(1, capture.summary.protocolCounts[PacketProtocol.ARP])
        assertEquals(2, capture.summary.protocolCounts[PacketProtocol.DNS])
        assertEquals(1, capture.summary.protocolCounts[PacketProtocol.TCP])
        assertEquals(1_750_000_000_100L, capture.packets.first().timestampMillis)
    }

    @Test
    fun identifiesAllFourWayHandshakeMessages() {
        val capture = parser.parse(DemoCaptureFactory.createPcapBytes(), "demo.pcap")
        val eapol = capture.packets.mapNotNull { it.eapol }
        assertEquals(4, eapol.size)
        assertEquals(listOf(1, 2, 3, 4), eapol.map { it.messageNumber })
        assertTrue(eapol.first().ack)
        assertTrue(eapol.first().pmkidPresent)
        assertTrue(eapol[1].mic)
        assertTrue(eapol[2].install)
        assertTrue(eapol[3].secure)
        assertTrue(eapol.all { it.noncePresent })
    }

    @Test
    fun parsesArpAndDnsMetadata() {
        val capture = parser.parse(DemoCaptureFactory.createPcapBytes(), "demo.pcap")
        val arp = capture.packets.first { it.protocol == PacketProtocol.ARP }.arp
        assertNotNull(arp)
        assertEquals(1, arp?.operation)
        assertEquals("192.168.50.10", arp?.senderIp)
        assertEquals("192.168.50.1", arp?.targetIp)

        val dns = capture.packets.filter { it.protocol == PacketProtocol.DNS }
        assertEquals(2, dns.size)
        assertTrue(dns.first().dns?.questions?.any { it.contains("demo.wifilab.local") } == true)
        assertTrue(dns.last().dns?.answers?.any { it.contains("192.168.50.1") } == true)
    }

    @Test
    fun parsesPcapNgEnhancedPacketBlock() {
        val bytes = createMinimalPcapNg()
        val capture = parser.parse(bytes, "demo.pcapng")
        assertEquals(CaptureFormat.PCAPNG, capture.summary.format)
        assertEquals(1, capture.summary.packetCount)
        assertEquals(PacketProtocol.ARP, capture.packets.first().protocol)
        assertEquals("192.168.50.10", capture.packets.first().arp?.senderIp)
    }

    @Test
    fun rejectsUnknownAndTruncatedFiles() {
        assertThrows(PcapParseException::class.java) {
            parser.parse(byteArrayOf(1, 2, 3, 4), "invalid.bin")
        }
        assertThrows(PcapParseException::class.java) {
            parser.parse(ByteArray(28) { 0 }, "empty.pcap")
        }
    }

    private fun createMinimalPcapNg(): ByteArray {
        val output = ByteArrayOutputStream()
        val sectionBody = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(0x1A2B3C4D)
            .putShort(1)
            .putShort(0)
            .putLong(-1L)
            .array()
        output.write(pcapNgBlock(0x0A0D0D0A, sectionBody))

        val interfaceBody = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(1)
            .putShort(0)
            .putInt(65_535)
            .array()
        output.write(pcapNgBlock(1, interfaceBody))

        val frame = arpEthernetFrame()
        val timestampMicros = 1_750_000_000_000_000L
        val high = timestampMicros ushr 32
        val low = timestampMicros and 0xFFFFFFFFL
        val packetBody = ByteArrayOutputStream()
        packetBody.write(
            ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(0)
                .putInt(high.toInt())
                .putInt(low.toInt())
                .putInt(frame.size)
                .putInt(frame.size)
                .array(),
        )
        packetBody.write(frame)
        output.write(pcapNgBlock(6, packetBody.toByteArray()))
        return output.toByteArray()
    }

    private fun pcapNgBlock(type: Int, body: ByteArray): ByteArray {
        val padding = (4 - body.size % 4) % 4
        val totalLength = 8 + body.size + padding + 4
        val buffer = ByteBuffer.allocate(totalLength).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(type)
        buffer.putInt(totalLength)
        buffer.put(body)
        repeat(padding) { buffer.put(0) }
        buffer.putInt(totalLength)
        return buffer.array()
    }

    private fun arpEthernetFrame(): ByteArray {
        val buffer = ByteBuffer.allocate(14 + 28).order(ByteOrder.BIG_ENDIAN)
        buffer.put(byteArrayOf(-1, -1, -1, -1, -1, -1))
        buffer.put(byteArrayOf(0x02, 0, 0, 0, 0, 0x10))
        buffer.putShort(0x0806.toShort())
        buffer.putShort(1)
        buffer.putShort(0x0800.toShort())
        buffer.put(6)
        buffer.put(4)
        buffer.putShort(1)
        buffer.put(byteArrayOf(0x02, 0, 0, 0, 0, 0x10))
        buffer.put(byteArrayOf(192.toByte(), 168.toByte(), 50, 10))
        buffer.put(ByteArray(6))
        buffer.put(byteArrayOf(192.toByte(), 168.toByte(), 50, 1))
        return buffer.array()
    }
}
