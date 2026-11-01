package com.example.salesforce.core.network

import com.example.salesforce.core.auth.AuthTokenStore
import com.example.salesforce.core.cache.AppCache

class AuthRepository(
    private val tokenStore: AuthTokenStore,
    private val api: ApiService = ApiClient.service,
) {
    suspend fun login(
        cnic: String,
        password: String,
    ): LoginResponse {
        val response = api.login(
            LoginRequest(
                cnic = cnic,
                password = password,
            ),
        )

        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }

        val body = response.body() ?: throw Exception("Invalid response body")
        tokenStore.save(body.accessToken, body.expiresIn)
        return body
    }

    suspend fun getHome(forceRefresh: Boolean = false): UserResponse? {
        val token = tokenStore.getValidToken() ?: return null

        val cacheKey = "user_home"

        if (!forceRefresh) {
            val cached: UserResponse? = AppCache.get(cacheKey)
            if (cached != null) return cached
        }

        val response = api.getHome(authorization = "Bearer $token")
        if (!response.isSuccessful) {
            return null
        }

        val user = response.body()
        if (user != null) {
            AppCache.put(cacheKey, user)
        }
        return user
    }

    suspend fun getUserActivity(userId: Int): UserActivityResponse? {
        val token = tokenStore.getValidToken() ?: return null
        val response = api.getUserActivity(authorization = "Bearer $token", userId = userId)
        if (!response.isSuccessful) return null
        return response.body()
    }

    suspend fun markAttendance(): Boolean {
        val token = tokenStore.getValidToken() ?: return false
        val response = api.markAttendance(authorization = "Bearer $token")
        return response.isSuccessful
    }

    suspend fun getIndustries(): List<Industry> {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.getIndustries(authorization = "Bearer $token")
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: emptyList()
    }

    suspend fun getProductTypes(industryId: Int? = null): List<ProductType> {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.getProductTypes(authorization = "Bearer $token", industryId = industryId)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: emptyList()
    }

    suspend fun getCustomers(search: String? = null): List<Customer> {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.getCustomers(authorization = "Bearer $token", search = search)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: emptyList()
    }

    suspend fun createCustomer(request: CustomerCreateRequest): Customer {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.createCustomer(authorization = "Bearer $token", request = request)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: throw Exception("Failed to create customer")
    }

    suspend fun updateCustomer(id: Int, request: CustomerCreateRequest): Customer {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.updateCustomer(authorization = "Bearer $token", id = id, request = request)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: throw Exception("Failed to update customer")
    }

    suspend fun getProducts(industryId: Int? = null, search: String? = null): List<Product> {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.getProducts(authorization = "Bearer $token", industryId = industryId, search = search)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: emptyList()
    }

    suspend fun getOrders(customerName: String? = null): List<Order> {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.getOrders(authorization = "Bearer $token", customerName = customerName)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: emptyList()
    }

    suspend fun getOrderDetails(orderId: Int): OrderDetails? {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.getOrderDetails(authorization = "Bearer $token", orderId = orderId)
        if (!response.isSuccessful) return null
        return response.body()
    }

    suspend fun createOrder(request: OrderCreateRequest): Order {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.createOrder(authorization = "Bearer $token", request = request)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: throw Exception("Failed to create order")
    }

    suspend fun updateOrder(id: Int, request: OrderCreateRequest): Order {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.updateOrder(authorization = "Bearer $token", id = id, request = request)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: throw Exception("Failed to update order")
    }

    suspend fun getOrderItems(orderId: Int): List<OrderItem> {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.getOrderItems(authorization = "Bearer $token", orderId = orderId)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: emptyList()
    }

    suspend fun createOrderItem(request: OrderItemCreateRequest): OrderItem {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.createOrderItem(authorization = "Bearer $token", request = request)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: throw Exception("Failed to create order item")
    }

    suspend fun getMeetings(customerName: String? = null): List<Meeting> {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.getMeetings(authorization = "Bearer $token", customerName = customerName)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: emptyList()
    }

    suspend fun createMeeting(request: MeetingCreateRequest): Meeting {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.createMeeting(authorization = "Bearer $token", request = request)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: throw Exception("Failed to create meeting")
    }

    suspend fun updateMeeting(id: Int, request: MeetingCreateRequest): Meeting {
        val token = tokenStore.getValidToken() ?: throw Exception("Unauthorized")
        val response = api.updateMeeting(authorization = "Bearer $token", id = id, request = request)
        if (!response.isSuccessful) {
            throw ApiException(response.code(), extractApiMessage(response.errorBody()?.string()))
        }
        return response.body() ?: throw Exception("Failed to update meeting")
    }

    suspend fun logout() {
        tokenStore.clear()
        AppCache.clear()
    }

    private fun extractApiMessage(body: String?): String? {
        if (body.isNullOrBlank()) return null

        val trimmed = body.trim()

        return try {
            val json = org.json.JSONObject(trimmed)

            when {
                json.has("detail") ->
                    json.opt("detail").toString()

                json.has("message") ->
                    json.optString("message")

                json.has("error") ->
                    json.opt("error").toString()

                else -> trimmed
            }.takeIf { it.isNotBlank() && it != "null" }
        } catch (_: org.json.JSONException) {
            trimmed
        }
    }
}
