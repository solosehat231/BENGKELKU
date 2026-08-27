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

private val DarkColorScheme = darkColorScheme(
    primary = MontecarloOrange,
    onPrimary = Color.White,
    primaryContainer = MontecarloOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = MasterAiBlue,
    onSecondary = Color.White,
    secondaryContainer = AutomotiveSlateLight,
    onSecondaryContainer = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = MontecarloOrange,
    onPrimary = Color.White,
    primaryContainer = HighDensityBlueLight,
    onPrimaryContainer = HighDensityNavy,
    secondary = HighDensityBlue,
    onSecondary = Color.White,
    secondaryContainer = HighDensityBlueLight,
    onSecondaryContainer = HighDensityNavy,
    background = HighDensityCanvas,
    onBackground = HighDensityTextPrimary,
    surface = CardSurface,
    onSurface = HighDensityTextPrimary,
    surfaceVariant = HighDensityBranchAvatar,
    onSurfaceVariant = HighDensityTextSecondary,
    outline = CardBorder
)

@Composable
fun BengkelKuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our brand colors for cohesive automotive look
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
