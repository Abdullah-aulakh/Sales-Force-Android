package com.example.salesforce.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.salesforce.core.ui.ThemeMode

// Option 2: Executive Indigo & Midnight (Modern SaaS)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color(0xFF0D1117),
    primaryContainer = Color(0xFF1E1B4B),
    onPrimaryContainer = Color(0xFF818CF8),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF0D1117),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF0D1117),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFF3F4F6),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF30363D),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF4F46E5),
    secondary = Color(0xFF0284C7),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFF59E0B),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFF4F6F9),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFE5E7EB),
)

@Composable
fun SalesForceTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> darkTheme
    }
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
