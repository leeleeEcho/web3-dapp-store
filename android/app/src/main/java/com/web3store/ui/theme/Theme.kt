package com.web3store.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DIColorScheme = darkColorScheme(
    primary = DIColors.Primary,
    onPrimary = DIColors.Background,
    primaryContainer = DIColors.Primary.copy(alpha = 0.2f),
    onPrimaryContainer = DIColors.Primary,

    secondary = DIColors.Secondary,
    onSecondary = DIColors.Background,
    secondaryContainer = DIColors.Secondary.copy(alpha = 0.2f),
    onSecondaryContainer = DIColors.Secondary,

    tertiary = DIColors.PrimaryDark,
    onTertiary = DIColors.TextPrimary,

    background = DIColors.Background,
    onBackground = DIColors.TextPrimary,

    surface = DIColors.Surface,
    onSurface = DIColors.TextPrimary,
    surfaceVariant = DIColors.Card,
    onSurfaceVariant = DIColors.TextSecondary,

    outline = DIColors.Border,
    outlineVariant = DIColors.BorderGold.copy(alpha = 0.3f),

    error = DIColors.Error,
    onError = DIColors.TextPrimary,
)

@Composable
fun Web3StoreTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DIColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DIColors.Background.toArgb()
            window.navigationBarColor = DIColors.Card.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
