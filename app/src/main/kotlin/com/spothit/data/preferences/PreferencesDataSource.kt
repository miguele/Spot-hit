package com.spothit.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesDataSource(private val dataStore: DataStore<Preferences>) {
    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    val selectedTheme: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_SELECTION] ?: THEME_SYSTEM
    }

    val animationSpeedMultiplier: Flow<Int> = dataStore.data.map { preferences ->
        preferences[ANIMATION_SPEED] ?: 1
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setSelectedTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_SELECTION] = theme
        }
    }

    suspend fun setAnimationSpeedMultiplier(multiplier: Int) {
        dataStore.edit { preferences ->
            preferences[ANIMATION_SPEED] = multiplier
        }
    }

    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"

        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val THEME_SELECTION = stringPreferencesKey("theme_selection")
        private val ANIMATION_SPEED = intPreferencesKey("animation_speed")

        fun create(
            context: Context,
            scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        ): PreferencesDataSource {
            val dataStore = PreferenceDataStoreFactory.create(
                scope = scope,
                produceFile = { context.preferencesDataStoreFile("spot_hit_preferences") }
            )
            return PreferencesDataSource(dataStore)
        }
    }
}
