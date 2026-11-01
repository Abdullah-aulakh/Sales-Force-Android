
package com.example.salesforce.core.network

class ApiException(
    val statusCode: Int,
    val apiMessage: String?
) : Exception(
    apiMessage ?: "HTTP $statusCode"
)
