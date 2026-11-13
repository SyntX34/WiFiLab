package com.syntx34.wifilab.data.repository

import com.syntx34.wifilab.data.local.dao.ActivityDao
import com.syntx34.wifilab.data.local.entity.ActivityEntity
import com.syntx34.wifilab.domain.model.ResearchActivity
import com.syntx34.wifilab.domain.model.ResearchActivityType
import com.syntx34.wifilab.domain.repository.ActivityRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao,
) : ActivityRepository {

    override fun observeRecent(limit: Int): Flow<List<ResearchActivity>> =
        activityDao.observeRecent(limit).map { activities ->
            activities.mapNotNull { entity -> entity.toDomain() }
        }

    override suspend fun log(type: ResearchActivityType, title: String, detail: String) {
        activityDao.insert(
            ActivityEntity(
                type = type.name,
                title = title,
                detail = detail,
                occurredAt = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun clear() {
        activityDao.deleteAll()
    }

    private fun ActivityEntity.toDomain(): ResearchActivity? {
        val activityType = runCatching { ResearchActivityType.valueOf(type) }.getOrNull() ?: return null
        return ResearchActivity(
            id = id,
            type = activityType,
            title = title,
            detail = detail,
            occurredAt = occurredAt,
        )
    }
}
