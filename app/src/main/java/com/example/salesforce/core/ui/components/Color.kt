package com.example.salesforce.core.ui.components

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Black: Color = Color(0xFF000000)
val Gray900: Color = Color(0xFF282828)
val Gray800: Color = Color(0xFF4b4b4b)
val Gray700: Color = Color(0xFF5e5e5e)
val Gray600: Color = Color(0xFF727272)
val Gray500: Color = Color(0xFF868686)
val Gray400: Color = Color(0xFFC7C7C7)
val Gray300: Color = Color(0xFFDFDFDF)
val Gray200: Color = Color(0xFFE2E2E2)
val Gray100: Color = Color(0xFFF7F7F7)
val Gray50: Color = Color(0xFFFFFFFF)
val White: Color = Color(0xFFFFFFFF)

val Red900: Color = Color(0xFF520810)
val Red800: Color = Color(0xFF950f22)
val Red700: Color = Color(0xFFbb032a)
val Red600: Color = Color(0xFFde1135)
val Red500: Color = Color(0xFFf83446)
val Red400: Color = Color(0xFFfc7f79)
val Red300: Color = Color(0xFFffb2ab)
val Red200: Color = Color(0xFFffd2cd)
val Red100: Color = Color(0xFFffe1de)
val Red50: Color = Color(0xFFfff0ee)

val Blue900: Color = Color(0xFF276EF1)
val Blue800: Color = Color(0xFF3F7EF2)
val Blue700: Color = Color(0xFF578EF4)
val Blue600: Color = Color(0xFF6F9EF5)
val Blue500: Color = Color(0xFF87AEF7)
val Blue400: Color = Color(0xFF9FBFF8)
val Blue300: Color = Color(0xFFB7CEFA)
val Blue200: Color = Color(0xFFCFDEFB)
val Blue100: Color = Color(0xFFE7EEFD)
val Blue50: Color = Color(0xFFFFFFFF)

val Green950: Color = Color(0xFF0B4627)
val Green900: Color = Color(0xFF16643B)
val Green800: Color = Color(0xFF1A7544)
val Green700: Color = Color(0xFF178C4E)
val Green600: Color = Color(0xFF1DAF61)
val Green500: Color = Color(0xFF1FC16B)
val Green400: Color = Color(0xFF3EE089)
val Green300: Color = Color(0xFF84EBB4)
val Green200: Color = Color(0xFFC2F5DA)
val Green100: Color = Color(0xFFD0FBE9)
val Green50: Color = Color(0xFFE0FAEC)

@Immutable
data class Colors(
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
    val surface: Color,
    val onSurface: Color,
    val background: Color,
    val onBackground: Color,
    val outline: Color,
    val transparent: Color = Color.Transparent,
    val white: Color = White,
    val black: Color = Black,
    val text: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val scrim: Color,
    val elevation: Color,
)

internal val LightColors =
    Colors(
        primary = Color(0xFF4F46E5),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF0284C7),
        onSecondary = Color(0xFFFFFFFF),
        tertiary = Color(0xFFF59E0B),
        onTertiary = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF111827),
        error = Color(0xFFDC2626),
        onError = Color(0xFFFFFFFF),
        success = Color(0xFF16A34A),
        onSuccess = Color(0xFFFFFFFF),
        disabled = Color(0xFFE5E7EB),
        onDisabled = Color(0xFF9CA3AF),
        background = Color(0xFFF4F6F9),
        onBackground = Color(0xFF111827),
        outline = Color(0xFFE5E7EB),
        transparent = Color.Transparent,
        white = Color(0xFFFFFFFF),
        black = Color(0xFF000000),
        text = Color(0xFF111827),
        textSecondary = Color(0xFF6B7280),
        textDisabled = Color(0xFF9CA3AF),
        scrim = Color(0x66111827),
        elevation = Color(0xFF111827),
    )

internal val DarkColors =
    Colors(
        primary = Color(0xFF6366F1),
        onPrimary = Color(0xFF0D1117),
        secondary = Color(0xFF38BDF8),
        onSecondary = Color(0xFF0D1117),
        tertiary = Color(0xFFFBBF24),
        onTertiary = Color(0xFF0D1117),
        surface = Color(0xFF161B22),
        onSurface = Color(0xFFF3F4F6),
        error = Color(0xFFF87171),
        onError = Color(0xFF450A0A),
        success = Color(0xFF34D399),
        onSuccess = Color(0xFF064E3B),
        disabled = Color(0xFF161B22),
        onDisabled = Color(0xFF6B7280),
        background = Color(0xFF0D1117),
        onBackground = Color(0xFFF3F4F6),
        outline = Color(0xFF30363D),
        transparent = Color.Transparent,
        white = Color(0xFFFFFFFF),
        black = Color(0xFF000000),
        text = Color(0xFFF3F4F6),
        textSecondary = Color(0xFF9CA3AF),
        textDisabled = Color(0xFF6B7280),
        scrim = Color(0xCC000000),
        elevation = Color(0xFF000000),
    )

val LocalColors = staticCompositionLocalOf { LightColors }
val LocalContentColor = compositionLocalOf { Color.Black }
val LocalContentAlpha = compositionLocalOf { 1f }

fun Colors.contentColorFor(backgroundColor: Color): Color {
    return when (backgroundColor) {
        primary -> onPrimary
        secondary -> onSecondary
        tertiary -> onTertiary
        surface -> onSurface
        error -> onError
        success -> onSuccess
        disabled -> onDisabled
        background -> onBackground
        else -> Color.Unspecified
    }
}
