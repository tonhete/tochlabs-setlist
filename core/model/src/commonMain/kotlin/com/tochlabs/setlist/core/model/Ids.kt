package com.tochlabs.setlist.core.model

import kotlin.jvm.JvmInline

// Ids are UUID strings (design.md, decision 3). Typed wrappers stop mixing them up.

@JvmInline
value class BandId(
    val value: String,
)

@JvmInline
value class SongId(
    val value: String,
)

@JvmInline
value class SetlistId(
    val value: String,
)

@JvmInline
value class SetlistItemId(
    val value: String,
)

@JvmInline
value class EventId(
    val value: String,
)

@JvmInline
value class EventMilestoneId(
    val value: String,
)
