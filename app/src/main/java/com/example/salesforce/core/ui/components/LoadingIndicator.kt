
package com.example.salesforce.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    message: String? = null,
    size: Dp = 22.dp,
    strokeWidth: Dp = 2.5.dp,
    textSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            strokeWidth = strokeWidth,
            color = MaterialTheme.colorScheme.primary
        )

        if (!message.isNullOrBlank()) {
            Spacer(Modifier.width(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = textSize,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FullScreenLoading(
    message: String = "Loading...",
    textSize: TextUnit = 16.sp,
    indicatorSize: Dp = 28.dp
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(
            message = message,
            size = indicatorSize,
            textSize = textSize
        )
    }
}

/**
 * Wrap your screen content with this composable.
 * When isLoading is true, it blurs the content underneath,
 * dims the screen, and blocks interaction with the screen.
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    message: String = "Loading...",
    blurRadius: Dp = 8.dp,
    dimAmount: Float = 0.30f,
    indicatorSize: Dp = 32.dp,
    strokeWidth: Dp = 3.dp,
    textSize: TextUnit = 16.sp,
    cardColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {

        // Screen content, blurred only while loading.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isLoading && blurRadius > 0.dp) {
                        Modifier.blur(blurRadius)
                    } else {
                        Modifier
                    }
                )
        ) {
            content()
        }

        if (isLoading) {
            // Translucent layer above the screen.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha = dimAmount.coerceIn(0f, 1f)
                        )
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = {}
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardColor.copy(alpha = 0.94f),
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .width(240.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(Modifier.size(4.dp))

                        LoadingIndicator(
                            message = null,
                            size = indicatorSize,
                            strokeWidth = strokeWidth
                        )

                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = textSize,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.size(4.dp))
                    }
                }
            }
        }
    }
}
