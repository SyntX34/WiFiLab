package com.syntx34.wifilab.domain.content

import com.syntx34.wifilab.domain.model.ResearchDifficulty
import com.syntx34.wifilab.domain.model.ResearchEntry
import com.syntx34.wifilab.domain.model.ResearchSection

object ExerciseContent {
    val exercises: List<ResearchEntry> = listOf(
        ResearchEntry(
            id = "exercise-channel-map",
            section = ResearchSection.EXERCISE,
            title = "Build a channel map",
            subtitle = "Use scanner data to reason about congestion",
            overview = "Inspect nearby networks and describe how channels are distributed. The goal is to separate performance observations from security conclusions.",
            details = listOf(
                "Record the channel and signal band for each visible network.",
                "Ignore hidden or very weak entries that may be stale.",
                "Look for overlapping 2.4 GHz channels and wide 5 GHz channels.",
            ),
            keyPoints = listOf("2.4 GHz channels 1, 6 and 11 do not overlap with each other", "A strong signal is not automatically trustworthy"),
            steps = listOf(
                "Open Wi Fi Scanner and complete a scan.",
                "Inspect the channel distribution chart.",
                "List up to five networks and their channels.",
                "Explain why a crowded channel may still be acceptable for low traffic.",
            ),
            reflectionQuestions = listOf(
                "Which channel would you recommend for a new 2.4 GHz network?",
                "Why can a 5 GHz network with a weaker signal perform better?",
                "Which security metadata on the card should you verify separately?",
            ),
            estimatedMinutes = 10,
            difficulty = ResearchDifficulty.FOUNDATION,
        ),
        ResearchEntry(
            id = "exercise-handshake-id",
            section = ResearchSection.EXERCISE,
            title = "Identify four way handshake messages",
            subtitle = "Practice reading EAPOL key metadata",
            overview = "Use the built in synthetic capture to identify M1 through M4 without looking up the answer first.",
            details = listOf(
                "The ACK and MIC bits are the most useful starting point.",
                "Install and secure bits distinguish parts of the exchange.",
                "Replay counters are visible values, not secrets.",
            ),
            keyPoints = listOf("M1 usually has ACK and no MIC", "M2 has MIC and usually no ACK", "M3 has ACK, MIC and install"),
            steps = listOf(
                "Open Packet Analyzer and load the offline demo.",
                "Filter to EAPOL and open the first key frame.",
                "Write down the ACK, MIC, install and secure bits.",
                "Predict the message number before reading the label.",
                "Verify your prediction against all four frames.",
            ),
            reflectionQuestions = listOf(
                "Which message is easiest to distinguish?",
                "Why is a PMKID not a password?",
                "What does the encrypted key data bit prevent you from reading?",
            ),
            estimatedMinutes = 15,
            difficulty = ResearchDifficulty.INTERMEDIATE,
        ),
        ResearchEntry(
            id = "exercise-baseline",
            section = ResearchSection.EXERCISE,
            title = "Review baseline drift",
            subtitle = "Investigate a new local device",
            overview = "Simulate a change in a trusted network inventory. The task is to decide what evidence to gather before taking action.",
            details = listOf(
                "An IP address can be assigned to a different device over time.",
                "Hostnames and open services are useful but incomplete identifiers.",
                "A single port response does not prove malicious intent.",
            ),
            keyPoints = listOf("Verify with the router device list", "Ask household members before assuming an intrusion", "Update the baseline only after validation"),
            steps = listOf(
                "Open Network Auditor and save a baseline after a scan.",
                "Connect a known test device or change a device address if possible.",
                "Run a second scan and inspect the unexpected device finding.",
                "Write a three step verification plan.",
                "Update the baseline only if the device is expected.",
            ),
            reflectionQuestions = listOf(
                "Which local evidence could confirm the device identity?",
                "Why should WiFiLab not automatically block a device?",
                "What privacy limits affect device discovery on Android?",
            ),
            estimatedMinutes = 15,
            difficulty = ResearchDifficulty.INTERMEDIATE,
        ),
        ResearchEntry(
            id = "exercise-entropy",
            section = ResearchSection.EXERCISE,
            title = "Compare password entropy",
            subtitle = "Test length, repetition and dictionary penalties",
            overview = "Use Password Lab to compare several non sensitive examples and explain why the effective estimate differs from the raw character pool calculation.",
            details = listOf(
                "Raw entropy assumes independent random characters.",
                "Human chosen suffixes repeat across many real passwords.",
                "A long random passphrase is easier to remember than a short random string.",
            ),
            keyPoints = listOf("Effective entropy is the useful number", "Longer secrets increase guessing cost dramatically", "Never enter a real password into an educational tool"),
            steps = listOf(
                "Enter four sample strings of increasing length.",
                "Record raw bits and effective bits for each.",
                "Find an example that triggers a repeated block warning.",
                "Explain why the dictionary penalty is larger than the raw formula suggests.",
            ),
            reflectionQuestions = listOf(
                "Which change produced the largest increase in effective entropy?",
                "Why can a common word with symbols still be weak?",
                "How does an offline guessing rate change the displayed time?",
            ),
            estimatedMinutes = 10,
            difficulty = ResearchDifficulty.FOUNDATION,
        ),
        ResearchEntry(
            id = "exercise-pcap-triage",
            section = ResearchSection.EXERCISE,
            title = "Triage an unfamiliar capture",
            subtitle = "Make a careful first pass without overclaiming",
            overview = "Use protocol counts, durations and a small number of packet details to build a cautious summary of a file. Avoid conclusions that the data cannot support.",
            details = listOf(
                "Summarize the file format and packet count first.",
                "Count protocols before reading packet payloads.",
                "Treat addresses and names as potentially sensitive.",
            ),
            keyPoints = listOf("Start broad and then zoom in", "Record uncertainty explicitly", "Never claim credential recovery"),
            steps = listOf(
                "Import a capture you generated or a synthetic demo.",
                "Record the format, duration and protocol distribution.",
                "Open two packets and describe their direction and purpose.",
                "Write a three sentence summary with one limitation.",
            ),
            reflectionQuestions = listOf(
                "What can protocol counts not tell you?",
                "Which privacy details should be removed before sharing a capture?",
                "Why is an EAPOL key message less revealing than it first appears?",
            ),
            estimatedMinutes = 12,
            difficulty = ResearchDifficulty.ADVANCED,
        ),
    )
}
