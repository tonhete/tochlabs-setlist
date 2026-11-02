package com.tochlabs.setlist.core.ui.component

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

// Drag-and-drop for lazy lists (design.md, open question on the reorderable list). The library
// stays behind this file, so it can be replaced without touching the feature modules.

/** Drag state of one lazy list. */
@Stable
class ReorderState internal constructor(
    internal val state: ReorderableLazyListState,
    private val onDragEnd: State<() -> Unit>,
) {
    internal fun dragEnded() = onDragEnd.value()
}

/**
 * [onMove] receives the keys of the dragged item and of the item it moved over, while dragging;
 * only keys of [reorderableItems] rows are passed. [onDragEnd] runs when the item is dropped.
 */
@Composable
fun rememberReorderState(
    listState: LazyListState,
    onMove: (fromKey: String, toKey: String) -> Unit,
    onDragEnd: () -> Unit,
): ReorderState {
    val haptics = LocalHapticFeedback.current
    val currentOnMove = rememberUpdatedState(onMove)
    val currentOnDragEnd = rememberUpdatedState(onDragEnd)
    val state =
        rememberReorderableLazyListState(listState) { from, to ->
            val fromKey = from.key as? String
            val toKey = to.key as? String
            if (fromKey != null && toKey != null) {
                currentOnMove.value(fromKey, toKey)
                haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
            }
        }
    return remember(state) { ReorderState(state, currentOnDragEnd) }
}

/** Lets an item's content mark the element that starts a drag. */
interface DragHandleScope {
    fun Modifier.dragHandle(): Modifier
}

/** Rows that can be dragged by their handle. Keys must be unique Strings. */
fun <T> LazyListScope.reorderableItems(
    state: ReorderState,
    items: List<T>,
    key: (T) -> String,
    content: @Composable DragHandleScope.(item: T, isDragging: Boolean) -> Unit,
) {
    items(items, key = key) { item ->
        ReorderableRow(state, key(item)) { isDragging -> content(item, isDragging) }
    }
}

@Composable
private fun LazyItemScope.ReorderableRow(
    state: ReorderState,
    key: String,
    content: @Composable DragHandleScope.(isDragging: Boolean) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    ReorderableItem(state.state, key = key) { isDragging ->
        val itemScope = this
        val handles =
            object : DragHandleScope {
                override fun Modifier.dragHandle(): Modifier =
                    with(itemScope) {
                        draggableHandle(
                            onDragStarted = {
                                haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                            },
                            onDragStopped = {
                                haptics.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                state.dragEnded()
                            },
                        )
                    }
            }
        handles.content(isDragging)
    }
}
