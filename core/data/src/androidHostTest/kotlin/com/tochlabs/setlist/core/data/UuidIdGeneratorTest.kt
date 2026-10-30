package com.tochlabs.setlist.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class UuidIdGeneratorTest {
    @Test
    fun idsAreUniqueUuidStrings() {
        val ids = List(100) { UuidIdGenerator.newId() }
        assertEquals(ids.size, ids.toSet().size)
        val uuid = Regex("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")
        assertTrue(ids.all { uuid.matches(it) })
    }
}
