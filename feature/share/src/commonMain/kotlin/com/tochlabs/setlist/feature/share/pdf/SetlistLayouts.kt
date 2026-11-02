package com.tochlabs.setlist.feature.share.pdf

import com.tochlabs.setlist.core.domain.setlist.ItemMarkers
import com.tochlabs.setlist.core.domain.setlist.ResolvedItem
import com.tochlabs.setlist.core.domain.setlist.calculateDuration
import com.tochlabs.setlist.core.domain.setlist.computeMarkers
import com.tochlabs.setlist.core.domain.setlist.effectiveBpm
import com.tochlabs.setlist.core.domain.setlist.effectiveKey
import com.tochlabs.setlist.core.ui.format.formatClock

// The two PDF layouts of specs/setlist-export.

private const val ITEM_GAP = 10f
private const val HEADER_GAP = 16f

/**
 * Large type, one block per item: number and title, key and markers, note, link
 * (specs/setlist-export, "Stage PDF").
 */
fun buildStageLayout(
    source: ExportSource,
    texts: ExportTexts,
): PdfLayout {
    val markers = computeMarkers(source.items)
    var songNumber = 0
    val blocks =
        source.items.mapIndexed { index, entry ->
            when (entry) {
                is ResolvedItem.SongEntry -> stageSong(++songNumber, entry, markers[index], texts)
                is ResolvedItem.StopEntry -> stageStop(entry, texts)
            }
        }
    return PdfLayout(paginate(listOf(header(source, texts)) + blocks))
}

private fun stageSong(
    number: Int,
    entry: ResolvedItem.SongEntry,
    markers: ItemMarkers?,
    texts: ExportTexts,
): PdfBlock {
    val details = listOfNotNull(entry.effectiveKey) + markers?.let { markerTexts(it, texts) }.orEmpty()
    return PdfBlock(
        buildList {
            add(PdfLine.Text("$number  ${entry.song.title}", PdfTextStyle.STAGE_TITLE))
            if (details.isNotEmpty()) {
                add(
                    PdfLine.Text(details.joinToString(" · "), PdfTextStyle.STAGE_DETAIL, accent = true),
                )
            }
            entry.item.note?.let { add(PdfLine.Text(it, PdfTextStyle.STAGE_DETAIL)) }
            if (markers?.isLinkedToNext == true) {
                add(
                    PdfLine.Text(
                        "↓ ${markers.transitionText ?: ""}".trimEnd(),
                        PdfTextStyle.STAGE_DETAIL,
                        accent = true,
                    ),
                )
            }
            add(PdfLine.Space(ITEM_GAP))
        },
    )
}

private fun stageStop(
    entry: ResolvedItem.StopEntry,
    texts: ExportTexts,
): PdfBlock {
    val stop = entry.item
    val label = texts.stopLabels.getValue(stop.type)
    return PdfBlock(
        buildList {
            add(PdfLine.Text("— $label ${formatClock(stop.durationSeconds.toLong())} —", PdfTextStyle.STAGE_STOP))
            stop.note?.let { add(PdfLine.Text(it, PdfTextStyle.STAGE_DETAIL)) }
            add(PdfLine.Space(ITEM_GAP))
        },
    )
}

/** Relative widths: number, title, artist, key, BPM, duration, changes. */
private val LIST_COLUMNS = listOf(0.05f, 0.27f, 0.18f, 0.07f, 0.06f, 0.1f, 0.27f)

/** Compact table, one row per item, then the song count and the total with stops (specs/setlist-export, "List PDF"). */
fun buildListLayout(
    source: ExportSource,
    texts: ExportTexts,
): PdfLayout {
    val markers = computeMarkers(source.items)
    val tableHeader = PdfBlock(listOf(PdfLine.Row(texts.columns, header = true)))
    var songNumber = 0
    val rows =
        source.items.mapIndexed { index, entry ->
            val cells =
                when (entry) {
                    is ResolvedItem.SongEntry -> songRow(++songNumber, entry, markers[index], texts)
                    is ResolvedItem.StopEntry -> stopRow(entry, texts)
                }
            PdfBlock(listOf(PdfLine.Row(cells)))
        }
    val blocks = listOf(header(source, texts), tableHeader) + rows + footer(source, texts)
    return PdfLayout(paginate(blocks, pageHeader = tableHeader), columnWeights = LIST_COLUMNS)
}

private fun songRow(
    number: Int,
    entry: ResolvedItem.SongEntry,
    markers: ItemMarkers?,
    texts: ExportTexts,
) = listOf(
    number.toString(),
    entry.song.title,
    entry.song.artist.orEmpty(),
    entry.effectiveKey.orEmpty(),
    entry.effectiveBpm?.toString().orEmpty(),
    entry.song.durationSeconds?.let { formatClock(it.toLong()) } ?: texts.noDuration,
    markers?.let { markerTexts(it, texts).joinToString(", ") }.orEmpty(),
)

private fun stopRow(
    entry: ResolvedItem.StopEntry,
    texts: ExportTexts,
): List<String> {
    val stop = entry.item
    val label = listOfNotNull(texts.stopLabels.getValue(stop.type), stop.note).joinToString(" · ")
    return listOf("", label, "", "", "", formatClock(stop.durationSeconds.toLong()), "")
}

private fun header(
    source: ExportSource,
    texts: ExportTexts,
) = PdfBlock(
    listOf(
        PdfLine.Text(source.setlist.name, PdfTextStyle.TITLE),
        PdfLine.Text(
            listOfNotNull(source.bandName, source.setlist.date?.let(texts.date)).joinToString(" · "),
            PdfTextStyle.SUBTITLE,
        ),
        PdfLine.Space(HEADER_GAP),
    ),
)

private fun footer(
    source: ExportSource,
    texts: ExportTexts,
): PdfBlock {
    val duration = calculateDuration(source.items, source.setlist.pauseSeconds)
    val songs = source.items.count { it is ResolvedItem.SongEntry }
    return PdfBlock(
        buildList {
            add(PdfLine.Space(HEADER_GAP))
            add(
                PdfLine.Text(
                    "${texts.songCount(
                        songs,
                    )} · ${texts.totalWithStops(texts.duration(duration.totalWithStopsSeconds))}",
                    PdfTextStyle.FOOTER,
                ),
            )
            if (!duration.isComplete) {
                add(PdfLine.Text(texts.incomplete(duration.songsWithoutDuration), PdfTextStyle.FOOTER, accent = true))
            }
        },
    )
}

/** Change markers as text, the same ones the setlist view and stage mode show. */
private fun markerTexts(
    markers: ItemMarkers,
    texts: ExportTexts,
): List<String> =
    buildList {
        markers.instrumentChangeTo?.let { add(texts.instrumentChange(it)) }
        markers.tuningChange?.let { add(texts.tuning(it.to ?: texts.standardTuning)) }
        markers.keyChange?.let { add(texts.key(it.to ?: texts.none, it.from ?: texts.none)) }
        markers.tempoChange?.let {
            add(
                texts.tempo(it.to?.toString() ?: texts.none, it.from?.toString() ?: texts.none),
            )
        }
        if (markers.isRepeated) add(texts.repeated)
    }
