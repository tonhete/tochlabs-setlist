package com.tochlabs.setlist.core.domain.setlist

import kotlin.test.Test
import kotlin.test.assertEquals

// specs/setlists: "Reorder and remove items".
class ReorderTest {
    private val items = listOf("a", "b", "c", "d")

    @Test
    fun reorderByDragging_thirdItemToFirstPosition() {
        assertEquals(listOf("c", "a", "b", "d"), items.moved(from = 2, to = 0))
    }

    @Test
    fun moveDownwards() {
        assertEquals(listOf("b", "c", "a", "d"), items.moved(from = 0, to = 2))
    }

    @Test
    fun moveOutOfRange_leavesTheOrderUnchanged() {
        assertEquals(items, items.moved(from = 0, to = -1))
        assertEquals(items, items.moved(from = 3, to = 4))
    }
}
