package com.syntx34.wifilab.ui.lab

import androidx.lifecycle.ViewModel
import com.syntx34.wifilab.domain.model.ProtocolExplanation
import com.syntx34.wifilab.domain.repository.ResearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SecurityLabViewModel @Inject constructor(
    repository: ResearchRepository,
) : ViewModel() {
    val protocols: List<ProtocolExplanation> = repository.protocols()
}
