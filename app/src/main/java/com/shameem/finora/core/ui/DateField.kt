package com.shameem.finora.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.services.date.shortDisplay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Date-only picker button (iOS `DatePicker(..., in: min...max, displayedComponents: .date)`).
 * Shows the date as a capsule; tapping opens a calendar dialog limited to [min]..[max] (inclusive, by day).
 * Picking a day keeps the original time of day, like the iOS control.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    date: Instant,
    onDateChange: (Instant) -> Unit,
    modifier: Modifier = Modifier,
    min: Instant? = null,
    max: Instant? = null,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    var showDialog by remember { mutableStateOf(false) }

    AppText(
        text = date.shortDisplay(zone),
        size = 15,
        weight = AppFontWeight.SemiBold,
        color = FinoraColors.TextPrimary,
        modifier = modifier
            .clip(CircleShape)
            .background(FinoraColors.FillSubtle)
            .clickable(role = Role.Button) { showDialog = true }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )

    if (showDialog) {
        val minDay = min?.atZone(zone)?.toLocalDate()
        val maxDay = max?.atZone(zone)?.toLocalDate()
        val currentDay = date.atZone(zone).toLocalDate()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = currentDay.toUtcMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val day = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    return (minDay == null || !day.isBefore(minDay)) && (maxDay == null || !day.isAfter(maxDay))
                }
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            colors = DatePickerDefaults.colors(containerColor = FinoraColors.ScreenBackground),
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            val time = date.atZone(zone).toLocalTime()
                            onDateChange(picked.atTime(time).atZone(zone).toInstant())
                        }
                        showDialog = false
                    },
                ) { AppText("OK", size = 16, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    AppText("Cancel", size = 16, weight = AppFontWeight.SemiBold, color = FinoraColors.TextSecondary)
                }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
