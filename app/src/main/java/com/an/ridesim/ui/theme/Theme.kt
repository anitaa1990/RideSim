package com.an.ridesim.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue700,
    onPrimary = PureWhite,
    primaryContainer = Amber200,
    onPrimaryContainer = PureBlack,
    secondary = Amber600,
    onSecondary = PureBlack,
    tertiary = Amber400,
    onTertiary = PureBlack,
    background = Grey100,
    onBackground = Grey950,
    surface = PureWhite,
    onSurface = Grey900,
    surfaceVariant = Grey200,
    onSurfaceVariant = Grey700,
    inverseSurface = Grey900,
    inverseOnSurface = PureWhite,
    surfaceContainer = Grey300,
    outline = Grey600,
    outlineVariant = Grey500,
    error = Amber800,
    onError = Grey750,
    errorContainer = Grey800,
    secondaryContainer = Blue100,
    surfaceTint = Grey600,
    tertiaryContainer = Black200,
    inversePrimary = Amber500,
    onTertiaryContainer = Black100
)

private val LightColorScheme = lightColorScheme(
    primary = Blue700,
    onPrimary = PureWhite,
    primaryContainer = Amber200,
    onPrimaryContainer = PureBlack,
    secondary = Amber600,
    onSecondary = PureBlack,
    tertiary = Amber400,
    onTertiary = PureBlack,
    background = Grey100,
    onBackground = Grey950,
    surface = PureWhite,
    onSurface = Grey900,
    surfaceVariant = Grey200,
    onSurfaceVariant = Grey700,
    inverseSurface = Grey900,
    inverseOnSurface = PureWhite,
    surfaceContainer = Grey300,
    outline = Grey600,
    outlineVariant = Grey500,
    error = Amber800,
    onError = Grey750,
    errorContainer = Grey800,
    secondaryContainer = Blue100,
    surfaceTint = Grey600,
    tertiaryContainer = Black200,
    inversePrimary = Amber500,
    onTertiaryContainer = Black100
)

@Composable
fun RideSimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
        content = content
    )
}