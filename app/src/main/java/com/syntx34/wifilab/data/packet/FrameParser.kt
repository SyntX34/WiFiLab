package com.syntx34.wifilab.data.packet

import com.syntx34.wifilab.domain.model.ArpDetails
import com.syntx34.wifilab.domain.model.PacketInfo
import com.syntx34.wifilab.domain.model.PacketProtocol

internal class FrameParser {

    fun parse(
        data: ByteArray,
        linkType: Int,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): PacketInfo {
        val reader = ByteReader(data)
        return when (linkType) {
            LINKTYPE_ETHERNET -> parseEthernet(reader, number, timestampMillis, originalLength)
            LINKTYPE_RAW_IP -> parseRawIp(reader, number, timestampMillis, originalLength)
            LINKTYPE_LINUX_SLL -> parseLinuxSll(reader, number, timestampMillis, originalLength)
            LINKTYPE_LINUX_SLL2 -> parseLinuxSll2(reader, number, timestampMillis, originalLength)
            LINKTYPE_IEEE80211 -> fromWlan(
                reader, Ieee80211Parser.parse(reader, 0, number, timestampMillis, originalLength),
                number, timestampMillis, originalLength,
            )

            LINKTYPE_RADIOTAP -> fromWlan(
                reader, Ieee80211Parser.parseRadiotap(reader, number, timestampMillis, originalLength),
                number, timestampMillis, originalLength,
            )

            else -> PacketInfo(
                number = number,
                timestampMillis = timestampMillis,
                protocol = PacketProtocol.OTHER,
                source = "Unknown",
                destination = "Unknown",
                summary = "Unsupported link type $linkType",
                originalLength = originalLength,
                capturedLength = data.size,
            )
        }
    }

    private fun parseEthernet(
        reader: ByteReader,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): PacketInfo {
        if (reader.limit < 14) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Truncated Ethernet frame")
        }
        val destinationMac = reader.mac(0)
        val sourceMac = reader.mac(6)
        var etherType = reader.u16be(12)
        var offset = 14
        while (etherType == 0x8100 || etherType == 0x88A8 || etherType == 0x9100) {
            if (offset + 4 > reader.limit) {
                return malformed(number, timestampMillis, originalLength, reader.limit, "Truncated VLAN tag")
            }
            etherType = reader.u16be(offset + 2)
            offset += 4
        }
        return when (etherType) {
            0x0800 -> parseIpv4(reader, offset, number, timestampMillis, originalLength)
            0x86DD -> parseIpv6(reader, offset, number, timestampMillis, originalLength)
            0x0806 -> parseArp(reader, offset, number, timestampMillis, originalLength)
            0x888E -> parseEapol(
                reader = reader,
                offset = offset,
                number = number,
                timestampMillis = timestampMillis,
                originalLength = originalLength,
                sourceMac = sourceMac,
                destinationMac = destinationMac,
            )

            else -> PacketInfo(
                number = number,
                timestampMillis = timestampMillis,
                protocol = PacketProtocol.OTHER,
                source = sourceMac,
                destination = destinationMac,
                summary = "EtherType 0x%04X".format(etherType),
                originalLength = originalLength,
                capturedLength = reader.limit,
                payloadPreview = reader.asciiPreview(offset, PREVIEW_BYTES),
            )
        }
    }

    private fun fromWlan(
        reader: ByteReader,
        result: WlanResult,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): PacketInfo = when (result) {
        is WlanResult.Described -> result.info
        is WlanResult.Payload -> when (result.etherType) {
            0x888E -> parseEapol(
                reader, result.offset, number, timestampMillis, originalLength,
                result.source, result.destination,
            )

            0x0800 -> parseIpv4(reader, result.offset, number, timestampMillis, originalLength)
            0x86DD -> parseIpv6(reader, result.offset, number, timestampMillis, originalLength)
            0x0806 -> parseArp(reader, result.offset, number, timestampMillis, originalLength)
            else -> PacketInfo(
                number = number,
                timestampMillis = timestampMillis,
                protocol = PacketProtocol.WLAN,
                source = result.source,
                destination = result.destination,
                summary = "802.11 data, EtherType 0x%04X".format(result.etherType),
                originalLength = originalLength,
                capturedLength = reader.limit,
            )
        }
    }

    private fun parseLinuxSll(
        reader: ByteReader,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): PacketInfo {
        if (reader.limit < 16) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Truncated Linux cooked frame")
        }
        val protocol = reader.u16be(14)
        val offset = 16
        return when (protocol) {
            0x0800 -> parseIpv4(reader, offset, number, timestampMillis, originalLength)
            0x86DD -> parseIpv6(reader, offset, number, timestampMillis, originalLength)
            0x0806 -> parseArp(reader, offset, number, timestampMillis, originalLength)
            0x888E -> parseEapol(reader, offset, number, timestampMillis, originalLength, "cooked", "cooked")
            else -> PacketInfo(
                number = number,
                timestampMillis = timestampMillis,
                protocol = PacketProtocol.OTHER,
                source = "Linux cooked",
                destination = "Linux cooked",
                summary = "EtherType 0x%04X".format(protocol),
                originalLength = originalLength,
                capturedLength = reader.limit,
            )
        }
    }

    private fun parseLinuxSll2(
        reader: ByteReader,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): PacketInfo {
        if (reader.limit < 20) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Truncated Linux cooked v2 frame")
        }
        val protocol = reader.u16be(0)
        val offset = 20
        return when (protocol) {
            0x0800 -> parseIpv4(reader, offset, number, timestampMillis, originalLength)
            0x86DD -> parseIpv6(reader, offset, number, timestampMillis, originalLength)
            0x0806 -> parseArp(reader, offset, number, timestampMillis, originalLength)
            0x888E -> parseEapol(reader, offset, number, timestampMillis, originalLength, "cooked", "cooked")
            else -> PacketInfo(
                number = number,
                timestampMillis = timestampMillis,
                protocol = PacketProtocol.OTHER,
                source = "Linux cooked v2",
                destination = "Linux cooked v2",
                summary = "EtherType 0x%04X".format(protocol),
                originalLength = originalLength,
                capturedLength = reader.limit,
            )
        }
    }

    private fun parseRawIp(
        reader: ByteReader,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): PacketInfo {
        if (reader.limit < 1) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Empty raw IP packet")
        }
        return when (reader.u8(0) ushr 4) {
            4 -> parseIpv4(reader, 0, number, timestampMillis, originalLength)
            6 -> parseIpv6(reader, 0, number, timestampMillis, originalLength)
            else -> malformed(number, timestampMillis, originalLength, reader.limit, "Unknown raw IP version")
        }
    }

    private fun parseEapol(
        reader: ByteReader,
        offset: Int,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
        sourceMac: String,
        destinationMac: String,
    ): PacketInfo {
        val result = runCatching { EapolParser.parse(reader, offset) }.getOrNull()
            ?: return PacketInfo(
                number = number,
                timestampMillis = timestampMillis,
                protocol = PacketProtocol.EAPOL,
                source = sourceMac,
                destination = destinationMac,
                summary = "Malformed EAPOL frame",
                originalLength = originalLength,
                capturedLength = reader.limit,
            )
        return PacketInfo(
            number = number,
            timestampMillis = timestampMillis,
            protocol = PacketProtocol.EAPOL,
            source = sourceMac,
            destination = destinationMac,
            summary = result.summary,
            originalLength = originalLength,
            capturedLength = reader.limit,
            eapol = result.details,
        )
    }

    private fun parseArp(
        reader: ByteReader,
        offset: Int,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): PacketInfo {
        if (offset + 28 > reader.limit) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Truncated ARP frame")
        }
        val hardwareType = reader.u16be(offset)
        val protocolType = reader.u16be(offset + 2)
        val hardwareLength = reader.u8(offset + 4)
        val protocolLength = reader.u8(offset + 5)
        val operation = reader.u16be(offset + 6)
        if (hardwareType != 1 || protocolType != 0x0800 || hardwareLength != 6 || protocolLength != 4) {
            return PacketInfo(
                number = number,
                timestampMillis = timestampMillis,
                protocol = PacketProtocol.ARP,
                source = "ARP",
                destination = "ARP",
                summary = "ARP operation $operation with unsupported address format",
                originalLength = originalLength,
                capturedLength = reader.limit,
            )
        }
        val senderMac = reader.mac(offset + 8)
        val senderIp = reader.ipv4(offset + 14)
        val targetMac = reader.mac(offset + 18)
        val targetIp = reader.ipv4(offset + 24)
        val details = ArpDetails(
            operation = operation,
            senderIp = senderIp,
            senderMac = senderMac,
            targetIp = targetIp,
            targetMac = targetMac,
        )
        val summary = if (operation == 1) {
            "ARP request: who has $targetIp? Tell $senderIp"
        } else {
            "ARP reply: $senderIp is at $senderMac"
        }
        return PacketInfo(
            number = number,
            timestampMillis = timestampMillis,
            protocol = PacketProtocol.ARP,
            source = senderIp,
            destination = targetIp,
            summary = summary,
            originalLength = originalLength,
            capturedLength = reader.limit,
            arp = details,
        )
    }

    private fun parseIpv4(
        reader: ByteReader,
        offset: Int,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): PacketInfo {
        if (offset + 20 > reader.limit) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Truncated IPv4 header")
        }
        val version = reader.u8(offset) ushr 4
        val ihl = reader.u8(offset) and 0x0F
        if (version != 4 || ihl < 5) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Invalid IPv4 header")
        }
        val headerLength = ihl * 4
        if (offset + headerLength > reader.limit) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Truncated IPv4 options")
        }
        val totalLength = reader.u16be(offset + 2)
        val end = minOf(reader.limit, offset + if (totalLength == 0) reader.limit - offset else totalLength)
        if (end < offset + headerLength) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Invalid IPv4 length")
        }
        val protocolNumber = reader.u8(offset + 9)
        val source = reader.ipv4(offset + 12)
        val destination = reader.ipv4(offset + 16)
        val payloadOffset = offset + headerLength
        return when (protocolNumber) {
            6 -> parseTcp(reader, payloadOffset, end, source, destination, number, timestampMillis, originalLength)
            17 -> parseUdp(reader, payloadOffset, end, source, destination, number, timestampMillis, originalLength)
            1 -> parseIcmp(reader, payloadOffset, end, source, destination, number, timestampMillis, originalLength, false)
            58 -> parseIcmp(reader, payloadOffset, end, source, destination, number, timestampMillis, originalLength, true)
            else -> PacketInfo(
                number = number,
                timestampMillis = timestampMillis,
                protocol = PacketProtocol.OTHER,
                source = source,
                destination = destination,
                summary = "IPv4 protocol $protocolNumber",
                originalLength = originalLength,
                capturedLength = reader.limit,
                payloadPreview = reader.asciiPreview(payloadOffset, PREVIEW_BYTES),
            )
        }
    }

    private fun parseIpv6(
        reader: ByteReader,
        offset: Int,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): PacketInfo {
        if (offset + 40 > reader.limit) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Truncated IPv6 header")
        }
        val version = reader.u8(offset) ushr 4
        if (version != 6) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Invalid IPv6 header")
        }
        val payloadLength = reader.u16be(offset + 4)
        val nextHeader = reader.u8(offset + 6)
        val source = reader.ipv6(offset + 8)
        val destination = reader.ipv6(offset + 24)
        val payloadOffset = offset + 40
        val end = minOf(reader.limit, payloadOffset + payloadLength)
        return when (nextHeader) {
            6 -> parseTcp(reader, payloadOffset, end, source, destination, number, timestampMillis, originalLength)
            17 -> parseUdp(reader, payloadOffset, end, source, destination, number, timestampMillis, originalLength)
            58 -> parseIcmp(reader, payloadOffset, end, source, destination, number, timestampMillis, originalLength, true)
            else -> PacketInfo(
                number = number,
                timestampMillis = timestampMillis,
                protocol = PacketProtocol.IPV6,
                source = source,
                destination = destination,
                summary = "IPv6 next header $nextHeader",
                originalLength = originalLength,
                capturedLength = reader.limit,
            )
        }
    }

    private fun parseTcp(
        reader: ByteReader,
        offset: Int,
        end: Int,
        sourceIp: String,
        destinationIp: String,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): PacketInfo {
        if (offset + 4 > end) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Truncated TCP header")
        }
        val sourcePort = reader.u16be(offset)
        val destinationPort = reader.u16be(offset + 2)
        val flags = if (offset + 14 <= end) {
            reader.u8(offset + 13)
        } else {
            0
        }
        val headerLength = if (offset + 13 <= end) {
            (reader.u8(offset + 12) ushr 4) * 4
        } else {
            20
        }
        val payloadOffset = minOf(end, offset + headerLength.coerceAtLeast(20))
        val payloadLength = (end - payloadOffset).coerceAtLeast(0)
        val flagText = tcpFlagText(flags)
        val service = portService(destinationPort) ?: portService(sourcePort)
        val serviceLabel = if (service != null) " $service" else ""
        return PacketInfo(
            number = number,
            timestampMillis = timestampMillis,
            protocol = PacketProtocol.TCP,
            source = sourceIp,
            destination = destinationIp,
            summary = "TCP $sourcePort to $destinationPort$serviceLabel $flagText, $payloadLength byte payload",
            originalLength = originalLength,
            capturedLength = reader.limit,
            payloadPreview = reader.asciiPreview(payloadOffset, PREVIEW_BYTES),
        )
    }

    private fun parseUdp(
        reader: ByteReader,
        offset: Int,
        end: Int,
        sourceIp: String,
        destinationIp: String,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): PacketInfo {
        if (offset + 8 > end) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Truncated UDP header")
        }
        val sourcePort = reader.u16be(offset)
        val destinationPort = reader.u16be(offset + 2)
        val payloadOffset = offset + 8
        if (sourcePort == 53 || destinationPort == 53) {
            val dns = DnsParser.parse(reader, payloadOffset)
            if (dns != null) {
                val question = dns.questions.firstOrNull() ?: "(no question)"
                val direction = if (dns.isResponse) "response" else "query"
                val answer = dns.answers.firstOrNull()
                val answerLabel = if (answer != null) " Answer: $answer" else ""
                return PacketInfo(
                    number = number,
                    timestampMillis = timestampMillis,
                    protocol = PacketProtocol.DNS,
                    source = sourceIp,
                    destination = destinationIp,
                    summary = "DNS $direction $question.$answerLabel",
                    originalLength = originalLength,
                    capturedLength = reader.limit,
                    dns = dns,
                )
            }
        }
        val protocol = if (sourcePort in 67..68 || destinationPort in 67..68) {
            PacketProtocol.DHCP
        } else {
            PacketProtocol.UDP
        }
        val service = portService(destinationPort) ?: portService(sourcePort)
        val serviceLabel = if (service != null) " $service" else ""
        return PacketInfo(
            number = number,
            timestampMillis = timestampMillis,
            protocol = protocol,
            source = sourceIp,
            destination = destinationIp,
            summary = "UDP $sourcePort to $destinationPort$serviceLabel, ${(end - payloadOffset).coerceAtLeast(0)} byte payload",
            originalLength = originalLength,
            capturedLength = reader.limit,
            payloadPreview = reader.asciiPreview(payloadOffset, PREVIEW_BYTES),
        )
    }

    private fun parseIcmp(
        reader: ByteReader,
        offset: Int,
        end: Int,
        sourceIp: String,
        destinationIp: String,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
        ipv6: Boolean,
    ): PacketInfo {
        if (offset + 4 > end) {
            return malformed(number, timestampMillis, originalLength, reader.limit, "Truncated ICMP header")
        }
        val type = reader.u8(offset)
        val code = reader.u8(offset + 1)
        val label = if (ipv6) "ICMPv6" else "ICMP"
        return PacketInfo(
            number = number,
            timestampMillis = timestampMillis,
            protocol = PacketProtocol.ICMP,
            source = sourceIp,
            destination = destinationIp,
            summary = "$label type $type code $code",
            originalLength = originalLength,
            capturedLength = reader.limit,
            payloadPreview = reader.asciiPreview(offset + 4, PREVIEW_BYTES),
        )
    }

    private fun malformed(
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
        capturedLength: Int,
        message: String,
    ): PacketInfo = PacketInfo(
        number = number,
        timestampMillis = timestampMillis,
        protocol = PacketProtocol.OTHER,
        source = "Malformed",
        destination = "Malformed",
        summary = message,
        originalLength = originalLength,
        capturedLength = capturedLength,
    )

    private fun tcpFlagText(flags: Int): String {
        val labels = mutableListOf<String>()
        if (flags and 0x02 != 0) labels += "SYN"
        if (flags and 0x10 != 0) labels += "ACK"
        if (flags and 0x01 != 0) labels += "FIN"
        if (flags and 0x04 != 0) labels += "RST"
        if (flags and 0x08 != 0) labels += "PSH"
        if (flags and 0x20 != 0) labels += "URG"
        return labels.joinToString(",").ifEmpty { "flags 0" }
    }

    private fun portService(port: Int): String? = when (port) {
        22 -> "SSH"
        53 -> "DNS"
        67, 68 -> "DHCP"
        80 -> "HTTP"
        123 -> "NTP"
        443 -> "HTTPS"
        5353 -> "mDNS"
        else -> null
    }

    companion object {
        const val LINKTYPE_ETHERNET = 1
        const val LINKTYPE_RAW_IP = 101
        const val LINKTYPE_LINUX_SLL = 113
        const val LINKTYPE_LINUX_SLL2 = 276
        const val LINKTYPE_IEEE80211 = 105
        const val LINKTYPE_RADIOTAP = 127
        private const val PREVIEW_BYTES = 64
    }
}
