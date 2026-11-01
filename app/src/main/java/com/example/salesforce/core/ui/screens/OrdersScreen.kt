package com.example.salesforce.core.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.salesforce.core.errors.AppError
import com.example.salesforce.core.network.AuthRepository
import com.example.salesforce.core.network.Customer
import com.example.salesforce.core.network.CustomerCreateRequest
import com.example.salesforce.core.network.Industry
import com.example.salesforce.core.network.Order
import com.example.salesforce.core.network.OrderCreateRequest
import com.example.salesforce.core.network.OrderDetails
import com.example.salesforce.core.network.OrderItemPayload
import com.example.salesforce.core.network.Product
import com.example.salesforce.core.ui.components.MonthYearPickerHeader
import com.example.salesforce.core.ui.notifications.LocalAppNotifier
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

@Composable
fun OrdersScreen(
    repository: AuthRepository,
    onBack: () -> Unit,
) {
    val notifier = LocalAppNotifier.current
    val scope = rememberCoroutineScope()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedOrderForDetails by remember { mutableStateOf<Order?>(null) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val currentCal = Calendar.getInstance()
    var selectedYear by remember { mutableStateOf(currentCal.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(currentCal.get(Calendar.MONTH) + 1) }

    fun loadData(query: String? = null) {
        scope.launch {
            isLoading = true
            try {
                orders = repository.getOrders(customerName = query?.ifBlank { null })
                customers = repository.getCustomers()
            } catch (e: Exception) {
                notifier.error("OrdersScreen", "loadData", AppError.BACKEND_ERROR, e)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(searchQuery) {
        loadData(searchQuery)
    }

    if (selectedOrderForDetails != null) {
        OrderDetailsSubScreen(
            order = selectedOrderForDetails!!,
            repository = repository,
            customers = customers,
            onBack = { selectedOrderForDetails = null },
        )
    } else if (customerToEdit != null) {
        EditCustomerSubScreen(
            customer = customerToEdit!!,
            repository = repository,
            onBack = { customerToEdit = null },
            onUpdated = {
                customerToEdit = null
                loadData(searchQuery)
            },
        )
    } else {
        // Filter by updated_at (falling back to created_at) for month/year counts
        val filteredOrders = orders
            .filter { order ->
                val timeToCheck = order.updatedAt ?: order.createdAt
                try {
                    val parts = timeToCheck.split("T")[0].split("-")
                    if (parts.size >= 2) {
                        val y = parts[0].toIntOrNull()
                        val m = parts[1].toIntOrNull()
                        y == selectedYear && m == selectedMonth
                    } else {
                        true
                    }
                } catch (_: Exception) {
                    true
                }
            }
            .sortedByDescending { it.id }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp),
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = "Orders & Sales",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Filter by customer name...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            MonthYearPickerHeader(
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                onYearMonthChanged = { y, m ->
                    selectedYear = y
                    selectedMonth = m
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders found for $selectedMonth/$selectedYear.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(filteredOrders) { order ->
                        val customer = customers.find { it.id == order.customerId }
                        val custName = customer?.name ?: "Customer #${order.customerId}"
                        val business = customer?.businessName ?: "N/A"
                        val cell = customer?.cellNumber ?: "N/A"
                        val address = customer?.address ?: "N/A"
                        val formattedDate = formatHumanDateTime(order.updatedAt ?: order.createdAt)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOrderForDetails = order },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Order #${order.id}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    if (customer != null) {
                                        IconButton(onClick = { customerToEdit = customer }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = "Edit Customer Details",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Customer: $custName",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )

                                if (business.isNotBlank() && business != "N/A") {
                                    Text(
                                        text = "Business: $business",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                Text(
                                    text = "Phone: $cell • Address: $address",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Updated: $formattedDate",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "Order Details →",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditCustomerSubScreen(
    customer: Customer,
    repository: AuthRepository,
    onBack: () -> Unit,
    onUpdated: () -> Unit,
) {
    val notifier = LocalAppNotifier.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(customer.name) }
    var business by remember { mutableStateOf(customer.businessName ?: "") }
    var cell by remember { mutableStateOf(customer.cellNumber ?: "") }
    var address by remember { mutableStateOf(customer.address ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = "Edit Customer #${customer.id}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Customer Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = business,
                onValueChange = { business = it },
                label = { Text("Business Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = cell,
                onValueChange = { input -> cell = input.filter { it.isDigit() } },
                label = { Text("Cell Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isBlank() || cell.isBlank()) {
                        notifier.error("EditCustomer", "validation", AppError.BACKEND_ERROR, Exception("Name and cell number are required"))
                        return@Button
                    }
                    scope.launch {
                        isSaving = true
                        try {
                            repository.updateCustomer(
                                id = customer.id,
                                request = CustomerCreateRequest(
                                    name = name,
                                    businessName = business.ifBlank { null },
                                    cellNumber = cell.ifBlank { null },
                                    address = address.ifBlank { null },
                                ),
                            )
                            onUpdated()
                        } catch (e: Exception) {
                            notifier.error("EditCustomer", "updateCustomer", AppError.BACKEND_ERROR, e)
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Save Changes", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OrderDetailsSubScreen(
    order: Order,
    repository: AuthRepository,
    customers: List<Customer>,
    onBack: () -> Unit,
) {
    val notifier = LocalAppNotifier.current
    var orderDetails by remember { mutableStateOf<OrderDetails?>(null) }
    var selectedProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val customer = customers.find { it.id == order.customerId }

    LaunchedEffect(order) {
        try {
            val allProducts = repository.getProducts()
            val items = try {
                repository.getOrderItems(order.id)
            } catch (_: Exception) {
                emptyList()
            }

            val prodIds = if (items.isNotEmpty()) {
                items.map { it.productId }
            } else {
                val details = repository.getOrderDetails(order.id)
                orderDetails = details
                details?.items?.mapNotNull { it.productId ?: it.product?.id } ?: emptyList()
            }

            selectedProducts = allProducts.filter { it.id in prodIds }
        } catch (e: Exception) {
            notifier.error("OrderDetails", "fetchDetails", AppError.BACKEND_ERROR, e)
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = "Order #${order.id} Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val custName = customer?.name ?: orderDetails?.name ?: orderDetails?.customer?.name ?: "Customer #${order.customerId}"
            val business = customer?.businessName ?: orderDetails?.businessName ?: orderDetails?.customer?.businessName ?: "N/A"
            val cell = customer?.cellNumber ?: orderDetails?.cellNumber ?: orderDetails?.customer?.cellNumber ?: "N/A"
            val address = customer?.address ?: orderDetails?.address ?: orderDetails?.customer?.address ?: "N/A"
            val createdDate = formatHumanDateTime(orderDetails?.createdAt ?: order.createdAt)
            val updatedDate = if (!orderDetails?.updatedAt.isNullOrBlank()) formatHumanDateTime(orderDetails!!.updatedAt!!) else null

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Customer Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Customer: $custName", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    if (business.isNotBlank() && business != "N/A") {
                        Text(text = "Business: $business", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(text = "Phone: $cell", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Address: $address", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Created At: $createdDate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (updatedDate != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Updated At: $updatedDate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Products Selected (${selectedProducts.size}):",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No products attached to this order.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(selectedProducts) { prod ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(prod.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                if (!prod.description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(prod.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatHumanDateTime(isoString: String): String {
    return try {
        if (isoString.contains("T")) {
            val parts = isoString.split("T")
            val dateComps = parts[0].split("-")
            val timeComps = if (parts.size > 1) parts[1].take(8).split(":") else emptyList()

            if (dateComps.size == 3 && timeComps.size >= 2) {
                val year = dateComps[0].toIntOrNull() ?: 2026
                val month = (dateComps[1].toIntOrNull() ?: 1) - 1
                val day = dateComps[2].toIntOrNull() ?: 1
                val hour = timeComps[0].toIntOrNull() ?: 0
                val minute = timeComps[1].toIntOrNull() ?: 0

                val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    set(year, month, day, hour, minute)
                }

                val pkCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi")).apply {
                    timeInMillis = utcCal.timeInMillis
                }

                val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val mStr = monthNames.getOrElse(pkCal.get(Calendar.MONTH)) { "Jan" }
                val dNum = pkCal.get(Calendar.DAY_OF_MONTH)
                val yNum = pkCal.get(Calendar.YEAR)

                val pkHour24 = pkCal.get(Calendar.HOUR_OF_DAY)
                val pkMinute = pkCal.get(Calendar.MINUTE)
                val amPm = if (pkHour24 >= 12) "PM" else "AM"
                val pkHour12 = when {
                    pkHour24 == 0 -> 12
                    pkHour24 > 12 -> pkHour24 - 12
                    else -> pkHour24
                }
                val minStr = if (pkMinute < 10) "0$pkMinute" else "$pkMinute"

                "$mStr $dNum, $yNum • $pkHour12:$minStr $amPm"
            } else {
                isoString
            }
        } else {
            isoString
        }
    } catch (_: Exception) {
        isoString
    }
}
