package com.takusima.takutune.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
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

private fun parseColor(value: String, fallback: Color): Color =
    runCatching { Color(android.graphics.Color.parseColor(value)) }.getOrDefault(fallback)

@Composable
fun TakuTuneTheme(
    settings: AppearanceSettings,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (settings.theme) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }

    val accent = parseColor(settings.accent, DefaultPurple)
    val context = LocalContext.current

    val colors = when {
        settings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> darkColorScheme(
            primary = accent,
            onPrimary = Color.White,
            background = if (settings.amoled) Color.Black else DarkBackground,
            surface = if (settings.amoled) Color.Black else DarkSurface
        )
        else -> lightColorScheme(
            primary = accent,
            onPrimary = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
