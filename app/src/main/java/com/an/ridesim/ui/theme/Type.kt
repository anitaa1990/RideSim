package com.an.ridesim.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.an.ridesim.R

val FigTreeFontFamily = FontFamily(
    Font(R.font.figtree_regular, weight = FontWeight.Normal),
    Font(R.font.figtree_medium, weight = FontWeight.Medium),
    Font(R.font.figtree_bold, weight = FontWeight.Bold),
    Font(R.font.figtree_black, weight = FontWeight.Black),
    Font(R.font.figtree_semibold, weight = FontWeight.SemiBold)
)

val Typography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FigTreeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FigTreeFontFamily,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FigTreeFontFamily,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FigTreeFontFamily,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FigTreeFontFamily,
        fontSize = 17.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
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
        letterSpacing = 0.5.sp
    ),
)

@Composable
fun headlineStyle(
    color: Color = MaterialTheme.colorScheme.onSecondaryContainer
) = MaterialTheme.typography.headlineMedium.copy(color = color)

@Composable
fun titleTextStyle(
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontWeight: FontWeight = FontWeight.SemiBold
) = MaterialTheme.typography.labelLarge.copy(
    color = color,
    fontWeight = fontWeight
)

@Composable
fun subTitleTextStyle(
    color: Color = MaterialTheme.colorScheme.outline
) = MaterialTheme.typography.labelMedium.copy(color = color)

@Composable
fun heading3TextStyle(
    color: Color = MaterialTheme.colorScheme.outlineVariant
) = MaterialTheme.typography.labelSmall.copy(color = color)

@Composable
fun primaryTextStyle(
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontWeight: FontWeight = FontWeight.Medium,
    fontSize: TextUnit = 15.sp
) = MaterialTheme.typography.bodyLarge.copy(
    color = color,
    fontWeight = fontWeight,
    fontSize = fontSize
)

@Composable
fun secondaryTextStyle(
    color: Color = MaterialTheme.colorScheme.outline,
    fontWeight: FontWeight = FontWeight.Medium
) = MaterialTheme.typography.bodyMedium.copy(
    color = color,
    fontWeight = fontWeight
)

@Composable
fun tertiaryTextStyle(
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: TextUnit = 12.sp
) = MaterialTheme.typography.bodySmall.copy(
    color = color,
    fontWeight = fontWeight,
    fontSize = fontSize
)
