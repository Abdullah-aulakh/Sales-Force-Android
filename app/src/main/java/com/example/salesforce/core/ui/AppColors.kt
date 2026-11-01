package com.example.salesforce.core.ui

import androidx.compose.ui.graphics.Color

data class AppColorPalette(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val error: Color,
    val onError: Color,
    val success: Color,
    val onSuccess: Color,
    val disabled: Color,
    val onDisabled: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val outline: Color,
    val text: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val scrim: Color,
    val elevation: Color,
)

object AppColors {

    // Option 2: Executive Indigo & Midnight (Modern SaaS)
    val Light = AppColorPalette(
        primary = Color(0xFF4F46E5),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF0284C7),
        onSecondary = Color(0xFFFFFFFF),
        tertiary = Color(0xFFF59E0B),
        onTertiary = Color(0xFFFFFFFF),
        error = Color(0xFFDC2626),
        onError = Color(0xFFFFFFFF),
        success = Color(0xFF16A34A),
        onSuccess = Color(0xFFFFFFFF),
        disabled = Color(0xFFE5E7EB),
        onDisabled = Color(0xFF9CA3AF),
        background = Color(0xFFF4F6F9),
        onBackground = Color(0xFF111827),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF111827),
        outline = Color(0xFFE5E7EB),
        text = Color(0xFF111827),
        textSecondary = Color(0xFF6B7280),
        textDisabled = Color(0xFF9CA3AF),
        scrim = Color(0x66111827),
        elevation = Color(0xFF111827),
    )

    val Dark = AppColorPalette(
        primary = Color(0xFF6366F1),
        onPrimary = Color(0xFF0D1117),
        secondary = Color(0xFF38BDF8),
        onSecondary = Color(0xFF0D1117),
        tertiary = Color(0xFFFBBF24),
        onTertiary = Color(0xFF0D1117),
        error = Color(0xFFF87171),
        onError = Color(0xFF450A0A),
        success = Color(0xFF34D399),
        onSuccess = Color(0xFF064E3B),
        disabled = Color(0xFF161B22),
        onDisabled = Color(0xFF6B7280),
        background = Color(0xFF0D1117),
        onBackground = Color(0xFFF3F4F6),
        surface = Color(0xFF161B22),
        onSurface = Color(0xFFF3F4F6),
        outline = Color(0xFF30363D),
        text = Color(0xFFF3F4F6),
        textSecondary = Color(0xFF9CA3AF),
        textDisabled = Color(0xFF6B7280),
        scrim = Color(0xCC000000),
        elevation = Color(0xFF000000),
    )
}
