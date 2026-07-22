package com.tudecitrus.ui.theme

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
    primary = CitrusLime,
    onPrimary = CitrusBlack,
    secondary = CitrusYellow,
    onSecondary = CitrusBlack,
    tertiary = CitrusPale,
    onTertiary = CitrusBlack,
    background = CitrusBgDark,
    onBackground = CitrusOnDark,
    surface = CitrusSurfaceDark,
    onSurface = CitrusOnDark,
    surfaceVariant = CitrusCardDark,
    onSurfaceVariant = CitrusGray,
    outline = CitrusGray,
    outlineVariant = CitrusOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = CitrusLime,
    onPrimary = CitrusBlack,
    secondary = CitrusYellow,
    onSecondary = CitrusBlack,
    tertiary = CitrusPale,
    onTertiary = CitrusBlack,
    background = CitrusBgLight,
    onBackground = CitrusBlack,
    surface = CitrusSurfaceLight,
    onSurface = CitrusBlack,
    surfaceVariant = CitrusPale,
    onSurfaceVariant = CitrusBlack,
    outline = CitrusGray
)

@Composable
fun CitrusScanTheme(
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
        content = content
    )
}
