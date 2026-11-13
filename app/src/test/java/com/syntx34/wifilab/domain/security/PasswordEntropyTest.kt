package com.syntx34.wifilab.domain.security

import com.syntx34.wifilab.domain.model.PasswordStrength
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordEntropyTest {

    private val entropy = PasswordEntropy()

    @Test
    fun emptyPasswordHasNoEntropy() {
        val result = entropy.analyze("")
        assertEquals(0, result.passwordLength)
        assertEquals(0.0, result.effectiveBits, 0.001)
        assertEquals(PasswordStrength.VERY_WEAK, result.strength)
        assertEquals("Instant", result.crackTime.display)
    }

    @Test
    fun longerRandomPasswordHasMoreEntropy() {
        val short = entropy.analyze("V7!qB2")
        val long = entropy.analyze("V7!qB2s#K9@xT4")
        assertTrue(long.effectiveBits > short.effectiveBits)
        assertTrue(long.crackTime.seconds > short.crackTime.seconds)
        assertEquals(PasswordStrength.VERY_STRONG, long.strength)
    }

    @Test
    fun dictionaryWordIsPenalized() {
        val common = entropy.analyze("password1")
        val random = entropy.analyze("vN7!qB2s")
        assertEquals("password", common.dictionaryMatch)
        assertTrue(common.effectiveBits < random.effectiveBits)
        assertTrue(common.patternWarnings.isNotEmpty())
    }

    @Test
    fun repeatedPatternAndSingleCharacterAreFlagged() {
        val repeated = entropy.analyze("abcabcabcabc")
        val single = entropy.analyze("aaaaaaaaaaaa")
        assertTrue(repeated.patternWarnings.any { it.contains("repeated", ignoreCase = true) })
        assertTrue(single.patternWarnings.any { it.contains("repeated", ignoreCase = true) })
        assertTrue(single.effectiveBits < 10.0)
    }

    @Test
    fun sequentialCharactersAreFlagged() {
        val sequential = entropy.analyze("12345678")
        assertTrue(sequential.patternWarnings.any { it.contains("Sequential", ignoreCase = true) })
    }

    @Test
    fun syntheticRecoveriesAreGeneratedLocally() {
        val simulations = entropy.syntheticRecoveries()
        assertEquals(5, simulations.size)
        simulations.forEach { simulation ->
            assertTrue(simulation.generatedPassword.isNotEmpty())
            assertTrue(simulation.searchSpaceBits > 0.0)
            assertNotNull(simulation.simulatedTimeLabel)
            assertTrue(simulation.explanation.contains("generated locally"))
        }
    }

    @Test
    fun comparisonSetIncludesLongerExamplesWithMoreBits() {
        val comparisons = entropy.comparisonSet()
        assertTrue(comparisons.size >= 5)
        val pin = comparisons.first { it.label.contains("PIN") }
        val longRandom = comparisons.first { it.label.contains("Twenty") }
        assertTrue(longRandom.bits > pin.bits)
    }
}
