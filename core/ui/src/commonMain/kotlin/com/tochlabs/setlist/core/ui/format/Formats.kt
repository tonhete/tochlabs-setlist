package com.tochlabs.setlist.core.ui.format

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/** Languages the UI is translated into (specs/localization). */
enum class UiLanguage { ES, EN }

data class HoursMinutes(
    val hours: Long,
    val minutes: Long,
)

private const val SECONDS_PER_MINUTE = 60L
private const val MINUTES_PER_HOUR = 60L
private const val HOURS_PER_HALF_DAY = 12

/** Exact value as minutes and seconds, for example 8:40 or 75:05. */
fun formatClock(seconds: Long): String {
    val minutes = seconds / SECONDS_PER_MINUTE
    val rest = seconds % SECONDS_PER_MINUTE
    return "$minutes:${rest.toString().padStart(2, '0')}"
}

/** Total for display in hours and minutes, rounded to the nearest minute. */
fun toHoursMinutes(seconds: Long): HoursMinutes {
    val totalMinutes = (seconds + SECONDS_PER_MINUTE / 2) / SECONDS_PER_MINUTE
    return HoursMinutes(totalMinutes / MINUTES_PER_HOUR, totalMinutes % MINUTES_PER_HOUR)
}

private val MONTHS_ES = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
private val MONTHS_EN = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** Spanish "14 nov 2026", English "Nov 14, 2026". */
fun formatDate(
    date: LocalDate,
    language: UiLanguage,
): String {
    val month = date.month.ordinal
    return when (language) {
        UiLanguage.ES -> "${date.day} ${MONTHS_ES[month]} ${date.year}"
        UiLanguage.EN -> "${MONTHS_EN[month]} ${date.day}, ${date.year}"
    }
}

private val MONTH_NAMES_ES =
    listOf(
        "enero",
        "febrero",
        "marzo",
        "abril",
        "mayo",
        "junio",
        "julio",
        "agosto",
        "septiembre",
        "octubre",
        "noviembre",
        "diciembre",
    )
private val MONTH_NAMES_EN =
    listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December",
    )

/** Calendar title: Spanish "noviembre 2026", English "November 2026". */
fun formatMonth(
    date: LocalDate,
    language: UiLanguage,
): String {
    val names = if (language == UiLanguage.ES) MONTH_NAMES_ES else MONTH_NAMES_EN
    return "${names[date.month.ordinal]} ${date.year}"
}

/** Column headers of the calendar, Monday first. */
fun weekdayInitials(language: UiLanguage): List<String> =
    when (language) {
        UiLanguage.ES -> listOf("L", "M", "X", "J", "V", "S", "D")
        UiLanguage.EN -> listOf("M", "T", "W", "T", "F", "S", "S")
    }

/** "20:00 – 21:00" for a range, the start time alone otherwise. */
fun formatTimeRange(
    start: LocalTime,
    end: LocalTime?,
    language: UiLanguage,
): String =
    if (end ==
        null
    ) {
        formatTime(start, language)
    } else {
        "${formatTime(start, language)} – ${formatTime(end, language)}"
    }

/** Spanish 24-hour "21:30", English 12-hour "9:30 PM". */
fun formatTime(
    time: LocalTime,
    language: UiLanguage,
): String {
    val minute = time.minute.toString().padStart(2, '0')
    return when (language) {
        UiLanguage.ES -> "${time.hour.toString().padStart(2, '0')}:$minute"
        UiLanguage.EN -> {
            val hour = (time.hour % HOURS_PER_HALF_DAY).let { if (it == 0) HOURS_PER_HALF_DAY else it }
            val suffix = if (time.hour < HOURS_PER_HALF_DAY) "AM" else "PM"
            "$hour:$minute $suffix"
        }
    }
}
