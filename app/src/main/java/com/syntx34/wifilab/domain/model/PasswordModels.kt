package com.syntx34.wifilab.domain.model

enum class PasswordStrength(val label: String) {
    VERY_WEAK("Very weak"),
    WEAK("Weak"),
    MODERATE("Moderate"),
    STRONG("Strong"),
    VERY_STRONG("Very strong"),
}

data class CrackTimeEstimate(
    val guessesPerSecond: Long,
    val seconds: Double,
    val display: String,
)

data class EntropyAnalysis(
    val passwordLength: Int,
    val poolSize: Int,
    val rawBits: Double,
    val effectiveBits: Double,
    val strength: PasswordStrength,
    val dictionaryMatch: String?,
    val patternWarnings: List<String>,
    val crackTime: CrackTimeEstimate,
    val uniqueCharacters: Int,
)

data class PasswordComparison(
    val label: String,
    val example: String,
    val bits: Double,
    val crackTimeLabel: String,
    val note: String,
)

data class SyntheticRecovery(
    val label: String,
    val generatedPassword: String,
    val patternDescription: String,
    val searchSpaceBits: Double,
    val simulatedSeconds: Double,
    val simulatedTimeLabel: String,
    val explanation: String,
)
