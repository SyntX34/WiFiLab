package com.syntx34.wifilab.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val WiFiLabColorScheme = darkColorScheme(
    primary = Cyan400,
    onPrimary = Navy990,
    primaryContainer = Color(0xFF0E3A4A),
    onPrimaryContainer = Cyan300,
    inversePrimary = Cyan700,
    secondary = Blue400,
    onSecondary = Navy990,
    secondaryContainer = Color(0xFF122C52),
    onSecondaryContainer = Blue300,
    tertiary = Teal300,
    onTertiary = Navy990,
    tertiaryContainer = Color(0xFF0E3D38),
    onTertiaryContainer = Teal300,
    background = Navy900,
    onBackground = TextPrimary,
    surface = Navy850,
    onSurface = TextPrimary,
    surfaceVariant = Navy800,
    onSurfaceVariant = TextSecondary,
    surfaceTint = Cyan400,
    inverseSurface = TextPrimary,
    inverseOnSurface = Navy900,
    error = Danger,
    onError = Navy990,
    errorContainer = Color(0xFF4A1B26),
    onErrorContainer = Color(0xFFFFC2CB),
    outline = BorderStrong,
    outlineVariant = BorderColor,
    scrim = Color(0xCC02070D),
)

private val WiFiLabShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp),
)

@Composable
fun WiFiLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WiFiLabColorScheme,
        typography = WiFiLabTypography,
        shapes = WiFiLabShapes,
        content = content,
    )
}
