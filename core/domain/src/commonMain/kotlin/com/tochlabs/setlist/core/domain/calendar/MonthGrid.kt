package com.tochlabs.setlist.core.domain.calendar

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

private const val DAYS_PER_WEEK = 7

fun firstDayOfMonth(date: LocalDate): LocalDate = LocalDate(date.year, date.month, 1)

/** First day of the month [months] after (or before, when negative) this date's month. */
fun LocalDate.plusMonths(months: Int): LocalDate = firstDayOfMonth(this).plus(months, DateTimeUnit.MONTH)

fun LocalDate.isInMonthOf(other: LocalDate): Boolean = year == other.year && month == other.month

/**
 * Weeks of the month that contains [date], Monday to Sunday. The first and last week are
 * completed with days of the neighbouring months.
 */
fun monthGrid(date: LocalDate): List<List<LocalDate>> {
    val first = firstDayOfMonth(date)
    val last = first.plusMonths(1).minus(1, DateTimeUnit.DAY)
    val start = first.minus(first.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    val end = last.plus(DAYS_PER_WEEK - last.dayOfWeek.isoDayNumber, DateTimeUnit.DAY)
    return generateSequence(start) { it.plus(1, DateTimeUnit.DAY) }
        .takeWhile { it <= end }
        .chunked(DAYS_PER_WEEK)
        .toList()
}
