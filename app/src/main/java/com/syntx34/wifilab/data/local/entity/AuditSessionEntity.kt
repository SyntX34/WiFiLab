package com.syntx34.wifilab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_sessions")
data class AuditSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scannedAt: Long,
    val cidr: String,
    val deviceCount: Int,
    val unexpectedCount: Int,
    val baselineUsed: Boolean,
)
