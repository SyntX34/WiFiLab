package com.syntx34.wifilab.domain.content

import com.syntx34.wifilab.domain.model.ResearchDifficulty
import com.syntx34.wifilab.domain.model.ResearchEntry
import com.syntx34.wifilab.domain.model.ResearchSection

object RecommendationContent {
    val recommendations: List<ResearchEntry> = listOf(
        RecommendationEntry(
            id = "recommend-wifi",
            title = "Choose modern wireless security",
            subtitle = "Prefer WPA3, then WPA2 AES",
            overview = "Use WPA3 Personal when every client supports it. Otherwise use WPA2 AES with a long random passphrase and disable WPS.",
            keyPoints = listOf(
                "Avoid WEP and open networks for private traffic",
                "Use a unique passphrase that is not reused anywhere",
                "Disable WPS after onboarding devices",
                "Check that firmware updates are enabled",
            ),
        ),
        RecommendationEntry(
            id = "recommend-router",
            title = "Lock down router administration",
            subtitle = "Keep the management interface private",
            overview = "Change default administrator credentials, disable remote management and verify that the router interface is not exposed to the internet.",
            keyPoints = listOf(
                "Use a password manager for the administrator password",
                "Turn off cloud management you do not use",
                "Review firmware update settings monthly",
                "Use the guest network for untrusted devices",
            ),
        ),
        RecommendationEntry(
            id = "recommend-guest",
            title = "Isolate guest and smart devices",
            subtitle = "Reduce unnecessary lateral access",
            overview = "Guest isolation prevents guest clients and many smart devices from reaching trusted computers. Keep management access on the trusted network.",
            keyPoints = listOf(
                "Enable client isolation on guest networks",
                "Group cameras and appliances on a separate network when possible",
                "Do not expose local administration pages to the internet",
                "Review which devices need local discovery services",
            ),
        ),
        RecommendationEntry(
            id = "recommend-passwords",
            title = "Use long unique credentials",
            subtitle = "Length and randomness beat complexity rules",
            overview = "A password manager and long random passphrases provide better protection than memorized substitutions. Enable multi factor authentication for accounts that support it.",
            keyPoints = listOf(
                "Never reuse a password across services",
                "Prefer at least 16 random characters for important accounts",
                "Use a password manager to generate and store secrets",
                "Enable multi factor authentication",
            ),
        ),
        RecommendationEntry(
            id = "recommend-captures",
            title = "Treat captures as sensitive records",
            subtitle = "Collect the minimum and delete when finished",
            overview = "Wireless and packet captures can expose addresses, device names and application metadata. Keep them local, limit retention and redact before sharing.",
            keyPoints = listOf(
                "Use synthetic captures for lessons and demonstrations",
                "Delete research files when they are no longer needed",
                "Do not upload captures to online analysis services without informed consent",
                "Share the smallest necessary excerpt",
            ),
        ),
        RecommendationEntry(
            id = "recommend-audits",
            title = "Audit only with authorization",
            subtitle = "Consent and scope come first",
            overview = "Scan only networks you own or have explicit permission to test. Keep scans conservative and avoid invasive service discovery.",
            keyPoints = listOf(
                "Confirm ownership or written authorization",
                "Use the smallest useful address range",
                "Do not attempt login or exploitation",
                "Report unexpected findings responsibly",
            ),
        ),
    )

    private fun RecommendationEntry(
        id: String,
        title: String,
        subtitle: String,
        overview: String,
        keyPoints: List<String>,
    ): ResearchEntry = ResearchEntry(
        id = id,
        section = ResearchSection.RECOMMENDATION,
        title = title,
        subtitle = subtitle,
        overview = overview,
        details = emptyList(),
        keyPoints = keyPoints,
        estimatedMinutes = 4,
        difficulty = ResearchDifficulty.FOUNDATION,
    )
}
