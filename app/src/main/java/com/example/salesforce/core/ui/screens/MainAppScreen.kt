package com.example.salesforce.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.salesforce.core.network.AuthRepository
import com.example.salesforce.core.network.UserResponse
import com.example.salesforce.core.ui.ThemeMode
import com.example.salesforce.core.ui.components.SalesBottomNavigation
import com.example.salesforce.core.ui.components.SalesTab
import com.example.salesforce.core.ui.screens.DashboardScreen
import com.example.salesforce.core.ui.screens.DashboardSubScreen
import com.example.salesforce.core.ui.screens.ProfileScreen

@Composable
fun MainAppScreen(
    userName: String,
    userResponse: UserResponse?,
    repository: AuthRepository,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    onRefresh: suspend () -> Unit,
) {
    var selectedTab by remember {
        mutableStateOf(SalesTab.Home)
    }
    var dashboardSubScreen by remember {
        mutableStateOf<DashboardSubScreen>(DashboardSubScreen.Grid)
    }

    Scaffold(
        bottomBar = {
            SalesBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AnimatedContent(
                targetState = selectedTab,
                label = "ScreenTransition",
                transitionSpec = {
                    val isMovingRight = targetState.ordinal > initialState.ordinal

                    if (isMovingRight) {
                        (slideInHorizontally(
                            animationSpec = tween(durationMillis = 280),
                            initialOffsetX = { fullWidth -> fullWidth / 4 },
                        ) + fadeIn(animationSpec = tween(280)) + scaleIn(initialScale = 0.98f))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(durationMillis = 220),
                                    targetOffsetX = { fullWidth -> -fullWidth / 4 },
                                ) + fadeOut(animationSpec = tween(220)),
                            )
                    } else {
                        (slideInHorizontally(
                            animationSpec = tween(durationMillis = 280),
                            initialOffsetX = { fullWidth -> -fullWidth / 4 },
                        ) + fadeIn(animationSpec = tween(280)) + scaleIn(initialScale = 0.98f))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(durationMillis = 220),
                                    targetOffsetX = { fullWidth -> fullWidth / 4 },
                                ) + fadeOut(animationSpec = tween(220)),
                            )
                    }
                },
            ) { targetTab ->
                when (targetTab) {
                    SalesTab.Home -> HomeScreen(
                        userResponse = userResponse,
                        onRefresh = onRefresh,
                    )
                    SalesTab.Dashboard -> DashboardScreen(
                        repository = repository,
                        currentUser = userResponse,
                        currentSubScreen = dashboardSubScreen,
                        onSubScreenChange = { dashboardSubScreen = it },
                    )
                    SalesTab.Profile -> ProfileScreen(
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                        onLogout = onLogout,
                    )
                }
            }
        }
    }
}
