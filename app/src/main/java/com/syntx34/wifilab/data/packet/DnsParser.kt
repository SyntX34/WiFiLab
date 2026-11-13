package com.syntx34.wifilab.data.packet

import com.syntx34.wifilab.domain.model.DnsDetails

internal object DnsParser {

    fun parse(reader: ByteReader, offset: Int): DnsDetails? {
        if (offset + 12 > reader.limit) return null
        return runCatching {
            val transactionId = reader.u16be(offset)
            val flags = reader.u16be(offset + 2)
            val questionCount = reader.u16be(offset + 4).coerceAtMost(MAX_RECORDS)
            val answerCount = reader.u16be(offset + 6).coerceAtMost(MAX_RECORDS)
            val isResponse = flags and 0x8000 != 0
            val responseCode = flags and 0x000F
            var cursor = offset + 12
            val questions = mutableListOf<String>()
            repeat(questionCount) {
                val nameResult = readName(reader, cursor)
                cursor = nameResult.second
                if (cursor + 4 > reader.limit) return null
                val type = reader.u16be(cursor)
                cursor += 4
                questions += "${nameResult.first} ${recordTypeName(type)}"
            }
            val answers = mutableListOf<String>()
            repeat(answerCount) {
                val nameResult = readName(reader, cursor)
                cursor = nameResult.second
                if (cursor + 10 > reader.limit) return null
                val type = reader.u16be(cursor)
                val recordClass = reader.u16be(cursor + 2)
                val ttl = reader.u32be(cursor + 4)
                val dataLength = reader.u16be(cursor + 8)
                cursor += 10
                if (cursor + dataLength > reader.limit) return null
                val value = when (type) {
                    1 -> if (dataLength == 4) reader.ipv4(cursor) else "${dataLength} byte record"
                    28 -> if (dataLength == 16) reader.ipv6(cursor) else "${dataLength} byte record"
                    5 -> readName(reader, cursor).first
                    15 -> if (dataLength >= 3) {
                        val preference = reader.u16be(cursor)
                        val exchange = readName(reader, cursor + 2).first
                        "$preference $exchange"
                    } else {
                        "${dataLength} byte record"
                    }

                    else -> "${dataLength} byte ${recordTypeName(type)} record"
                }
                val classLabel = if (recordClass == 1) "" else " class $recordClass"
                answers += "${nameResult.first}${classLabel} ttl $ttl ${recordTypeName(type)} $value"
                cursor += dataLength
            }
            DnsDetails(
                transactionId = transactionId,
                isResponse = isResponse,
                questions = questions,
                answers = answers,
                responseCode = responseCode,
            )
        }.getOrNull()
    }

    private fun readName(reader: ByteReader, startOffset: Int): Pair<String, Int> {
        var cursor = startOffset
        var nextOffset = -1
        var jumps = 0
        val labels = mutableListOf<String>()
        val visited = mutableSetOf<Int>()
        while (cursor in 0 until reader.limit) {
            if (!visited.add(cursor) || jumps > MAX_JUMPS) {
                throw PacketParseException("DNS compression pointer loop")
            }
            val length = reader.u8(cursor)
            when {
                length == 0 -> {
                    cursor += 1
                    if (nextOffset < 0) nextOffset = cursor
                    break
                }

                length and 0xC0 == 0xC0 -> {
                    if (cursor + 1 >= reader.limit) throw PacketParseException("DNS pointer truncation")
                    val pointer = ((length and 0x3F) shl 8) or reader.u8(cursor + 1)
                    if (nextOffset < 0) nextOffset = cursor + 2
                    cursor = pointer
                    jumps++
                }

                length and 0xC0 != 0 -> throw PacketParseException("Unsupported DNS label")
                else -> {
                    if (cursor + 1 + length > reader.limit) {
                        throw PacketParseException("DNS label truncation")
                    }
                    val label = buildString {
                        for (index in 0 until length) {
                            val value = reader.u8(cursor + 1 + index)
                            append(if (value in 32..126) value.toChar() else '.')
                        }
                    }
                    labels += label
                    cursor += 1 + length
                }
            }
        }
        if (nextOffset < 0) nextOffset = cursor
        val name = labels.joinToString(".").ifEmpty { "(root)" }
        return name to nextOffset
    }

    private fun recordTypeName(type: Int): String = when (type) {
        1 -> "A"
        2 -> "NS"
        5 -> "CNAME"
        6 -> "SOA"
        12 -> "PTR"
        15 -> "MX"
        16 -> "TXT"
        28 -> "AAAA"
        33 -> "SRV"
        41 -> "OPT"
        65 -> "HTTPS"
        else -> "type $type"
    }

    private const val MAX_RECORDS = 12
    private const val MAX_JUMPS = 24
}
