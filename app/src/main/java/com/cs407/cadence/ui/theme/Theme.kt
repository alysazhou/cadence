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
import com.cs407.cadence.ui.screens.HomeScreen
private val ColorScheme = lightColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF0094C4),
    secondary = Color(0xFFFFBB33),
    tertiary = Color(0xFF888888),
    background = Color(0xFFEAEAEA),
    surface = Color(0xFFEAEAEA),

    /* Other default colors to override if needed */
)

@Composable
fun CadenceTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = ColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}