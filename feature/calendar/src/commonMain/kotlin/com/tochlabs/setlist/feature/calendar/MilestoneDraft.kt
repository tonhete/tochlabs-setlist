package com.tochlabs.setlist.feature.calendar

import com.tochlabs.setlist.core.domain.calendar.MilestoneInput
import com.tochlabs.setlist.core.domain.calendar.isRange
import com.tochlabs.setlist.core.domain.calendar.validateMilestone
import com.tochlabs.setlist.core.domain.validation.Validated
import com.tochlabs.setlist.core.domain.validation.ValidationError
import com.tochlabs.setlist.core.model.EventMilestone
import com.tochlabs.setlist.core.model.EventMilestoneId
import com.tochlabs.setlist.core.model.MilestoneKind
import kotlinx.datetime.LocalTime

/**
 * What the milestone sheet edits: a new milestone ([id] null) or an existing one. [label] renames a
 * standard milestone and is required for a custom one.
 */
data class MilestoneDraft(
    val id: EventMilestoneId? = null,
    val kind: MilestoneKind = MilestoneKind.ARRIVAL,
    val label: String = "",
    val start: LocalTime? = null,
    val end: LocalTime? = null,
    /** Whether the milestone has an end time (a range, such as soundcheck 20:00 – 21:00). */
    val hasEnd: Boolean = kind.isRange,
) {
    /** Choosing a kind also chooses whether it is a range, as the standard milestones suggest. */
    fun withKind(kind: MilestoneKind) = copy(kind = kind, hasEnd = kind.isRange)

    fun withLabel(text: String) = copy(label = text)

    fun withStart(time: LocalTime) = copy(start = time)

    fun withEnd(time: LocalTime) = copy(end = time, hasEnd = true)

    fun withoutEnd() = copy(end = null, hasEnd = false)

    internal val validation: Validated<MilestoneInput>
        get() = validateMilestone(kind, label, start, end.takeIf { hasEnd })

    /** First problem to show, or null when the milestone can be saved. */
    val error: ValidationError? get() = (validation as? Validated.Invalid)?.error

    companion object {
        fun of(milestone: EventMilestone) =
            MilestoneDraft(
                id = milestone.id,
                kind = milestone.kind,
                label = milestone.customLabel.orEmpty(),
                start = milestone.startTime,
                end = milestone.endTime,
                hasEnd = milestone.endTime != null,
            )
    }
}
