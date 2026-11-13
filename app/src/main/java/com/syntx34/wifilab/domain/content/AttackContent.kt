package com.syntx34.wifilab.domain.content

import com.syntx34.wifilab.domain.model.AttackScenario
import com.syntx34.wifilab.domain.model.SimulationActor
import com.syntx34.wifilab.domain.model.SimulationStage

object AttackContent {
    val scenarios: List<AttackScenario> = listOf(
        AttackScenario(
            id = "arp-spoofing",
            title = "ARP spoofing",
            subtitle = "How an unauthenticated address mapping can misdirect local traffic",
            riskLevel = "High local impact",
            concept = "ARP has no built in authentication. On a local network, a hostile device can claim that the gateway address belongs to its own link layer address, causing traffic to pass through or toward the wrong host.",
            stages = listOf(
                SimulationStage(
                    "Observe a normal mapping",
                    "The client knows the gateway IPv4 address and has learned the gateway link layer address through ARP.",
                    SimulationActor.CLIENT,
                ),
                SimulationStage(
                    "Inject a false reply",
                    "In this isolated simulation, the hostile node describes an unsolicited mapping. No real frame is sent.",
                    SimulationActor.ATTACKER,
                ),
                SimulationStage(
                    "Cache replacement",
                    "The client neighbor cache accepts the newer mapping and starts sending off network traffic to the simulated hostile node.",
                    SimulationActor.CLIENT,
                ),
                SimulationStage(
                    "Detection and recovery",
                    "A duplicate gateway mapping, changed latency or router logs can reveal the problem. Reconnecting clears the neighbor cache.",
                    SimulationActor.NETWORK,
                ),
            ),
            defenses = listOf(
                "Use encrypted application protocols so local interception does not reveal plaintext.",
                "Enable dynamic ARP inspection where the network equipment supports it.",
                "Monitor the router device list and gateway mappings.",
                "Isolate guest and untrusted devices from trusted clients.",
            ),
            takeaway = "ARP spoofing is a local trust problem. The lab demonstrates the logic with fixed actors and never generates network traffic.",
        ),
        AttackScenario(
            id = "dns-spoofing",
            title = "DNS spoofing",
            subtitle = "Why a false answer can redirect a name without changing the address bar",
            riskLevel = "High when DNS is unencrypted",
            concept = "A device asks a resolver to translate a name into an address. If a hostile local actor can answer first or modify the path, the client may connect to an unintended address.",
            stages = listOf(
                SimulationStage(
                    "Client asks a question",
                    "The simulated client requests the address for a demonstration domain.",
                    SimulationActor.CLIENT,
                ),
                SimulationStage(
                    "A fast false reply",
                    "The simulated hostile node returns an answer before the legitimate resolver response arrives.",
                    SimulationActor.ATTACKER,
                ),
                SimulationStage(
                    "Unexpected address",
                    "The client caches the false answer and contacts the simulated address.",
                    SimulationActor.CLIENT,
                ),
                SimulationStage(
                    "Integrity controls",
                    "Encrypted DNS, DNSSEC validation and certificate checks reduce different parts of this risk.",
                    SimulationActor.DNS,
                ),
            ),
            defenses = listOf(
                "Use encrypted DNS where the device and network policy permit it.",
                "Rely on TLS certificate validation for websites and applications.",
                "Compare answers from a trusted out of band resolver.",
                "Keep router and client firmware updated.",
            ),
            takeaway = "DNS spoofing is simulated as a timeline. Real name resolution and real devices are not touched.",
        ),
        AttackScenario(
            id = "evil-twin",
            title = "Evil twin",
            subtitle = "A convincing network name is not an identity proof",
            riskLevel = "Credential and privacy risk",
            concept = "An access point can advertise an SSID that looks familiar. If a client joins it, the operator controls the local network path and can present deceptive login pages or observe unencrypted metadata.",
            stages = listOf(
                SimulationStage(
                    "Trusted network nearby",
                    "A client remembers a familiar SSID from prior use.",
                    SimulationActor.CLIENT,
                ),
                SimulationStage(
                    "Lookalike advertisement",
                    "The simulated hostile access point advertises the same name with different radio properties.",
                    SimulationActor.ATTACKER,
                ),
                SimulationStage(
                    "Automatic association",
                    "The client joins the lookalike network because it matches the remembered label.",
                    SimulationActor.CLIENT,
                ),
                SimulationStage(
                    "Verification fails",
                    "The user notices a captive portal and checks the gateway and certificate before entering anything.",
                    SimulationActor.ACCESS_POINT,
                ),
            ),
            defenses = listOf(
                "Forget old open networks and avoid auto joining unfamiliar SSIDs.",
                "Verify captive portals before entering credentials.",
                "Use a trusted VPN on untrusted networks when appropriate.",
                "Prefer networks with modern authentication and verified certificates.",
            ),
            takeaway = "The simulation never creates a hotspot. It teaches why SSID text must never be treated as authentication.",
        ),
        AttackScenario(
            id = "wpa-authentication",
            title = "WPA authentication",
            subtitle = "The intended four way handshake, shown as an educational flow",
            riskLevel = "Foundational concept",
            concept = "WPA2 and WPA3 use authentication and key derivation to establish fresh session keys. Understanding the intended flow makes capture analysis and deployment reviews more accurate.",
            stages = listOf(
                SimulationStage(
                    "Shared key material",
                    "The simulated client and access point derive a pairwise master key from the configured secret or enterprise authentication.",
                    SimulationActor.ACCESS_POINT,
                ),
                SimulationStage(
                    "M1 and M2",
                    "Nonces are exchanged and the client proves possession of the key with a MIC.",
                    SimulationActor.CLIENT,
                ),
                SimulationStage(
                    "M3 and M4",
                    "The access point delivers key material and the client confirms installation.",
                    SimulationActor.ACCESS_POINT,
                ),
                SimulationStage(
                    "Protected traffic",
                    "Data frames use fresh session keys. The handshake itself does not transmit the passphrase.",
                    SimulationActor.NETWORK,
                ),
            ),
            defenses = listOf(
                "Use a long random passphrase and modern security settings.",
                "Prefer WPA3 where every client supports it.",
                "Keep firmware current and disable WPS.",
                "Monitor for unusual management frame activity.",
            ),
            takeaway = "This is the defensive baseline: no attack is performed and no key is recovered.",
        ),
        AttackScenario(
            id = "deauthentication",
            title = "Deauthentication concepts",
            subtitle = "Why management frame protection matters",
            riskLevel = "Availability risk",
            concept = "Deauthentication frames terminate a wireless session. Without protected management frames, forged frames can cause repeated disconnections and poor availability.",
            stages = listOf(
                SimulationStage(
                    "Normal session",
                    "The simulated client and access point exchange protected data frames.",
                    SimulationActor.CLIENT,
                ),
                SimulationStage(
                    "Forged management frame",
                    "A simulated hostile actor forges a deauthentication notification. No real frame is generated.",
                    SimulationActor.ATTACKER,
                ),
                SimulationStage(
                    "Session loss",
                    "The client disconnects and reconnects, causing a visible service interruption.",
                    SimulationActor.CLIENT,
                ),
                SimulationStage(
                    "Protection enabled",
                    "With PMF, the forged frame fails integrity validation and the session continues.",
                    SimulationActor.ACCESS_POINT,
                ),
            ),
            defenses = listOf(
                "Enable protected management frames on supported access points.",
                "Prefer WPA3 deployments that require PMF.",
                "Monitor for repeated disconnects and investigate the wireless environment.",
                "Keep client drivers and router firmware updated.",
            ),
            takeaway = "This is a conceptual availability discussion. WiFiLab does not transmit deauthentication frames.",
        ),
        AttackScenario(
            id = "packet-interception",
            title = "Packet interception",
            subtitle = "What a local observer can and cannot see",
            riskLevel = "Privacy risk",
            concept = "The scope of local packet exposure depends on encryption and network position. Captures can reveal metadata even when application payloads are encrypted.",
            stages = listOf(
                SimulationStage(
                    "Encrypted wireless link",
                    "Protected 802.11 frames hide frame contents from a passive nearby observer.",
                    SimulationActor.NETWORK,
                ),
                SimulationStage(
                    "Local network path",
                    "A host on the local segment may observe unencrypted broadcast protocols such as ARP and some discovery services.",
                    SimulationActor.ATTACKER,
                ),
                SimulationStage(
                    "Application security",
                    "TLS protects application data end to end even when the local path is untrusted.",
                    SimulationActor.CLIENT,
                ),
                SimulationStage(
                    "Metadata remains",
                    "Addresses, timing, packet sizes and DNS names may still reveal information unless additional protections are used.",
                    SimulationActor.NETWORK,
                ),
            ),
            defenses = listOf(
                "Use modern wireless encryption and HTTPS.",
                "Enable encrypted DNS where appropriate.",
                "Avoid sensitive activity on captured or untrusted networks.",
                "Keep captures local and delete them after analysis.",
            ),
            takeaway = "This lab uses simulated frames to explain visibility boundaries without intercepting real traffic.",
        ),
    )
}
