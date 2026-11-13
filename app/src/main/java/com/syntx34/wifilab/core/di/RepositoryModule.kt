package com.syntx34.wifilab.core.di

import com.syntx34.wifilab.data.network.AndroidNetworkAuditor
import com.syntx34.wifilab.data.preferences.SettingsRepositoryImpl
import com.syntx34.wifilab.data.repository.ActivityRepositoryImpl
import com.syntx34.wifilab.data.repository.AuditRepositoryImpl
import com.syntx34.wifilab.data.repository.LocalResearchRepository
import com.syntx34.wifilab.data.repository.PacketCaptureRepositoryImpl
import com.syntx34.wifilab.data.wifi.AndroidWifiRepository
import com.syntx34.wifilab.domain.repository.ActivityRepository
import com.syntx34.wifilab.domain.repository.AuditRepository
import com.syntx34.wifilab.domain.repository.NetworkAuditService
import com.syntx34.wifilab.domain.repository.PacketCaptureRepository
import com.syntx34.wifilab.domain.repository.ResearchRepository
import com.syntx34.wifilab.domain.repository.SettingsRepository
import com.syntx34.wifilab.domain.repository.WifiRepository
import com.syntx34.wifilab.domain.security.PasswordEntropy
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWifiRepository(implementation: AndroidWifiRepository): WifiRepository

    @Binds
    @Singleton
    abstract fun bindNetworkAuditService(implementation: AndroidNetworkAuditor): NetworkAuditService

    @Binds
    @Singleton
    abstract fun bindAuditRepository(implementation: AuditRepositoryImpl): AuditRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(implementation: ActivityRepositoryImpl): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindPacketCaptureRepository(
        implementation: PacketCaptureRepositoryImpl,
    ): PacketCaptureRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(implementation: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindResearchRepository(implementation: LocalResearchRepository): ResearchRepository

    companion object {
        @Provides
        @Singleton
        fun providePasswordEntropy(): PasswordEntropy = PasswordEntropy()
    }
}
