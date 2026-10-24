package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SahityaIndigoPrimary,
    onPrimary = Color.White,
    secondary = SahityaCyanSecondary,
    onSecondary = Color.Black,
    tertiary = SahityaEmeraldAccent,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    error = SahityaRoseError
)

private val LightColorScheme = lightColorScheme(
    primary = SahityaIndigoPrimary,
    onPrimary = Color.White,
    secondary = SahityaCyanSecondary,
    onSecondary = Color.White,
    tertiary = SahityaEmeraldAccent,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    error = SahityaRoseError
)

@Composable
fun SahityaCreatorTheme(
    darkTheme: Boolean = true, // Default to sleek dark theme for Creator App
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
