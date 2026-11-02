package com.tochlabs.setlist.core.domain.transfer

import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistPackage

/** Reads and writes `.setlist` files (design.md, decision 8). */
interface SetlistFileCodec {
    fun encode(pkg: SetlistPackage): String

    /** Never throws: invalid input is a result, not an exception. */
    fun decode(text: String): SetlistFileResult
}

sealed interface SetlistFileResult {
    data class Valid(
        val pkg: SetlistPackage,
    ) : SetlistFileResult

    /** Not a `.setlist` file, or damaged. */
    data object Unreadable : SetlistFileResult

    /** Written by a newer, incompatible version of the app. */
    data object NewerVersion : SetlistFileResult
}

/** Builds packages from the database and imports them (specs/setlist-sharing). */
interface SetlistTransferRepository {
    /** The setlist with file-local ids, or null when it does not exist. */
    suspend fun exportPackage(setlistId: SetlistId): SetlistPackage?

    /**
     * Creates a new setlist for [bandId] in one transaction. Songs that match the catalogue by title
     * and artist (ignoring case and accents) are reused; missing ones are added to the catalogue and
     * to the band's repertoire. Existing setlists are never modified.
     */
    suspend fun import(
        pkg: SetlistPackage,
        bandId: BandId,
    ): SetlistId
}
