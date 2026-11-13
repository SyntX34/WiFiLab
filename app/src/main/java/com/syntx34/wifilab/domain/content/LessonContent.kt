package com.syntx34.wifilab.domain.content

import com.syntx34.wifilab.domain.model.ResearchDifficulty
import com.syntx34.wifilab.domain.model.ResearchEntry
import com.syntx34.wifilab.domain.model.ResearchSection

object LessonContent {
    val lessons: List<ResearchEntry> = listOf(
        ResearchEntry(
            id = "lesson-beacon",
            section = ResearchSection.PACKET_LESSON,
            title = "Read a wireless beacon",
            subtitle = "Separate identity, capability and radio metadata",
            overview = "A beacon is an access point advertisement. Learning which fields are trustworthy helps you read scanner results without overstating what the device can prove.",
            details = listOf(
                "The SSID is a label and can be imitated by any access point.",
                "The BSSID identifies a radio interface, not a brand or a security guarantee.",
                "Supported rates and capability strings describe the advertised configuration.",
                "RSSI describes the receiver and environment as much as the transmitter.",
            ),
            keyPoints = listOf(
                "Treat SSID as untrusted text",
                "Compare security warnings against the advertised capability string",
                "Use channel and RSSI for performance analysis rather than trust decisions",
            ),
            estimatedMinutes = 5,
            difficulty = ResearchDifficulty.FOUNDATION,
        ),
        ResearchEntry(
            id = "lesson-handshake",
            section = ResearchSection.PACKET_LESSON,
            title = "Follow a WPA2 four way handshake",
            subtitle = "Identify EAPOL key messages by their control bits",
            overview = "The four way handshake confirms that a client and access point share a pairwise master key. Packet analysis can identify the exchange stages without exposing the passphrase.",
            details = listOf(
                "M1 travels from the access point to the client and usually contains the ANonce.",
                "M2 travels to the access point with the SNonce and a MIC.",
                "M3 carries encrypted key data and installs the pairwise key.",
                "M4 confirms key installation. No plaintext password appears in any message.",
            ),
            keyPoints = listOf(
                "ACK and MIC bits help distinguish the four messages",
                "Replay counters are visible metadata",
                "PMKID presence is not a recovered password",
            ),
            steps = listOf(
                "Import a capture in Packet Analyzer.",
                "Filter the packet list to EAPOL.",
                "Open each key frame and read its message number and flags.",
                "Write down the replay counter progression without attempting recovery.",
            ),
            reflectionQuestions = listOf(
                "Which message carries the access point nonce?",
                "Why does a captured MIC not reveal the password?",
                "Which defense reduces offline passphrase evaluation risk?",
            ),
            estimatedMinutes = 10,
            difficulty = ResearchDifficulty.INTERMEDIATE,
        ),
        ResearchEntry(
            id = "lesson-arp",
            section = ResearchSection.PACKET_LESSON,
            title = "Understand ARP on a home network",
            subtitle = "Map addresses and spot unauthenticated bindings",
            overview = "ARP translates IPv4 addresses into link layer addresses. Because it lacks authentication, local devices can be tricked into associating an address with the wrong hardware.",
            details = listOf(
                "ARP requests are usually broadcast to every local host.",
                "ARP replies can be sent without a matching request.",
                "The operating system maintains a short lived neighbor cache.",
                "A duplicate gateway mapping is a useful local anomaly indicator.",
            ),
            keyPoints = listOf(
                "Correlate ARP observations with DHCP and router records",
                "Do not modify third party traffic",
                "Prefer encrypted protocols and client isolation on guest networks",
            ),
            estimatedMinutes = 7,
            difficulty = ResearchDifficulty.FOUNDATION,
        ),
        ResearchEntry(
            id = "lesson-dns",
            section = ResearchSection.PACKET_LESSON,
            title = "Trace a DNS lookup",
            subtitle = "Read questions, answers and response codes",
            overview = "DNS queries and responses are compact and revealing. A controlled capture shows how a name becomes an address and where integrity protections fit.",
            details = listOf(
                "The transaction identifier matches a query to its response.",
                "Question sections carry names and record types.",
                "Answers can be cached and do not always come from an authoritative server.",
                "Plain UDP DNS has no transport authentication by itself.",
            ),
            keyPoints = listOf(
                "Use a local test domain or synthetic capture",
                "Compare answers from two trusted resolvers",
                "Enable encrypted DNS when it fits the network policy",
            ),
            estimatedMinutes = 8,
            difficulty = ResearchDifficulty.FOUNDATION,
        ),
        ResearchEntry(
            id = "lesson-baseline",
            section = ResearchSection.PACKET_LESSON,
            title = "Build a home network baseline",
            subtitle = "Turn a one time scan into a useful comparison",
            overview = "A baseline is a reviewed snapshot of devices you expect on your network. Comparisons reveal additions, but IP addresses can change, so treat differences as prompts for investigation.",
            details = listOf(
                "Audit only networks you own or are explicitly authorized to test.",
                "Verify new devices before classifying them as unwanted.",
                "Save a new baseline after intentional changes.",
                "Device identification is limited by the Android sandbox and network configuration.",
            ),
            keyPoints = listOf(
                "Baselines need periodic review",
                "Use router logs for a second source of truth",
                "Do not block devices automatically from an unverified scan",
            ),
            steps = listOf(
                "Confirm authorization in Network Auditor.",
                "Scan the local /24 or smaller range.",
                "Identify the gateway and all expected devices.",
                "Save the reviewed result as a trusted baseline.",
            ),
            estimatedMinutes = 12,
            difficulty = ResearchDifficulty.FOUNDATION,
        ),
        ResearchEntry(
            id = "lesson-pcap",
            section = ResearchSection.PACKET_LESSON,
            title = "Analyze a PCAP safely",
            subtitle = "Protect privacy while learning packet structure",
            overview = "Packet captures can contain addresses, names and application data. Treat them as sensitive research material even when they do not include passwords.",
            details = listOf(
                "Prefer synthetic or self generated captures for exercises.",
                "Remove or avoid sharing captures from networks you do not own.",
                "Do not attempt credential recovery on data you are not authorized to possess.",
                "WiFiLab parses files locally and never uploads them.",
            ),
            keyPoints = listOf(
                "Captures are sensitive data",
                "Use offline demos for classroom work",
                "Delete old research files when they are no longer needed",
            ),
            estimatedMinutes = 6,
            difficulty = ResearchDifficulty.INTERMEDIATE,
        ),
        ResearchEntry(
            id = "lesson-passwords",
            section = ResearchSection.PACKET_LESSON,
            title = "Evaluate a password policy",
            subtitle = "Measure entropy instead of relying on symbols",
            overview = "Password strength depends on unpredictability. A short password with every character class can still be much weaker than a long randomly generated passphrase.",
            details = listOf(
                "Length multiplies the search space more reliably than one extra symbol.",
                "Common words and personal patterns defeat naive pool calculations.",
                "Offline guessing speed depends on the hash and the attacker hardware.",
                "A password manager makes unique long credentials practical.",
            ),
            keyPoints = listOf(
                "Compare effective entropy, not just character classes",
                "Reject reused and common passwords",
                "Use multi factor authentication wherever possible",
            ),
            steps = listOf(
                "Open Password Lab and enter a sample non sensitive passphrase.",
                "Compare the raw and effective entropy values.",
                "Review each pattern warning.",
                "Try a longer random passphrase and compare the simulated time.",
            ),
            estimatedMinutes = 8,
            difficulty = ResearchDifficulty.FOUNDATION,
        ),
        ResearchEntry(
            id = "lesson-router",
            section = ResearchSection.PACKET_LESSON,
            title = "Harden a home router",
            subtitle = "Practical steps after an audit",
            overview = "Most home network improvements come from a short set of durable changes. Prioritize firmware, administration, wireless configuration and guest isolation.",
            details = listOf(
                "Enable automatic firmware updates and replace devices that no longer receive them.",
                "Use a unique router administrator password and disable remote administration.",
                "Prefer WPA3 or WPA2 AES with a long random passphrase.",
                "Disable WPS and unused services, then segment guest and smart devices.",
            ),
            keyPoints = listOf(
                "Router administration should never be exposed to the internet",
                "Guest isolation limits lateral movement",
                "Review connected devices periodically",
            ),
            estimatedMinutes = 10,
            difficulty = ResearchDifficulty.FOUNDATION,
        ),
    )
}
