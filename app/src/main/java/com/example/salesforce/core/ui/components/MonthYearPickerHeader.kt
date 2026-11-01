package com.example.salesforce.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun MonthYearPickerHeader(
    selectedYear: Int,
    selectedMonth: Int, // 1-12
    onYearMonthChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {
                if (selectedMonth > 1) {
                    onYearMonthChanged(selectedYear, selectedMonth - 1)
                } else {
                    onYearMonthChanged(selectedYear - 1, 12)
                }
            }) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous Month")
            }

            Text(
                text = "${months.getOrElse(selectedMonth - 1) { "January" }} $selectedYear",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            val currentCal = Calendar.getInstance()
            val currentY = currentCal.get(Calendar.YEAR)
            val currentM = currentCal.get(Calendar.MONTH) + 1
            val canGoNext = selectedYear < currentY || (selectedYear == currentY && selectedMonth < currentM)

            IconButton(
                onClick = {
                    if (canGoNext) {
                        if (selectedMonth < 12) {
                            onYearMonthChanged(selectedYear, selectedMonth + 1)
                        } else {
                            onYearMonthChanged(selectedYear + 1, 1)
                        }
                    }
                },
                enabled = canGoNext,
            ) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next Month")
            }
        }
    }
}
