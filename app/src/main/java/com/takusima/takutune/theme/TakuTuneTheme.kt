package com.takusima.takutune.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.takusima.takutune.core.preferences.AppearanceSettings

private val Purple = Color(0xFFB36BFF)
private val DarkBackground = Color(0xFF09070D)

@Composable
fun TakuTuneTheme(settings: AppearanceSettings, content: @Composable () -> Unit) {
    val dark = settings.theme != "light"
    val colors = if (dark) darkColorScheme(primary = Purple, background = if (settings.amoled) Color.Black else DarkBackground, surface = if (settings.amoled) Color.Black else Color(0xFF15101C))
    else lightColorScheme(primary = Color(0xFF6A32A8))
    MaterialTheme(colorScheme = colors, content = content)
}