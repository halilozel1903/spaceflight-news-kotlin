package io.github.halilozel1903.spaceflightnews.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2A4BC8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE1FF),
    onPrimaryContainer = Color(0xFF001257),
    secondary = Color(0xFF5A5D72),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDFE1F9),
    onSecondaryContainer = Color(0xFF171B2C),
    tertiary = Color(0xFFC2410C),
    onTertiary = Color.White,
    background = Color(0xFFF8F8FC),
    onBackground = Color(0xFF1A1B21),
    surface = Color(0xFFF8F8FC),
    onSurface = Color(0xFF1A1B21),
    surfaceVariant = Color(0xFFE6E7F2),
    onSurfaceVariant = Color(0xFF45464F),
    outline = Color(0xFF767680),
    outlineVariant = Color(0xFFC6C6D0),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB8C4FF),
    onPrimary = Color(0xFF002388),
    primaryContainer = Color(0xFF0B34AE),
    onPrimaryContainer = Color(0xFFDDE1FF),
    secondary = Color(0xFFC3C5DD),
    onSecondary = Color(0xFF2C2F42),
    secondaryContainer = Color(0xFF424659),
    onSecondaryContainer = Color(0xFFDFE1F9),
    tertiary = Color(0xFFFFB693),
    onTertiary = Color(0xFF561F00),
    background = Color(0xFF0D0F1A),
    onBackground = Color(0xFFE3E2EA),
    surface = Color(0xFF0D0F1A),
    onSurface = Color(0xFFE3E2EA),
    surfaceVariant = Color(0xFF242734),
    onSurfaceVariant = Color(0xFFC6C6D0),
    outline = Color(0xFF90909A),
    outlineVariant = Color(0xFF45464F),
)

@Composable
fun NewsTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
