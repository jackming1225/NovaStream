package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = InstrumentCyan,
    onPrimary = StudioBlack,
    primaryContainer = StudioCard,
    onPrimaryContainer = InstrumentCyan,
    secondary = SlateBlue,
    onSecondary = Color.White,
    secondaryContainer = StudioCardHover,
    onSecondaryContainer = SlateBlue,
    tertiary = PhosphorGreen,
    onTertiary = StudioBlack,
    background = StudioCanvas,
    onBackground = TextPrimary,
    surface = StudioCard,
    onSurface = TextPrimary,
    surfaceVariant = StudioCardHover,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    outlineVariant = StudioBorderSubtle,
    error = SafetyRed,
    onError = Color.White
)

@Composable
fun NovaStreamTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
