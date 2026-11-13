package com.syntx34.wifilab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.syntx34.wifilab.data.local.entity.PacketCaptureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PacketCaptureDao {

    @Query("SELECT * FROM packet_captures ORDER BY importedAt DESC")
    fun observeAll(): Flow<List<PacketCaptureEntity>>

    @Query("SELECT * FROM packet_captures WHERE id = :id")
    suspend fun getById(id: Long): PacketCaptureEntity?

    @Insert
    suspend fun insert(capture: PacketCaptureEntity): Long

    @Query("DELETE FROM packet_captures WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT storedFileName FROM packet_captures")
    suspend fun allStoredFileNames(): List<String>

    @Query("DELETE FROM packet_captures")
    suspend fun deleteAll()
}
