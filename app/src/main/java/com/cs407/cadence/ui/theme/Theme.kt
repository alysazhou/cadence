package com.cs407.cadence.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

// 1. Define your DarkColorScheme
private val DarkColorScheme = darkColorScheme(
    primary = RussianViolet,
    onPrimary = Color.White,
    secondary = Turquoise,
    onSecondary = RussianViolet,
    tertiary = MintGreen,
    background = RussianViolet,
    surface = TransparentWhite,
    onSurface = Color.White
)

// 2. Define your LightColorScheme
private val LightColorScheme = lightColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = UCLABlue,
    onSecondary = Color.White,
    tertiary = RussianViolet,
    background = Color.White,
    surface = TransparentBlack,
    onSurface = Color.Black
)

@Composable
fun CadenceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 3. Choose the color scheme dynamically
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}