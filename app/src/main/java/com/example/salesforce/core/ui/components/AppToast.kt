package com.example.salesforce.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay

enum class ToastType {
    Success, Error, Warning, Info
}

data class ToastData(
    val id: Long,
    val message: String,
    val type: ToastType,
    val durationMillis: Long,
)

class AppToastState {
    var currentToast by mutableStateOf<ToastData?>(null)
        private set
    var dialogHostCount by mutableStateOf(0)
        private set

    private var nextId = 0L

    fun registerDialogHost() {
        dialogHostCount++
    }

    fun unregisterDialogHost() {
        dialogHostCount = (dialogHostCount - 1).coerceAtLeast(0)
    }

    fun show(
        message: String,
        type: ToastType = ToastType.Info,
        durationMillis: Long = 4000L,
    ) {
        nextId++
        currentToast = ToastData(
            id = nextId,
            message = message,
            type = type,
            durationMillis = durationMillis,
        )
    }

    fun dismiss(id: Long? = null) {
        if (id == null || currentToast?.id == id) {
            currentToast = null
        }
    }
}

@Composable
fun rememberAppToastState(): AppToastState =
    androidx.compose.runtime.remember { AppToastState() }

@Composable
fun AppToastHost(
    state: AppToastState,
    inDialog: Boolean = false,
) {
    val toast = state.currentToast
    val isVisible = toast != null && (inDialog || state.dialogHostCount == 0)
    val toastToShow = if (isVisible) toast else null

    DisposableEffect(state, inDialog) {
        if (inDialog) state.registerDialogHost()
        onDispose {
            if (inDialog) state.unregisterDialogHost()
        }
    }

    LaunchedEffect(toastToShow?.id) {
        if (toastToShow != null) {
            delay(toastToShow.durationMillis)
            state.dismiss(toastToShow.id)
        }
    }

    if (toastToShow != null) {
        if (inDialog) {
            Popup(alignment = Alignment.TopCenter) {
                AnimatedVisibility(
                    visible = true,
                    modifier = Modifier.padding(top = 12.dp),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    ToastCard(
                        message = toastToShow.message,
                        type = toastToShow.type,
                        onDismiss = { state.dismiss(toastToShow.id) },
                    )
                }
            }
            return
        }

        Popup(
            alignment = Alignment.TopEnd,
        ) {
            AnimatedVisibility(
                visible = true,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 12.dp, end = 12.dp),
                enter = slideInHorizontally { it } + fadeIn(),
                exit = slideOutHorizontally { it } + fadeOut(),
            ) {
                ToastCard(
                    message = toastToShow.message,
                    type = toastToShow.type,
                    onDismiss = { state.dismiss(toastToShow.id) },
                )
            }
        }
    }
}

@Composable
private fun ToastCard(
    message: String,
    type: ToastType,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    val icon: ImageVector
    val containerColor: Color
    val contentColor: Color
    val iconColor: Color

    when (type) {
        ToastType.Success -> {
            icon = Icons.Rounded.CheckCircle
            containerColor = scheme.primaryContainer
            contentColor = scheme.onPrimaryContainer
            iconColor = scheme.primary
        }
        ToastType.Error -> {
            icon = Icons.Rounded.ErrorOutline
            containerColor = scheme.errorContainer
            contentColor = scheme.onErrorContainer
            iconColor = scheme.error
        }
        ToastType.Warning -> {
            icon = Icons.Rounded.WarningAmber
            containerColor = scheme.tertiaryContainer
            contentColor = scheme.onTertiaryContainer
            iconColor = scheme.tertiary
        }
        ToastType.Info -> {
            icon = Icons.Rounded.Info
            containerColor = scheme.secondaryContainer
            contentColor = scheme.onSecondaryContainer
            iconColor = scheme.secondary
        }
    }

    Surface(
        modifier = Modifier
            .width(320.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(
                start = 14.dp,
                top = 10.dp,
                end = 6.dp,
                bottom = 10.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = type.name,
                modifier = Modifier.size(25.dp),
                tint = iconColor,
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dismiss",
                )
            }
        }
    }
}
