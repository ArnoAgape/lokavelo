package com.arnoagape.lokavelo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Color.White,

    secondary = Blue40,
    onSecondary = Color.White,
    secondaryContainer = Blue40,

    tertiary = Blue,
    onTertiary = Color.White,

    background = DarkBlue,
    onBackground = Color(0xFFEAF4FB),

    surface = Color(0xFF071D32),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF0F2A45)
)

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,

    secondary = Blue,
    onSecondary = Color.White,
    secondaryContainer = BlueGrey80,

    tertiary = Blue80,
    onTertiary = Color.Black,

    background = Color(0xFFF4F9FD),
    onBackground = DarkBlue,

    surface = Color(0xFFF4FaFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE8F3F9)
)

@Composable
fun LokaveloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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

    val spacing = Spacing()

    CompositionLocalProvider(
        LocalSpacing provides spacing
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                content()
            }
        }
    }
}