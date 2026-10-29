package com.tochlabs.setlist.core.domain.validation

/** Result of validating user input. Invalid input is a value, never an exception. */
sealed interface Validated<out T> {
    data class Valid<out T>(
        val value: T,
    ) : Validated<T>

    data class Invalid(
        val error: ValidationError,
    ) : Validated<Nothing>
}

enum class ValidationError { REQUIRED, OUT_OF_RANGE, NOT_A_STEP }
