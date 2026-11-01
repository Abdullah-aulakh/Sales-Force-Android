
package com.example.salesforce.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    onLoginRequested: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppDestination.LOGIN) {
            PlaceholderScreen("Login")
        }

        composable(AppDestination.DASHBOARD) {
            PlaceholderScreen("Dashboard")
        }

        composable(AppDestination.MEETINGS) {
            PlaceholderScreen("Meetings")
        }

        composable(AppDestination.PROFILE) {
            PlaceholderScreen("Profile")
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title)
    }
}
