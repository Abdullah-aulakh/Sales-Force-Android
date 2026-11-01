package com.example.salesforce.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
    ): Response<LoginResponse>

    @GET("api/v1/users/home")
    suspend fun getHome(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
    ): Response<UserResponse>

    @GET("api/v1/users/{user_id}/activity/current-month")
    suspend fun getUserActivity(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Path("user_id") userId: Int,
    ): Response<UserActivityResponse>

    @POST("api/v1/attendance/me")
    suspend fun markAttendance(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
    ): Response<ResponseBody>

    // Industry & Product Types
    @GET("api/v1/industry")
    suspend fun getIndustries(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 50,
    ): Response<List<Industry>>

    @GET("api/v1/product-types")
    suspend fun getProductTypes(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Query("industry_id") industryId: Int? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 50,
    ): Response<List<ProductType>>

    // Customers
    @GET("api/v1/customers")
    suspend fun getCustomers(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Query("search") search: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 50,
    ): Response<List<Customer>>

    @POST("api/v1/customers")
    suspend fun createCustomer(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Body request: CustomerCreateRequest,
    ): Response<Customer>

    @PATCH("api/v1/customers/{id}")
    suspend fun updateCustomer(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Path("id") id: Int,
        @Body request: CustomerCreateRequest,
    ): Response<Customer>

    @GET("api/v1/customers/{id}")
    suspend fun getCustomer(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Path("id") id: Int,
    ): Response<Customer>

    // Products
    @GET("api/v1/products")
    suspend fun getProducts(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Query("industry_id") industryId: Int? = null,
        @Query("search") search: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 50,
    ): Response<List<Product>>

    // Orders
    @GET("api/v1/orders")
    suspend fun getOrders(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Query("customer_name") customerName: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 50,
    ): Response<List<Order>>

    @GET("api/v1/orders/{order_id}")
    suspend fun getOrderDetails(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Path("order_id") orderId: Int,
    ): Response<OrderDetails>

    @POST("api/v1/orders")
    suspend fun createOrder(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Body request: OrderCreateRequest,
    ): Response<Order>

    @PATCH("api/v1/orders/{id}")
    suspend fun updateOrder(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Path("id") id: Int,
        @Body request: OrderCreateRequest,
    ): Response<Order>

    // Order Items
    @GET("api/v1/order-items")
    suspend fun getOrderItems(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Query("order_id") orderId: Int,
    ): Response<List<OrderItem>>

    @POST("api/v1/order-items")
    suspend fun createOrderItem(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Body request: OrderItemCreateRequest,
    ): Response<OrderItem>

    // Meetings
    @GET("api/v1/meetings")
    suspend fun getMeetings(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Query("customer_name") customerName: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 50,
    ): Response<List<Meeting>>

    @POST("api/v1/meetings")
    suspend fun createMeeting(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Body request: MeetingCreateRequest,
    ): Response<Meeting>

    @PATCH("api/v1/meetings/{id}")
    suspend fun updateMeeting(
        @Header("Authorization") authorization: String,
        @Header("X-API-Key") apiKey: String = DEFAULT_API_KEY,
        @Path("id") id: Int,
        @Body request: MeetingCreateRequest,
    ): Response<Meeting>

    companion object {
        const val DEFAULT_API_KEY = "5421"
    }
}

@Serializable
data class LoginRequest(
    val cnic: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("token_type")
    val tokenType: String = "bearer",

    @SerialName("expires_in")
    val expiresIn: Long,

    val user: UserSummary,
)

@Serializable
data class UserSummary(
    val id: Int,
    val username: String,
    val name: String,
    val role: String,
    val status: String,

    @SerialName("offices_id")
    val officeId: Int,
)

@Serializable
data class UserResponse(
    val id: Int,
    val username: String,
    val name: String,

    @SerialName("father_name")
    val fatherName: String?,

    val role: String,
    val status: String,

    @SerialName("date_of_joining")
    val dateOfJoining: String,

    @SerialName("date_of_separation")
    val dateOfSeparation: String?,

    @SerialName("offices_id")
    val officeId: Int,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    @SerialName("manager_id")
    val managerId: Int? = null,

    @SerialName("manager_name")
    val managerName: String? = null,

    @SerialName("monthly_targets")
    val monthlyTargets: Int? = null,

    @SerialName("daily_targets")
    val dailyTargets: Int? = null,

    @SerialName("today_orders")
    val todayOrders: Int? = null,

    @SerialName("today_meetings")
    val todayMeetings: Int? = null,

    @SerialName("monthly_orders")
    val monthlyOrders: Int? = null,

    @SerialName("monthly_meetings")
    val monthlyMeetings: Int? = null,

    @SerialName("all_time_orders")
    val allTimeOrders: Int? = null,

    @SerialName("all_time_meetings")
    val allTimeMeetings: Int? = null,
)

@Serializable
data class DailyActivityItem(
    val day: Int,
    @SerialName("order_count") val orderCount: Int,
    @SerialName("order_amount") val orderAmount: String? = null,
    @SerialName("meeting_count") val meetingCount: Int,
)

@Serializable
data class AttendanceMonthData(
    val year: Int,
    val month: Int,
    val attendance: List<Boolean?>,
)

@Serializable
data class UserActivityResponse(
    @SerialName("user_id") val userId: Int,
    val year: Int,
    val month: Int,
    @SerialName("attendance_months") val attendanceMonths: List<AttendanceMonthData>? = null,
    @SerialName("oldest_attendance_date") val oldestAttendanceDate: String? = null,
)

@Serializable
data class Industry(
    val id: Int,
    val name: String,
)

@Serializable
data class ProductType(
    val id: Int,
    val name: String,
    @SerialName("industry_id") val industryId: Int,
)

@Serializable
data class Customer(
    val id: Int,
    val name: String,
    @SerialName("business_name") val businessName: String? = null,
    val address: String? = null,
    @SerialName("cell_number") val cellNumber: String? = null,
)

@Serializable
data class CustomerCreateRequest(
    val name: String,
    @SerialName("business_name") val businessName: String? = null,
    val address: String? = null,
    @SerialName("cell_number") val cellNumber: String? = null,
)

@Serializable
data class Product(
    val id: Int,
    val name: String,
    val description: String? = null,
    @SerialName("product_types_id") val productTypesId: Int,
)

@Serializable
data class Order(
    val id: Int,
    @SerialName("agent_id") val agentId: Int,
    @SerialName("customer_id") val customerId: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class OrderDetails(
    val id: Int,
    @SerialName("agent_id") val agentId: Int? = null,
    @SerialName("customer_id") val customerId: Int? = null,
    val name: String? = null,
    @SerialName("business_name") val businessName: String? = null,
    val address: String? = null,
    @SerialName("cell_number") val cellNumber: String? = null,
    val customer: OrderDetailCustomer? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val items: List<OrderDetailLine> = emptyList(),
)

@Serializable
data class OrderDetailLine(
    val id: Int? = null,
    @SerialName("product_id") val productId: Int? = null,
    val product: OrderDetailProduct? = null,
)

@Serializable
data class OrderDetailCustomer(
    val name: String? = null,
    @SerialName("business_name") val businessName: String? = null,
    val address: String? = null,
    @SerialName("cell_number") val cellNumber: String? = null,
)

@Serializable
data class OrderDetailProduct(
    val id: Int,
    val name: String,
    val description: String? = null,
    @SerialName("product_types_id") val productTypesId: Int? = null,
    @SerialName("product_type") val productType: OrderDetailProductType? = null,
)

@Serializable
data class OrderDetailProductType(
    val id: Int,
    val name: String,
    val industry: OrderDetailIndustry? = null,
)

@Serializable
data class OrderDetailIndustry(
    val id: Int,
    val name: String,
)

@Serializable
data class OrderItemPayload(
    @SerialName("product_id") val productId: Int,
)

@Serializable
data class OrderCreateRequest(
    @SerialName("agent_id") val agentId: Int? = null,
    val name: String,
    @SerialName("business_name") val businessName: String? = null,
    val address: String? = null,
    @SerialName("cell_number") val cellNumber: String? = null,
    val items: List<OrderItemPayload>,
)

@Serializable
data class OrderItem(
    val id: Int,
    @SerialName("order_id") val orderId: Int,
    @SerialName("product_id") val productId: Int,
)

@Serializable
data class OrderItemCreateRequest(
    @SerialName("order_id") val orderId: Int,
    @SerialName("product_id") val productId: Int,
)

@Serializable
data class Meeting(
    val id: Int,
    @SerialName("customer_id") val customerId: Int,
    @SerialName("executive_id") val executiveId: Int,
    val status: String,
    @SerialName("follow_up_time") val followUpTime: String? = null,
    val feedback: String? = null,
    @SerialName("order_id") val orderId: Int? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class MeetingCreateRequest(
    @SerialName("customer_id") val customerId: Int? = null,
    val name: String? = null,
    @SerialName("business_name") val businessName: String? = null,
    val address: String? = null,
    @SerialName("cell_number") val cellNumber: String? = null,
    @SerialName("executive_id") val executiveId: Int,
    val status: String, // "rejected", "follow_up", "successful"
    @SerialName("follow_up_time") val followUpTime: String? = null,
    val feedback: String? = null,
    val items: List<OrderItemPayload>? = null,
)
