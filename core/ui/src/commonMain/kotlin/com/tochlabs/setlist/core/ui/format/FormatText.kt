package com.tochlabs.setlist.core.ui.format

import androidx.compose.runtime.Composable
import com.tochlabs.setlist.core.ui.resources.Res
import com.tochlabs.setlist.core.ui.resources.duration_hours_minutes
import com.tochlabs.setlist.core.ui.resources.duration_minutes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

/** The applied interface language (specs/localization); follows the in-app choice at once. */
@Composable
fun currentUiLanguage(): UiLanguage = LocalUiLanguage.current

/** Total in hours and minutes with translated units, for example "1 h 05 min" or "14 min". */
@Composable
fun durationText(seconds: Long): String {
    val total = toHoursMinutes(seconds)
    return if (total.hours > 0) {
        stringResource(
            Res.string.duration_hours_minutes,
            total.hours.toString(),
            total.minutes.toString().padStart(2, '0'),
        )
    } else {
        stringResource(Res.string.duration_minutes, total.minutes.toString())
    }
}

@Composable
fun dateText(date: LocalDate): String = formatDate(date, currentUiLanguage())

@Composable
fun timeText(time: LocalTime): String = formatTime(time, currentUiLanguage())
