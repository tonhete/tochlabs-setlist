package com.tochlabs.setlist.feature.share.pdf

import com.tochlabs.setlist.core.domain.setlist.ResolvedItem
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.StopType
import kotlinx.datetime.LocalDate

/** What a PDF is made from: the setlist, its band and its items with their songs. */
data class ExportSource(
    val setlist: Setlist,
    val bandName: String,
    val items: List<ResolvedItem>,
)

/**
 * Translated texts for the PDFs. The screen fills them in the current language, so the layout
 * stays testable without resources.
 */
data class ExportTexts(
    val stopLabels: Map<StopType, String>,
    val standardTuning: String,
    val none: String,
    val repeated: String,
    val noDuration: String,
    /** Table header of the list PDF: number, title, artist, key, BPM, duration, changes. */
    val columns: List<String>,
    val instrumentChange: (String) -> String,
    val tuning: (String) -> String,
    /** (setlist key, original key) */
    val key: (String, String) -> String,
    /** (setlist tempo, original tempo) */
    val tempo: (String, String) -> String,
    val songCount: (Int) -> String,
    /** Receives the formatted total. */
    val totalWithStops: (String) -> String,
    /** Receives the number of songs without duration. */
    val incomplete: (Int) -> String,
    /** Total in hours and minutes. */
    val duration: (Long) -> String,
    val date: (LocalDate) -> String,
    /** Added to the PDF file names, for example "Sala Norte - stage.pdf". */
    val stageFileLabel: String,
    val listFileLabel: String,
)
