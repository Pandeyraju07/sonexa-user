package com.sonexa.app.pg.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Modern Marketplace Color Palette
val PgPrimary = Color(0xFF4F46E5)       // Deep Indigo
val PgPrimaryDark = Color(0xFF6366F1)
val PgSecondary = Color(0xFF0EA5E9)     // Sky Blue
val PgVerifiedGreen = Color(0xFF10B981) // Emerald Green
val PgWarningAmber = Color(0xFFF59E0B)  // Amber
val PgRose = Color(0xFFF43F5E)          // Rose Accent
val PgBackgroundDark = Color(0xFF0F172A)// Slate 900
val PgSurfaceDark = Color(0xFF1E293B)   // Slate 800
val PgCardSurface = Color(0xFF1E293B)
val PgTextPrimary = Color(0xFFF8FAFC)
val PgTextSecondary = Color(0xFF94A3B8)
val PgDivider = Color(0xFF334155)

private val DarkColorScheme = darkColorScheme(
    primary = PgPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF312E81),
    secondary = PgSecondary,
    onSecondary = Color.White,
    background = PgBackgroundDark,
    surface = PgSurfaceDark,
    surfaceVariant = Color(0xFF273549),
    onSurface = PgTextPrimary,
    onSurfaceVariant = PgTextSecondary,
    outline = PgDivider
)

private val LightColorScheme = lightColorScheme(
    primary = PgPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    secondary = PgSecondary,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0)
)

val PgShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun PgMarketplaceTheme(
    darkTheme: Boolean = true, // Default to sleek dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = PgShapes,
        content = content
    )
}
