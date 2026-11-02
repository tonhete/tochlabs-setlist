package com.tochlabs.setlist.core.domain.setlist

import com.tochlabs.setlist.core.model.Setlist
import kotlinx.datetime.LocalDate

data class SetlistGroups(
    /** No date or dated from today on: by date, undated last. */
    val upcoming: List<Setlist>,
    /** Dated before today: newest first. */
    val past: List<Setlist>,
)

/** Splits setlists into upcoming and past (specs/setlists, "Upcoming and past setlists"). */
fun groupByDate(
    setlists: List<Setlist>,
    today: LocalDate,
): SetlistGroups {
    val (past, upcoming) = setlists.partition { it.date?.let { date -> date < today } ?: false }
    val byName = compareBy<Setlist> { it.name.lowercase() }
    return SetlistGroups(
        upcoming = upcoming.sortedWith(compareBy<Setlist>({ it.date == null }, { it.date }).then(byName)),
        past = past.sortedWith(compareByDescending<Setlist> { it.date }.then(byName)),
    )
}
