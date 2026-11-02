package com.tochlabs.setlist.core.domain.calendar

import com.tochlabs.setlist.core.domain.validation.Validated
import com.tochlabs.setlist.core.domain.validation.ValidationError
import com.tochlabs.setlist.core.model.MilestoneKind
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

// specs/event-calendar: "Roadmap of milestones"; docs/product/data-model.md: EventMilestone rules.
class MilestoneRulesTest {
    private val eight = LocalTime(20, 0)
    private val nine = LocalTime(21, 0)

    @Test
    fun standardMilestonesForAGig_inOrderWithTheirRanges() {
        assertEquals(
            listOf(
                MilestoneKind.ARRIVAL,
                MilestoneKind.BACKLINE,
                MilestoneKind.SOUNDCHECK,
                MilestoneKind.ON_STAGE,
                MilestoneKind.GIG,
                MilestoneKind.PACK_UP,
                MilestoneKind.DEPARTURE,
            ),
            STANDARD_MILESTONES,
        )
        assertEquals(
            setOf(MilestoneKind.BACKLINE, MilestoneKind.SOUNDCHECK, MilestoneKind.GIG),
            MilestoneKind.entries.filter { it.isRange }.toSet(),
        )
    }

    @Test
    fun rangeMilestone_isValid() {
        assertEquals(
            Validated.Valid(MilestoneInput(MilestoneKind.SOUNDCHECK, null, eight, nine)),
            validateMilestone(MilestoneKind.SOUNDCHECK, "", eight, nine),
        )
    }

    @Test
    fun endEqualToStartIsAllowed_endBeforeStartIsNot() {
        assertEquals(
            Validated.Invalid(ValidationError.END_BEFORE_START),
            validateMilestone(MilestoneKind.GIG, null, nine, eight),
        )
        assertEquals(
            Validated.Valid(MilestoneInput(MilestoneKind.GIG, null, nine, nine)),
            validateMilestone(MilestoneKind.GIG, null, nine, nine),
        )
    }

    @Test
    fun startTimeIsRequired() {
        assertEquals(
            Validated.Invalid(ValidationError.START_REQUIRED),
            validateMilestone(MilestoneKind.ARRIVAL, null, null, null),
        )
    }

    @Test
    fun customMilestone_needsALabel() {
        assertEquals(
            Validated.Invalid(ValidationError.LABEL_REQUIRED),
            validateMilestone(MilestoneKind.CUSTOM, "  ", eight, null),
        )
        assertEquals(
            Validated.Valid(MilestoneInput(MilestoneKind.CUSTOM, "Dinner", eight, null)),
            validateMilestone(MilestoneKind.CUSTOM, " Dinner ", eight, null),
        )
    }

    @Test
    fun renamedStandardMilestone_keepsItsKindWithTheNewLabel() {
        assertEquals(
            Validated.Valid(MilestoneInput(MilestoneKind.ARRIVAL, "Meet at the van", eight, null)),
            validateMilestone(MilestoneKind.ARRIVAL, "Meet at the van", eight, null),
        )
    }
}
