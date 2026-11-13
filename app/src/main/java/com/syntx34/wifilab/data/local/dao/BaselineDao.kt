package com.syntx34.wifilab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.syntx34.wifilab.data.local.entity.BaselineDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BaselineDao {

    @Query("SELECT * FROM baseline_devices ORDER BY ipAddress ASC")
    fun observeAll(): Flow<List<BaselineDeviceEntity>>

    @Query("SELECT * FROM baseline_devices ORDER BY ipAddress ASC")
    suspend fun getAll(): List<BaselineDeviceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(devices: List<BaselineDeviceEntity>)

    @Query("DELETE FROM baseline_devices")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(devices: List<BaselineDeviceEntity>) {
        deleteAll()
        if (devices.isNotEmpty()) {
            insertAll(devices)
        }
    }
}
