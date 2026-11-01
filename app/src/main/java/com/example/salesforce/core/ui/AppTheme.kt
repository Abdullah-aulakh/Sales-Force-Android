
package com.example.salesforce.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.salesforce.core.ui.components.AppTheme as LumoTheme
import com.example.salesforce.ui.theme.SalesForceTheme as MaterialTheme

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MaterialTheme(darkTheme = darkTheme) {
        LumoTheme(isDarkTheme = darkTheme) {
            content()
        }
    }
}
