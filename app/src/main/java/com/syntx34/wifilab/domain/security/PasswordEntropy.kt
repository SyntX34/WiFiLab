package com.syntx34.wifilab.domain.security

import com.syntx34.wifilab.core.util.TimeFormatter
import com.syntx34.wifilab.domain.model.CrackTimeEstimate
import com.syntx34.wifilab.domain.model.EntropyAnalysis
import com.syntx34.wifilab.domain.model.PasswordComparison
import com.syntx34.wifilab.domain.model.PasswordStrength
import com.syntx34.wifilab.domain.model.SyntheticRecovery
import java.util.Locale
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

class PasswordEntropy(
    private val guessesPerSecond: Long = DEFAULT_GUESSES_PER_SECOND,
) {

    fun analyze(password: String): EntropyAnalysis {
        if (password.isEmpty()) {
            return EntropyAnalysis(
                passwordLength = 0,
                poolSize = 0,
                rawBits = 0.0,
                effectiveBits = 0.0,
                strength = PasswordStrength.VERY_WEAK,
                dictionaryMatch = null,
                patternWarnings = listOf("Enter a password to calculate an estimate"),
                crackTime = CrackTimeEstimate(guessesPerSecond, 0.0, "Instant"),
                uniqueCharacters = 0,
            )
        }

        val pool = characterPoolSize(password)
        val rawBits = password.length * log2(pool.toDouble())
        var effectiveBits = rawBits
        var dictionaryMatch: String? = null
        val warnings = mutableListOf<String>()
        val normalized = password.lowercase(Locale.US)

        for (word in COMMON_WORDS) {
            when {
                normalized == word -> {
                    dictionaryMatch = word
                    effectiveBits = min(effectiveBits, log2(COMMON_WORDS.size.toDouble()) + 1.0)
                }
                normalized.startsWith(word) && normalized.length > word.length -> {
                    val suffix = normalized.substring(word.length)
                    if (suffix.all { it.isDigit() }) {
                        dictionaryMatch = word
                        val suffixBits = suffix.length * log2(10.0)
                        effectiveBits = min(
                            effectiveBits,
                            log2(COMMON_WORDS.size.toDouble()) + suffixBits,
                        )
                        warnings += "A dictionary word with a numeric suffix is easy to guess"
                    }
                }
                word.length >= 4 && normalized.contains(word) -> {
                    warnings += "The password contains the common pattern \"$word\""
                    effectiveBits = min(effectiveBits, effectiveBits * 0.65)
                }
            }
        }

        if (password.length >= 3) {
            val ascending = password.zipWithNext().all { (a, b) -> b.code - a.code == 1 }
            val descending = password.zipWithNext().all { (a, b) -> a.code - b.code == 1 }
            if (ascending || descending) {
                warnings += "Sequential characters are predictable"
                effectiveBits = min(effectiveBits, log2(pool.toDouble()) + password.length.coerceAtMost(4))
            }
        }

        if (password.toSet().size == 1) {
            warnings += "A single repeated character has almost no entropy"
            effectiveBits = log2(pool.toDouble())
        } else {
            val repeatedUnit = smallestRepeatingUnit(normalized)
            if (repeatedUnit != null && repeatedUnit.length < password.length) {
                warnings += "A repeated block reduces the effective search space"
                val repeatBits = repeatedUnit.length * log2(pool.toDouble())
                effectiveBits = min(effectiveBits, repeatBits + log2((password.length / repeatedUnit.length).toDouble() + 1.0))
            }
        }

        if (password.length < 8) {
            warnings += "Short passwords are vulnerable to exhaustive guessing"
        }
        if (password.all { it.isDigit() }) {
            warnings += "Digits alone provide a small character pool"
        }
        if (dictionaryMatch == null && warnings.isEmpty() && password.length < 12) {
            warnings += "Increase length before adding more character types"
        }

        effectiveBits = effectiveBits.coerceIn(0.0, rawBits)
        val crackTime = estimateCrackTime(effectiveBits)
        return EntropyAnalysis(
            passwordLength = password.length,
            poolSize = pool,
            rawBits = rawBits,
            effectiveBits = effectiveBits,
            strength = strengthFor(effectiveBits),
            dictionaryMatch = dictionaryMatch,
            patternWarnings = warnings.distinct(),
            crackTime = crackTime,
            uniqueCharacters = password.toSet().size,
        )
    }

    fun comparisonSet(): List<PasswordComparison> = listOf(
        comparison("Four digit PIN", "4829", 4 * log2(10.0), "Small search space"),
        comparison("Single common word", "sunshine", log2(COMMON_WORDS.size.toDouble() * 8.0), "Dictionary attacks try likely words first"),
        comparison("Eight random characters", "vN7!qB2s", 8 * log2(94.0), "Every added character multiplies the search space"),
        comparison("Twelve random characters", "f8Lw!2rQ#sZ9", 12 * log2(94.0), "Recommended for unique account passwords"),
        comparison("Five random words", "harbor-otter-violet-anchor-mint", 5 * log2(7776.0), "A long passphrase can be both memorable and strong"),
        comparison("Twenty random characters", "q7#Lm2!vR9@xT4\$zK8&p", 20 * log2(94.0), "Very large offline search space"),
    )

    fun syntheticRecoveries(random: Random = Random.Default): List<SyntheticRecovery> {
        val pin = random.nextInt(0, 10_000).toString().padStart(4, '0')
        val lowercase = (1..6).map { ('a' + random.nextInt(26)) }.joinToString("")
        val word = COMMON_WORDS[random.nextInt(COMMON_WORDS.size)]
        val suffix = random.nextInt(0, 100).toString().padStart(2, '0')
        val mixed = buildString {
            val pools = listOf(
                "abcdefghijkmnopqrstuvwxyz",
                "ABCDEFGHJKLMNPQRSTUVWXYZ",
                "23456789",
                "!@#$%^&*",
            )
            repeat(10) {
                val pool = pools[it % pools.size]
                append(pool[random.nextInt(pool.length)])
            }
        }
        val twelve = buildString {
            val pools = listOf(
                "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789",
                "!@#$%^&*",
            )
            repeat(12) {
                val pool = pools[it % pools.size]
                append(pool[random.nextInt(pool.length)])
            }
        }
        return listOf(
            recovery("Four digit PIN", pin, "10,000 possible values", 4 * log2(10.0)),
            recovery("Six lowercase letters", lowercase, "26 to the power of 6 values", 6 * log2(26.0)),
            recovery(
                "Common word plus two digits",
                "$word$suffix",
                "A likely dictionary word combined with a tiny suffix",
                log2(COMMON_WORDS.size.toDouble()) + 2 * log2(10.0),
            ),
            recovery("Ten mixed characters", mixed, "Mixed synthetic alphabet of 88 characters", 10 * log2(88.0)),
            recovery("Twelve mixed characters", twelve, "Mixed synthetic alphabet of 68 symbols", 12 * log2(68.0)),
        )
    }

    fun estimateCrackTime(bits: Double): CrackTimeEstimate {
        if (bits <= 0.0) {
            return CrackTimeEstimate(guessesPerSecond, 0.0, "Instant")
        }
        val attempts = if (bits > 1_024) Double.POSITIVE_INFINITY else 2.0.pow(bits - 1.0)
        val seconds = attempts / guessesPerSecond.toDouble()
        return CrackTimeEstimate(
            guessesPerSecond = guessesPerSecond,
            seconds = seconds,
            display = TimeFormatter.humanDuration(seconds),
        )
    }

    private fun comparison(label: String, example: String, bits: Double, note: String): PasswordComparison =
        PasswordComparison(
            label = label,
            example = example,
            bits = bits,
            crackTimeLabel = TimeFormatter.humanDuration(estimateCrackTime(bits).seconds),
            note = note,
        )

    private fun recovery(
        label: String,
        password: String,
        pattern: String,
        bits: Double,
    ): SyntheticRecovery {
        val seconds = estimateCrackTime(bits).seconds
        return SyntheticRecovery(
            label = label,
            generatedPassword = password,
            patternDescription = pattern,
            searchSpaceBits = bits,
            simulatedSeconds = seconds,
            simulatedTimeLabel = TimeFormatter.humanDuration(seconds),
            explanation = "This target was generated locally for the demonstration. It is not a captured credential.",
        )
    }

    private fun strengthFor(bits: Double): PasswordStrength = when {
        bits < 28.0 -> PasswordStrength.VERY_WEAK
        bits < 40.0 -> PasswordStrength.WEAK
        bits < 60.0 -> PasswordStrength.MODERATE
        bits < 80.0 -> PasswordStrength.STRONG
        else -> PasswordStrength.VERY_STRONG
    }

    private fun characterPoolSize(password: String): Int {
        var pool = 0
        if (password.any { it in 'a'..'z' }) pool += 26
        if (password.any { it in 'A'..'Z' }) pool += 26
        if (password.any { it in '0'..'9' }) pool += 10
        if (password.any { it in PUNCTUATION }) pool += PUNCTUATION.length
        if (password.any { it == ' ' }) pool += 1
        if (password.any { it.code > 126 }) pool += 100
        return pool.coerceAtLeast(1)
    }

    private fun smallestRepeatingUnit(value: String): String? {
        for (size in 1..value.length / 2) {
            if (value.length % size != 0) continue
            val unit = value.substring(0, size)
            if (unit.repeat(value.length / size) == value) return unit
        }
        return null
    }

    companion object {
        const val DEFAULT_GUESSES_PER_SECOND = 10_000_000_000L
        private const val PUNCTUATION = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
        private val COMMON_WORDS = listOf(
            "password",
            "123456",
            "12345678",
            "qwerty",
            "letmein",
            "welcome",
            "admin",
            "iloveyou",
            "monkey",
            "dragon",
            "football",
            "sunshine",
            "princess",
            "abc123",
            "passw0rd",
            "shadow",
            "master",
            "superman",
            "michael",
            "baseball",
            "trustno1",
            "login",
            "starwars",
            "whatever",
        )
    }
}
