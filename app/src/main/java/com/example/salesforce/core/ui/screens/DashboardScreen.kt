package com.example.salesforce.core.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.salesforce.core.network.AuthRepository
import com.example.salesforce.core.network.UserResponse

sealed interface DashboardSubScreen {
    object Grid : DashboardSubScreen
    object Orders : DashboardSubScreen
    object Meetings : DashboardSubScreen
    object Attendance : DashboardSubScreen
    object Followups : DashboardSubScreen
}

data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val subScreen: DashboardSubScreen,
)

@Composable
fun DashboardScreen(
    repository: AuthRepository,
    currentUser: UserResponse?,
    currentSubScreen: DashboardSubScreen,
    onSubScreenChange: (DashboardSubScreen) -> Unit,
) {
    val items = listOf(
        DashboardItem("Orders", Icons.Rounded.ShoppingCart, DashboardSubScreen.Orders),
        DashboardItem("Meetings", Icons.Rounded.Event, DashboardSubScreen.Meetings),
        DashboardItem("Attendance", Icons.Rounded.Fingerprint, DashboardSubScreen.Attendance),
        DashboardItem("Followups", Icons.Rounded.Assessment, DashboardSubScreen.Followups),
    )

    when (currentSubScreen) {
        DashboardSubScreen.Grid -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = "Quick access to sales tools & operations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(items) { item ->
                        SquareDashboardButton(
                            title = item.title,
                            icon = item.icon,
                            onClick = { onSubScreenChange(item.subScreen) },
                        )
                    }
                }
            }
        }
        DashboardSubScreen.Orders -> {
            OrdersScreen(
                repository = repository,
                onBack = { onSubScreenChange(DashboardSubScreen.Grid) },
            )
        }
        DashboardSubScreen.Meetings -> {
            MeetingsScreen(
                repository = repository,
                currentUser = currentUser,
                onBack = { onSubScreenChange(DashboardSubScreen.Grid) },
            )
        }
        DashboardSubScreen.Attendance -> {
            AttendanceScreen(
                repository = repository,
                currentUser = currentUser,
                onBack = { onSubScreenChange(DashboardSubScreen.Grid) },
            )
        }
        DashboardSubScreen.Followups -> {
            FollowupsScreen(
                repository = repository,
                currentUser = currentUser,
                onBack = { onSubScreenChange(DashboardSubScreen.Grid) },
            )
        }
    }
}

@Composable
fun SquareDashboardButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Perfect square shape
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
