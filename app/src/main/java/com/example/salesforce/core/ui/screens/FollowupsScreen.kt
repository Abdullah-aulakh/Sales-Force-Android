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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.salesforce.core.errors.AppError
import com.example.salesforce.core.network.AuthRepository
import com.example.salesforce.core.network.Customer
import com.example.salesforce.core.network.Meeting
import com.example.salesforce.core.network.MeetingCreateRequest
import com.example.salesforce.core.network.UserResponse
import com.example.salesforce.core.ui.components.NativeDateTimePickerDialog
import com.example.salesforce.core.ui.notifications.LocalAppNotifier
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

@Composable
fun FollowupsScreen(
    repository: AuthRepository,
    currentUser: UserResponse?,
    onBack: () -> Unit,
) {
    val notifier = LocalAppNotifier.current
    val scope = rememberCoroutineScope()

    var meetings by remember { mutableStateOf<List<Meeting>>(emptyList()) }
    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Upcoming, 1: Expired
    var meetingToEdit by remember { mutableStateOf<Meeting?>(null) }

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                val allMeetings = repository.getMeetings()
                meetings = allMeetings
                    .filter { it.status == "follow_up" || !it.followUpTime.isNullOrBlank() }
                    .sortedByDescending { it.id }
                customers = repository.getCustomers()
            } catch (e: Exception) {
                notifier.error("FollowupsScreen", "loadData", AppError.BACKEND_ERROR, e)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    val upcomingFollowups = remember(meetings) {
        meetings.filter { !isExpired(it.followUpTime) }
    }
    val expiredFollowups = remember(meetings) {
        meetings.filter { isExpired(it.followUpTime) }
    }

    val displayList = if (selectedTab == 0) upcomingFollowups else expiredFollowups

    if (meetingToEdit != null) {
        val associatedCustomer = customers.find { it.id == meetingToEdit!!.customerId }
        EditFollowupSubScreen(
            meeting = meetingToEdit!!,
            customer = associatedCustomer,
            repository = repository,
            currentUser = currentUser,
            onBack = { meetingToEdit = null },
            onUpdated = {
                meetingToEdit = null
                loadData()
            },
        )
    } else {
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
                    text = "Follow-ups",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Upcoming (${upcomingFollowups.size})", fontWeight = FontWeight.SemiBold) },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Expired (${expiredFollowups.size})", fontWeight = FontWeight.SemiBold) },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (displayList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (selectedTab == 0) "No upcoming follow-ups scheduled." else "No expired follow-ups.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(displayList) { meeting ->
                        val customer = customers.find { it.id == meeting.customerId }
                        val customerName = customer?.name ?: "Customer #${meeting.customerId}"
                        val businessName = customer?.businessName ?: "N/A"
                        val cellNumber = customer?.cellNumber ?: "N/A"
                        val timeStr = meeting.followUpTime ?: meeting.createdAt
                        val formattedTime = formatHumanDateTime(timeStr)

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
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = customerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    IconButton(onClick = { meetingToEdit = meeting }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = "Edit Follow-up",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Business: $businessName",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "Phone: $cellNumber",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                if (!meeting.feedback.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Notes: ${meeting.feedback}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedTab == 1) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (selectedTab == 1) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                                        )
                                        Text(
                                            text = "Due: $formattedTime",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selectedTab == 1) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                                            fontWeight = FontWeight.SemiBold,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditFollowupSubScreen(
    meeting: Meeting,
    customer: Customer?,
    repository: AuthRepository,
    currentUser: UserResponse?,
    onBack: () -> Unit,
    onUpdated: () -> Unit,
) {
    val notifier = LocalAppNotifier.current
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf(meeting.status) }
    var followUpTimeIso by remember { mutableStateOf(meeting.followUpTime ?: "") }
    var followUpDisplayStr by remember { mutableStateOf(if (!meeting.followUpTime.isNullOrBlank()) formatHumanDateTime(meeting.followUpTime) else "Select Date & Time...") }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf(meeting.feedback ?: "") }
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
                text = "Edit Follow-up #${meeting.id}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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

            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                label = { Text("Feedback / Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (status == "follow_up" && followUpTimeIso.isBlank()) {
                    notifier.error("EditFollowup", "validation", AppError.BACKEND_ERROR, Exception("Follow-up date and time are required"))
                    return@Button
                }

                val execId = currentUser?.id ?: meeting.executiveId

                scope.launch {
                    isSaving = true
                    try {
                        repository.updateMeeting(
                            id = meeting.id,
                            request = MeetingCreateRequest(
                                customerId = meeting.customerId,
                                executiveId = execId,
                                status = status,
                                followUpTime = if (status == "follow_up") followUpTimeIso else null,
                                feedback = feedbackText.ifBlank { null },
                            ),
                        )
                        onUpdated()
                    } catch (e: Exception) {
                        notifier.error("EditFollowup", "updateMeeting", AppError.BACKEND_ERROR, e)
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

private fun isExpired(followUpTimeStr: String?): Boolean {
    if (followUpTimeStr.isNullOrBlank()) return false
    return try {
        val parts = followUpTimeStr.split("T")
        val dateComps = parts[0].split("-")
        val timeComps = if (parts.size > 1) parts[1].take(8).split(":") else emptyList()
        if (dateComps.size == 3) {
            val year = dateComps[0].toInt()
            val month = dateComps[1].toInt() - 1
            val day = dateComps[2].toInt()
            val hour = if (timeComps.size >= 2) timeComps[0].toInt() else 0
            val minute = if (timeComps.size >= 2) timeComps[1].toInt() else 0

            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(year, month, day, hour, minute)
            }
            cal.timeInMillis < System.currentTimeMillis()
        } else false
    } catch (_: Exception) {
        false
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
