package com.tochlabs.setlist.core.ui.format

import com.tochlabs.setlist.core.model.AppLanguage
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

// specs/localization (localized formats) and specs/setlist-duration (hours and minutes,
// exact value in minutes and seconds).
class FormatTest {
    @Test
    fun exactValueInMinutesAndSeconds() {
        assertEquals("0:00", formatClock(0))
        assertEquals("8:40", formatClock(520))
        assertEquals("14:00", formatClock(840))
        assertEquals("75:05", formatClock(4505))
    }

    @Test
    fun totalInHoursAndMinutesRoundsToTheNearestMinute() {
        assertEquals(HoursMinutes(0, 9), toHoursMinutes(520))
        assertEquals(HoursMinutes(1, 5), toHoursMinutes(3900))
        assertEquals(HoursMinutes(0, 0), toHoursMinutes(29))
        assertEquals(HoursMinutes(0, 1), toHoursMinutes(30))
        assertEquals(HoursMinutes(1, 0), toHoursMinutes(3599))
    }

    @Test
    fun dateInSpanish() {
        assertEquals("14 nov 2026", formatDate(LocalDate(2026, 11, 14), UiLanguage.ES))
    }

    @Test
    fun dateInEnglish() {
        assertEquals("Nov 14, 2026", formatDate(LocalDate(2026, 11, 14), UiLanguage.EN))
    }

    @Test
    fun monthNameFollowsTheLanguage() {
        assertEquals("3 ene 2027", formatDate(LocalDate(2027, 1, 3), UiLanguage.ES))
        assertEquals("Jan 3, 2027", formatDate(LocalDate(2027, 1, 3), UiLanguage.EN))
    }

    @Test
    fun timeFollowsTheLanguage() {
        assertEquals("21:30", formatTime(LocalTime(21, 30), UiLanguage.ES))
        assertEquals("9:30 PM", formatTime(LocalTime(21, 30), UiLanguage.EN))
        assertEquals("12:05 AM", formatTime(LocalTime(0, 5), UiLanguage.EN))
        assertEquals("12:00 PM", formatTime(LocalTime(12, 0), UiLanguage.EN))
        assertEquals("08:05", formatTime(LocalTime(8, 5), UiLanguage.ES))
    }

    @Test
    fun monthTitleFollowsTheLanguage() {
        assertEquals("noviembre 2026", formatMonth(LocalDate(2026, 11, 14), UiLanguage.ES))
        assertEquals("November 2026", formatMonth(LocalDate(2026, 11, 14), UiLanguage.EN))
    }

    @Test
    fun weekdayInitialsStartOnMonday() {
        assertEquals(listOf("L", "M", "X", "J", "V", "S", "D"), weekdayInitials(UiLanguage.ES))
        assertEquals(listOf("M", "T", "W", "T", "F", "S", "S"), weekdayInitials(UiLanguage.EN))
    }

    @Test
    fun timeRange() {
        assertEquals("20:00 – 21:00", formatTimeRange(LocalTime(20, 0), LocalTime(21, 0), UiLanguage.ES))
        assertEquals("20:00", formatTimeRange(LocalTime(20, 0), null, UiLanguage.ES))
    }

    // specs/localization: "Spanish and English interface".
    @Test
    fun deviceInSpanish_theAppIsInSpanish() {
        assertEquals(UiLanguage.ES, resolveUiLanguage(AppLanguage.SYSTEM, deviceLanguage = "es"))
    }

    @Test
    fun unsupportedDeviceLanguage_theAppIsInEnglish() {
        assertEquals(UiLanguage.EN, resolveUiLanguage(AppLanguage.SYSTEM, deviceLanguage = "fr"))
        assertEquals(UiLanguage.EN, resolveUiLanguage(AppLanguage.SYSTEM, deviceLanguage = "en"))
    }

    @Test
    fun aChosenLanguageWinsOverTheDevice() {
        assertEquals(UiLanguage.EN, resolveUiLanguage(AppLanguage.EN, deviceLanguage = "es"))
        assertEquals(UiLanguage.ES, resolveUiLanguage(AppLanguage.ES, deviceLanguage = "fr"))
    }
}
