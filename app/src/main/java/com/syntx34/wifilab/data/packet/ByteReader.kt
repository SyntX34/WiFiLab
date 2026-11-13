package com.syntx34.wifilab.data.packet

import java.net.InetAddress

internal class ByteReader(
    val data: ByteArray,
    val limit: Int = data.size,
) {
    fun requireRange(offset: Int, length: Int) {
        if (offset < 0 || length < 0 || offset > limit || offset + length > limit) {
            throw PacketParseException("Byte range outside packet boundary")
        }
    }

    fun u8(offset: Int): Int {
        requireRange(offset, 1)
        return data[offset].toInt() and 0xFF
    }

    fun u16be(offset: Int): Int {
        requireRange(offset, 2)
        return ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)
    }

    fun u16le(offset: Int): Int {
        requireRange(offset, 2)
        return (data[offset].toInt() and 0xFF) or ((data[offset + 1].toInt() and 0xFF) shl 8)
    }

    fun u32be(offset: Int): Long {
        requireRange(offset, 4)
        var value = 0L
        for (index in 0 until 4) {
            value = (value shl 8) or (data[offset + index].toLong() and 0xFF)
        }
        return value
    }

    fun u64be(offset: Int): Long {
        requireRange(offset, 8)
        var value = 0L
        for (index in 0 until 8) {
            value = (value shl 8) or (data[offset + index].toLong() and 0xFF)
        }
        return value
    }

    fun mac(offset: Int): String {
        requireRange(offset, 6)
        return (0 until 6).joinToString(":") { index ->
            "%02x".format(data[offset + index].toInt() and 0xFF)
        }
    }

    fun ipv4(offset: Int): String {
        requireRange(offset, 4)
        return (0 until 4).joinToString(".") { index ->
            (data[offset + index].toInt() and 0xFF).toString()
        }
    }

    fun ipv6(offset: Int): String {
        requireRange(offset, 16)
        val bytes = data.copyOfRange(offset, offset + 16)
        return runCatching { InetAddress.getByAddress(bytes).hostAddress.orEmpty() }
            .getOrDefault("IPv6 address unavailable")
    }

    fun asciiPreview(offset: Int, maxBytes: Int): String? {
        if (offset !in 0 until limit || maxBytes <= 0) return null
        val end = minOf(limit, offset + maxBytes)
        if (end <= offset) return null
        val builder = StringBuilder()
        for (index in offset until end) {
            val value = data[index].toInt() and 0xFF
            builder.append(if (value in 32..126) value.toChar() else '.')
        }
        return builder.toString()
    }

    fun absoluteU16be(offset: Int): Int = u16be(offset)

    fun absoluteU32be(offset: Int): Long = u32be(offset)
}

internal class PacketParseException(message: String) : Exception(message)
