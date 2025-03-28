package com.an.ridesim.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.an.ridesim.R

// 👇 FigTree
val FigTreeFontFamily = FontFamily(
    Font(R.font.figtree_regular, weight = FontWeight.Normal),
    Font(R.font.figtree_medium, weight = FontWeight.Medium),
    Font(R.font.figtree_bold, weight = FontWeight.Bold),
    Font(R.font.figtree_black, weight = FontWeight.Black)
)

val Typography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FigTreeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 25.sp,
        color = Color(0XFF2A3B31),
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FigTreeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp,
        color = Color(0xFF202124)
    ),
    bodyMedium = TextStyle(
        fontFamily = FigTreeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp,
        color = Color(0xFF5F6368)
    ),
    labelMedium = TextStyle(
        fontFamily = FigTreeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FigTreeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp,
        color = Color(0xFF515151)
    ),
    bodySmall = TextStyle(
        fontFamily = FigTreeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp,
        color = Color(0xFF7B8997)
    ),
)