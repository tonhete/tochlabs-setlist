package com.tochlabs.setlist.core.data

import com.tochlabs.setlist.core.domain.id.IdGenerator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Random UUID v4 ids. */
@OptIn(ExperimentalUuidApi::class)
internal object UuidIdGenerator : IdGenerator {
    override fun newId(): String = Uuid.random().toString()
}
