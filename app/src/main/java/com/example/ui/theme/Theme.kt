package com.example.ui.theme

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

private val GreenDarkColorScheme = darkColorScheme(
    primary = ForestDarkPrimary,
    onPrimary = Color(0xFF022C22),
    primaryContainer = ForestDarkPrimaryContainer,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = ForestDarkPrimaryDark,
    onSecondary = Color.White,
    secondaryContainer = ForestDarkPrimaryPale,
    onSecondaryContainer = Color(0xFF6EE7B7),
    tertiary = ForestDarkGold,
    onTertiary = Color(0xFF451A03),
    background = ForestDarkBackground,
    onBackground = ForestDarkText,
    surface = ForestDarkSurface,
    onSurface = ForestDarkText,
    surfaceVariant = ForestDarkCard,
    onSurfaceVariant = ForestDarkTextMuted,
    outline = ForestDarkCardBorder,
    outlineVariant = ForestDarkDivider
)

private val GreenLightColorScheme = lightColorScheme(
    primary = SleekGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = SleekGreenPale,
    onPrimaryContainer = SleekGreenDark,
    secondary = SleekGreenMedium,
    onSecondary = Color.White,
    secondaryContainer = SleekGreenSurface,
    onSecondaryContainer = SleekGreenDark,
    tertiary = ForestGoldDark,
    onTertiary = Color.White,
    background = SleekGreenSurface,
    onBackground = SleekSlate900,
    surface = Color.White,
    onSurface = SleekTextDark,
    surfaceVariant = SleekSlate100,
    onSurfaceVariant = SleekSlate500,
    outline = SleekBorder,
    outlineVariant = SleekSlate200
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to user-requested green dark theme
    dynamicColor: Boolean = false, // Keep consistent Forestry brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

