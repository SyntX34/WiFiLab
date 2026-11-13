package com.syntx34.wifilab.ui.attack

import androidx.lifecycle.ViewModel
import com.syntx34.wifilab.domain.model.AttackScenario
import com.syntx34.wifilab.domain.repository.ResearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class AttackAnatomyViewModel @Inject constructor(
    repository: ResearchRepository,
) : ViewModel() {

    val scenarios: List<AttackScenario> = repository.attacks()

    private val _selectedId = MutableStateFlow(scenarios.firstOrNull()?.id.orEmpty())
    val selectedId: StateFlow<String> = _selectedId.asStateFlow()

    fun selectScenario(id: String) {
        _selectedId.value = id
    }
}
