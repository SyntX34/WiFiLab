package com.syntx34.wifilab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.syntx34.wifilab.data.local.dao.ActivityDao
import com.syntx34.wifilab.data.local.dao.AuditDao
import com.syntx34.wifilab.data.local.dao.BaselineDao
import com.syntx34.wifilab.data.local.dao.PacketCaptureDao
import com.syntx34.wifilab.data.local.entity.ActivityEntity
import com.syntx34.wifilab.data.local.entity.AuditDeviceEntity
import com.syntx34.wifilab.data.local.entity.AuditSessionEntity
import com.syntx34.wifilab.data.local.entity.BaselineDeviceEntity
import com.syntx34.wifilab.data.local.entity.PacketCaptureEntity

@Database(
    entities = [
        AuditSessionEntity::class,
        AuditDeviceEntity::class,
        BaselineDeviceEntity::class,
        ActivityEntity::class,
        PacketCaptureEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class WiFiLabDatabase : RoomDatabase() {
    abstract fun auditDao(): AuditDao
    abstract fun baselineDao(): BaselineDao
    abstract fun activityDao(): ActivityDao
    abstract fun packetCaptureDao(): PacketCaptureDao
}
