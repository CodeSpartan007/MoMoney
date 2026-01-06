package com.kp.momoney.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
    }

    /**
     * Get the current theme preference as a Flow
     * Defaults to SYSTEM if not set
     */
    val theme: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        val themeString = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
        try {
            AppTheme.valueOf(themeString)
        } catch (e: IllegalArgumentException) {
            AppTheme.SYSTEM
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
}

