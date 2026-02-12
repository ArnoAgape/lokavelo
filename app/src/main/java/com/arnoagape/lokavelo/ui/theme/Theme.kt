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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Color.White,

    secondary = Blue40,
    onSecondary = Color.White,

    tertiary = BlueGrey40,
    onTertiary = Color.White,

    background = DarkBlue,
    onBackground = Color(0xFFEAF4FB),

    surface = Color(0xFF162B3F),
    onSurface = Color(0xFFEAF4FB)
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,

    secondary = BlueGrey40,
    onSecondary = Color.White,

    tertiary = Blue80,
    onTertiary = Color.Black,

    background = Color(0xFFF4F9FD),
    onBackground = DarkBlue,

    surface = Color.White,
    onSurface = Color(0xFF1A1A1A)
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