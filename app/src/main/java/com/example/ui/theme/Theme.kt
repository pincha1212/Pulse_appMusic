package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StandardDarkScheme = darkColorScheme(
    primary = ElectricCyan,
    secondary = NeonViolet,
    tertiary = AccentGradientEnd,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = AmoledBackground,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder
)

private val AmoledDarkScheme = darkColorScheme(
    primary = ElectricCyan,
    secondary = NeonViolet,
    tertiary = AccentGradientEnd,
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onPrimary = AmoledBackground,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = AmoledBorder
)

@Composable
fun PulseTheme(
    isAmoledMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isAmoledMode) AmoledDarkScheme else StandardDarkScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
