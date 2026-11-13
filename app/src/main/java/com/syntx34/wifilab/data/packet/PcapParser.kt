package com.syntx34.wifilab.data.packet

import com.syntx34.wifilab.domain.model.CaptureFormat
import com.syntx34.wifilab.domain.model.CaptureSummary
import com.syntx34.wifilab.domain.model.PacketInfo
import com.syntx34.wifilab.domain.model.PacketProtocol
import com.syntx34.wifilab.domain.model.ParsedCapture
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class PcapParser @Inject constructor() {

    private val frameParser = FrameParser()

    fun parse(bytes: ByteArray, fileName: String): ParsedCapture {
        if (bytes.size < 4) {
            throw PcapParseException("The selected file is too small to be a packet capture")
        }
        val magic = readU32(bytes, 0, littleEndian = false)
        return when (magic) {
            MAGIC_PCAP_BE -> parseClassic(bytes, fileName, littleEndian = false, nanoseconds = false)
            MAGIC_PCAP_LE -> parseClassic(bytes, fileName, littleEndian = true, nanoseconds = false)
            MAGIC_PCAP_NS_BE -> parseClassic(bytes, fileName, littleEndian = false, nanoseconds = true)
            MAGIC_PCAP_NS_LE -> parseClassic(bytes, fileName, littleEndian = true, nanoseconds = true)
            MAGIC_PCAPNG -> parsePcapNg(bytes, fileName)
            else -> throw PcapParseException(
                "Unrecognized capture format. Use classic PCAP or PCAPNG files.",
            )
        }
    }

    private fun parseClassic(
        bytes: ByteArray,
        fileName: String,
        littleEndian: Boolean,
        nanoseconds: Boolean,
    ): ParsedCapture {
        if (bytes.size < CLASSIC_HEADER_BYTES) {
            throw PcapParseException("Truncated PCAP global header")
        }
        val major = readU16(bytes, 4, littleEndian)
        val minor = readU16(bytes, 6, littleEndian)
        val network = readU32(bytes, 20, littleEndian).toInt() and 0xFFFF
        val warnings = mutableListOf<String>()
        if (major != 2 || minor != 4) {
            warnings += "Unexpected PCAP version $major.$minor; parsing as version 2.4"
        }
        val records = mutableListOf<RawPacket>()
        var offset = CLASSIC_HEADER_BYTES
        while (offset + PACKET_HEADER_BYTES <= bytes.size) {
            if (records.size >= MAX_PACKETS) {
                warnings += "Capture contains more than $MAX_PACKETS packets; the remainder was not parsed"
                break
            }
            val seconds = readU32(bytes, offset, littleEndian)
            val fraction = readU32(bytes, offset + 4, littleEndian)
            val capturedLength = readU32(bytes, offset + 8, littleEndian)
            val originalLength = readU32(bytes, offset + 12, littleEndian)
            if (capturedLength > MAX_PACKET_BYTES) {
                warnings += "A packet length exceeded the parser limit; parsing stopped"
                break
            }
            if (capturedLength > Int.MAX_VALUE / 2) {
                warnings += "A packet length was invalid; parsing stopped"
                break
            }
            val dataStart = offset + PACKET_HEADER_BYTES
            val dataEnd = dataStart + capturedLength.toInt()
            if (dataEnd > bytes.size || dataEnd < dataStart) {
                warnings += "Capture ended in the middle of packet ${records.size + 1}"
                break
            }
            val timestamp = seconds * 1_000L + if (nanoseconds) {
                fraction / 1_000_000L
            } else {
                fraction / 1_000L
            }
            records += RawPacket(
                linkType = network,
                timestampMillis = timestamp,
                originalLength = originalLength.toInt(),
                capturedData = bytes.copyOfRange(dataStart, dataEnd),
            )
            offset = dataEnd
        }
        if (offset < bytes.size && records.size < MAX_PACKETS) {
            warnings += "Trailing bytes were present after the final packet"
        }
        return buildCapture(fileName, CaptureFormat.PCAP, records, warnings)
    }

    private fun parsePcapNg(bytes: ByteArray, fileName: String): ParsedCapture {
        val records = mutableListOf<RawPacket>()
        val warnings = mutableListOf<String>()
        val interfaces = mutableListOf<InterfaceInfo>()
        var offset = 0
        var littleEndian = false
        var sectionCount = 0
        while (offset + BLOCK_HEADER_BYTES <= bytes.size) {
            val isSectionHeader = bytes[offset] == 0x0A.toByte() &&
                bytes[offset + 1] == 0x0D.toByte() &&
                bytes[offset + 2] == 0x0D.toByte() &&
                bytes[offset + 3] == 0x0A.toByte()
            if (isSectionHeader) {
                if (offset + 12 > bytes.size) {
                    warnings += "Truncated PCAPNG section header"
                    break
                }
                val byteOrderMagic = readU32(bytes, offset + 8, littleEndian = false)
                littleEndian = when (byteOrderMagic) {
                    BYTE_ORDER_MAGIC_BE -> false
                    BYTE_ORDER_MAGIC_LE -> true
                    else -> {
                        throw PcapParseException("Invalid PCAPNG byte order marker")
                    }
                }
                val blockLength = readU32(bytes, offset + 4, littleEndian)
                if (!validBlock(bytes, offset, blockLength, littleEndian)) {
                    throw PcapParseException("Invalid PCAPNG section length")
                }
                interfaces.clear()
                sectionCount++
                offset += blockLength.toInt()
                continue
            }
            if (sectionCount == 0) {
                throw PcapParseException("PCAPNG data does not begin with a section header")
            }
            if (records.size >= MAX_PACKETS) {
                warnings += "Capture contains more than $MAX_PACKETS packets; the remainder was not parsed"
                break
            }
            val blockType = readU32(bytes, offset, littleEndian)
            val totalLength = readU32(bytes, offset + 4, littleEndian)
            if (!validBlock(bytes, offset, totalLength, littleEndian)) {
                warnings += "Invalid PCAPNG block length at byte $offset; parsing stopped"
                break
            }
            val blockEnd = offset + totalLength.toInt()
            when (blockType) {
                BLOCK_INTERFACE_DESCRIPTION -> {
                    if (offset + 16 <= blockEnd) {
                        val linkType = readU16(bytes, offset + 8, littleEndian)
                        val snapLength = readU32(bytes, offset + 12, littleEndian).toInt()
                        val units = parseTimestampResolution(bytes, offset + 16, blockEnd - 4, littleEndian)
                        interfaces += InterfaceInfo(linkType, snapLength, units)
                    } else {
                        warnings += "A truncated interface description block was skipped"
                    }
                }

                BLOCK_ENHANCED_PACKET -> {
                    if (offset + 28 > blockEnd) {
                        warnings += "A truncated enhanced packet block was skipped"
                    } else {
                        val interfaceId = readU32(bytes, offset + 8, littleEndian).toInt()
                        val timestampHigh = readU32(bytes, offset + 12, littleEndian)
                        val timestampLow = readU32(bytes, offset + 16, littleEndian)
                        val capturedLength = readU32(bytes, offset + 20, littleEndian)
                        val originalLength = readU32(bytes, offset + 24, littleEndian)
                        val dataStart = offset + 28
                        val dataEnd = dataStart + capturedLength.toInt()
                        val interfaceInfo = interfaces.getOrNull(interfaceId)
                        if (interfaceInfo == null) {
                            warnings += "A packet referenced a missing PCAPNG interface"
                        } else if (capturedLength > MAX_PACKET_BYTES || dataEnd > blockEnd) {
                            warnings += "A PCAPNG packet length was invalid and was skipped"
                        } else {
                            val rawTimestamp = (timestampHigh shl 32) + timestampLow
                            val timestamp = (rawTimestamp.toDouble() / interfaceInfo.unitsPerSecond * 1_000.0).toLong()
                            records += RawPacket(
                                linkType = interfaceInfo.linkType,
                                timestampMillis = timestamp,
                                originalLength = originalLength.toInt(),
                                capturedData = bytes.copyOfRange(dataStart, dataEnd),
                            )
                        }
                    }
                }

                BLOCK_SIMPLE_PACKET -> {
                    if (offset + 16 > blockEnd) {
                        warnings += "A truncated simple packet block was skipped"
                    } else {
                        val originalLength = readU32(bytes, offset + 8, littleEndian)
                        val dataStart = offset + 12
                        val available = blockEnd - 4 - dataStart
                        val captured = minOf(originalLength.toInt(), available)
                        if (captured > 0 && captured <= MAX_PACKET_BYTES) {
                            val interfaceInfo = interfaces.firstOrNull()
                            if (interfaceInfo == null) {
                                warnings += "A simple packet referenced a missing PCAPNG interface"
                            } else {
                                records += RawPacket(
                                    linkType = interfaceInfo.linkType,
                                    timestampMillis = 0L,
                                    originalLength = originalLength.toInt(),
                                    capturedData = bytes.copyOfRange(dataStart, dataStart + captured),
                                )
                            }
                        }
                    }
                }

                else -> Unit
            }
            offset = blockEnd
        }
        if (sectionCount == 0) {
            throw PcapParseException("No PCAPNG section header was found")
        }
        return buildCapture(fileName, CaptureFormat.PCAPNG, records, warnings)
    }

    private fun parseTimestampResolution(
        bytes: ByteArray,
        start: Int,
        end: Int,
        littleEndian: Boolean,
    ): Double {
        var offset = start
        var units = 1_000_000.0
        while (offset + 4 <= end) {
            val code = readU16(bytes, offset, littleEndian)
            val length = readU16(bytes, offset + 2, littleEndian)
            val valueOffset = offset + 4
            if (code == 0 || length == 0) break
            if (valueOffset + length > end) break
            if (code == OPTION_TS_RESOLUTION && length >= 1) {
                val raw = bytes[valueOffset].toInt() and 0xFF
                val highBit = raw and 0x80 != 0
                val exponent = raw and 0x7F
                units = if (highBit) {
                    2.0.pow(exponent.toDouble())
                } else {
                    10.0.pow(exponent.toDouble())
                }
            }
            offset = valueOffset + ((length + 3) / 4) * 4
        }
        if (!units.isFinite() || units <= 0.0) return 1_000_000.0
        return units
    }

    private fun validBlock(
        bytes: ByteArray,
        offset: Int,
        totalLength: Long,
        littleEndian: Boolean,
    ): Boolean {
        if (totalLength < BLOCK_HEADER_BYTES || totalLength % 4L != 0L) return false
        if (totalLength > Int.MAX_VALUE) return false
        val end = offset + totalLength.toInt()
        if (end > bytes.size) return false
        val trailer = readU32(bytes, end - 4, littleEndian)
        return trailer == totalLength
    }

    private fun buildCapture(
        fileName: String,
        format: CaptureFormat,
        records: List<RawPacket>,
        initialWarnings: List<String>,
    ): ParsedCapture {
        val warnings = initialWarnings.toMutableList()
        val packets = records.mapIndexedNotNull { index, raw ->
            runCatching {
                frameParser.parse(
                    data = raw.capturedData,
                    linkType = raw.linkType,
                    number = index + 1,
                    timestampMillis = raw.timestampMillis,
                    originalLength = raw.originalLength,
                )
            }.getOrElse {
                warnings += "Packet ${index + 1} could not be fully parsed"
                PacketInfo(
                    number = index + 1,
                    timestampMillis = raw.timestampMillis,
                    protocol = PacketProtocol.OTHER,
                    source = "Unknown",
                    destination = "Unknown",
                    summary = "Malformed or unsupported packet",
                    originalLength = raw.originalLength,
                    capturedLength = raw.capturedData.size,
                )
            }
        }
        val counts = mutableMapOf<PacketProtocol, Int>()
        packets.forEach { packet ->
            counts[packet.protocol] = (counts[packet.protocol] ?: 0) + 1
        }
        val timestamps = packets.map { it.timestampMillis }.filter { it > 0L }
        val duration = if (timestamps.isEmpty()) 0L else {
            (timestamps.maxOrNull() ?: 0L) - (timestamps.minOrNull() ?: 0L)
        }
        val summary = CaptureSummary(
            format = format,
            packetCount = packets.size,
            eapolCount = counts[PacketProtocol.EAPOL] ?: 0,
            durationMillis = duration.coerceAtLeast(0L),
            protocolCounts = counts,
            warnings = warnings.distinct().take(MAX_WARNINGS),
        )
        return ParsedCapture(fileName = fileName, summary = summary, packets = packets)
    }

    private fun readU16(bytes: ByteArray, offset: Int, littleEndian: Boolean): Int {
        if (offset < 0 || offset + 2 > bytes.size) throw PcapParseException("Unexpected end of file")
        return if (littleEndian) {
            (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)
        } else {
            ((bytes[offset].toInt() and 0xFF) shl 8) or (bytes[offset + 1].toInt() and 0xFF)
        }
    }

    private fun readU32(bytes: ByteArray, offset: Int, littleEndian: Boolean): Long {
        if (offset < 0 || offset + 4 > bytes.size) throw PcapParseException("Unexpected end of file")
        var value = 0L
        if (littleEndian) {
            for (index in 3 downTo 0) {
                value = (value shl 8) or (bytes[offset + index].toLong() and 0xFF)
            }
        } else {
            for (index in 0 until 4) {
                value = (value shl 8) or (bytes[offset + index].toLong() and 0xFF)
            }
        }
        return value
    }

    private data class RawPacket(
        val linkType: Int,
        val timestampMillis: Long,
        val originalLength: Int,
        val capturedData: ByteArray,
    )

    private data class InterfaceInfo(
        val linkType: Int,
        val snapLength: Int,
        val unitsPerSecond: Double,
    )

    companion object {
        private const val CLASSIC_HEADER_BYTES = 24
        private const val PACKET_HEADER_BYTES = 16
        private const val BLOCK_HEADER_BYTES = 12
        private const val MAX_PACKETS = 50_000
        private const val MAX_WARNINGS = 24
        private const val MAX_PACKET_BYTES = 16L * 1024L * 1024L
        private const val MAGIC_PCAP_BE = 0xA1B2C3D4L
        private const val MAGIC_PCAP_LE = 0xD4C3B2A1L
        private const val MAGIC_PCAP_NS_BE = 0xA1B23C4DL
        private const val MAGIC_PCAP_NS_LE = 0x4D3CB2A1L
        private const val MAGIC_PCAPNG = 0x0A0D0D0AL
        private const val BYTE_ORDER_MAGIC_BE = 0x1A2B3C4DL
        private const val BYTE_ORDER_MAGIC_LE = 0x4D3C2B1AL
        private const val BLOCK_INTERFACE_DESCRIPTION = 1L
        private const val BLOCK_SIMPLE_PACKET = 3L
        private const val BLOCK_ENHANCED_PACKET = 6L
        private const val OPTION_TS_RESOLUTION = 9
    }
}

class PcapParseException(message: String) : Exception(message)
