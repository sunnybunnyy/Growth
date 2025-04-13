package com.example.growth.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    // Primary colors (greens)
    primary = Green40,
    onPrimary = GrowthOnPrimary,
    primaryContainer = Green90,
    onPrimaryContainer = Green10,

    // Secondary colors (earthy browns)
    secondary = Brown40,
    onSecondary = GrowthOnSecondary,
    secondaryContainer = Brown90,
    onSecondaryContainer = Brown10,

    // Tertiary colors (water blue)
    tertiary = WaterBlue,

    // Background/surface colors
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral95,
    onSurface = Neutral10,

    // Error colors
    error = Error30,
    onError = GrowthOnError,
    errorContainer = Error90,
    onErrorContainer = Error10,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreen30,
    onPrimary = GrowthOnPrimary,
    primaryContainer = DarkGreen80,
    onPrimaryContainer = DarkGreen10,

    secondary = DarkBrown30,
    onSecondary = GrowthOnSecondary,
    secondaryContainer = DarkBrown80,
    onSecondaryContainer = DarkBrown10,

    background = DarkNeutral99,
    onBackground = DarkNeutral10,
    surface = DarkNeutral95,
    onSurface = DarkNeutral10,

    error = Error80,
    onError = GrowthOnError,
    errorContainer = Error30,
    onErrorContainer = Error90,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun GrowthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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