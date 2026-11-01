package com.example.salesforce.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.salesforce.core.network.UserResponse
import com.example.salesforce.core.ui.components.CircularPercentageIndicator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userResponse: UserResponse? = null,
    onRefresh: suspend () -> Unit = {},
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val userName = userResponse?.name ?: "User"
    val role = userResponse?.role ?: "Sales Executive"
    val status = userResponse?.status ?: "Active"
    val managerName = userResponse?.managerName
    val managerId = userResponse?.managerId

    val monthlyTargets = userResponse?.monthlyTargets ?: 100
    val dailyTargets = userResponse?.dailyTargets ?: 10
    val liveTodayOrders = userResponse?.todayOrders ?: 0
    val liveMonthlyOrders = userResponse?.monthlyOrders ?: 0
    val liveMonthlyMeetings = userResponse?.monthlyMeetings ?: 0
    val liveAllTimeOrders = userResponse?.allTimeOrders ?: 0
    val liveAllTimeMeetings = userResponse?.allTimeMeetings ?: 0

    // Computed percentages & success rates from updated live counts
    val monthlyTargetPct = if (monthlyTargets > 0) {
        ((liveMonthlyOrders.toFloat() / monthlyTargets.toFloat()) * 100f).coerceIn(0f, 100f)
    } else {
        0f
    }

    val dailyTargetPct = if (dailyTargets > 0) {
        ((liveTodayOrders.toFloat() / dailyTargets.toFloat()) * 100f).coerceIn(0f, 100f)
    } else {
        0f
    }

    val monthlySuccessRate = if (liveMonthlyMeetings > 0) {
        ((liveMonthlyOrders.toFloat() / liveMonthlyMeetings.toFloat()) * 100f).toInt().coerceIn(0, 100)
    } else {
        0
    }

    val allTimeSuccessRate = if (liveAllTimeMeetings > 0) {
        ((liveAllTimeOrders.toFloat() / liveAllTimeMeetings.toFloat()) * 100f).toInt().coerceIn(0, 100)
    } else {
        0
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                try {
                    onRefresh()
                } finally {
                    isRefreshing = false
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // 1. Welcome Section fully wired to UserResponse
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Welcome back!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                ) {
                    val managerText = when {
                        !managerName.isNullOrBlank() -> "Managed by $managerName"
                        managerId != null -> "Manager ID: #$managerId"
                        else -> "Director Managed"
                    }
                    Text(
                        text = managerText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 0.dp, vertical = 5.dp),
                    )
                }
            }

            // 2. Target Progression Section (Wired to monthlyTargets, dailyTargets, monthlyOrders, todayOrders)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Target Progression",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        CircularPercentageIndicator(
                            percentage = monthlyTargetPct,
                            title = "Monthly Target",
                            subtitle = "$liveMonthlyOrders of $monthlyTargets orders",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        )
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        CircularPercentageIndicator(
                            percentage = dailyTargetPct,
                            title = "Daily Target",
                            subtitle = "$liveTodayOrders of $dailyTargets orders today",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        )
                    }
                }
            }

            // 3. Performance Overview Section (Wired to monthlyMeetings, allTimeMeetings, monthlyOrders, allTimeOrders)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Performance Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                // Success Rate Row (Computed from actual orders vs meetings conversion)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    PerformanceMetricCard(
                        modifier = Modifier.weight(1f),
                        label = "Success Rate",
                        targetValue = monthlySuccessRate,
                        suffix = "%",
                        trend = "Monthly conversion",
                        color = MaterialTheme.colorScheme.primary,
                    )
                    PerformanceMetricCard(
                        modifier = Modifier.weight(1f),
                        label = "Success Rate",
                        targetValue = allTimeSuccessRate,
                        suffix = "%",
                        trend = "All-time conversion",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                // Meetings & Orders Activity Row (Wired to real backend response fields)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ActivityOverviewCard(
                        modifier = Modifier.weight(1f),
                        title = "Meetings",
                        currentValue = liveMonthlyMeetings,
                        totalValue = liveAllTimeMeetings,
                        period = "Monthly / All-time",
                    )
                    ActivityOverviewCard(
                        modifier = Modifier.weight(1f),
                        title = "Orders",
                        currentValue = liveMonthlyOrders,
                        totalValue = liveAllTimeOrders,
                        period = "Monthly / All-time",
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceMetricCard(
    label: String,
    targetValue: Int,
    suffix: String,
    trend: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatable.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        )
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${animatable.value.toInt()}$suffix",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                text = trend,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun ActivityOverviewCard(
    title: String,
    currentValue: Int,
    totalValue: Int,
    period: String,
    modifier: Modifier = Modifier,
) {
    val currentAnimatable = remember { Animatable(0f) }
    val totalAnimatable = remember { Animatable(0f) }

    LaunchedEffect(currentValue, totalValue) {
        launch {
            currentAnimatable.animateTo(
                targetValue = currentValue.toFloat(),
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            )
        }
        launch {
            totalAnimatable.animateTo(
                targetValue = totalValue.toFloat(),
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            )
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "${currentAnimatable.value.toInt()}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "/ ${totalAnimatable.value.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
            Text(
                text = period,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
