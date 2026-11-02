package com.example.shoppinglistgenerator.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = OnGreen20,
    primaryContainer = GreenContainer30,
    onPrimaryContainer = OnGreenContainer90,
    secondaryContainer = GreenContainer30,
    onSecondaryContainer = OnGreenContainer90,
    background = Dark10,
    onBackground = OnDark90,
    surface = Dark10,
    onSurface = OnDark90,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outlineVariant = OutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = GreenContainer90,
    onPrimaryContainer = OnGreenContainer10,
    secondaryContainer = GreenContainer90,
    onSecondaryContainer = OnGreenContainer10,
    background = Cream98,
    onBackground = OnCream10,
    surface = Cream98,
    onSurface = OnCream10,
    surfaceContainerLowest = Color.White,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outlineVariant = OutlineVariantLight
)

@Composable
fun ShoppingListGeneratorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+, but off by default to keep the Figma palette
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
