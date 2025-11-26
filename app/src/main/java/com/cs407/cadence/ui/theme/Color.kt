package com.cs407.cadence.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val MintGreen = Color(0xFFD6FFF6)
val RussianViolet = Color(0xFF231651)
val Turquoise = Color(0xFF4DCCBD)
val UCLABlue = Color(0xFF2374AB)

val LightRed = Color(0xFFFF8484)
val TransparentWhite = Color(0x19FFFFFF)
val TransparentBlack = Color(0x19000000)

val lightGradient = Brush.linearGradient(
    colors = listOf(
        MintGreen,
        Turquoise
    )
)

val darkGradient = Brush.linearGradient(
    colors = listOf(
        UCLABlue,
        RussianViolet
    )
)