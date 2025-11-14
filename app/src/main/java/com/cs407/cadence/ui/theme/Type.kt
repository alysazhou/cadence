package com.cs407.cadence.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cs407.cadence.R

val WixMadeforText = FontFamily(
    Font(R.font.wixmadefortext_regular, FontWeight.Normal),
    Font(R.font.wixmadefortext_bold, FontWeight.Bold),
    Font(R.font.wixmadefortext_medium, FontWeight.Medium)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 2.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
    ),

    bodyMedium = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
    ),

    bodySmall = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),

    titleLarge = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
    ),

    titleMedium = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
    ),

    titleSmall = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    ),

    labelLarge = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
    ),

    labelMedium = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    ),

    labelSmall = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    ),
)
