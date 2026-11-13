package com.syntx34.wifilab.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.syntx34.wifilab.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: androidx.datastore.core.DataStore<Preferences> by
    preferencesDataStore(name = "wifi_lab_preferences")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    override val scanCooldownSeconds: Flow<Int> = context.settingsDataStore.data.map { preferences ->
        (preferences[SCAN_COOLDOWN] ?: DEFAULT_COOLDOWN).coerceIn(MIN_COOLDOWN, MAX_COOLDOWN)
    }

    override val completedLessonIds: Flow<Set<String>> = context.settingsDataStore.data.map { preferences ->
        preferences[COMPLETED_LESSONS] ?: emptySet()
    }

    override suspend fun setScanCooldownSeconds(seconds: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[SCAN_COOLDOWN] = seconds.coerceIn(MIN_COOLDOWN, MAX_COOLDOWN)
        }
    }

    override suspend fun setLessonCompleted(lessonId: String, completed: Boolean) {
        context.settingsDataStore.edit { preferences ->
            val current = preferences[COMPLETED_LESSONS] ?: emptySet()
            preferences[COMPLETED_LESSONS] = if (completed) current + lessonId else current - lessonId
        }
    }

    override suspend fun clearLearningProgress() {
        context.settingsDataStore.edit { preferences ->
            preferences.remove(COMPLETED_LESSONS)
        }
    }

    override suspend fun clearAllPreferences() {
        context.settingsDataStore.edit { it.clear() }
    }

    companion object {
        const val DEFAULT_COOLDOWN = 30
        const val MIN_COOLDOWN = 15
        const val MAX_COOLDOWN = 120
        private val SCAN_COOLDOWN = intPreferencesKey("scan_cooldown_seconds")
        private val COMPLETED_LESSONS = stringSetPreferencesKey("completed_lessons")
    }
}
