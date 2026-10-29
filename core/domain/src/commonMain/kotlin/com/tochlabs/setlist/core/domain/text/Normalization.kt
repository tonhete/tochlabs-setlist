package com.tochlabs.setlist.core.domain.text

// Case and accent insensitive comparison, used by the catalogue search and by import matching.
// Common Kotlin has no Unicode normaliser, so accented Latin letters are folded with a table.

private val FOLDS: Map<Char, Char> =
    buildMap {
        fun fold(
            from: String,
            to: Char,
        ) = from.forEach { put(it, to) }
        fold("àáâãäåāăą", 'a')
        fold("çćĉċč", 'c')
        fold("ďđ", 'd')
        fold("èéêëēĕėęě", 'e')
        fold("ĝğġģ", 'g')
        fold("ìíîïĩīĭįı", 'i')
        fold("ñńņňŉ", 'n')
        fold("òóôõöøōŏő", 'o')
        fold("ŕŗř", 'r')
        fold("śŝşš", 's')
        fold("ţťŧ", 't')
        fold("ùúûüũūŭůűų", 'u')
        fold("ýÿŷ", 'y')
        fold("źżž", 'z')
    }

private val WHITESPACE = Regex("""\s+""")

/** Lower case, without accents, trimmed, with single spaces. */
fun normalizeText(text: String): String =
    text
        .lowercase()
        .map { FOLDS[it] ?: it }
        .joinToString("")
        .trim()
        .replace(WHITESPACE, " ")

/** True when the query is blank or is contained in the title or the artist. */
fun matchesSearch(
    query: String,
    title: String,
    artist: String?,
): Boolean {
    val needle = normalizeText(query)
    if (needle.isEmpty()) return true
    return needle in normalizeText(title) || (artist != null && needle in normalizeText(artist))
}

/** Import matching: same title and same artist after normalisation; a missing artist equals a blank one. */
fun sameSong(
    titleA: String,
    artistA: String?,
    titleB: String,
    artistB: String?,
): Boolean =
    normalizeText(titleA) == normalizeText(titleB) &&
        normalizeText(artistA.orEmpty()) == normalizeText(artistB.orEmpty())
