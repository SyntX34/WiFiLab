package com.syntx34.wifilab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packet_captures")
data class PacketCaptureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val storedFileName: String,
    val importedAt: Long,
    val sizeBytes: Long,
    val packetCount: Int,
    val eapolCount: Int,
    val durationMillis: Long,
)
