package com.tochlabs.setlist.core.domain.setlist

/** The list with the element at [from] moved to [to]; unchanged when either is out of range. */
fun <T> List<T>.moved(
    from: Int,
    to: Int,
): List<T> {
    if (from !in indices || to !in indices || from == to) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}
