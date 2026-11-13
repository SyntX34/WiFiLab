package com.syntx34.wifilab.domain.model

enum class ResearchSection(val label: String) {
    TERMINOLOGY("Terminology"),
    PROTOCOL("Protocols"),
    PACKET_LESSON("Packet lessons"),
    EXERCISE("Exercises"),
    RECOMMENDATION("Recommendations"),
}

enum class ResearchDifficulty(val label: String) {
    FOUNDATION("Foundation"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
}

data class ResearchEntry(
    val id: String,
    val section: ResearchSection,
    val title: String,
    val subtitle: String,
    val overview: String,
    val details: List<String>,
    val keyPoints: List<String>,
    val steps: List<String> = emptyList(),
    val reflectionQuestions: List<String> = emptyList(),
    val estimatedMinutes: Int = 5,
    val difficulty: ResearchDifficulty = ResearchDifficulty.FOUNDATION,
)

enum class SimulationActor(val label: String) {
    CLIENT("Client"),
    ACCESS_POINT("Access point"),
    ATTACKER("Attacker"),
    DNS("DNS service"),
    NETWORK("Network"),
}

data class SimulationStage(
    val title: String,
    val description: String,
    val actor: SimulationActor,
)

data class AttackScenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val riskLevel: String,
    val concept: String,
    val stages: List<SimulationStage>,
    val defenses: List<String>,
    val takeaway: String,
)

enum class ResearchActivityType(val label: String) {
    WIFI_SCAN("Wi-Fi scan"),
    NETWORK_AUDIT("Network audit"),
    PACKET_ANALYSIS("Packet analysis"),
    PASSWORD_STUDY("Password study"),
    LEARNING("Learning"),
}

data class ResearchActivity(
    val id: Long,
    val type: ResearchActivityType,
    val title: String,
    val detail: String,
    val occurredAt: Long,
)
