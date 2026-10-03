package com.takusima.takutune.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("settings")

data class AppearanceSettings(
    val theme: String = "system",
    val accent: String = "#B36BFF",
    val amoled: Boolean = false
)

class SettingsStore(private val context: Context) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val accent = stringPreferencesKey("accent")
        val amoled = booleanPreferencesKey("amoled")
    }

    val appearance: Flow<AppearanceSettings> = context.settingsDataStore.data.map {
        AppearanceSettings(it[Keys.theme] ?: "system", it[Keys.accent] ?: "#B36BFF", it[Keys.amoled] ?: false)
    }

    suspend fun setTheme(value: String) = context.settingsDataStore.edit { it[Keys.theme] = value }
    suspend fun setAccent(value: String) = context.settingsDataStore.edit { it[Keys.accent] = value }
    suspend fun setAmoled(value: Boolean) = context.settingsDataStore.edit { it[Keys.amoled] = value }
}