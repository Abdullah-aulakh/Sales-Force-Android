package com.example.salesforce.core.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object PakistanTime {
    private const val TIME_ZONE_ID = "Asia/Karachi"

    val timeZone: TimeZone
        get() = TimeZone.getTimeZone(TIME_ZONE_ID)

    fun nowCalendar(): Calendar = Calendar.getInstance(timeZone)

    fun datePickerSelectionMillis(instantMillis: Long): Long {
        val pakistanCalendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = instantMillis
        }
        // Material 3 DatePicker represents calendar days as UTC-midnight milliseconds.
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                pakistanCalendar.get(Calendar.YEAR),
                pakistanCalendar.get(Calendar.MONTH),
                pakistanCalendar.get(Calendar.DAY_OF_MONTH),
            )
        }.timeInMillis
    }

    fun formatApiDateTime(calendar: Calendar): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = PakistanTime.timeZone
        }.format(calendar.time)

    fun formatDateTime(value: String): String {
        val calendar = parseDateTime(value) ?: return value
        val month = MONTH_NAMES.getOrElse(calendar.get(Calendar.MONTH)) { "Jan" }
        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
        val amPm = if (hour24 >= 12) "PM" else "AM"
        return "$month ${calendar.get(Calendar.DAY_OF_MONTH)}, ${calendar.get(Calendar.YEAR)} • $hour12:$minute $amPm"
    }

    fun isExpired(value: String?, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (value.isNullOrBlank()) return false
        return parseDateTime(value)?.timeInMillis?.let { it < nowMillis } ?: false
    }

    private fun parseDateTime(value: String): Calendar? {
        val match = DATE_TIME_PATTERN.matchEntire(value) ?: return null
        val year = match.groupValues[1].toIntOrNull() ?: return null
        val month = match.groupValues[2].toIntOrNull() ?: return null
        val day = match.groupValues[3].toIntOrNull() ?: return null
        val hour = match.groupValues[4].toIntOrNull() ?: return null
        val minute = match.groupValues[5].toIntOrNull() ?: return null
        val second = match.groupValues[6].toIntOrNull() ?: 0

        return Calendar.getInstance(timeZone).apply {
            clear()
            isLenient = false
            set(year, month - 1, day, hour, minute, second)
        }.takeIf { calendar ->
            try {
                calendar.timeInMillis
                true
            } catch (_: IllegalArgumentException) {
                false
            }
        }
    }

    private val DATE_TIME_PATTERN = Regex(
        """(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})(?::(\d{2})(?:\.\d+)?)?.*""",
    )
    private val MONTH_NAMES =
        arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
}
