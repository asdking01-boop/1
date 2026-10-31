package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NovaDarkColorScheme = darkColorScheme(
    primary = NovaRed,
    onPrimary = NovaTextPrimary,
    primaryContainer = NovaRedDark,
    onPrimaryContainer = NovaTextPrimary,
    secondary = NovaGold,
    onSecondary = NovaBlack,
    secondaryContainer = NovaSurfaceHighlight,
    onSecondaryContainer = NovaTextPrimary,
    background = NovaBlack,
    onBackground = NovaTextPrimary,
    surface = NovaSurfaceDark,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaCardBg,
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaCardBorder,
    tertiary = NovaGreenLive
)

@Composable
fun NovaIptvTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NovaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
