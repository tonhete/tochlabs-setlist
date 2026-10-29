package com.tochlabs.setlist.core.domain.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// Scenarios from specs/bands, specs/song-catalog and specs/setlists.
class TextValidationTest {
    @Test
    fun rejectAnEmptyName() {
        assertEquals(Validated.Invalid(ValidationError.REQUIRED), validateRequiredText(""))
        assertEquals(Validated.Invalid(ValidationError.REQUIRED), validateRequiredText("   "))
    }

    @Test
    fun rejectAnEmptyTitle() {
        assertEquals(Validated.Invalid(ValidationError.REQUIRED), validateRequiredText("\t\n"))
    }

    @Test
    fun createABandWithAName() {
        assertEquals(Validated.Valid("Mareas"), validateRequiredText("  Mareas "))
    }

    @Test
    fun tuningTooLongKeepsTheFirst20Characters() {
        assertEquals("Drop D with capo 2nd", limitShortText("Drop D with capo 2nd fret"))
    }

    @Test
    fun instrumentAndTuningLimitsAcceptExactly20Characters() {
        val twenty = "12345678901234567890"
        assertEquals(twenty, limitShortText(twenty))
    }

    @Test
    fun shortTextIsTrimmedBeforeLimiting() {
        assertEquals("Strato", limitShortText("  Strato  "))
    }

    @Test
    fun noTuningSetWhenBlank() {
        assertNull(limitShortText("   "))
        assertNull(limitShortText(null))
    }
}
