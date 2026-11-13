package com.syntx34.wifilab.data.packet

import com.syntx34.wifilab.domain.model.EapolKeyDetails

internal data class EapolParseResult(
    val details: EapolKeyDetails?,
    val summary: String,
)

internal object EapolParser {

    fun parse(reader: ByteReader, offset: Int): EapolParseResult {
        if (offset + 4 > reader.limit) {
            return EapolParseResult(null, "Truncated EAPOL header")
        }
        val version = reader.u8(offset)
        val packetType = reader.u8(offset + 1)
        val length = reader.u16be(offset + 2)
        val typeLabel = when (packetType) {
            0 -> "EAP packet"
            1 -> "EAPOL start"
            2 -> "EAPOL logoff"
            3 -> "EAPOL key"
            4 -> "EAPOL ASF alert"
            else -> "EAPOL type $packetType"
        }
        if (packetType != 3) {
            return EapolParseResult(null, "$typeLabel, version $version, length $length")
        }
        val keyOffset = offset + 4
        if (keyOffset + 95 > reader.limit) {
            return EapolParseResult(null, "Truncated EAPOL key frame")
        }
        val descriptorType = reader.u8(keyOffset)
        val keyInfo = reader.u16be(keyOffset + 1)
        val keyLength = reader.u16be(keyOffset + 3)
        val replayCounter = reader.u64be(keyOffset + 5)
        val nonceOffset = keyOffset + 13
        val noncePresent = (0 until 32).any { reader.u8(nonceOffset + it) != 0 }
        val keyDataLengthOffset = keyOffset + 93
        val keyDataLength = reader.u16be(keyDataLengthOffset)
        val ack = keyInfo and 0x0080 != 0
        val mic = keyInfo and 0x0100 != 0
        val secure = keyInfo and 0x0200 != 0
        val install = keyInfo and 0x0040 != 0
        val pairwise = keyInfo and 0x0008 != 0
        val encrypted = keyInfo and 0x1000 != 0
        val keyDataOffset = keyDataLengthOffset + 2
        val pmkidPresent = !encrypted && containsPmkid(reader, keyDataOffset, keyDataLength)
        val messageNumber = if (pairwise) {
            inferMessageNumber(ack = ack, mic = mic, install = install, secure = secure)
        } else {
            null
        }
        val details = EapolKeyDetails(
            eapolVersion = version,
            packetType = packetType,
            eapolLength = length,
            descriptorType = descriptorType,
            keyInfo = keyInfo,
            keyLength = keyLength,
            replayCounter = replayCounter,
            messageNumber = messageNumber,
            ack = ack,
            mic = mic,
            install = install,
            secure = secure,
            encryptedKeyData = encrypted,
            pmkidPresent = pmkidPresent,
            noncePresent = noncePresent,
            keyDataLength = keyDataLength,
        )
        val messageLabel = details.messageLabel
        val direction = when {
            ack -> "access point to client"
            else -> "client to access point"
        }
        val pmkidLabel = if (pmkidPresent) ", PMKID present" else ""
        return EapolParseResult(
            details = details,
            summary = "$messageLabel, $direction$pmkidLabel",
        )
    }

    private fun inferMessageNumber(
        ack: Boolean,
        mic: Boolean,
        install: Boolean,
        secure: Boolean,
    ): Int? = when {
        ack && !mic -> 1
        ack && mic && install -> 3
        !ack && mic && secure -> 4
        !ack && mic -> 2
        else -> null
    }

    private fun containsPmkid(reader: ByteReader, start: Int, length: Int): Boolean {
        if (length <= 0) return false
        val end = minOf(reader.limit, start + length)
        var offset = start
        while (offset + 2 <= end) {
            val type = reader.u8(offset)
            val itemLength = reader.u8(offset + 1)
            val valueOffset = offset + 2
            val valueEnd = valueOffset + itemLength
            if (valueEnd > end) return false
            if (type == 0xDD && itemLength >= 4) {
                val oui0 = reader.u8(valueOffset)
                val oui1 = reader.u8(valueOffset + 1)
                val oui2 = reader.u8(valueOffset + 2)
                val dataType = reader.u8(valueOffset + 3)
                if (oui0 == 0x00 && oui1 == 0x0F && oui2 == 0xAC && dataType == 4) {
                    return true
                }
            }
            offset = valueEnd
        }
        return false
    }
}
