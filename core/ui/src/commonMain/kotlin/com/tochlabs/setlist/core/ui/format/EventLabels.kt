package com.tochlabs.setlist.core.ui.format

import androidx.compose.runtime.Composable
import com.tochlabs.setlist.core.model.EventMilestone
import com.tochlabs.setlist.core.model.EventType
import com.tochlabs.setlist.core.model.MilestoneKind
import com.tochlabs.setlist.core.ui.resources.Res
import com.tochlabs.setlist.core.ui.resources.event_gig
import com.tochlabs.setlist.core.ui.resources.event_rehearsal
import com.tochlabs.setlist.core.ui.resources.milestone_arrival
import com.tochlabs.setlist.core.ui.resources.milestone_backline
import com.tochlabs.setlist.core.ui.resources.milestone_custom
import com.tochlabs.setlist.core.ui.resources.milestone_departure
import com.tochlabs.setlist.core.ui.resources.milestone_gig
import com.tochlabs.setlist.core.ui.resources.milestone_on_stage
import com.tochlabs.setlist.core.ui.resources.milestone_pack_up
import com.tochlabs.setlist.core.ui.resources.milestone_soundcheck
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Translated name of an event type. */
fun EventType.label(): StringResource =
    when (this) {
        EventType.GIG -> Res.string.event_gig
        EventType.REHEARSAL -> Res.string.event_rehearsal
    }

/** Translated name of a milestone kind; standard kinds are labelled by the app (docs/product/data-model.md). */
fun MilestoneKind.label(): StringResource =
    when (this) {
        MilestoneKind.ARRIVAL -> Res.string.milestone_arrival
        MilestoneKind.BACKLINE -> Res.string.milestone_backline
        MilestoneKind.SOUNDCHECK -> Res.string.milestone_soundcheck
        MilestoneKind.ON_STAGE -> Res.string.milestone_on_stage
        MilestoneKind.GIG -> Res.string.milestone_gig
        MilestoneKind.PACK_UP -> Res.string.milestone_pack_up
        MilestoneKind.DEPARTURE -> Res.string.milestone_departure
        MilestoneKind.CUSTOM -> Res.string.milestone_custom
    }

/** The user's label when there is one, otherwise the translated standard name. */
@Composable
fun milestoneLabel(milestone: EventMilestone): String = milestone.customLabel ?: stringResource(milestone.kind.label())
