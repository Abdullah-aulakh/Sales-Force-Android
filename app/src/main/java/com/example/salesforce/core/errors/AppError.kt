package com.example.salesforce.core.errors

enum class AppError(
    val code: String,
    val userMessage: String
) {
    INVALID_CNIC(
        "AUTH-001",
        "CNIC must contain exactly 13 digits."
    ),
    EMPTY_PASSWORD(
        "AUTH-002",
        "Please enter your password."
    ),
    INVALID_CREDENTIALS(
        "AUTH-003",
        "Invalid CNIC or password."
    ),
    NETWORK_UNAVAILABLE(
        "NET-001",
        "Unable to connect. Please try again."
    ),
    SESSION_CHECK_FAILED(
        "AUTH-004",
        "Unable to verify your session."
    ),
    LOGOUT_FAILED(
        "AUTH-005",
        "Logout failed. Please try again."
    ),
    UNEXPECTED_ERROR(
        "APP-001",
        "Something went wrong. Please try again."
    ),
    BACKEND_ERROR(
        "APP-002",
        "backend_error"
    )
}
