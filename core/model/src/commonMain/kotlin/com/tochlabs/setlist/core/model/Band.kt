package com.tochlabs.setlist.core.model

import kotlin.jvm.JvmInline
import kotlin.time.Instant

data class Band(
    val id: BandId,
    val name: String,
    val color: BandColor,
    val archivedAt: Instant? = null,
) {
    val isArchived: Boolean get() = archivedAt != null
}

/** Colour as 0xAARRGGBB, independent of any UI toolkit. */
@JvmInline
value class BandColor(
    val argb: Long,
)
