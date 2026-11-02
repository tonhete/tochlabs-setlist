package com.tochlabs.setlist.core.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import com.tochlabs.setlist.core.ui.format.UiLanguage
import com.tochlabs.setlist.core.ui.format.currentUiLanguage
import com.tochlabs.setlist.core.ui.resources.Res
import com.tochlabs.setlist.core.ui.resources.action_cancel
import com.tochlabs.setlist.core.ui.resources.action_ok
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

// The Material date picker works with midnight UTC in milliseconds.
private const val MILLIS_PER_DAY = 86_400_000L

internal fun LocalDate.toUtcMillis(): Long = toEpochDays() * MILLIS_PER_DAY

internal fun utcMillisToDate(millis: Long): LocalDate = LocalDate.fromEpochDays(millis.floorDiv(MILLIS_PER_DAY))

/** Date picker dialog; [onPicked] receives the chosen date. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogFor(
    initial: LocalDate?,
    onPicked: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initial?.toUtcMillis())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onPicked(utcMillisToDate(it)) }
                onDismiss()
            }) { Text(stringResource(Res.string.action_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) } },
    ) { DatePicker(state) }
}

/** Time picker dialog: 24-hour clock in Spanish, 12-hour in English (specs/localization). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogFor(
    initial: LocalTime?,
    onPicked: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state =
        rememberTimePickerState(
            initialHour = initial?.hour ?: DEFAULT_HOUR,
            initialMinute = initial?.minute ?: 0,
            is24Hour = currentUiLanguage() == UiLanguage.ES,
        )
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state) },
        confirmButton = {
            TextButton(onClick = {
                onPicked(LocalTime(state.hour, state.minute))
                onDismiss()
            }) { Text(stringResource(Res.string.action_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) } },
    )
}

/** Evening, when most gig milestones happen. */
private const val DEFAULT_HOUR = 20
