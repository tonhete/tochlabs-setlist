package com.tochlabs.setlist.core.domain.calendar

import com.tochlabs.setlist.core.domain.validation.Validated
import com.tochlabs.setlist.core.domain.validation.ValidationError
import com.tochlabs.setlist.core.model.MilestoneKind
import kotlinx.datetime.LocalTime

/** Milestones offered for a gig, in their usual order (specs/event-calendar). */
val STANDARD_MILESTONES: List<MilestoneKind> =
    listOf(
        MilestoneKind.ARRIVAL,
        MilestoneKind.BACKLINE,
        MilestoneKind.SOUNDCHECK,
        MilestoneKind.ON_STAGE,
        MilestoneKind.GIG,
        MilestoneKind.PACK_UP,
        MilestoneKind.DEPARTURE,
    )

/** Standard milestones that usually have an end time. */
val MilestoneKind.isRange: Boolean
    get() = this == MilestoneKind.BACKLINE || this == MilestoneKind.SOUNDCHECK || this == MilestoneKind.GIG

/** A valid milestone, before it gets an id and an event. */
data class MilestoneInput(
    val kind: MilestoneKind,
    /** The user's label: required for custom milestones, a rename for standard ones. */
    val label: String?,
    val start: LocalTime,
    val end: LocalTime?,
)

/** Start time required, end not before start, custom milestones need a label. */
fun validateMilestone(
    kind: MilestoneKind,
    label: String?,
    start: LocalTime?,
    end: LocalTime?,
): Validated<MilestoneInput> {
    val cleanLabel = label?.trim()?.ifEmpty { null }
    return when {
        start == null -> Validated.Invalid(ValidationError.START_REQUIRED)
        end != null && end < start -> Validated.Invalid(ValidationError.END_BEFORE_START)
        kind == MilestoneKind.CUSTOM && cleanLabel == null -> Validated.Invalid(ValidationError.LABEL_REQUIRED)
        else -> Validated.Valid(MilestoneInput(kind, cleanLabel, start, end))
    }
}
