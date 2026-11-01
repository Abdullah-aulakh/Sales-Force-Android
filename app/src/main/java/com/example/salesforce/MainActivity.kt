package com.example.salesforce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.salesforce.core.auth.AuthTokenStore
import com.example.salesforce.core.errors.AppError
import com.example.salesforce.core.network.ApiClient
import com.example.salesforce.core.network.AuthRepository
import com.example.salesforce.core.network.UserResponse
import com.example.salesforce.core.ui.PakistanTime
import com.example.salesforce.core.ui.ThemeMode
import com.example.salesforce.core.ui.ThemePreferences
import com.example.salesforce.core.ui.rememberScreenScaleFactor
import com.example.salesforce.core.ui.components.AppToastHost
import com.example.salesforce.core.ui.components.LoadingOverlay
import com.example.salesforce.core.ui.components.rememberAppToastState
import com.example.salesforce.core.ui.notifications.AppNotifier
import com.example.salesforce.core.ui.notifications.LocalAppNotifier
import com.example.salesforce.core.ui.screens.OfflineScreen
import com.example.salesforce.core.ui.screens.SplashScreen
import com.example.salesforce.ui.screens.LoginScreen
import com.example.salesforce.ui.screens.MainAppScreen
import com.example.salesforce.ui.theme.SalesForceTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.TimeZone

class MainActivity : ComponentActivity() {

    companion object {
        // Configurable Splash Screen Stay Duration in Milliseconds (Modify to test duration)
        const val SPLASH_SCREEN_DURATION_MS = 250L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        TimeZone.setDefault(PakistanTime.timeZone)
        super.onCreate(savedInstanceState)

        val tokenStore = AuthTokenStore(applicationContext)
        val repository = AuthRepository(
            tokenStore = tokenStore,
            api = ApiClient.service,
        )
        val themePreferences = ThemePreferences(applicationContext)

        setContent {
            val scope = rememberCoroutineScope()
            val toastState = rememberAppToastState()
            val appNotifier = remember {
                AppNotifier(toastState = toastState)
            }

            val themeMode by themePreferences.themeMode.collectAsState(
                initial = ThemeMode.LIGHT,
            )

            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemDark
            }

            DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                )
                onDispose {}
            }

            var loggedIn by remember {
                mutableStateOf(false)
            }

            var checkingSession by remember {
                mutableStateOf(true)
            }

            var isOffline by remember {
                mutableStateOf(false)
            }

            var isLoginLoading by remember {
                mutableStateOf(false)
            }

            var loginErrorMessage by remember {
                mutableStateOf<String?>(null)
            }

            var userName by remember {
                mutableStateOf("")
            }

            var userResponse by remember {
                mutableStateOf<UserResponse?>(null)
            }

            fun checkSession() {
                scope.launch {
                    val startTime = System.currentTimeMillis()
                    checkingSession = true
                    isOffline = false
                    try {
                        val user = repository.getHome()

                        if (user != null) {
                            loggedIn = true
                            userName = user.name
                            userResponse = user
                        }
                    } catch (e: Exception) {
                        isOffline = true
                        appNotifier.error(
                            screen = "MainActivity",
                            operation = "checkSession",
                            error = AppError.SESSION_CHECK_FAILED,
                            cause = e,
                        )
                    } finally {
                        val elapsedTime = System.currentTimeMillis() - startTime
                        if (elapsedTime < SPLASH_SCREEN_DURATION_MS) {
                            delay(SPLASH_SCREEN_DURATION_MS - elapsedTime)
                        }
                        checkingSession = false
                    }
                }
            }

            LaunchedEffect(Unit) {
                checkSession()
            }

            SalesForceTheme(themeMode = themeMode) {
                val baseDensity = LocalDensity.current
                val screenScaleFactor = rememberScreenScaleFactor()
                CompositionLocalProvider(
                    LocalDensity provides Density(
                        density = baseDensity.density * screenScaleFactor,
                        fontScale = baseDensity.fontScale,
                    ),
                ) {
                    CompositionLocalProvider(
                        LocalAppNotifier provides appNotifier,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            when {
                                checkingSession -> {
                                    SplashScreen()
                                }

                                isOffline && !loggedIn -> {
                                    OfflineScreen(onRetry = { checkSession() })
                                }

                                loggedIn -> {
                                    MainAppScreen(
                                        userName = userName,
                                        userResponse = userResponse,
                                        repository = repository,
                                        themeMode = themeMode,
                                        onThemeModeChange = { newMode ->
                                            scope.launch {
                                                themePreferences.setThemeMode(newMode)
                                            }
                                        },
                                        onRefresh = {
                                            try {
                                                val freshUser = repository.getHome(forceRefresh = true)
                                                if (freshUser != null) {
                                                    userResponse = freshUser
                                                    userName = freshUser.name
                                                }
                                            } catch (e: Exception) {
                                                appNotifier.error(
                                                    screen = "HomeScreen",
                                                    operation = "refresh",
                                                    error = AppError.BACKEND_ERROR,
                                                    cause = e,
                                                )
                                            }
                                        },
                                        onLogout = {
                                            scope.launch {
                                                try {
                                                    repository.logout()
                                                } catch (_: Exception) {
                                                    // Ignore logout failure
                                                } finally {
                                                    loggedIn = false
                                                    userName = ""
                                                    userResponse = null
                                                }
                                            }
                                        },
                                    )
                                }

                                else -> {
                                    LoadingOverlay(
                                        isLoading = isLoginLoading,
                                        message = "Signing in...",
                                    ) {
                                        LoginScreen(
                                            loading = isLoginLoading,
                                            errorMessage = loginErrorMessage,
                                            onLogin = { cnic, password ->
                                                scope.launch {
                                                    isLoginLoading = true
                                                    loginErrorMessage = null
                                                    try {
                                                        repository.login(cnic, password)
                                                        val user = repository.getHome()
                                                        if (user != null) {
                                                            loggedIn = true
                                                            userName = user.name
                                                            userResponse = user
                                                        }
                                                    } catch (e: Exception) {
                                                        loginErrorMessage = e.localizedMessage ?: "Login failed"
                                                        appNotifier.error(
                                                            screen = "LoginScreen",
                                                            operation = "login",
                                                            error = AppError.BACKEND_ERROR,
                                                            cause = e,
                                                        )
                                                    } finally {
                                                        isLoginLoading = false
                                                    }
                                                }
                                            },
                                        )
                                    }
                                }
                            }

                            AppToastHost(state = toastState)
                        }
                    }
                }
            }
        }
    }
}
