package com.tochlabs.setlist.core.domain.validation

import kotlin.test.Test
import kotlin.test.assertEquals

// Scenarios from specs/app-settings and specs/setlist-duration (configurable pause).
class PauseValidationTest {
    @Test
    fun defaultPauseIs20Seconds() {
        assertEquals(20, PauseRules.DEFAULT_SECONDS)
    }

    @Test
    fun pauseInRangeAndOnStepIsValid() {
        assertEquals(Validated.Valid(0), validatePause(0))
        assertEquals(Validated.Valid(30), validatePause(30))
        assertEquals(Validated.Valid(120), validatePause(120))
    }

    @Test
    fun pauseOutOfRangeIsRejected() {
        assertEquals(Validated.Invalid(ValidationError.OUT_OF_RANGE), validatePause(-5))
        assertEquals(Validated.Invalid(ValidationError.OUT_OF_RANGE), validatePause(125))
    }

    @Test
    fun pauseNotOnAStepOf5IsRejected() {
        assertEquals(Validated.Invalid(ValidationError.NOT_A_STEP), validatePause(22))
    }

    @Test
    fun changeThePauseFrom20To25() {
        assertEquals(25, increasePause(20))
        assertEquals(20, decreasePause(25))
    }

    @Test
    fun limitsKeepTheValueAt0And120() {
        assertEquals(0, decreasePause(0))
        assertEquals(120, increasePause(120))
    }
}
