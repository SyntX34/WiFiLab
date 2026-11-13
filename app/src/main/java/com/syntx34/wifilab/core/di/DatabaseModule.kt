package com.syntx34.wifilab.core.di

import android.content.Context
import androidx.room.Room
import com.syntx34.wifilab.data.local.WiFiLabDatabase
import com.syntx34.wifilab.data.local.dao.ActivityDao
import com.syntx34.wifilab.data.local.dao.AuditDao
import com.syntx34.wifilab.data.local.dao.BaselineDao
import com.syntx34.wifilab.data.local.dao.PacketCaptureDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WiFiLabDatabase =
        Room.databaseBuilder(
            context,
            WiFiLabDatabase::class.java,
            "wifilab.db",
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideAuditDao(database: WiFiLabDatabase): AuditDao = database.auditDao()

    @Provides
    fun provideBaselineDao(database: WiFiLabDatabase): BaselineDao = database.baselineDao()

    @Provides
    fun provideActivityDao(database: WiFiLabDatabase): ActivityDao = database.activityDao()

    @Provides
    fun providePacketCaptureDao(database: WiFiLabDatabase): PacketCaptureDao =
        database.packetCaptureDao()
}
