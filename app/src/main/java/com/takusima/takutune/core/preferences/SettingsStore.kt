package com.takusima.takutune.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("settings")

data class AppearanceSettings(
    val theme: String = "system",
    val accent: String = "#B36BFF",
    val amoled: Boolean = false,
    val dynamicColor: Boolean = false,
    val animationScale: Float = 1f
)

class SettingsStore(private val context: Context) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val accent = stringPreferencesKey("accent")
        val amoled = booleanPreferencesKey("amoled")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val animationScale = floatPreferencesKey("animation_scale")
    }

    val appearance: Flow<AppearanceSettings> = context.settingsDataStore.data.map {
        AppearanceSettings(
            it[Keys.theme] ?: "system",
            it[Keys.accent] ?: "#B36BFF",
            it[Keys.amoled] ?: false,
            it[Keys.dynamicColor] ?: false,
            it[Keys.animationScale] ?: 1f
        )
    }

    suspend fun setTheme(value: String) = context.settingsDataStore.edit { it[Keys.theme] = value }
    suspend fun setAccent(value: String) = context.settingsDataStore.edit { it[Keys.accent] = value }
    suspend fun setAmoled(value: Boolean) = context.settingsDataStore.edit { it[Keys.amoled] = value }
    suspend fun setDynamicColor(value: Boolean) = context.settingsDataStore.edit { it[Keys.dynamicColor] = value }
    suspend fun setAnimationScale(value: Float) = context.settingsDataStore.edit { it[Keys.animationScale] = value }
}