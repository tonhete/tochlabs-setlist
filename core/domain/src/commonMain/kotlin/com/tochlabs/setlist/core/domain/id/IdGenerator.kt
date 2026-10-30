package com.tochlabs.setlist.core.domain.id

/** Source of new ids (UUID strings, design.md decision 3). Injected so tests get predictable ids. */
interface IdGenerator {
    fun newId(): String
}
