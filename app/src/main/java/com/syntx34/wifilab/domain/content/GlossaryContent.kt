package com.syntx34.wifilab.domain.content

import com.syntx34.wifilab.domain.model.ResearchDifficulty
import com.syntx34.wifilab.domain.model.ResearchEntry
import com.syntx34.wifilab.domain.model.ResearchSection

object GlossaryContent {

    private fun term(
        id: String,
        title: String,
        subtitle: String,
        overview: String,
        keyPoints: List<String>,
        difficulty: ResearchDifficulty = ResearchDifficulty.FOUNDATION,
    ) = ResearchEntry(
        id = id,
        section = ResearchSection.TERMINOLOGY,
        title = title,
        subtitle = subtitle,
        overview = overview,
        details = emptyList(),
        keyPoints = keyPoints,
        estimatedMinutes = 2,
        difficulty = difficulty,
    )

    val terms: List<ResearchEntry> = listOf(
        term(
            "ssid",
            "SSID",
            "The public name of a wireless network",
            "A Service Set Identifier is the human readable network name carried in beacons and probe responses. It is not a security credential and can be spoofed.",
            listOf("Multiple access points may share an SSID", "Hidden SSIDs are still discoverable", "Never use the SSID as proof of identity"),
        ),
        term(
            "bssid",
            "BSSID",
            "The address of one basic service set",
            "A BSSID is normally the MAC address of the radio interface serving a basic service set. Roaming networks use one BSSID per access point.",
            listOf("BSSIDs identify radios rather than networks", "A vendor prefix may hint at the hardware maker", "Android may hide BSSIDs without Wi Fi permission"),
        ),
        term(
            "rssi",
            "RSSI",
            "Received signal strength indication",
            "RSSI is a relative received signal measurement in dBm. Values closer to zero are stronger. Walls, antenna orientation and channel width all affect it.",
            listOf("Typical useful range is about minus 30 to minus 90 dBm", "Signal strength is not a security property", "Compare measurements from the same device"),
        ),
        term(
            "channel",
            "Wireless channel",
            "A frequency slice used by an access point",
            "Channels are numbered frequency ranges. The 2.4 GHz band has overlapping channels, while 5 GHz and 6 GHz provide many more non overlapping choices.",
            listOf("Channels 1, 6 and 11 are common 2.4 GHz choices", "Wide channels increase throughput and spectrum use", "Channel congestion can reduce real world performance"),
        ),
        term(
            "eapol",
            "EAPOL",
            "Extensible Authentication Protocol over LAN",
            "EAPOL frames carry authentication and key management messages. In Wi Fi they are EtherType 0x888E frames, often used by the four way handshake.",
            listOf("EAPOL type 3 identifies key frames", "Key information bits describe the handshake role", "Captured frames do not contain the plaintext passphrase"),
        ),
        term(
            "four-way-handshake",
            "Four way handshake",
            "The WPA and WPA2 key confirmation exchange",
            "Four EAPOL key messages confirm that both sides possess the pairwise master key and install fresh session keys. Captured metadata can support offline passphrase evaluation.",
            listOf("M1 from access point includes an ANonce", "M2 from client includes an SNonce and MIC", "M3 delivers encrypted key material", "M4 confirms key installation"),
        ),
        term(
            "pmkid",
            "PMKID",
            "A key identifier associated with a pairwise master key",
            "The PMKID can be included in the first EAPOL key message on some access points. It is a small identifier and not the pairwise master key itself.",
            listOf("Presence is visible in packet metadata", "It can be used for offline guessing in lab research", "Modern WPA3 resists this class of evaluation"),
        ),
        term(
            "mic",
            "MIC",
            "Message integrity check",
            "A MIC detects tampering in protected frames. It is derived from key material, so a captured MIC alone cannot decrypt traffic.",
            listOf("Do not confuse MIC with a cryptographic hash", "A valid MIC proves knowledge of a key", "Capture analysis can show MIC presence only"),
        ),
        term(
            "sae",
            "SAE",
            "Simultaneous Authentication of Equals",
            "SAE is the password authenticated key exchange used by WPA3 Personal. It uses a commit and confirm exchange designed to resist offline dictionary attacks.",
            listOf("Also known as Dragonfly", "Provides forward secrecy", "Transition mode often coexists with WPA2"),
        ),
        term(
            "pmf",
            "Protected management frames",
            "Integrity protection for management traffic",
            "PMF protects certain management frames such as deauthentication, disassociation and robust action frames using 802.11w.",
            listOf("Required by WPA3", "Optional in some WPA2 deployments", "Client support can vary on older hardware"),
        ),
        term(
            "beacon",
            "Beacon frame",
            "An access point advertisement",
            "Access points transmit beacons that advertise the SSID, supported rates, security capabilities and other parameters. Beacon analysis is a safe first step in capture study.",
            listOf("Beacons are usually unencrypted", "Capability strings can be misleading", "Hidden SSIDs may appear as blank in beacons"),
        ),
        term(
            "probe",
            "Probe request and response",
            "A discovery exchange between clients and access points",
            "Clients may actively probe for known networks and access points may answer with details. Probe data can expose preferred network names unless randomization is used.",
            listOf("Directed probes can reveal past networks", "Modern operating systems randomize addresses", "Probe analysis is useful for privacy review"),
        ),
        term(
            "arp",
            "ARP",
            "Address Resolution Protocol",
            "ARP maps IPv4 addresses to link layer addresses on a local network. It has no built in authentication, which is why local spoofing concepts appear in security education.",
            listOf("ARP tables can be inspected locally", "Unsolicited ARP replies deserve scrutiny", "Dynamic ARP inspection can help on managed switches"),
        ),
        term(
            "dns",
            "DNS",
            "Domain name system",
            "DNS resolves names to addresses. Plain DNS is usually unencrypted and can reveal browsing patterns or be tampered with on an untrusted network.",
            listOf("DNS over HTTPS or TLS adds transport privacy", "Local DNS caches can be poisoned", "DNSSEC adds origin authentication"),
        ),
        term(
            "dhcp",
            "DHCP",
            "Dynamic host configuration protocol",
            "DHCP provides local address, gateway and DNS configuration. A rogue DHCP server can hand out misleading network settings, so network verification matters.",
            listOf("DHCP leases are usually short lived", "The default gateway is a key configuration value", "DHCP snooping can protect managed networks"),
        ),
        term(
            "gateway",
            "Default gateway",
            "The router address used for off network traffic",
            "A host sends traffic outside its local subnet to the default gateway. On a home network this is normally the router.",
            listOf("Gateway discovery is local and passive", "A changed gateway may indicate a rogue device", "Router administration should use a unique password"),
        ),
        term(
            "subnet",
            "IPv4 subnet",
            "A bounded group of local addresses",
            "A subnet mask separates the network prefix from host bits. WiFiLab limits network audits to small RFC 1918 ranges and requires explicit authorization.",
            listOf("A /24 contains 254 usable host addresses", "Never scan address ranges you do not own", "Private addresses are not automatically safe to scan"),
        ),
        term(
            "mac",
            "MAC address",
            "A link layer hardware identifier",
            "A MAC address is used by Ethernet and Wi Fi link layers. It can often be randomized, so it is not a reliable permanent device identity.",
            listOf("The first three bytes usually identify the vendor", "Android limits access to neighbor MAC addresses", "Use multiple signals before identifying a device"),
        ),
        term(
            "monitor-mode",
            "Monitor mode",
            "A radio mode that receives nearby frames",
            "Monitor mode captures raw 802.11 frames. Standard Android applications usually cannot enter this mode without special hardware or elevated privileges.",
            listOf("WiFiLab does not assume root", "PCAP import supports offline study", "Radiotap headers describe capture radio metadata"),
        ),
        term(
            "pcap",
            "PCAP",
            "A packet capture file format",
            "PCAP files contain captured packets plus timing and interface metadata. PCAPNG is a newer extensible container. Both can be analyzed entirely offline.",
            listOf("WiFiLab accepts user selected files only", "Imported captures stay in app private storage", "No capture data is uploaded"),
        ),
        term(
            "evil-twin",
            "Evil twin",
            "A lookalike access point",
            "An evil twin imitates a trusted network name to attract clients. Users should verify the network and avoid sensitive logins on untrusted access points.",
            listOf("SSID matching is not authentication", "Captive portals can be deceptive", "Prefer known networks and verified certificates"),
        ),
        term(
            "pmk",
            "Pairwise master key",
            "The root key for a pairwise wireless session",
            "The PMK is derived from a passphrase or enterprise authentication. Session keys are derived from it and fresh nonces for each connection.",
            listOf("WPA2 PSK derives the PMK with PBKDF2", "A long random passphrase increases guessing cost", "Never share a home network passphrase publicly"),
        ),
        term(
            "entropy",
            "Password entropy",
            "A measure of uncertainty in a secret",
            "Entropy estimates the number of equally likely guesses an attacker may need. Human chosen patterns reduce real entropy far below a naive character pool calculation.",
            listOf("Length matters more than character variety", "Reused patterns collapse the search space", "Passphrases can be strong and memorable"),
        ),
        term(
            "dictionary-attack",
            "Dictionary attack",
            "Trying likely secrets before exhaustive search",
            "Attackers try common passwords, leaked lists and pattern mutations before brute force. This is why unique long passphrases and a password manager are valuable.",
            listOf("Randomness is important", "Never reuse credentials across sites", "Offline simulations in WiFiLab use synthetic data only"),
        ),
    )
}
