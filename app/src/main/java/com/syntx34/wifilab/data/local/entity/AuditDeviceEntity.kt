package com.syntx34.wifilab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audit_devices",
    foreignKeys = [
        ForeignKey(
            entity = AuditSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class AuditDeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val ipAddress: String,
    val hostname: String?,
    val serviceNames: String,
    val openPorts: String,
    val isGateway: Boolean,
    val unexpected: Boolean,
    val responseKind: String,
    val firstSeenAt: Long,
)
