package com.syntx34.wifilab.domain.usecase

import com.syntx34.wifilab.domain.model.ResearchActivityType
import com.syntx34.wifilab.domain.repository.ActivityRepository
import javax.inject.Inject

class RecordActivityUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
) {
    suspend operator fun invoke(
        type: ResearchActivityType,
        title: String,
        detail: String,
    ) {
        runCatching { activityRepository.log(type, title, detail) }
    }
}
