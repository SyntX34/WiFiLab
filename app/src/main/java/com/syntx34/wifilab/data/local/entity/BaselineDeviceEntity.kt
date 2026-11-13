package com.syntx34.wifilab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "baseline_devices")
data class BaselineDeviceEntity(
    @PrimaryKey val identity: String,
    val ipAddress: String,
    val hostname: String?,
    val serviceNames: String,
    val approvedAt: Long,
)
