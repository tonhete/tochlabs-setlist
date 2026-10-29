package com.tochlabs.setlist.core.data.db

import androidx.room3.ColumnTypeConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/** Dates and times are stored as ISO text, for example 2026-10-29 and 21:30. */
internal object DateTimeConverters {
    @ColumnTypeConverter
    fun dateToText(date: LocalDate?): String? = date?.toString()

    @ColumnTypeConverter
    fun textToDate(text: String?): LocalDate? = text?.let(LocalDate::parse)

    @ColumnTypeConverter
    fun timeToText(time: LocalTime?): String? = time?.toString()

    @ColumnTypeConverter
    fun textToTime(text: String?): LocalTime? = text?.let(LocalTime::parse)
}
