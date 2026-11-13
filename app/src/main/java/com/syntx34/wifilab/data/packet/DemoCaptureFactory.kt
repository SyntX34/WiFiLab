package com.syntx34.wifilab.data.packet

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object DemoCaptureFactory {

    fun createPcapBytes(): ByteArray {
        val frames = mutableListOf<ByteArray>()
        val clientMac = mac("02:00:00:00:00:10")
        val routerMac = mac("02:00:00:00:00:01")
        val broadcast = mac("ff:ff:ff:ff:ff:ff")
        val clientIp = ipv4("192.168.50.10")
        val routerIp = ipv4("192.168.50.1")

        frames += ethernet(
            destination = broadcast,
            source = clientMac,
            etherType = 0x0806,
            payload = arpRequest(clientMac, clientIp, routerIp),
        )
        frames += ethernet(
            destination = routerMac,
            source = clientMac,
            etherType = 0x0800,
            payload = ipv4Packet(
                source = clientIp,
                destination = routerIp,
                protocol = 17,
                payload = udpPacket(53000, 53, dnsQuery()),
            ),
        )
        frames += ethernet(
            destination = clientMac,
            source = routerMac,
            etherType = 0x888E,
            payload = eapolKey(
                keyInfo = 0x008A,
                replayCounter = 1L,
                nonceByte = 0x11,
                micByte = 0x00,
                keyData = pmkidKde(),
            ),
        )
        frames += ethernet(
            destination = routerMac,
            source = clientMac,
            etherType = 0x888E,
            payload = eapolKey(
                keyInfo = 0x010A,
                replayCounter = 1L,
                nonceByte = 0x22,
                micByte = 0x33,
                keyData = ByteArray(0),
            ),
        )
        frames += ethernet(
            destination = clientMac,
            source = routerMac,
            etherType = 0x888E,
            payload = eapolKey(
                keyInfo = 0x03CA,
                replayCounter = 2L,
                nonceByte = 0x11,
                micByte = 0x44,
                keyData = ByteArray(0),
            ),
        )
        frames += ethernet(
            destination = routerMac,
            source = clientMac,
            etherType = 0x888E,
            payload = eapolKey(
                keyInfo = 0x030A,
                replayCounter = 2L,
                nonceByte = 0x22,
                micByte = 0x55,
                keyData = ByteArray(0),
            ),
        )
        frames += ethernet(
            destination = clientMac,
            source = routerMac,
            etherType = 0x0800,
            payload = ipv4Packet(
                source = routerIp,
                destination = clientIp,
                protocol = 17,
                payload = udpPacket(53, 53000, dnsResponse()),
            ),
        )
        frames += ethernet(
            destination = routerMac,
            source = clientMac,
            etherType = 0x0800,
            payload = ipv4Packet(
                source = clientIp,
                destination = routerIp,
                protocol = 6,
                payload = tcpSyn(50123, 443),
            ),
        )
        return buildClassicPcap(frames)
    }

    private fun buildClassicPcap(frames: List<ByteArray>): ByteArray {
        val output = ByteArrayOutputStream()
        val header = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN)
        header.putInt(0xA1B2C3D4.toInt())
        header.putShort(2)
        header.putShort(4)
        header.putInt(0)
        header.putInt(0)
        header.putInt(65_535)
        header.putInt(1)
        output.write(header.array())
        var seconds = 1_750_000_000L
        var micros = 100_000
        frames.forEachIndexed { index, frame ->
            val record = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN)
            record.putInt(seconds.toInt())
            record.putInt(micros)
            record.putInt(frame.size)
            record.putInt(frame.size)
            output.write(record.array())
            output.write(frame)
            seconds += 1
            micros = 100_000 + index * 1_000
        }
        return output.toByteArray()
    }

    private fun ethernet(
        destination: ByteArray,
        source: ByteArray,
        etherType: Int,
        payload: ByteArray,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(14 + payload.size).order(ByteOrder.BIG_ENDIAN)
        buffer.put(destination)
        buffer.put(source)
        buffer.putShort(etherType.toShort())
        buffer.put(payload)
        return buffer.array()
    }

    private fun arpRequest(senderMac: ByteArray, senderIp: ByteArray, targetIp: ByteArray): ByteArray {
        val buffer = ByteBuffer.allocate(28).order(ByteOrder.BIG_ENDIAN)
        buffer.putShort(1)
        buffer.putShort(0x0800.toShort())
        buffer.put(6)
        buffer.put(4)
        buffer.putShort(1)
        buffer.put(senderMac)
        buffer.put(senderIp)
        buffer.put(ByteArray(6))
        buffer.put(targetIp)
        return buffer.array()
    }

    private fun ipv4Packet(
        source: ByteArray,
        destination: ByteArray,
        protocol: Int,
        payload: ByteArray,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(20 + payload.size).order(ByteOrder.BIG_ENDIAN)
        buffer.put(0x45)
        buffer.put(0)
        buffer.putShort((20 + payload.size).toShort())
        buffer.putShort(0x4242)
        buffer.putShort(0x4000)
        buffer.put(64)
        buffer.put(protocol.toByte())
        buffer.putShort(0)
        buffer.put(source)
        buffer.put(destination)
        buffer.put(payload)
        return buffer.array()
    }

    private fun udpPacket(sourcePort: Int, destinationPort: Int, payload: ByteArray): ByteArray {
        val buffer = ByteBuffer.allocate(8 + payload.size).order(ByteOrder.BIG_ENDIAN)
        buffer.putShort(sourcePort.toShort())
        buffer.putShort(destinationPort.toShort())
        buffer.putShort((8 + payload.size).toShort())
        buffer.putShort(0)
        buffer.put(payload)
        return buffer.array()
    }

    private fun tcpSyn(sourcePort: Int, destinationPort: Int): ByteArray {
        val buffer = ByteBuffer.allocate(20).order(ByteOrder.BIG_ENDIAN)
        buffer.putShort(sourcePort.toShort())
        buffer.putShort(destinationPort.toShort())
        buffer.putInt(1)
        buffer.putInt(0)
        buffer.put(0x50)
        buffer.put(0x02)
        buffer.putShort(65_535.toShort())
        buffer.putShort(0)
        buffer.putShort(0)
        return buffer.array()
    }

    private fun dnsQuery(): ByteArray = dnsMessage(
        flags = 0x0100,
        includeAnswer = false,
    )

    private fun dnsResponse(): ByteArray = dnsMessage(
        flags = 0x8180,
        includeAnswer = true,
    )

    private fun dnsMessage(flags: Int, includeAnswer: Boolean): ByteArray {
        val output = ByteArrayOutputStream()
        val header = ByteBuffer.allocate(12).order(ByteOrder.BIG_ENDIAN)
        header.putShort(0x1234)
        header.putShort(flags.toShort())
        header.putShort(1)
        header.putShort(if (includeAnswer) 1 else 0)
        header.putShort(0)
        header.putShort(0)
        output.write(header.array())
        output.write(encodeName("demo.wifilab.local"))
        output.write(byteArrayOf(0, 1, 0, 1))
        if (includeAnswer) {
            output.write(byteArrayOf(0xC0.toByte(), 0x0C))
            output.write(byteArrayOf(0, 1, 0, 1, 0, 0, 0, 60, 0, 4))
            output.write(ipv4("192.168.50.1"))
        }
        return output.toByteArray()
    }

    private fun encodeName(name: String): ByteArray {
        val output = ByteArrayOutputStream()
        name.split('.').forEach { label ->
            output.write(label.length)
            output.write(label.toByteArray(Charsets.US_ASCII))
        }
        output.write(0)
        return output.toByteArray()
    }

    private fun eapolKey(
        keyInfo: Int,
        replayCounter: Long,
        nonceByte: Int,
        micByte: Int,
        keyData: ByteArray,
    ): ByteArray {
        val keyBody = ByteArray(95 + keyData.size)
        keyBody[0] = 2
        keyBody[1] = ((keyInfo ushr 8) and 0xFF).toByte()
        keyBody[2] = (keyInfo and 0xFF).toByte()
        keyBody[3] = 0
        keyBody[4] = 16
        val replay = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(replayCounter).array()
        System.arraycopy(replay, 0, keyBody, 5, replay.size)
        for (index in 13 until 45) {
            keyBody[index] = nonceByte.toByte()
        }
        if (micByte != 0) {
            for (index in 77 until 93) {
                keyBody[index] = micByte.toByte()
            }
        }
        keyBody[93] = ((keyData.size ushr 8) and 0xFF).toByte()
        keyBody[94] = (keyData.size and 0xFF).toByte()
        System.arraycopy(keyData, 0, keyBody, 95, keyData.size)
        val eapol = ByteBuffer.allocate(4 + keyBody.size).order(ByteOrder.BIG_ENDIAN)
        eapol.put(2)
        eapol.put(3)
        eapol.putShort(keyBody.size.toShort())
        eapol.put(keyBody)
        return eapol.array()
    }

    private fun pmkidKde(): ByteArray {
        val kde = ByteArray(22)
        kde[0] = 0xDD.toByte()
        kde[1] = 20
        kde[2] = 0
        kde[3] = 0x0F
        kde[4] = 0xAC.toByte()
        kde[5] = 4
        for (index in 6 until kde.size) {
            kde[index] = 0xA5.toByte()
        }
        return kde
    }

    private fun ipv4(address: String): ByteArray =
        address.split('.').map { it.toInt().toByte() }.toByteArray()

    private fun mac(address: String): ByteArray =
        address.split(':').map { it.toInt(16).toByte() }.toByteArray()
}
