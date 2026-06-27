package com.example.tetris.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TetrisColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    secondary = AccentCyan,
    tertiary = AccentPink,
    background = AppBackground,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariant,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun TetrisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TetrisColorScheme,
        typography = Typography,
        content = content,
    )
}
