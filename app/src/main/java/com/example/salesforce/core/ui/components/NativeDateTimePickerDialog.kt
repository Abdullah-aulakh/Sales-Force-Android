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
import com.example.salesforce.core.ui.PakistanTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeDateTimePickerDialog(
    initialDateTimeMillis: Long = System.currentTimeMillis(),
    onDateTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialDatePickerMillis = PakistanTime.datePickerSelectionMillis(initialDateTimeMillis)
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDatePickerMillis)
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedMillis by remember { mutableStateOf(initialDatePickerMillis) }
    val currentPakistanTime = remember { PakistanTime.nowCalendar() }

    val timePickerState = rememberTimePickerState(
        initialHour = currentPakistanTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentPakistanTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    if (!showTimePicker) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    selectedMillis = datePickerState.selectedDateMillis ?: initialDatePickerMillis
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
                    val pakistanCalendar = PakistanTime.nowCalendar().apply {
                        timeInMillis = selectedMillis
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    onDateTimeSelected(PakistanTime.formatApiDateTime(pakistanCalendar))
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
