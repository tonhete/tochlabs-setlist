package com.tochlabs.setlist.feature.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.setlist.resolveItems
import com.tochlabs.setlist.core.domain.transfer.SetlistFileCodec
import com.tochlabs.setlist.core.domain.transfer.SetlistTransferRepository
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistPackage
import com.tochlabs.setlist.feature.share.pdf.ExportSource
import com.tochlabs.setlist.feature.share.pdf.buildListLayout
import com.tochlabs.setlist.feature.share.pdf.buildStageLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The three things the export sheet can produce (docs/product/screens-and-flows.md). */
enum class ExportMode { STAGE_PDF, LIST_PDF, BAND_FILE }

enum class ExportAction { SHARE, PRINT }

/** A file prepared for the share sheet or the print dialog; the screen hands it over and calls consumed. */
class ReadyExport(
    val file: ExportFile,
    val action: ExportAction,
)

data class ExportUiState(
    val loading: Boolean = true,
    /** The setlist no longer exists. */
    val missing: Boolean = false,
    val setlistName: String = "",
    val bandName: String = "",
    val songCount: Int = 0,
    val stopCount: Int = 0,
    val mode: ExportMode = ExportMode.STAGE_PDF,
    val busy: Boolean = false,
    val ready: ReadyExport? = null,
) {
    /** The band file is shared, not printed (specs/setlist-export, "Share and print"). */
    val canPrint: Boolean get() = mode != ExportMode.BAND_FILE
}

/** Stage PDF, list PDF or `.setlist` file of one setlist, to share or print. */
class ExportViewModel(
    setlistId: SetlistId,
    private val transfer: SetlistTransferRepository,
    private val codec: SetlistFileCodec,
    private val renderer: PdfRenderer,
    private val texts: ExportTextsProvider,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ExportUiState())
    val state: StateFlow<ExportUiState> = mutableState.asStateFlow()

    /** A snapshot taken when the sheet opens; it is what every mode exports. */
    private var pkg: SetlistPackage? = null

    init {
        viewModelScope.launch {
            val loaded = transfer.exportPackage(setlistId)
            pkg = loaded
            mutableState.update {
                if (loaded == null) {
                    it.copy(loading = false, missing = true)
                } else {
                    it.copy(
                        loading = false,
                        setlistName = loaded.setlist.name,
                        bandName = loaded.bandName,
                        songCount = loaded.songCount,
                        stopCount = loaded.stopCount,
                    )
                }
            }
        }
    }

    fun selectMode(mode: ExportMode) = mutableState.update { it.copy(mode = mode) }

    fun share() = prepare(ExportAction.SHARE)

    fun print() {
        if (mutableState.value.canPrint) prepare(ExportAction.PRINT)
    }

    /** The screen has opened the share sheet or the print dialog. */
    fun consumed() = mutableState.update { it.copy(ready = null) }

    private fun prepare(action: ExportAction) {
        val source = pkg ?: return
        if (mutableState.value.busy) return
        mutableState.update { it.copy(busy = true) }
        viewModelScope.launch {
            val file = build(source, mutableState.value.mode)
            mutableState.update { it.copy(busy = false, ready = ReadyExport(file, action)) }
        }
    }

    private suspend fun build(
        pkg: SetlistPackage,
        mode: ExportMode,
    ): ExportFile {
        val baseName = safeFileName(listOfNotNull(pkg.setlist.name, pkg.setlist.date?.toString()).joinToString(" "))
        if (mode == ExportMode.BAND_FILE) {
            return ExportFile("$baseName.setlist", SETLIST_MIME_TYPE, codec.encode(pkg).encodeToByteArray())
        }
        val texts = texts.texts()
        val source = ExportSource(pkg.setlist, pkg.bandName, resolveItems(pkg.items, pkg.songs.associateBy { it.id }))
        val (layout, label) =
            if (mode == ExportMode.STAGE_PDF) {
                buildStageLayout(source, texts) to texts.stageFileLabel
            } else {
                buildListLayout(source, texts) to texts.listFileLabel
            }
        return ExportFile("$baseName - $label.pdf", PDF_MIME_TYPE, renderer.render(layout))
    }
}

private val UNSAFE_FILE_CHARACTERS = Regex("""[\\/:*?"<>|]""")
private val SPACES = Regex("""\s+""")
private const val MAX_FILE_NAME = 80

/** A name every file system accepts: no reserved characters, single spaces, not empty. */
internal fun safeFileName(text: String): String =
    text
        .replace(UNSAFE_FILE_CHARACTERS, "-")
        .replace(SPACES, " ")
        .trim()
        .take(MAX_FILE_NAME)
        .trim()
        .trim('-')
        .trim()
        .ifEmpty { "setlist" }
