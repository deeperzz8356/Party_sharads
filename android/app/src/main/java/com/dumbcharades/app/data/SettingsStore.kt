package com.dumbcharades.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("settings")

data class AppSettings(
    val onboardingComplete: Boolean = false,
    val adultUnlocked: Boolean = false,
    val tiltThreshold: Float = 50f,
    val theme: String = "LIGHT",
    val soundVolume: Float = 0.75f,
    val soundPack: String = "CHIME",
    val guestMode: Boolean = false,
)

@Singleton
class SettingsStore @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val onboarding = booleanPreferencesKey("onboarding")
        val adult = booleanPreferencesKey("adult")
        val threshold = floatPreferencesKey("tilt_threshold")
        val theme = stringPreferencesKey("theme")
        val soundVolume = floatPreferencesKey("sound_volume")
        val soundPack = stringPreferencesKey("sound_pack")
        val guestMode = booleanPreferencesKey("guest_mode")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { values ->
        AppSettings(values[Keys.onboarding] ?: false, values[Keys.adult] ?: false, values[Keys.threshold] ?: 50f,
            values[Keys.theme] ?: "LIGHT", values[Keys.soundVolume] ?: 0.75f, values[Keys.soundPack] ?: "CHIME",
            values[Keys.guestMode] ?: false)
    }

    suspend fun completeOnboarding() = context.dataStore.edit { it[Keys.onboarding] = true }
    suspend fun setAdultUnlocked(value: Boolean) = context.dataStore.edit { it[Keys.adult] = value }
    suspend fun setThreshold(value: Float) = context.dataStore.edit { it[Keys.threshold] = value.coerceIn(30f, 75f) }
    suspend fun setTheme(value: String) = context.dataStore.edit { it[Keys.theme] = value }
    suspend fun setSoundVolume(value: Float) = context.dataStore.edit { it[Keys.soundVolume] = value.coerceIn(0f, 1f) }
    suspend fun setSoundPack(value: String) = context.dataStore.edit { it[Keys.soundPack] = value }
    suspend fun setGuestMode(value: Boolean) = context.dataStore.edit { it[Keys.guestMode] = value }
}
