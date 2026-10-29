package com.tochlabs.setlist.core.domain.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Scenarios from specs/song-catalog (search) and specs/setlist-sharing (import matching).
class NormalizationTest {
    @Test
    fun accentInsensitiveSearch() {
        assertTrue(matchesSearch("cancion", title = "Canción de cuna", artist = null))
    }

    @Test
    fun caseInsensitiveSearch() {
        assertTrue(matchesSearch("CUNA", title = "Canción de cuna", artist = null))
    }

    @Test
    fun searchMatchesTheArtist() {
        assertTrue(matchesSearch("perez", title = "Otra", artist = "Ana Pérez"))
    }

    @Test
    fun searchWithoutMatch() {
        assertFalse(matchesSearch("rock", title = "Canción de cuna", artist = "Ana Pérez"))
    }

    @Test
    fun blankQueryMatchesEverySong() {
        assertTrue(matchesSearch("  ", title = "Canción de cuna", artist = null))
    }

    @Test
    fun normalisationRemovesAccentsCaseAndExtraSpaces() {
        assertEquals("nino aeiou cancion", normalizeText("  Niño  ÁÉÍÓÚ\tCANCIÓN "))
        assertEquals("aaaaeeeeiiiioooouuuuc", normalizeText("àâäãèêëéìîïíòôöõùûüúç"))
    }

    @Test
    fun importMatchesTitleAndArtistIgnoringCaseAndAccents() {
        assertTrue(sameSong("CANCIÓN de cuna", "ana perez", "Canción de cuna", "Ana Pérez"))
        assertTrue(sameSong("Intro", null, "intro", ""))
        assertFalse(sameSong("Intro", "Mareas", "Intro", "Otros"))
    }
}
