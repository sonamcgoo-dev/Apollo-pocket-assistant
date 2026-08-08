package com.appolopocket.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Vaporwave Color Palette
object VaporwaveColors {
    // Primary - Neon Magenta
    val NeonMagenta = Color(0xFFFF00FF)
    val NeonMagentaDark = Color(0xFFCC00CC)
    val NeonMagentaLight = Color(0xFFFF66FF)
    
    // Secondary - Electric Cyan
    val ElectricCyan = Color(0xFF00FFFF)
    val ElectricCyanDark = Color(0xFF00CCCC)
    val ElectricCyanLight = Color(0xFF66FFFF)
    
    // Tertiary - Vaporwave Purple
    val VaporwavePurple = Color(0xFF8B5CF6)
    val VaporwavePurpleDark = Color(0xFF6D28D9)
    val VaporwavePurpleLight = Color(0xFFA78BFA)
    
    // Backgrounds
    val DeepNight = Color(0xFF0D0221)
    val SoftLavender = Color(0xFF1A0A2E)
    val GlassyPurple = Color(0xFF2D1B4E)
    val SurfaceElevated = Color(0xFF3D2B5E)
    
    // Text
    val TextPrimary = Color(0xFFF0E6FF)
    val TextSecondary = Color(0xFFA78BFA)
    val TextTertiary = Color(0xFF6B5B7E)
    
    // Status
    val NeonGreen = Color(0xFF39FF14)
    val HotPink = Color(0xFFFF1493)
    val GoldenBronze = Color(0xFFCD7F32)
    val SunsetOrange = Color(0xFFFF6B35)
    
    // Gradients
    val GradientStart = NeonMagenta
    val GradientCenter = VaporwavePurple
    val GradientEnd = ElectricCyan
    
    // Glass effect
    val GlassBackground = Color(0x401A0A2E)
    val GlassBorder = Color(0x60FF00FF)
}

private val VaporwaveColorScheme = darkColorScheme(
    primary = VaporwaveColors.NeonMagenta,
    onPrimary = VaporwaveColors.DeepNight,
    primaryContainer = VaporwaveColors.VaporwavePurple,
    onPrimaryContainer = VaporwaveColors.TextPrimary,
    
    secondary = VaporwaveColors.ElectricCyan,
    onSecondary = VaporwaveColors.DeepNight,
    secondaryContainer = VaporwaveColors.GlassyPurple,
    onSecondaryContainer = VaporwaveColors.TextPrimary,
    
    tertiary = VaporwaveColors.VaporwavePurpleLight,
    onTertiary = VaporwaveColors.DeepNight,
    tertiaryContainer = VaporwaveColors.GlassyPurple,
    onTertiaryContainer = VaporwaveColors.TextPrimary,
    
    error = VaporwaveColors.HotPink,
    onError = VaporwaveColors.TextPrimary,
    errorContainer = Color(0xFF8B0000),
    onErrorContainer = VaporwaveColors.TextPrimary,
    
    background = VaporwaveColors.DeepNight,
    onBackground = VaporwaveColors.TextPrimary,
    
    surface = VaporwaveColors.SoftLavender,
    onSurface = VaporwaveColors.TextPrimary,
    surfaceVariant = VaporwaveColors.GlassyPurple,
    onSurfaceVariant = VaporwaveColors.TextSecondary,
    
    outline = VaporwaveColors.NeonMagenta,
    outlineVariant = VaporwaveColors.GlassyPurple,
    
    inverseSurface = VaporwaveColors.TextPrimary,
    inverseOnSurface = VaporwaveColors.DeepNight,
    inversePrimary = VaporwaveColors.VaporwavePurple
)

@Composable
fun AppoloPocketTheme(
    darkTheme: Boolean = true, // Always dark for vaporwave
    content: @Composable () -> Unit
) {
    val colorScheme = VaporwaveColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = VaporwaveColors.DeepNight.toArgb()
            window.navigationBarColor = VaporwaveColors.DeepNight.toArgb()
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
