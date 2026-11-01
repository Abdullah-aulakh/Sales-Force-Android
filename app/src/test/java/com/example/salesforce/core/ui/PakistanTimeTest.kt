package com.example.salesforce.core.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class PakistanTimeTest {
    @Test
    fun formatsBackendPakistanTimeWithoutUtcConversion() {
        assertEquals(
            "Nov 2, 2026 • 2:45 AM",
            PakistanTime.formatDateTime("2026-11-02T02:45:22"),
        )
    }

    @Test
    fun comparesBackendPakistanTimeAgainstInstantCorrectly() {
        val now = Calendar.getInstance(PakistanTime.timeZone).apply {
            clear()
            set(2026, Calendar.NOVEMBER, 2, 3, 0, 0)
        }.timeInMillis

        assertTrue(PakistanTime.isExpired("2026-11-02T02:45:22", now))
        assertFalse(PakistanTime.isExpired("2026-11-02T03:15:00", now))
    }

    @Test
    fun datePickerUsesPakistanCalendarDate() {
        val utcInstant = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(2026, Calendar.NOVEMBER, 1, 21, 45, 22)
        }.timeInMillis

        val pickerCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = PakistanTime.datePickerSelectionMillis(utcInstant)
        }

        assertEquals(2026, pickerCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, pickerCalendar.get(Calendar.MONTH))
        assertEquals(2, pickerCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun selectedPickerDateIsSubmittedAsPakistanLocalTime() {
        val utcInstant = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(2026, Calendar.NOVEMBER, 1, 21, 45, 22)
        }.timeInMillis
        val selectedPakistanTime = Calendar.getInstance(PakistanTime.timeZone).apply {
            timeInMillis = PakistanTime.datePickerSelectionMillis(utcInstant)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }

        assertEquals(
            "2026-11-02T09:30:00",
            PakistanTime.formatApiDateTime(selectedPakistanTime),
        )
    }
}
