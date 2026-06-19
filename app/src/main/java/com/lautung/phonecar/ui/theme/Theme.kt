package com.lautung.phonecar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PhoneCarColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = BrandIndigo,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    outline = Slate200,
    error = AlertRed,
)

@Composable
fun PhoneCarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PhoneCarColors,
        typography = PhoneCarTypography,
        content = content,
    )
}
