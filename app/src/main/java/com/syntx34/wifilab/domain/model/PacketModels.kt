package com.syntx34.wifilab.domain.model

enum class CaptureFormat(val label: String) {
    PCAP("Classic PCAP"),
    PCAPNG("PCAPNG"),
}

enum class PacketProtocol(val label: String) {
    EAPOL("EAPOL"),
    WLAN("802.11"),
    ARP("ARP"),
    DNS("DNS"),
    DHCP("DHCP"),
    ICMP("ICMP"),
    TCP("TCP"),
    UDP("UDP"),
    IPV6("IPv6"),
    OTHER("Other"),
}

data class EapolKeyDetails(
    val eapolVersion: Int,
    val packetType: Int,
    val eapolLength: Int,
    val descriptorType: Int,
    val keyInfo: Int,
    val keyLength: Int,
    val replayCounter: Long,
    val messageNumber: Int?,
    val ack: Boolean,
    val mic: Boolean,
    val install: Boolean,
    val secure: Boolean,
    val encryptedKeyData: Boolean,
    val pmkidPresent: Boolean,
    val noncePresent: Boolean,
    val keyDataLength: Int,
) {
    val messageLabel: String
        get() = when (messageNumber) {
            1 -> "EAPOL Key M1"
            2 -> "EAPOL Key M2"
            3 -> "EAPOL Key M3"
            4 -> "EAPOL Key M4"
            else -> "EAPOL Key"
        }
}

data class DnsDetails(
    val transactionId: Int,
    val isResponse: Boolean,
    val questions: List<String>,
    val answers: List<String>,
    val responseCode: Int,
)

data class ArpDetails(
    val operation: Int,
    val senderIp: String,
    val senderMac: String,
    val targetIp: String,
    val targetMac: String,
) {
    val operationLabel: String
        get() = when (operation) {
            1 -> "Request"
            2 -> "Reply"
            else -> "Operation $operation"
        }
}

data class PacketInfo(
    val number: Int,
    val timestampMillis: Long,
    val protocol: PacketProtocol,
    val source: String,
    val destination: String,
    val summary: String,
    val originalLength: Int,
    val capturedLength: Int,
    val eapol: EapolKeyDetails? = null,
    val dns: DnsDetails? = null,
    val arp: ArpDetails? = null,
    val payloadPreview: String? = null,
)

data class CaptureSummary(
    val format: CaptureFormat,
    val packetCount: Int,
    val eapolCount: Int,
    val durationMillis: Long,
    val protocolCounts: Map<PacketProtocol, Int>,
    val warnings: List<String>,
)

data class ParsedCapture(
    val fileName: String,
    val summary: CaptureSummary,
    val packets: List<PacketInfo>,
)

data class StoredCapture(
    val id: Long,
    val fileName: String,
    val importedAt: Long,
    val sizeBytes: Long,
    val packetCount: Int,
    val eapolCount: Int,
    val durationMillis: Long,
)
