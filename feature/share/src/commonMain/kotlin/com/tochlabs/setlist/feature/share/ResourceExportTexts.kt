package com.tochlabs.setlist.feature.share

import androidx.compose.ui.text.intl.Locale
import com.tochlabs.setlist.core.model.StopType
import com.tochlabs.setlist.core.ui.format.UiLanguage
import com.tochlabs.setlist.core.ui.format.formatDate
import com.tochlabs.setlist.core.ui.format.label
import com.tochlabs.setlist.core.ui.format.toHoursMinutes
import com.tochlabs.setlist.core.ui.resources.duration_hours_minutes
import com.tochlabs.setlist.core.ui.resources.duration_minutes
import com.tochlabs.setlist.core.ui.resources.marker_instrument
import com.tochlabs.setlist.core.ui.resources.marker_key
import com.tochlabs.setlist.core.ui.resources.marker_repeated
import com.tochlabs.setlist.core.ui.resources.marker_tempo
import com.tochlabs.setlist.core.ui.resources.marker_tuning
import com.tochlabs.setlist.core.ui.resources.tuning_standard
import com.tochlabs.setlist.core.ui.resources.value_none
import com.tochlabs.setlist.feature.share.pdf.ExportTexts
import com.tochlabs.setlist.feature.share.resources.Res
import com.tochlabs.setlist.feature.share.resources.pdf_col_artist
import com.tochlabs.setlist.feature.share.resources.pdf_col_changes
import com.tochlabs.setlist.feature.share.resources.pdf_col_key
import com.tochlabs.setlist.feature.share.resources.pdf_col_time
import com.tochlabs.setlist.feature.share.resources.pdf_col_title
import com.tochlabs.setlist.feature.share.resources.pdf_incomplete
import com.tochlabs.setlist.feature.share.resources.pdf_list_label
import com.tochlabs.setlist.feature.share.resources.pdf_no_duration
import com.tochlabs.setlist.feature.share.resources.pdf_stage_label
import com.tochlabs.setlist.feature.share.resources.pdf_total_with_stops
import com.tochlabs.setlist.feature.share.resources.share_songs
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import com.tochlabs.setlist.core.ui.resources.Res as CoreRes

/**
 * Reads the PDF texts from the string resources in the current language. Templates are read with
 * placeholders and filled later, so the layout code stays free of resources.
 */
internal object ResourceExportTexts : ExportTextsProvider {
    override suspend fun texts(): ExportTexts {
        val language = if (Locale.current.language == "es") UiLanguage.ES else UiLanguage.EN
        val instrument = getString(CoreRes.string.marker_instrument, PLACEHOLDER_1)
        val tuning = getString(CoreRes.string.marker_tuning, PLACEHOLDER_1)
        val key = getString(CoreRes.string.marker_key, PLACEHOLDER_1, PLACEHOLDER_2)
        val tempo = getString(CoreRes.string.marker_tempo, PLACEHOLDER_1, PLACEHOLDER_2)
        val total = getString(Res.string.pdf_total_with_stops, PLACEHOLDER_1)
        val hoursMinutes = getString(CoreRes.string.duration_hours_minutes, PLACEHOLDER_1, PLACEHOLDER_2)
        val minutes = getString(CoreRes.string.duration_minutes, PLACEHOLDER_1)
        // Spanish and English have two plural forms: one, and other.
        val songsOne = getPluralString(Res.plurals.share_songs, 1, PLACEHOLDER_1)
        val songsOther = getPluralString(Res.plurals.share_songs, 2, PLACEHOLDER_1)
        val incompleteOne = getPluralString(Res.plurals.pdf_incomplete, 1, PLACEHOLDER_1)
        val incompleteOther = getPluralString(Res.plurals.pdf_incomplete, 2, PLACEHOLDER_1)
        return ExportTexts(
            stopLabels = StopType.entries.associateWith { getString(it.label()) },
            standardTuning = getString(CoreRes.string.tuning_standard),
            none = getString(CoreRes.string.value_none),
            repeated = getString(CoreRes.string.marker_repeated),
            noDuration = getString(Res.string.pdf_no_duration),
            columns =
                listOf(
                    "#",
                    getString(Res.string.pdf_col_title),
                    getString(Res.string.pdf_col_artist),
                    getString(Res.string.pdf_col_key),
                    "BPM",
                    getString(Res.string.pdf_col_time),
                    getString(Res.string.pdf_col_changes),
                ),
            instrumentChange = { instrument.fill(it) },
            tuning = { tuning.fill(it) },
            key = { to, from -> key.fill(to, from) },
            tempo = { to, from -> tempo.fill(to, from) },
            songCount = { (if (it == 1) songsOne else songsOther).fill(it.toString()) },
            totalWithStops = { total.fill(it) },
            incomplete = { (if (it == 1) incompleteOne else incompleteOther).fill(it.toString()) },
            duration = { seconds ->
                val value = toHoursMinutes(seconds)
                if (value.hours > 0) {
                    hoursMinutes.fill(value.hours.toString(), value.minutes.toString().padStart(2, '0'))
                } else {
                    minutes.fill(value.minutes.toString())
                }
            },
            date = { formatDate(it, language) },
            stageFileLabel = getString(Res.string.pdf_stage_label),
            listFileLabel = getString(Res.string.pdf_list_label),
        )
    }
}

// Unlikely to appear in user text; replaced after reading the template.
private const val PLACEHOLDER_1 = "\u0001"
private const val PLACEHOLDER_2 = "\u0002"

private fun String.fill(
    first: String,
    second: String = "",
) = replace(PLACEHOLDER_1, first).replace(PLACEHOLDER_2, second)
