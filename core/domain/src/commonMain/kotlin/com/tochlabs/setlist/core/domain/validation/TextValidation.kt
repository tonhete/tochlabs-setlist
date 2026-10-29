package com.tochlabs.setlist.core.domain.validation

/** Maximum length of instrument and tuning texts. */
const val SHORT_TEXT_MAX_LENGTH = 20

/** Names and titles: required, trimmed. */
fun validateRequiredText(raw: String): Validated<String> {
    val trimmed = raw.trim()
    return if (trimmed.isEmpty()) Validated.Invalid(ValidationError.REQUIRED) else Validated.Valid(trimmed)
}

/** Instrument and tuning: trimmed, cut to 20 characters, blank means not set. */
fun limitShortText(raw: String?): String? =
    raw
        ?.trim()
        ?.take(SHORT_TEXT_MAX_LENGTH)
        ?.trimEnd()
        ?.ifEmpty { null }
