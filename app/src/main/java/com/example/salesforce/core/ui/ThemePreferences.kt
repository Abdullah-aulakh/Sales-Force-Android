
package com.example.salesforce.core.ui

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(
    name = "theme_preferences"
)

class ThemePreferences(private val context: Context) {

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val themeMode: Flow<ThemeMode> =
        context.themeDataStore.data.map { preferences ->
            preferences[THEME_MODE]
                ?.let { saved -> ThemeMode.entries.find { it.name == saved } }
                ?: ThemeMode.SYSTEM
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }
}
