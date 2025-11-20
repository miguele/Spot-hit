package com.spothit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SpotHitTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 34.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontFamily = FontFamily.SansSerif
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = FontFamily.SansSerif
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 14.sp
    )
)
