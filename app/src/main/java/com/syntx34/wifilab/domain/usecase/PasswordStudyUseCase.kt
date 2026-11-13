package com.syntx34.wifilab.domain.usecase

import com.syntx34.wifilab.domain.model.EntropyAnalysis
import com.syntx34.wifilab.domain.model.PasswordComparison
import com.syntx34.wifilab.domain.model.SyntheticRecovery
import com.syntx34.wifilab.domain.security.PasswordEntropy
import javax.inject.Inject

class PasswordStudyUseCase @Inject constructor(
    private val passwordEntropy: PasswordEntropy,
) {
    fun analyze(password: String): EntropyAnalysis = passwordEntropy.analyze(password)

    fun comparisons(): List<PasswordComparison> = passwordEntropy.comparisonSet()

    fun syntheticRecoveries(): List<SyntheticRecovery> = passwordEntropy.syntheticRecoveries()
}
