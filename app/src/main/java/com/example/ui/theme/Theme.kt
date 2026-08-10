package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurplePrimaryDark,
    secondary = GoldAccent,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = ErrorRed
)

@Composable
fun ArenaXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun FrostedGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Ambient Glowing Background Mesh
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-Left Glowing Purple Radial
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x389333EA), // Purple 600 glow
                        Color(0x207C3AED),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.1f, height * 0.15f),
                    radius = width * 0.85f
                ),
                radius = width * 0.85f,
                center = Offset(width * 0.1f, height * 0.15f)
            )

            // Bottom-Right Glowing Blue/Indigo Radial
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x304F46E5), // Indigo 600 glow
                        Color(0x183B82F6),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.9f, height * 0.85f),
                    radius = width * 0.8f
                ),
                radius = width * 0.8f,
                center = Offset(width * 0.9f, height * 0.85f)
            )
        }

        content()
    }
}

