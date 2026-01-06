package com.kp.momoney.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val THEME_KEY = stringPreferencesKey("app_theme")
        val SEED_COLOR_KEY = longPreferencesKey("seed_color")
    }

    /**
     * Get the current theme preference as a Flow
     * Defaults to LIGHT if not set
     */
    val theme: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        val themeString = preferences[THEME_KEY] ?: AppTheme.LIGHT.name
        try {
            AppTheme.valueOf(themeString)
        } catch (e: IllegalArgumentException) {
            AppTheme.LIGHT
        }
    }

    /**
     * Get the current theme preference (suspending function)
     */
    suspend fun getTheme(): AppTheme = theme.first()

    /**
     * Set the theme preference
     * Runs on Dispatchers.IO
     */
    suspend fun setTheme(theme: AppTheme) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[THEME_KEY] = theme.name
            }
        }
    }

    /**
     * Get the current seed color preference as a Flow
     * Defaults to SunYellow if not set
     */
    val seedColor: Flow<Color> = context.dataStore.data.map { preferences ->
        val colorValue = preferences[SEED_COLOR_KEY]
        if (colorValue != null) {
            Color(colorValue.toULong())
        } else {
            // Default to SunYellow
            Color(0xFFD4A017)
        }
    }

    /**
     * Get the current seed color preference (suspending function)
     */
    suspend fun getSeedColor(): Color = seedColor.first()

    /**
     * Set the seed color preference
     * Runs on Dispatchers.IO
     */
    suspend fun setSeedColor(color: Color) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[SEED_COLOR_KEY] = color.value.toLong()
            }
        }
    }
}

