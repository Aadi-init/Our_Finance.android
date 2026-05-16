package com.altf4.ourfinance.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.altf4.ourfinance.ui.theme.NavTrayDarkHex

// Mapping your specific names to the system's "Dark Mode"
private val DarkColorScheme = darkColorScheme(

    onPrimary = PrimaryButtonTextDark,          //#000000
    background = AppBackgroundDark,         //#141414
    onBackground = TextPrimaryDark,         //#ffffff
    surface = SurfaceBackgroundDark,        //#1d1e20
    onSurface = TextPrimaryDark,            //#ffffff
    error = AccentRedNegative,          //#ff383c
    tertiary = AccentGreenPositive,          //#34c759

    // For Google Sign Up Container
    surfaceContainer = OperatorBtnDarkHex,           //#3d3d3d

    // For Nav Bar
    surfaceContainerLow = NavTrayDarkHex,     // Tray Background #1B1B1B
    surfaceVariant = TextBoxBackgroundDark,      // Slider Background #242527
    primary = PrimaryButtonBgDark,    // Highlighted Icon #FFFFFF
    onSurfaceVariant = TextHintDark,           // Unselected Icons #717171
)

// Mapping your specific names to the system's "Light Mode"
private val LightColorScheme = lightColorScheme(
             //#000000
    onPrimary = PrimaryButtonTextLight,         //#ffffff
    background = AppBackgroundLight,            //#f6f6f6
    onBackground = TextPrimaryLight,            //#000000
    surface = SurfaceBackgroundLight,           //#ffffff
    onSurface = TextPrimaryLight,           //#000000
    error = AccentRedNegative,          //#ff383c
    tertiary = AccentGreenPositive,          //#34c759

    // For Google Sign Up Container
    surfaceContainer = OperatorBtnLightHex,           //#d6d6d6

    // For Nav Bar
    surfaceContainerLow = NavTrayLightHex,     // Tray Background #EBEBEB
    surfaceVariant = TextBoxBackgroundLight,      // Slider Background #FFFFFF
    primary = PrimaryButtonBgLight,    // Highlighted Icon #000000
    onSurfaceVariant = TextHintLight           // Unselected Icons #A9A9A9


)

@Composable
fun OurFinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}