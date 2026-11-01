package com.example.salesforce.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircularPercentageIndicator(
    percentage: Float, // 0f to 100f
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    size: Dp = 90.dp,
    strokeWidth: Dp = 10.dp,
) {
    val clampedPercentage = percentage.coerceIn(0f, 100f)

    val progressAnimatable = remember { Animatable(0f) }

    LaunchedEffect(clampedPercentage) {
        progressAnimatable.animateTo(
            targetValue = clampedPercentage,
            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        )
    }

    val animatedSweep = (progressAnimatable.value / 100f) * 360f
    val animatedProgressText = progressAnimatable.value

    // Dynamic color based on percentage
    val resolvedColor = when {
        clampedPercentage >= 75f -> Color(0xFF10B981) // Emerald Success
        clampedPercentage >= 40f -> MaterialTheme.colorScheme.primary // Primary Indigo
        else -> Color(0xFFF59E0B) // Amber Warning
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val titleColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                // Draw background track
                drawCircle(
                    color = trackColor,
                    radius = (size.toPx() - strokeWidth.toPx()) / 2f,
                    style = stroke,
                )
                // Draw active arc
                drawArc(
                    color = resolvedColor,
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = stroke,
                )
            }

            Text(
                text = "${animatedProgressText.toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = subtitleColor,
        )
    }
}
