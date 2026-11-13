package com.syntx34.wifilab.domain.content

import com.syntx34.wifilab.domain.model.ProtocolExplanation
import com.syntx34.wifilab.domain.model.ResearchDifficulty
import com.syntx34.wifilab.domain.model.ResearchEntry
import com.syntx34.wifilab.domain.model.ResearchSection

object ProtocolContent {
    val protocols: List<ProtocolExplanation> = listOf(
        ProtocolExplanation(
            id = "wpa2",
            name = "Wi Fi Protected Access 2",
            shortName = "WPA2",
            summary = "WPA2 uses AES with CCMP and a preshared key or an enterprise authentication server. It remains widely deployed and is secure when the passphrase is long, unique and rotated.",
            authenticationFlow = listOf(
                "The client and access point derive the Pairwise Master Key from the passphrase and SSID.",
                "A four way handshake proves both sides know the key without sending it over the air.",
                "Each side supplies a random nonce and the access point sends an encrypted GTK.",
                "The client installs the pairwise key and protected data frames can begin.",
            ),
            strengths = listOf(
                "Strong encryption with CCMP when configured correctly.",
                "Wide compatibility across client devices.",
                "Supports enterprise authentication through 802.1X.",
            ),
            weaknesses = listOf(
                "Weak passphrases permit offline guessing from captured handshake metadata.",
                "WPS and older transition settings can reintroduce weak paths.",
                "Management frames are not always protected unless PMF is enabled.",
            ),
            researcherNote = "WPA2 captures reveal handshake metadata, not the plaintext key. Evaluating passphrase quality is an offline lab exercise, never a live network operation.",
        ),
        ProtocolExplanation(
            id = "wpa3",
            name = "Wi Fi Protected Access 3",
            shortName = "WPA3",
            summary = "WPA3 Personal replaces the preshared key exchange with Simultaneous Authentication of Equals. The exchange resists passive offline guessing and provides stronger forward secrecy.",
            authenticationFlow = listOf(
                "Client and access point run the SAE commitment exchange.",
                "Both derive a shared secret without exposing material for offline guessing.",
                "The normal four way handshake confirms keys and installs cipher suites.",
                "Protected management frames help resist forged disassociation and deauthentication.",
            ),
            strengths = listOf(
                "Passive capture does not provide a practical offline dictionary oracle.",
                "Forward secrecy limits the impact of a future key compromise.",
                "Protected management frames are required in the specification.",
            ),
            weaknesses = listOf(
                "Transition mode can preserve compatibility with weaker WPA2 behavior.",
                "Some deployments still enable WPS or legacy cipher suites.",
                "Client device support varies across older Android releases.",
            ),
            researcherNote = "WPA3 is a meaningful improvement, but the surrounding configuration still matters. Verify firmware, disable WPS and review guest network isolation.",
        ),
        ProtocolExplanation(
            id = "wps",
            name = "Wi Fi Protected Setup",
            shortName = "WPS",
            summary = "WPS was designed to simplify onboarding. The PIN method split an eight digit secret into two shorter halves, which made online guessing practical and led to permanent recommendations to disable it.",
            authenticationFlow = listOf(
                "A client discovers that the access point supports WPS.",
                "The router accepts a PIN or a push button registration request.",
                "The router shares the network credential with the new client.",
                "The client joins using the normal wireless association process.",
            ),
            strengths = listOf(
                "Simple onboarding for devices without a keyboard.",
                "Push button mode can be convenient when physically supervised.",
            ),
            weaknesses = listOf(
                "PIN based onboarding historically had a small effective search space.",
                "WPS can bypass otherwise strong passphrases.",
                "Many consumer routers leave WPS enabled by default.",
            ),
            researcherNote = "Disable WPS unless a specific device requires it temporarily. WiFiLab explains the design flaw but does not attempt WPS attacks against real access points.",
        ),
        ProtocolExplanation(
            id = "enterprise",
            name = "Enterprise wireless authentication",
            shortName = "802.1X",
            summary = "Enterprise wireless authentication uses a RADIUS server and Extensible Authentication Protocol to grant per user or per device access instead of a shared passphrase.",
            authenticationFlow = listOf(
                "The access point creates a controlled port for the new client.",
                "EAP messages carry identity and authentication credentials to the RADIUS server.",
                "The server and client validate certificates or other credentials.",
                "The controlled port opens and keys are delivered through the wireless association.",
            ),
            strengths = listOf(
                "Individual credentials can be revoked without changing a shared key.",
                "Central policy and accounting support larger networks.",
                "Certificate validation resists some impersonation attempts.",
            ),
            weaknesses = listOf(
                "Clients that skip certificate validation can be tricked by a rogue access point.",
                "Misconfigured EAP methods can expose credentials.",
                "Server and client certificate lifecycle management adds operational complexity.",
            ),
            researcherNote = "The most common enterprise weakness is a client configuration that trusts any certificate. A strong server certificate check is not optional.",
        ),
        ProtocolExplanation(
            id = "owe",
            name = "Enhanced Open",
            shortName = "OWE",
            summary = "Enhanced Open provides unauthenticated encryption on public networks. It protects traffic against passive observation without requiring a shared password.",
            authenticationFlow = listOf(
                "The access point advertises an OWE transition network.",
                "Client and access point perform a Diffie Hellman exchange.",
                "A pairwise key is derived for the session.",
                "Traffic is encrypted even though no user credential is required.",
            ),
            strengths = listOf(
                "Removes passive plaintext exposure on open networks.",
                "No shared password to distribute.",
                "Useful for guest and public access.",
            ),
            weaknesses = listOf(
                "Does not authenticate the network to the user.",
                "A hostile access point can still advertise an open or lookalike network.",
                "Older clients may fall back to an unencrypted open network.",
            ),
            researcherNote = "Encryption without authentication is not a substitute for a trusted network. Verify the network name and use application layer security.",
        ),
    )

    val researchEntries: List<ResearchEntry> = protocols.map { protocol ->
        ResearchEntry(
            id = "protocol-${protocol.id}",
            section = ResearchSection.PROTOCOL,
            title = protocol.shortName,
            subtitle = protocol.name,
            overview = protocol.summary,
            details = protocol.authenticationFlow,
            keyPoints = protocol.strengths + protocol.weaknesses + protocol.researcherNote,
            estimatedMinutes = 8,
            difficulty = ResearchDifficulty.INTERMEDIATE,
        )
    }
}
