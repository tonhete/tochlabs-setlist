package com.tochlabs.setlist.feature.share

import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandColor
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistItem
import com.tochlabs.setlist.core.model.SetlistItemId
import com.tochlabs.setlist.core.model.Song
import com.tochlabs.setlist.core.model.SongId
import com.tochlabs.setlist.core.model.StopType
import com.tochlabs.setlist.core.testing.FakeBandRepository
import com.tochlabs.setlist.core.testing.FakeSetlistFileCodec
import com.tochlabs.setlist.core.testing.FakeSetlistTransferRepository
import com.tochlabs.setlist.core.testing.FakeStore
import com.tochlabs.setlist.feature.share.pdf.PdfLayout
import com.tochlabs.setlist.feature.share.pdf.TEXTS
import kotlinx.datetime.LocalDate

internal val mareas = Band(BandId("mareas"), "Mareas", BandColor(0xFF3366CC))
internal val sabados = Band(BandId("sabados"), "Los Sábados", BandColor(0xFFE65100))

/** Remembers the last layout and returns a recognisable payload. */
internal class FakePdfRenderer : PdfRenderer {
    var lastLayout: PdfLayout? = null

    override fun render(layout: PdfLayout): ByteArray {
        lastLayout = layout
        return "%PDF".encodeToByteArray()
    }
}

internal class ShareTestEnv {
    val store = FakeStore()
    val bands = FakeBandRepository(store)
    val transfer = FakeSetlistTransferRepository(store)
    val codec = FakeSetlistFileCodec()
    val renderer = FakePdfRenderer()
    val inbox = IncomingSetlistFiles()
    private val texts = ExportTextsProvider { TEXTS }

    fun exportViewModel(setlistId: SetlistId) = ExportViewModel(setlistId, transfer, codec, renderer, texts)

    fun importViewModel() = ImportViewModel(inbox, codec, transfer, bands)

    /** "Concierto Sala Norte" for Mareas: two songs and a break. */
    suspend fun salaNorte(): Setlist {
        bands.save(mareas)
        bands.save(sabados)
        val setlist = Setlist(SetlistId("sala"), mareas.id, "Concierto Sala Norte", LocalDate(2026, 11, 20), 20)
        val uno = Song(SongId("uno"), "Uno", "Olas", durationSeconds = 180)
        val dos = Song(SongId("dos"), "Dos", durationSeconds = 240)
        store.songs.value = listOf(uno, dos)
        store.setlists.value = listOf(setlist)
        store.items.value =
            mapOf(
                setlist.id to
                    listOf(
                        SetlistItem.SongItem(SetlistItemId("a"), uno.id),
                        SetlistItem.Stop(SetlistItemId("b"), StopType.BREAK, 900),
                        SetlistItem.SongItem(SetlistItemId("c"), dos.id),
                    ),
            )
        return setlist
    }
}
