package com.tochlabs.setlist.core.domain.validation

/** Pause between songs, in seconds: 0 to 120 in steps of 5. */
object PauseRules {
    const val MIN_SECONDS = 0
    const val MAX_SECONDS = 120
    const val STEP_SECONDS = 5
    const val DEFAULT_SECONDS = 20
}

fun validatePause(seconds: Int): Validated<Int> =
    when {
        seconds !in PauseRules.MIN_SECONDS..PauseRules.MAX_SECONDS -> Validated.Invalid(ValidationError.OUT_OF_RANGE)
        seconds % PauseRules.STEP_SECONDS != 0 -> Validated.Invalid(ValidationError.NOT_A_STEP)
        else -> Validated.Valid(seconds)
    }

fun increasePause(seconds: Int): Int = (seconds + PauseRules.STEP_SECONDS).coerceAtMost(PauseRules.MAX_SECONDS)

fun decreasePause(seconds: Int): Int = (seconds - PauseRules.STEP_SECONDS).coerceAtLeast(PauseRules.MIN_SECONDS)
