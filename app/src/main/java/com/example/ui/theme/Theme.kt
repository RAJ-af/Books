package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val EditorialLightColorScheme = lightColorScheme(
    primary = ObsidianBlack,
    onPrimary = Color.White,
    primaryContainer = SoftSepiaSurface,
    onPrimaryContainer = ObsidianBlack,
    secondary = AmberGold,
    onSecondary = Color.White,
    secondaryContainer = SoftSepiaSurface,
    onSecondaryContainer = ObsidianBlack,
    tertiary = TerracottaWarm,
    onTertiary = Color.White,
    background = CreamBackground,
    onBackground = ObsidianBlack,
    surface = WarmOffWhite,
    onSurface = ObsidianBlack,
    surfaceVariant = SoftSepiaSurface,
    onSurfaceVariant = TextMuted,
    outline = WarmBorder,
    outlineVariant = SoftSepiaSurface
)

private val EditorialDarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = ObsidianBlack,
    primaryContainer = CharcoalDark,
    onPrimaryContainer = Color.White,
    secondary = AmberGold,
    onSecondary = ObsidianBlack,
    secondaryContainer = CharcoalDark,
    onSecondaryContainer = Color.White,
    tertiary = TerracottaWarm,
    onTertiary = ObsidianBlack,
    background = ReaderDarkBg,
    onBackground = ReaderDarkText,
    surface = ReaderDarkSurface,
    onSurface = ReaderDarkText,
    surfaceVariant = CharcoalDark,
    onSurfaceVariant = TextLightMuted,
    outline = CharcoalDark,
    outlineVariant = ReaderDarkSurface
)

val EditorialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EditorialDarkColorScheme else EditorialLightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                it.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(it, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = EditorialShapes,
        content = content
    )
}
