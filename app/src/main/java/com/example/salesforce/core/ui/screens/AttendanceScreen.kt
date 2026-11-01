package com.example.salesforce.core.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.salesforce.core.errors.AppError
import com.example.salesforce.core.network.ApiException
import com.example.salesforce.core.network.AuthRepository
import com.example.salesforce.core.network.UserActivityResponse
import com.example.salesforce.core.network.UserResponse
import com.example.salesforce.core.ui.components.MonthYearPickerHeader
import com.example.salesforce.core.ui.PakistanTime
import com.example.salesforce.core.ui.notifications.LocalAppNotifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.launch

data class MonthItem(val year: Int, val month: Int, val label: String)

private fun parseDateOnly(date: String?, timeZone: TimeZone): Calendar? {
    if (date == null || !date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return null

    val year = date.substring(0, 4).toIntOrNull() ?: return null
    val month = date.substring(5, 7).toIntOrNull() ?: return null
    val day = date.substring(8, 10).toIntOrNull() ?: return null
    if (year < 1 || month !in 1..12 || day !in 1..31) return null

    return Calendar.getInstance(timeZone).apply {
        clear()
        isLenient = false
        set(year, month - 1, day)
    }.let { calendar ->
        try {
            calendar.timeInMillis
            calendar
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

@Composable
fun AttendanceScreen(
    repository: AuthRepository,
    currentUser: UserResponse?,
    onBack: () -> Unit,
) {
    val notifier = LocalAppNotifier.current
    var activityResponse by remember { mutableStateOf<UserActivityResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var markedTodayLocally by remember(currentUser?.id) { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val pakistanTimeZone = remember { PakistanTime.timeZone }

    val currentCal = Calendar.getInstance(pakistanTimeZone)
    var selectedYear by remember { mutableStateOf(currentCal.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(currentCal.get(Calendar.MONTH) + 1) } // 1-12

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            try {
                activityResponse = repository.getUserActivity(currentUser.id)
            } catch (e: Exception) {
                notifier.error("AttendanceScreen", "getUserActivity", AppError.BACKEND_ERROR, e)
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    val oldestDateStr = activityResponse?.oldestAttendanceDate
    val oldestCal = remember(oldestDateStr) {
        parseDateOnly(oldestDateStr, pakistanTimeZone)
            ?: Calendar.getInstance(pakistanTimeZone).apply {
                clear()
                set(2023, Calendar.JANUARY, 1)
            }
    }

    val monthsList = remember(oldestCal) {
        val list = mutableListOf<MonthItem>()
        val cal = (oldestCal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val targetCal = Calendar.getInstance(pakistanTimeZone)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).apply {
            timeZone = pakistanTimeZone
        }

        while (!cal.after(targetCal)) {
            list.add(MonthItem(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, sdf.format(cal.time)))
            cal.add(Calendar.MONTH, 1)
        }
        if (list.isEmpty()) {
            list.add(
                MonthItem(
                    currentCal.get(Calendar.YEAR),
                    currentCal.get(Calendar.MONTH) + 1,
                    sdf.format(currentCal.time),
                ),
            )
        }
        list
    }

    val cal = Calendar.getInstance(pakistanTimeZone).apply {
        set(selectedYear, selectedMonth - 1, 1)
    }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val todayCal = Calendar.getInstance(pakistanTimeZone)
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH) + 1 // 1-12 (matches API)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    // API months are 1-indexed (e.g. 10 = October) -> exact match only
    val monthAttendanceList: List<Boolean?>? = activityResponse?.attendanceMonths
        ?.find { it.year == selectedYear && it.month == selectedMonth }
        ?.attendance

    // Today's status comes only from the server data, whatever month is being viewed
    val isAlreadyMarked = markedTodayLocally || activityResponse?.attendanceMonths
        ?.find { it.year == todayYear && it.month == todayMonth }
        ?.attendance
        ?.getOrNull(todayDay - 1) == true
    val isCurrentMonthAndYear = selectedYear == todayYear && selectedMonth == todayMonth

    // No server data for the selected month => "No data to display"
    val hasMonthData = monthAttendanceList != null

    val attendanceDays = (1..daysInMonth).map { day ->
        day to monthAttendanceList?.getOrNull(day - 1)
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
                text = "Attendance Records",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            MonthYearPickerHeader(
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                onYearMonthChanged = { y, m ->
                    selectedYear = y
                    selectedMonth = m
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Daily Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(10.dp).background(Color(0xFF10B981), CircleShape))
                                Text("Present", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(10.dp).background(Color(0xFFEF4444), CircleShape))
                                Text("Absent", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!hasMonthData) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No data to display",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        // Fancy Calendar Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(300.dp),
                        ) {
                            items(attendanceDays) { (day, attendance) ->
                                val bgColor = when (attendance) {
                                    true -> Color(0xFF10B981).copy(alpha = 0.2f)
                                    false -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                    null -> Color.Transparent
                                }
                                val textColor = when (attendance) {
                                    true -> Color(0xFF065F46)
                                    false -> Color(0xFF991B1B)
                                    null -> MaterialTheme.colorScheme.onSurface
                                }

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(bgColor, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!isLoading) {
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val userId = currentUser?.id ?: return@Button
                    coroutineScope.launch {
                        isSubmitting = true
                        try {
                            repository.markAttendance()
                            markedTodayLocally = true
                            notifier.success("Attendance marked successfully")
                            try {
                                activityResponse = repository.getUserActivity(userId)
                            } catch (e: Exception) {
                                notifier.error("AttendanceScreen", "getUserActivity", AppError.BACKEND_ERROR, e)
                            }
                        } catch (e: ApiException) {
                            if (e.statusCode == 409) {
                                markedTodayLocally = true
                                notifier.info("Attendance has already been marked today")
                                try {
                                    activityResponse = repository.getUserActivity(userId)
                                } catch (refreshError: Exception) {
                                    notifier.error("AttendanceScreen", "getUserActivity", AppError.BACKEND_ERROR, refreshError)
                                }
                            } else {
                                notifier.error("AttendanceScreen", "markAttendance", AppError.BACKEND_ERROR, e)
                            }
                        } catch (e: Exception) {
                            notifier.error("AttendanceScreen", "markAttendance", AppError.BACKEND_ERROR, e)
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = currentUser != null && isCurrentMonthAndYear && !isAlreadyMarked && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = if (isAlreadyMarked) Color(0xFF10B981)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = if (isAlreadyMarked) Color.White
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
            ) {
                if (isAlreadyMarked) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = when {
                        isAlreadyMarked -> "Attendance Marked"
                        isSubmitting -> "Marking Attendance..."
                        else -> "Mark Attendance"
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}