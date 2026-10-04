package com.takusima.takutune.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.takusima.takutune.core.preferences.AppearanceSettings

private val DefaultPurple = Color(0xFFB36BFF)
private val DarkBackground = Color(0xFF09070D)
private val DarkSurface = Color(0xFF15101C)
private val LightBackground = Color(0xFFF8F7FB)
private val LightSurface = Color.White
private val LightText = Color(0xFF1A171D)
private val LightSecondary = Color(0xFF625D66)

private fun parseColor(value: String, fallback: Color): Color =
    runCatching { Color(android.graphics.Color.parseColor(value)) }.getOrDefault(fallback)

private fun customDarkScheme(accent: Color, amoled: Boolean): ColorScheme =
    darkColorScheme(primary = accent, onPrimary = Color.White, primaryContainer = accent.copy(alpha = 0.28f), onPrimaryContainer = Color.White, secondary = accent, onSecondary = Color.White, secondaryContainer = accent.copy(alpha = 0.20f), onSecondaryContainer = Color.White, tertiary = accent, onTertiary = Color.White, background = if (amoled) Color.Black else DarkBackground, onBackground = Color(0xFFF4EFF7), surface = if (amoled) Color.Black else DarkSurface, onSurface = Color(0xFFF4EFF7), surfaceVariant = Color(0xFF27212D), onSurfaceVariant = Color(0xFFC9C0CC), outline = Color(0xFF958B9A))

private fun customLightScheme(accent: Color): ColorScheme =
    lightColorScheme(primary = accent, onPrimary = Color.White, primaryContainer = accent.copy(alpha = 0.18f), onPrimaryContainer = LightText, secondary = accent, onSecondary = Color.White, secondaryContainer = accent.copy(alpha = 0.12f), onSecondaryContainer = LightText, tertiary = accent, onTertiary = Color.White, background = LightBackground, onBackground = LightText, surface = LightSurface, onSurface = LightText, surfaceVariant = Color(0xFFEAE6ED), onSurfaceVariant = LightSecondary, outline = Color(0xFF77717B))

@Composable
fun TakuTuneTheme(settings: AppearanceSettings, content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (settings.theme) { "light" -> false; "dark" -> true; else -> systemDark }
    val accent = parseColor(settings.accent, DefaultPurple)
    val context = LocalContext.current
    val colors = when {
        settings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> customDarkScheme(accent, settings.amoled)
        else -> customLightScheme(accent)
    }
    MaterialTheme(colorScheme = colors, content = content)
}