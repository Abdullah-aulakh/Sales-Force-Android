package com.example.salesforce.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeDateTimePickerDialog(
    initialDateTimeMillis: Long = System.currentTimeMillis(),
    onDateTimeSelected: (String) -> Unit, // Returns ISO string in UTC for backend
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateTimeMillis)
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedMillis by remember { mutableStateOf(initialDateTimeMillis) }

    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi")).get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi")).get(Calendar.MINUTE),
        is24Hour = true,
    )

    if (!showTimePicker) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    selectedMillis = datePickerState.selectedDateMillis ?: initialDateTimeMillis
                    showTimePicker = true
                }) {
                    Text("Next: Select Time")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Follow-up Time (PKT)") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(onClick = {
                    // 1. User picked date and time in PKT (Asia/Karachi)
                    val pkCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi")).apply {
                        timeInMillis = selectedMillis
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    // 2. Convert PKT to UTC for backend transmission
                    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = pkCal.timeInMillis
                    }

                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val isoString = sdf.format(utcCal.time)
                    onDateTimeSelected(isoString)
                }) {
                    Text("Confirm Date & Time")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Back") }
            },
        )
    }
}
