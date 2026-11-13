package com.syntx34.wifilab.data.packet

import com.syntx34.wifilab.domain.model.PacketInfo
import com.syntx34.wifilab.domain.model.PacketProtocol

/*
 * Result of decoding an 802.11 frame. Either the frame was fully described
 * (management, control, encrypted or empty data) or it carries an unencrypted
 * LLC payload that the caller should dispatch by EtherType.
 */
internal sealed interface WlanResult {
    data class Described(val info: PacketInfo) : WlanResult

    data class Payload(
        val etherType: Int,
        val offset: Int,
        val source: String,
        val destination: String,
    ) : WlanResult
}

/*
 * Decodes raw 802.11 captures (link type 105) and radiotap captures (link type
 * 127), which is how monitor mode tools store WPA handshakes. Only headers are
 * read. Encrypted payloads are reported as such and never decrypted.
 */
internal object Ieee80211Parser {

    fun parseRadiotap(
        reader: ByteReader,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): WlanResult {
        if (reader.limit < 8) {
            return described(number, timestampMillis, originalLength, reader.limit, "Truncated radiotap header")
        }
        val headerLength = reader.u16le(2)
        if (headerLength < 8 || headerLength >= reader.limit) {
            return described(number, timestampMillis, originalLength, reader.limit, "Invalid radiotap length")
        }
        return parse(reader, headerLength, number, timestampMillis, originalLength)
    }

    fun parse(
        reader: ByteReader,
        start: Int,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): WlanResult {
        if (start + 10 > reader.limit) {
            return described(number, timestampMillis, originalLength, reader.limit, "Truncated 802.11 frame")
        }
        val fc0 = reader.u8(start)
        val fc1 = reader.u8(start + 1)
        val type = (fc0 shr 2) and 0x03
        val subtype = (fc0 shr 4) and 0x0F
        val toDs = fc1 and 0x01 != 0
        val fromDs = fc1 and 0x02 != 0
        val protectedFrame = fc1 and 0x40 != 0
        val ordered = fc1 and 0x80 != 0

        return when (type) {
            0 -> parseManagement(reader, start, subtype, number, timestampMillis, originalLength)
            1 -> described(
                number, timestampMillis, originalLength, reader.limit,
                "802.11 control frame, ${controlName(subtype)}",
            )

            2 -> parseData(
                reader, start, subtype, toDs, fromDs, protectedFrame, ordered,
                number, timestampMillis, originalLength,
            )

            else -> described(number, timestampMillis, originalLength, reader.limit, "802.11 extension frame")
        }
    }

    private fun parseManagement(
        reader: ByteReader,
        start: Int,
        subtype: Int,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): WlanResult {
        if (start + 24 > reader.limit) {
            return described(number, timestampMillis, originalLength, reader.limit, "Truncated 802.11 management frame")
        }
        val destination = reader.mac(start + 4)
        val source = reader.mac(start + 10)
        val bssid = reader.mac(start + 16)
        val body = start + 24
        val summary = when (subtype) {
            8, 5 -> {
                val ssid = ssidFromTags(reader, body + 12)
                val label = if (subtype == 8) "Beacon" else "Probe response"
                "$label, SSID ${ssid ?: "hidden"}, BSSID $bssid"
            }

            4 -> "Probe request"
            0 -> "Association request"
            1 -> "Association response"
            2 -> "Reassociation request"
            3 -> "Reassociation response"
            11 -> "Authentication"
            10 -> "Disassociation${reasonText(reader, body)}"
            12 -> "Deauthentication${reasonText(reader, body)}"
            13 -> "Action frame"
            else -> "Management subtype $subtype"
        }
        return WlanResult.Described(
            PacketInfo(
                number = number,
                timestampMillis = timestampMillis,
                protocol = PacketProtocol.WLAN,
                source = source,
                destination = destination,
                summary = "802.11 $summary",
                originalLength = originalLength,
                capturedLength = reader.limit,
            ),
        )
    }

    private fun parseData(
        reader: ByteReader,
        start: Int,
        subtype: Int,
        toDs: Boolean,
        fromDs: Boolean,
        protectedFrame: Boolean,
        ordered: Boolean,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
    ): WlanResult {
        var headerLength = 24
        if (toDs && fromDs) headerLength += 6
        val qos = subtype and 0x08 != 0
        if (qos) headerLength += 2
        if (qos && ordered) headerLength += 4
        if (start + headerLength > reader.limit) {
            return described(number, timestampMillis, originalLength, reader.limit, "Truncated 802.11 data frame")
        }
        val addr1 = reader.mac(start + 4)
        val addr2 = reader.mac(start + 10)
        val addr3 = reader.mac(start + 16)
        val source: String
        val destination: String
        when {
            !toDs && fromDs -> {
                destination = addr1
                source = addr3
            }

            toDs && !fromDs -> {
                source = addr2
                destination = addr3
            }

            else -> {
                source = addr2
                destination = addr1
            }
        }
        val bodyOffset = start + headerLength
        val nullFrame = subtype and 0x04 != 0
        if (nullFrame || bodyOffset + 8 > reader.limit) {
            return WlanResult.Described(
                data(reader, number, timestampMillis, originalLength, source, destination, "802.11 data frame without payload"),
            )
        }
        if (protectedFrame) {
            return WlanResult.Described(
                data(
                    reader, number, timestampMillis, originalLength, source, destination,
                    "802.11 encrypted data frame, ${reader.limit - bodyOffset} byte payload",
                ),
            )
        }
        val snap = reader.u8(bodyOffset) == 0xAA && reader.u8(bodyOffset + 1) == 0xAA && reader.u8(bodyOffset + 2) == 0x03
        if (!snap) {
            return WlanResult.Described(
                data(reader, number, timestampMillis, originalLength, source, destination, "802.11 data frame without LLC header"),
            )
        }
        return WlanResult.Payload(
            etherType = reader.u16be(bodyOffset + 6),
            offset = bodyOffset + 8,
            source = source,
            destination = destination,
        )
    }

    private fun data(
        reader: ByteReader,
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
        source: String,
        destination: String,
        summary: String,
    ) = PacketInfo(
        number = number,
        timestampMillis = timestampMillis,
        protocol = PacketProtocol.WLAN,
        source = source,
        destination = destination,
        summary = summary,
        originalLength = originalLength,
        capturedLength = reader.limit,
    )

    private fun described(
        number: Int,
        timestampMillis: Long,
        originalLength: Int,
        capturedLength: Int,
        message: String,
    ) = WlanResult.Described(
        PacketInfo(
            number = number,
            timestampMillis = timestampMillis,
            protocol = PacketProtocol.WLAN,
            source = "802.11",
            destination = "802.11",
            summary = message,
            originalLength = originalLength,
            capturedLength = capturedLength,
        ),
    )

    private fun ssidFromTags(reader: ByteReader, start: Int): String? {
        var offset = start
        while (offset + 2 <= reader.limit) {
            val id = reader.u8(offset)
            val length = reader.u8(offset + 1)
            if (offset + 2 + length > reader.limit) return null
            if (id == 0) {
                if (length == 0) return null
                return String(reader.data, offset + 2, length, Charsets.UTF_8)
                    .filter { it.code in 32..126 || it.code > 127 }
                    .ifBlank { null }
            }
            offset += 2 + length
        }
        return null
    }

    private fun reasonText(reader: ByteReader, body: Int): String {
        if (body + 2 > reader.limit) return ""
        val code = reader.u16le(body)
        val meaning = when (code) {
            1 -> "unspecified"
            2 -> "previous authentication no longer valid"
            3 -> "station is leaving"
            4 -> "inactivity"
            6 -> "class 2 frame from non authenticated station"
            7 -> "class 3 frame from non associated station"
            15 -> "4 way handshake timeout"
            else -> null
        }
        return if (meaning != null) ", reason $code ($meaning)" else ", reason $code"
    }

    private fun controlName(subtype: Int): String = when (subtype) {
        8 -> "block ack request"
        9 -> "block ack"
        10 -> "power save poll"
        11 -> "request to send"
        12 -> "clear to send"
        13 -> "acknowledgement"
        else -> "subtype $subtype"
    }
}
