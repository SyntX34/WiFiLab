package com.syntx34.wifilab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.syntx34.wifilab.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Insert
    suspend fun insert(activity: ActivityEntity)

    @Query("SELECT * FROM research_activity ORDER BY occurredAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ActivityEntity>>

    @Query("DELETE FROM research_activity")
    suspend fun deleteAll()
}
