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

// 2. Update the Typography object to use your new WixMadeforText font family.
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
    ),

    bodyMedium = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
    ),

    bodySmall = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
    ),

    titleLarge = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
    ),

    labelSmall = TextStyle(
        fontFamily = WixMadeforText,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)
