package com.kp.momoney.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Configuration for app theme
 */
data class AppThemeConfig(
    val seedColor: Color,
    val isDark: Boolean
)

// ==================== Jungle Palette (Specific Colors) ====================
// Primary: #122B1D (Dark Jungle Green)
val JunglePrimary = Color(0xFF122B1D)
// Secondary: #537E72 (William Hooker's Green)
val JungleSecondary = Color(0xFF537E72)
// Tertiary/Accent: #9CC97F (Pistachio)
val JungleTertiary = Color(0xFF9CC97F)
// Background/Neutral: #CDDECB (Light Green)
val JungleBackground = Color(0xFFCDDECB)

// Light Jungle Color Scheme
private val JungleLightColorScheme = lightColorScheme(
    primary = JunglePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A4D3A),
    onPrimaryContainer = Color(0xFFE8F5E9),
    
    secondary = JungleSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF6A9A8A),
    onSecondaryContainer = Color(0xFFE8F5E9),
    
    tertiary = JungleTertiary,
    onTertiary = Color(0xFF1A2E1F),
    tertiaryContainer = Color(0xFFB8E0A0),
    onTertiaryContainer = Color(0xFF0F1A0D),
    
    background = JungleBackground,
    onBackground = Color(0xFF1A2E1F),
    
    surface = Color(0xFFF5F9F4),
    onSurface = Color(0xFF1A2E1F),
    surfaceVariant = Color(0xFFD8E8D5),
    onSurfaceVariant = Color(0xFF3A4D3F),
    
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    outline = Color(0xFF6F8074),
    outlineVariant = Color(0xFFBFC9C0),
    scrim = Color.Black,
    inverseSurface = Color(0xFF2F4233),
    inverseOnSurface = Color(0xFFF0F7ED),
    inversePrimary = Color(0xFF4D7A5F),
    surfaceTint = JunglePrimary,
)

// Dark Jungle Color Scheme
private val JungleDarkColorScheme = darkColorScheme(
    primary = JungleTertiary, // Use lighter color for dark mode
    onPrimary = Color(0xFF0F1A0D),
    primaryContainer = Color(0xFF2A4D3A),
    onPrimaryContainer = Color(0xFFB8E0A0),
    
    secondary = Color(0xFF7DB5A3),
    onSecondary = Color(0xFF0F1A0D),
    secondaryContainer = Color(0xFF537E72),
    onSecondaryContainer = Color(0xFFD8E8D5),
    
    tertiary = JungleTertiary,
    onTertiary = Color(0xFF1A2E1F),
    tertiaryContainer = Color(0xFF6A9A8A),
    onTertiaryContainer = Color(0xFFE8F5E9),
    
    background = Color(0xFF0F1A0D),
    onBackground = Color(0xFFD8E8D5),
    
    surface = Color(0xFF1A2E1F),
    onSurface = Color(0xFFD8E8D5),
    surfaceVariant = Color(0xFF3A4D3F),
    onSurfaceVariant = Color(0xFFBFC9C0),
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    outline = Color(0xFF899A8D),
    outlineVariant = Color(0xFF3A4D3F),
    scrim = Color.Black,
    inverseSurface = Color(0xFFD8E8D5),
    inverseOnSurface = Color(0xFF2F4233),
    inversePrimary = JunglePrimary,
    surfaceTint = JungleTertiary,
)

// ==================== Seed Colors for Other Palettes ====================
val JungleGreen = JunglePrimary // #122B1D
val OceanBlue = Color(0xFF1E3A5F) // Blue-based
val VolcanoRed = Color(0xFF8B2635) // Red-based
val SunYellow = Color(0xFFD4A017) // Yellow-based

// Ocean Palette - Light
private val OceanLightColorScheme = lightColorScheme(
    primary = OceanBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2E5A8A),
    onPrimaryContainer = Color(0xFFE3F2FD),
    
    secondary = Color(0xFF4A7BA7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF6B9BC8),
    onSecondaryContainer = Color(0xFF0A1F2E),
    
    tertiary = Color(0xFF5B9BD4),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB3D9F0),
    onTertiaryContainer = Color(0xFF0A1F2E),
    
    background = Color(0xFFF5F9FC),
    onBackground = Color(0xFF1A1F24),
    
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1F24),
    surfaceVariant = Color(0xFFE3E8ED),
    onSurfaceVariant = Color(0xFF3A4A5A),
    
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    outline = Color(0xFF6B7A8A),
    outlineVariant = Color(0xFFBFC8D0),
    scrim = Color.Black,
    inverseSurface = Color(0xFF2A2F34),
    inverseOnSurface = Color(0xFFF0F5F8),
    inversePrimary = Color(0xFF5B9BD4),
    surfaceTint = OceanBlue,
)

// Ocean Palette - Dark
private val OceanDarkColorScheme = darkColorScheme(
    primary = Color(0xFF5B9BD4),
    onPrimary = Color(0xFF0A1F2E),
    primaryContainer = Color(0xFF2E5A8A),
    onPrimaryContainer = Color(0xFFB3D9F0),
    
    secondary = Color(0xFF6B9BC8),
    onSecondary = Color(0xFF0A1F2E),
    secondaryContainer = Color(0xFF4A7BA7),
    onSecondaryContainer = Color(0xFFE3F2FD),
    
    tertiary = Color(0xFF7DB5E0),
    onTertiary = Color(0xFF0A1F2E),
    tertiaryContainer = Color(0xFF4A7BA7),
    onTertiaryContainer = Color(0xFFE3F2FD),
    
    background = Color(0xFF0F1419),
    onBackground = Color(0xFFE3E8ED),
    
    surface = Color(0xFF1A1F24),
    onSurface = Color(0xFFE3E8ED),
    surfaceVariant = Color(0xFF3A4A5A),
    onSurfaceVariant = Color(0xFFBFC8D0),
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    outline = Color(0xFF8A9AAA),
    outlineVariant = Color(0xFF3A4A5A),
    scrim = Color.Black,
    inverseSurface = Color(0xFFE3E8ED),
    inverseOnSurface = Color(0xFF2A2F34),
    inversePrimary = OceanBlue,
    surfaceTint = Color(0xFF5B9BD4),
)

// Volcano Palette - Light
private val VolcanoLightColorScheme = lightColorScheme(
    primary = VolcanoRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB04A5A),
    onPrimaryContainer = Color(0xFFFFE5E8),
    
    secondary = Color(0xFFC85A6A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE08A9A),
    onSecondaryContainer = Color(0xFF2E0A0F),
    
    tertiary = Color(0xFFE08A9A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFB4C4),
    onTertiaryContainer = Color(0xFF2E0A0F),
    
    background = Color(0xFFFFF5F6),
    onBackground = Color(0xFF2E0A0F),
    
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2E0A0F),
    surfaceVariant = Color(0xFFFFE5E8),
    onSurfaceVariant = Color(0xFF5A2A35),
    
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    outline = Color(0xFF8A5A6A),
    outlineVariant = Color(0xFFE0B4C4),
    scrim = Color.Black,
    inverseSurface = Color(0xFF4A2A35),
    inverseOnSurface = Color(0xFFFFF5F6),
    inversePrimary = Color(0xFFE08A9A),
    surfaceTint = VolcanoRed,
)

// Volcano Palette - Dark
private val VolcanoDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE08A9A),
    onPrimary = Color(0xFF2E0A0F),
    primaryContainer = Color(0xFFB04A5A),
    onPrimaryContainer = Color(0xFFFFB4C4),
    
    secondary = Color(0xFFE08A9A),
    onSecondary = Color(0xFF2E0A0F),
    secondaryContainer = Color(0xFFC85A6A),
    onSecondaryContainer = Color(0xFFFFE5E8),
    
    tertiary = Color(0xFFFFB4C4),
    onTertiary = Color(0xFF2E0A0F),
    tertiaryContainer = Color(0xFFC85A6A),
    onTertiaryContainer = Color(0xFFFFE5E8),
    
    background = Color(0xFF1A0A0F),
    onBackground = Color(0xFFFFE5E8),
    
    surface = Color(0xFF2E0A0F),
    onSurface = Color(0xFFFFE5E8),
    surfaceVariant = Color(0xFF5A2A35),
    onSurfaceVariant = Color(0xFFE0B4C4),
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    outline = Color(0xFFAA7A8A),
    outlineVariant = Color(0xFF5A2A35),
    scrim = Color.Black,
    inverseSurface = Color(0xFFFFE5E8),
    inverseOnSurface = Color(0xFF4A2A35),
    inversePrimary = VolcanoRed,
    surfaceTint = Color(0xFFE08A9A),
)

// Sun Palette - Light
private val SunLightColorScheme = lightColorScheme(
    primary = SunYellow,
    onPrimary = Color(0xFF2A1F0A),
    primaryContainer = Color(0xFFE8C85A),
    onPrimaryContainer = Color(0xFF2A1F0A),
    
    secondary = Color(0xFFE8C85A),
    onSecondary = Color(0xFF2A1F0A),
    secondaryContainer = Color(0xFFFFE08A),
    onSecondaryContainer = Color(0xFF2A1F0A),
    
    tertiary = Color(0xFFFFE08A),
    onTertiary = Color(0xFF2A1F0A),
    tertiaryContainer = Color(0xFFFFF0B4),
    onTertiaryContainer = Color(0xFF2A1F0A),
    
    background = Color(0xFFFFFBF0),
    onBackground = Color(0xFF2A1F0A),
    
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2A1F0A),
    surfaceVariant = Color(0xFFFFF0B4),
    onSurfaceVariant = Color(0xFF5A4A2A),
    
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    outline = Color(0xFFAA9A7A),
    outlineVariant = Color(0xFFE0D4B4),
    scrim = Color.Black,
    inverseSurface = Color(0xFF4A3A2A),
    inverseOnSurface = Color(0xFFFFFBF0),
    inversePrimary = Color(0xFFFFE08A),
    surfaceTint = SunYellow,
)

// Sun Palette - Dark
private val SunDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFE08A),
    onPrimary = Color(0xFF2A1F0A),
    primaryContainer = Color(0xFFE8C85A),
    onPrimaryContainer = Color(0xFF2A1F0A),
    
    secondary = Color(0xFFFFE08A),
    onSecondary = Color(0xFF2A1F0A),
    secondaryContainer = Color(0xFFE8C85A),
    onSecondaryContainer = Color(0xFF2A1F0A),
    
    tertiary = Color(0xFFFFF0B4),
    onTertiary = Color(0xFF2A1F0A),
    tertiaryContainer = Color(0xFFE8C85A),
    onTertiaryContainer = Color(0xFF2A1F0A),
    
    background = Color(0xFF1A150A),
    onBackground = Color(0xFFFFF0B4),
    
    surface = Color(0xFF2A1F0A),
    onSurface = Color(0xFFFFF0B4),
    surfaceVariant = Color(0xFF5A4A2A),
    onSurfaceVariant = Color(0xFFE0D4B4),
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    outline = Color(0xFFAA9A7A),
    outlineVariant = Color(0xFF5A4A2A),
    scrim = Color.Black,
    inverseSurface = Color(0xFFFFF0B4),
    inverseOnSurface = Color(0xFF4A3A2A),
    inversePrimary = SunYellow,
    surfaceTint = Color(0xFFFFE08A),
)

/**
 * List of available theme seed colors
 */
val ThemeSeeds = listOf(
    "Jungle" to JungleGreen,
    "Ocean" to OceanBlue,
    "Volcano" to VolcanoRed,
    "Sun" to SunYellow
)

/**
 * Generate a ColorScheme from a seed color and dark mode preference
 */
fun generateColorScheme(seedColor: Color, isDark: Boolean): ColorScheme {
    return when {
        // Use specific Jungle palette if it matches (with exact colors)
        seedColor == JungleGreen -> {
            if (isDark) JungleDarkColorScheme else JungleLightColorScheme
        }
        // Ocean palette
        seedColor == OceanBlue -> {
            if (isDark) OceanDarkColorScheme else OceanLightColorScheme
        }
        // Volcano palette
        seedColor == VolcanoRed -> {
            if (isDark) VolcanoDarkColorScheme else VolcanoLightColorScheme
        }
        // Sun palette
        seedColor == SunYellow -> {
            if (isDark) SunDarkColorScheme else SunLightColorScheme
        }
        // Fallback to Jungle if unknown color
        else -> {
            if (isDark) JungleDarkColorScheme else JungleLightColorScheme
        }
    }
}


