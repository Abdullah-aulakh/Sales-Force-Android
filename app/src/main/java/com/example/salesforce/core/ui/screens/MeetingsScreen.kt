package com.example.salesforce.core.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
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
import androidx.compose.material3.FilterChip
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
import com.example.salesforce.core.network.Meeting
import com.example.salesforce.core.network.MeetingCreateRequest
import com.example.salesforce.core.network.OrderItemPayload
import com.example.salesforce.core.network.Product
import com.example.salesforce.core.network.UserResponse
import com.example.salesforce.core.ui.PakistanTime
import com.example.salesforce.core.ui.components.MonthYearPickerHeader
import com.example.salesforce.core.ui.components.NativeDateTimePickerDialog
import com.example.salesforce.core.ui.notifications.LocalAppNotifier
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun MeetingsScreen(
    repository: AuthRepository,
    currentUser: UserResponse?,
    onBack: () -> Unit,
) {
    val notifier = LocalAppNotifier.current
    val scope = rememberCoroutineScope()

    var meetings by remember { mutableStateOf<List<Meeting>>(emptyList()) }
    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateSubScreen by remember { mutableStateOf(false) }
    var meetingToEdit by remember { mutableStateOf<Meeting?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val currentCal = Calendar.getInstance(PakistanTime.timeZone)
    var selectedYear by remember { mutableStateOf(currentCal.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(currentCal.get(Calendar.MONTH) + 1) }

    fun loadData(query: String? = null) {
        scope.launch {
            isLoading = true
            try {
                meetings = repository.getMeetings(customerName = query?.ifBlank { null })
                customers = repository.getCustomers()
            } catch (e: Exception) {
                notifier.error("MeetingsScreen", "loadData", AppError.BACKEND_ERROR, e)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(searchQuery) {
        loadData(searchQuery)
    }

    if (showCreateSubScreen) {
        CreateMeetingSubScreen(
            repository = repository,
            currentUser = currentUser,
            onBack = { showCreateSubScreen = false },
            onMeetingCreated = {
                showCreateSubScreen = false
                loadData(searchQuery)
            },
        )
    } else if (meetingToEdit != null) {
        val associatedCustomer = customers.find { it.id == meetingToEdit!!.customerId }
        EditMeetingSubScreen(
            meeting = meetingToEdit!!,
            customer = associatedCustomer,
            repository = repository,
            currentUser = currentUser,
            onBack = { meetingToEdit = null },
            onUpdated = {
                meetingToEdit = null
                loadData(searchQuery)
            },
        )
    } else {
        // Only show successful or rejected meetings in Meetings Screen
        val filteredMeetings = meetings
            .filter { meeting ->
                val isMeetingStatus = meeting.status == "successful" || meeting.status == "rejected"
                val timeToCheck = meeting.updatedAt ?: meeting.createdAt
                isMeetingStatus && try {
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
                    text = "Meetings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                ) {
                    IconButton(onClick = { showCreateSubScreen = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Create Meeting",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
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
            } else if (filteredMeetings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No meetings found for $selectedMonth/$selectedYear.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(filteredMeetings) { meeting ->
                        val customer = customers.find { it.id == meeting.customerId }
                        val customerName = customer?.name ?: "Customer #${meeting.customerId}"
                        val business = customer?.businessName ?: "N/A"
                        val formattedDate = formatHumanDateTime(meeting.updatedAt ?: meeting.createdAt)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                    ) {
                                        Text(
                                            text = customerName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = "Business: $business",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (meeting.status) {
                                            "successful" -> MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.errorContainer
                                        },
                                    ) {
                                        Text(
                                            text = meeting.status.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = when (meeting.status) {
                                                "successful" -> MaterialTheme.colorScheme.onPrimaryContainer
                                                else -> MaterialTheme.colorScheme.onErrorContainer
                                            },
                                        )
                                    }
                                }

                                if (!meeting.feedback.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Feedback: ${meeting.feedback}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Updated: $formattedDate",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (customer != null) {
                                        IconButton(onClick = { meetingToEdit = meeting }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = "Edit Meeting",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp),
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
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateMeetingSubScreen(
    repository: AuthRepository,
    currentUser: UserResponse?,
    onBack: () -> Unit,
    onMeetingCreated: () -> Unit,
) {
    val notifier = LocalAppNotifier.current
    val scope = rememberCoroutineScope()

    var custName by remember { mutableStateOf("") }
    var custBusiness by remember { mutableStateOf("") }
    var custCell by remember { mutableStateOf("") }
    var custAddress by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("successful") }
    var followUpTimeIso by remember { mutableStateOf("") }
    var followUpDisplayStr by remember { mutableStateOf("Select Date & Time...") }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }

    var industries by remember { mutableStateOf<List<Industry>>(emptyList()) }
    var selectedIndustry by remember { mutableStateOf<Industry?>(null) }
    var productSearchQuery by remember { mutableStateOf("") }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var selectedProducts by remember { mutableStateOf<Set<Product>>(emptySet()) }

    LaunchedEffect(Unit) {
        try {
            industries = repository.getIndustries()
        } catch (e: Exception) {
            notifier.error("CreateMeeting", "getIndustries", AppError.BACKEND_ERROR, e)
        }
    }

    LaunchedEffect(selectedIndustry, productSearchQuery) {
        if (selectedIndustry != null) {
            try {
                products = repository.getProducts(industryId = selectedIndustry!!.id, search = productSearchQuery.ifBlank { null })
            } catch (e: Exception) {
                notifier.error("CreateMeeting", "getProducts", AppError.BACKEND_ERROR, e)
            }
        } else {
            products = emptyList()
            selectedProducts = emptySet()
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
                text = "Create Meeting & Order",
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
            Text("Customer Details:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = custName,
                onValueChange = { custName = it },
                label = { Text("Customer Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = custBusiness,
                onValueChange = { custBusiness = it },
                label = { Text("Business Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = custCell,
                onValueChange = { input -> custCell = input.filter { it.isDigit() } },
                label = { Text("Cell Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = custAddress,
                onValueChange = { custAddress = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text("Meeting Status:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf("successful", "follow_up", "rejected").forEach { s ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = { Text(s.replace("_", " ").uppercase()) },
                    )
                }
            }

            if (status == "follow_up") {
                OutlinedButton(
                    onClick = { showDateTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Follow-up: $followUpDisplayStr")
                }
            }

            if (status == "successful") {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Take Order (Attach Products):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                var industryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = industryExpanded,
                    onExpandedChange = { industryExpanded = !industryExpanded },
                ) {
                    OutlinedTextField(
                        value = selectedIndustry?.name ?: "Select Industry...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Industry *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = industryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = industryExpanded,
                        onDismissRequest = { industryExpanded = false },
                    ) {
                        industries.forEach { ind ->
                            DropdownMenuItem(
                                text = { Text(ind.name) },
                                onClick = {
                                    selectedIndustry = ind
                                    selectedProducts = emptySet()
                                    industryExpanded = false
                                },
                            )
                        }
                    }
                }

                if (selectedIndustry != null) {
                    var productDropdownExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = productDropdownExpanded,
                        onExpandedChange = { productDropdownExpanded = !productDropdownExpanded },
                    ) {
                        OutlinedTextField(
                            value = if (selectedProducts.isEmpty()) "Select Products..." else "${selectedProducts.size} Product(s) Selected",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Products (${selectedIndustry?.name}) *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        )

                        ExposedDropdownMenu(
                            expanded = productDropdownExpanded,
                            onDismissRequest = { productDropdownExpanded = false },
                        ) {
                            if (products.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No products available for this industry") },
                                    onClick = { productDropdownExpanded = false },
                                )
                            } else {
                                products.forEach { prod ->
                                    val isSelected = selectedProducts.contains(prod)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = null,
                                                )
                                                Column {
                                                    Text(prod.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                    if (!prod.description.isNullOrBlank()) {
                                                        Text(prod.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedProducts = if (isSelected) {
                                                selectedProducts - prod
                                            } else {
                                                selectedProducts + prod
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = feedback,
                onValueChange = { feedback = it },
                label = { Text("Feedback / Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (custName.isBlank() || custCell.isBlank()) {
                        notifier.error("CreateMeeting", "validation", AppError.BACKEND_ERROR, Exception("Customer name and cell number are required"))
                        return@Button
                    }
                    if (status == "follow_up" && followUpTimeIso.isBlank()) {
                        notifier.error("CreateMeeting", "validation", AppError.BACKEND_ERROR, Exception("Follow-up date and time are required"))
                        return@Button
                    }
                    if (status == "successful" && selectedProducts.isEmpty()) {
                        notifier.error("CreateMeeting", "validation", AppError.BACKEND_ERROR, Exception("Please select at least one product for a successful order meeting"))
                        return@Button
                    }
                    if (currentUser == null) {
                        notifier.error("CreateMeeting", "validation", AppError.BACKEND_ERROR, Exception("User not authenticated"))
                        return@Button
                    }

                    scope.launch {
                        isCreating = true
                        try {
                            val itemsPayload = if (status == "successful") {
                                selectedProducts.map { OrderItemPayload(productId = it.id) }
                            } else null

                            repository.createMeeting(
                                MeetingCreateRequest(
                                    name = custName,
                                    businessName = custBusiness.ifBlank { null },
                                    address = custAddress.ifBlank { null },
                                    cellNumber = custCell.ifBlank { null },
                                    executiveId = currentUser.id,
                                    status = status,
                                    followUpTime = if (status == "follow_up") followUpTimeIso else null,
                                    feedback = feedback.ifBlank { null },
                                    items = itemsPayload,
                                ),
                            )
                            onMeetingCreated()
                        } catch (e: Exception) {
                            notifier.error("CreateMeeting", "createMeeting", AppError.BACKEND_ERROR, e)
                        } finally {
                            isCreating = false
                        }
                    }
                },
                enabled = !isCreating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Save Meeting & Order", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDateTimePicker) {
        NativeDateTimePickerDialog(
            onDateTimeSelected = { isoStr ->
                followUpTimeIso = isoStr
                followUpDisplayStr = formatHumanDateTime(isoStr)
                showDateTimePicker = false
            },
            onDismiss = { showDateTimePicker = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditMeetingSubScreen(
    meeting: Meeting,
    customer: Customer?,
    repository: AuthRepository,
    currentUser: UserResponse?,
    onBack: () -> Unit,
    onUpdated: () -> Unit,
) {
    val notifier = LocalAppNotifier.current
    val scope = rememberCoroutineScope()

    var custName by remember { mutableStateOf(customer?.name ?: "") }
    var custBusiness by remember { mutableStateOf(customer?.businessName ?: "") }
    var custCell by remember { mutableStateOf(customer?.cellNumber ?: "") }
    var custAddress by remember { mutableStateOf(customer?.address ?: "") }

    val availableStatuses = remember(meeting.status) {
        listOf("successful", "follow_up", "rejected").filter { it != meeting.status }
    }
    var status by remember { mutableStateOf(availableStatuses.firstOrNull() ?: "follow_up") }

    var followUpTimeIso by remember { mutableStateOf(meeting.followUpTime ?: "") }
    var followUpDisplayStr by remember { mutableStateOf(if (!meeting.followUpTime.isNullOrBlank()) formatHumanDateTime(meeting.followUpTime) else "Select Date & Time...") }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf(meeting.feedback ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    var industries by remember { mutableStateOf<List<Industry>>(emptyList()) }
    var selectedIndustry by remember { mutableStateOf<Industry?>(null) }
    var productSearchQuery by remember { mutableStateOf("") }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var selectedProducts by remember { mutableStateOf<Set<Product>>(emptySet()) }

    LaunchedEffect(Unit) {
        try {
            industries = repository.getIndustries()
        } catch (e: Exception) {
            notifier.error("EditMeeting", "getIndustries", AppError.BACKEND_ERROR, e)
        }
    }

    LaunchedEffect(selectedIndustry, productSearchQuery) {
        if (selectedIndustry != null) {
            try {
                products = repository.getProducts(industryId = selectedIndustry!!.id, search = productSearchQuery.ifBlank { null })
            } catch (e: Exception) {
                notifier.error("EditMeeting", "getProducts", AppError.BACKEND_ERROR, e)
            }
        } else {
            products = emptyList()
            selectedProducts = emptySet()
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
                text = "Edit Meeting #${meeting.id}",
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
            Text("Customer Details:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(
                value = custName,
                onValueChange = { custName = it },
                label = { Text("Customer Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = custBusiness,
                onValueChange = { custBusiness = it },
                label = { Text("Business Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = custCell,
                onValueChange = { input -> custCell = input.filter { it.isDigit() } },
                label = { Text("Cell Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = custAddress,
                onValueChange = { custAddress = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text("Meeting Status (Current: ${meeting.status.uppercase()}):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                availableStatuses.forEach { s ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = { Text(s.replace("_", " ").uppercase()) },
                    )
                }
            }

            if (status == "follow_up") {
                OutlinedButton(
                    onClick = { showDateTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Follow-up: $followUpDisplayStr")
                }
            }

            if (status == "successful") {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Attach / Replace Order Products:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                var industryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = industryExpanded,
                    onExpandedChange = { industryExpanded = !industryExpanded },
                ) {
                    OutlinedTextField(
                        value = selectedIndustry?.name ?: "Select Industry...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Industry *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = industryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = industryExpanded,
                        onDismissRequest = { industryExpanded = false },
                    ) {
                        industries.forEach { ind ->
                            DropdownMenuItem(
                                text = { Text(ind.name) },
                                onClick = {
                                    selectedIndustry = ind
                                    selectedProducts = emptySet()
                                    industryExpanded = false
                                },
                            )
                        }
                    }
                }

                if (selectedIndustry != null) {
                    var productDropdownExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = productDropdownExpanded,
                        onExpandedChange = { productDropdownExpanded = !productDropdownExpanded },
                    ) {
                        OutlinedTextField(
                            value = if (selectedProducts.isEmpty()) "Select Products..." else "${selectedProducts.size} Product(s) Selected",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Products (${selectedIndustry?.name}) *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        )

                        ExposedDropdownMenu(
                            expanded = productDropdownExpanded,
                            onDismissRequest = { productDropdownExpanded = false },
                        ) {
                            if (products.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No products available for this industry") },
                                    onClick = { productDropdownExpanded = false },
                                )
                            } else {
                                products.forEach { prod ->
                                    val isSelected = selectedProducts.contains(prod)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = null,
                                                )
                                                Column {
                                                    Text(prod.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                    if (!prod.description.isNullOrBlank()) {
                                                        Text(prod.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedProducts = if (isSelected) {
                                                selectedProducts - prod
                                            } else {
                                                selectedProducts + prod
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                label = { Text("Feedback / Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (custName.isBlank() || custCell.isBlank()) {
                        notifier.error("EditMeeting", "validation", AppError.BACKEND_ERROR, Exception("Customer name and cell number are required"))
                        return@Button
                    }
                    if (status == "follow_up" && followUpTimeIso.isBlank()) {
                        notifier.error("EditMeeting", "validation", AppError.BACKEND_ERROR, Exception("Follow-up date and time are required"))
                        return@Button
                    }

                    val execId = currentUser?.id ?: meeting.executiveId

                    scope.launch {
                        isSaving = true
                        try {
                            if (customer != null) {
                                repository.updateCustomer(
                                    id = customer.id,
                                    request = CustomerCreateRequest(
                                        name = custName,
                                        businessName = custBusiness.ifBlank { null },
                                        cellNumber = custCell.ifBlank { null },
                                        address = custAddress.ifBlank { null },
                                    ),
                                )
                            }

                            val itemsPayload = if (status == "successful" && selectedProducts.isNotEmpty()) {
                                selectedProducts.map { OrderItemPayload(productId = it.id) }
                            } else null

                            repository.updateMeeting(
                                id = meeting.id,
                                request = MeetingCreateRequest(
                                    customerId = meeting.customerId,
                                    executiveId = execId,
                                    status = status,
                                    followUpTime = if (status == "follow_up") followUpTimeIso else null,
                                    feedback = feedbackText.ifBlank { null },
                                    items = itemsPayload,
                                ),
                            )
                            onUpdated()
                        } catch (e: Exception) {
                            notifier.error("EditMeeting", "updateMeeting", AppError.BACKEND_ERROR, e)
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

    if (showDateTimePicker) {
        NativeDateTimePickerDialog(
            onDateTimeSelected = { isoStr ->
                followUpTimeIso = isoStr
                followUpDisplayStr = formatHumanDateTime(isoStr)
                showDateTimePicker = false
            },
            onDismiss = { showDateTimePicker = false },
        )
    }
}

private fun formatHumanDateTime(isoString: String): String {
    return PakistanTime.formatDateTime(isoString)
}
