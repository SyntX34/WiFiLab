package com.syntx34.wifilab.ui.password

import androidx.lifecycle.ViewModel
import com.syntx34.wifilab.domain.model.EntropyAnalysis
import com.syntx34.wifilab.domain.model.PasswordComparison
import com.syntx34.wifilab.domain.model.SyntheticRecovery
import com.syntx34.wifilab.domain.usecase.PasswordStudyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PasswordLabUiState(
    val input: String = EXAMPLE_PASSPHRASE,
    val analysis: EntropyAnalysis? = null,
    val comparisons: List<PasswordComparison> = emptyList(),
    val simulations: List<SyntheticRecovery> = emptyList(),
)

private const val EXAMPLE_PASSPHRASE = "Harbor-Otter-42-Sky"

@HiltViewModel
class PasswordLabViewModel @Inject constructor(
    private val study: PasswordStudyUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PasswordLabUiState(
            input = EXAMPLE_PASSPHRASE,
            analysis = study.analyze(EXAMPLE_PASSPHRASE),
            comparisons = study.comparisons(),
            simulations = study.syntheticRecoveries(),
        ),
    )
    val uiState: StateFlow<PasswordLabUiState> = _uiState.asStateFlow()

    fun updateInput(value: String) {
        _uiState.update {
            it.copy(
                input = value,
                analysis = study.analyze(value),
            )
        }
    }

    fun useExample() {
        updateInput(EXAMPLE_PASSPHRASE)
    }

    fun clearInput() {
        updateInput("")
    }

    fun regenerateSimulations() {
        _uiState.update { it.copy(simulations = study.syntheticRecoveries()) }
    }
}
