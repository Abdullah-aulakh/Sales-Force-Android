package com.example.salesforce.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun rememberScreenScaleFactor(): Float {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    return when {
        screenWidthDp < 360 -> 0.85f // Small compact phones
        screenWidthDp < 410 -> 0.92f // Standard smaller phones
        screenWidthDp <= 600 -> 1.0f  // Regular phones
        else -> 1.15f                // Tablets & large screens
    }
}
