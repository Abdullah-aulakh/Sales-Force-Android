package com.example.salesforce.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun rememberScreenScaleFactor(): Float {
    return screenScaleFactor(LocalConfiguration.current.screenWidthDp)
}

internal fun screenScaleFactor(screenWidthDp: Int): Float {
    if (screenWidthDp <= 0) return 1f
    return (screenWidthDp / 390f).coerceIn(0.82f, 1f)
}
