package com.tochlabs.setlist.core.data.db

/** Row timestamps in epoch milliseconds. Repositories set them from the injected Clock. */
internal data class Timestamps(
    val createdAt: Long,
    val updatedAt: Long,
)
