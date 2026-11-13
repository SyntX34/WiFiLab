package com.syntx34.wifilab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.syntx34.wifilab.data.local.entity.AuditDeviceEntity
import com.syntx34.wifilab.data.local.entity.AuditSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {

    @Insert
    suspend fun insertSession(session: AuditSessionEntity): Long

    @Insert
    suspend fun insertDevices(devices: List<AuditDeviceEntity>)

    @Transaction
    suspend fun saveSession(session: AuditSessionEntity, devices: List<AuditDeviceEntity>): Long {
        val sessionId = insertSession(session)
        if (devices.isNotEmpty()) {
            insertDevices(devices.map { it.copy(sessionId = sessionId) })
        }
        return sessionId
    }

    @Query("SELECT * FROM audit_sessions ORDER BY scannedAt DESC LIMIT :limit")
    fun observeRecentSessions(limit: Int): Flow<List<AuditSessionEntity>>

    @Query("DELETE FROM audit_sessions")
    suspend fun deleteAllSessions()
}
