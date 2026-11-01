package com.example.salesforce.core.ui.notifications

import android.util.Log
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.salesforce.core.errors.AppError
import com.example.salesforce.core.ui.components.AppToastState
import com.example.salesforce.core.ui.components.ToastType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException

class AppNotifier(
    val toastState: AppToastState
) {
    fun error(
        screen: String,
        operation: String,
        error: AppError,
        cause: Throwable? = null,
        apiMessage: String? = null
    ) {
        val responseMessage = apiMessage
            ?.takeIf { it.isNotBlank() }
            ?: extractApiMessage(cause)

        val displayMessage = buildString {
            if(error.userMessage!="backend_error") {
                append(error.userMessage)

            }
            if(error.userMessage!="backend_error" && !responseMessage.isNullOrBlank())
            {
                append("\n")
            }

            if (!responseMessage.isNullOrBlank()) {
                append(responseMessage)
            }

        }

        Log.e(
            "SalesForce.Error",
            "screen=$screen operation=$operation " +
                    "code=${error.code} " +
                    "apiMessage=${responseMessage ?: "none"}",
            cause
        )

        toastState.show(
            message = displayMessage,
            type = ToastType.Error
        )
    }

    fun success(message: String) {
        toastState.show(message, ToastType.Success)
    }

    fun warning(message: String) {
        toastState.show(message, ToastType.Warning)
    }

    fun info(message: String) {
        toastState.show(message, ToastType.Info)
    }


    private fun extractApiMessage(cause: Throwable?): String? {
        if (cause == null) return null

        if (cause is com.example.salesforce.core.network.ApiException) {
            return cause.apiMessage
        }
        // Retrofit HTTP errors contain the response body here.
        if (cause is retrofit2.HttpException) {
            val body = try {
                cause.response()?.errorBody()?.string()
            } catch (e: Exception) {
                Log.e("SalesForce.Error", "Failed to read API error body", e)
                null
            }

            Log.e(
                "SalesForce.Error",
                "HTTP ${cause.code()} response body: $body"
            )

            return parseResponseMessage(body)
        }

        // Walk through wrapped exceptions.
        return extractApiMessage(cause.cause)
            ?: cause.message?.takeIf { it.isNotBlank() }
    }

    private fun parseResponseMessage(body: String?): String? {
        if (body.isNullOrBlank()) return null

        val trimmed = body.trim()

        return try {
            val json = org.json.JSONObject(trimmed)

            when {
                json.has("detail") -> json.opt("detail")
                    .toString()
                    .takeIf { it.isNotBlank() && it != "null" }

                json.has("message") -> json.optString("message")
                    .takeIf { it.isNotBlank() }

                json.has("error") -> json.opt("error")
                    .toString()
                    .takeIf { it.isNotBlank() && it != "null" }

                else -> trimmed
            }
        } catch (_: org.json.JSONException) {
            trimmed
        }
    }

}

val LocalAppNotifier = staticCompositionLocalOf<AppNotifier> {
    error("AppNotifier was not provided")
}